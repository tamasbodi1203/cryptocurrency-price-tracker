package com.example.cryptocurrencypricetracker.repository;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.Log;

import com.example.cryptocurrencypricetracker.R;
import com.example.cryptocurrencypricetracker.entity.Coin;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CoinRepository {

    private static final String LOG_TAG = CoinRepository.class.getName();
    private static CoinRepository INSTANCE;
    private CollectionReference mCoins;
    private ArrayList<Coin> mCoinsData;

    private CoinRepository() {
        mCoins = FirebaseFirestore.getInstance().collection("Coins");
        mCoinsData = new ArrayList<>();

    }

    public static CoinRepository getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new CoinRepository();
        }

        return INSTANCE;
    }

    public ArrayList<Coin> getCoinsData() {
        return this.mCoinsData;
    }

    public Task<QuerySnapshot> getCoins() {
        return mCoins.orderBy("symbol").get();
    }

    public void addCoin(Coin coin) {
        mCoins.add(coin).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(LOG_TAG, coin.getSymbol() + " coin created.");
            } else {
                Log.d(LOG_TAG, "Create error: ", task.getException());
            }
        });
    }

    public void updateCoin(Coin coin) {
        mCoins.document(coin._getId()).update("price", coin.getPrice()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(LOG_TAG, coin.getSymbol() + " price updated: " + coin.getPrice());
            } else {
                Log.d(LOG_TAG, "Update error: ", task.getException());
            }
        });

        mCoins.document(coin._getId()).update("percentageChange",coin.getPercentageChange());
    }

    public Task<QuerySnapshot> initCoins() {
        mCoinsData.clear();
        return mCoins.orderBy("symbol").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot.getDocuments().isEmpty()) {
                            initDataFromFile();
                        } else {
                            for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                                Coin coin = documentSnapshot.toObject(Coin.class);
                                coin.setId(documentSnapshot.getId());
                                mCoinsData.add(coin);
                            }
                            refreshPrices();
                        }
                    }
                });
    }

    public void initDataFromFile() {
        String[] itemCoinGeckoId = Resources.getSystem().getStringArray(R.array.cryptocurrency_item_coin_gecko_ids);
        String[] itemSymbol = Resources.getSystem().getStringArray(R.array.cryptocurrency_item_symbols);
        String[] itemsPrice = Resources.getSystem().getStringArray(R.array.cryptocurrency_item_prices);
        String[] itemsPercentageChange = Resources.getSystem().getStringArray(R.array.cryptocurrency_item_percentage_changes);
        TypedArray itemsImageResource = Resources.getSystem().obtainTypedArray(R.array.cryptocurrency_item_images);


        for (int i = 0; i < itemSymbol.length; i++) {
            Coin coin = new Coin(itemCoinGeckoId[i], itemSymbol[i], Double.parseDouble(itemsPrice[i]), Double.parseDouble(itemsPercentageChange[i]), itemsImageResource.getResourceId(i, 0));
            addCoin(coin);
        }

        itemsImageResource.recycle();

    }

    public void refreshPrices() {
        String ids = "";
        Iterator<Coin> iterator = mCoinsData.iterator();
        while (iterator.hasNext()) {
            Coin item = iterator.next();
            ids = ids + item.getCoinGeckoId() + "%2C";
            if (!iterator.hasNext()) {
                ids = ids + item.getCoinGeckoId();
            }
        }

        Request request = new Request.Builder()
                .url("https://api.coingecko.com/api/v3/simple/price?ids=" + ids + "&vs_currencies=USD&include_24hr_change=true")
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(LOG_TAG, "Hiba történt az árfolyam lekérdezése során: "
                        + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response)
                    throws IOException {
                final String body = response.body().string();
                parseResponse(body);
            }
        });

    }

    private void parseResponse(String body) {
        try {
            JSONObject jsonObject = new JSONObject(body);
            for (Coin coin : mCoinsData) {
                JSONObject priceObject = jsonObject.getJSONObject(coin.getCoinGeckoId());
                String priceString = String.valueOf(priceObject.get("usd"));
                String percentageChangeString = String.valueOf(priceObject.get("usd_24h_change"));
                Log.d(LOG_TAG, coin.getSymbol() + " latest price: $" + priceString + ", 24h change: " + percentageChangeString + "%");

                coin.setPrice(Double.parseDouble(priceString));
                coin.setPercentageChange(Double.parseDouble(percentageChangeString));
                //coinRepository.updateCoin(coin);
            }
            if (UserAccountRepository.getInstance() != null) {
                for (Coin watchlistCoin : UserAccountRepository.getInstance().getUserAccountData().getWatchlist()) {
                    JSONObject priceObject = jsonObject.getJSONObject(watchlistCoin.getCoinGeckoId());
                    String priceString = String.valueOf(priceObject.get("usd"));
                    String percentageChangeString = String.valueOf(priceObject.get("usd_24h_change"));

                    watchlistCoin.setPrice(Double.parseDouble(priceString));
                    watchlistCoin.setPercentageChange(Double.parseDouble(percentageChangeString));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
