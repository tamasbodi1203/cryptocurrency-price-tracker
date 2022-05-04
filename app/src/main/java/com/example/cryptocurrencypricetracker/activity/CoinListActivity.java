package com.example.cryptocurrencypricetracker.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cryptocurrencypricetracker.R;
import com.example.cryptocurrencypricetracker.adapter.CoinAdapter;
import com.example.cryptocurrencypricetracker.entity.Coin;

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

public class CoinListActivity extends BaseActivity {

    private static final String LOG_TAG = CoinListActivity.class.getName();

    Handler handler = new Handler();
    private final OkHttpClient okHttpClient = new OkHttpClient();

    private final Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            Log.d("Handlers", "Called on main thread");
            // Repeat this the same runnable code block every 10 seconds
            refreshPrices();
            mAdapter.notifyDataSetChanged();
            // 'this' is referencing the Runnable object
            handler.postDelayed(this, 10000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mAuth.getCurrentUser() != null) {
            Log.d(LOG_TAG, "Autentikált felhasználó!");
        } else {
            Log.e(LOG_TAG, "Nem autentikált felhasználó!");
            finish();
        }
        setContentView(R.layout.activity_coin_list);

        RecyclerView mRecyclerView = findViewById(R.id.recycleView);
        int gridNumber = 1;
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, gridNumber));

        mAdapter = new CoinAdapter(this, mCoinsData, mWatchlistData, false);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();
        hideProgressDialog();
        //queryData();

        // Periódikus árfolyam frissítés
        handler.post(runnableCode);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.coin_list_menu, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(LOG_TAG, s);
                mAdapter.getFilter().filter(s);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.watchlist:
                Log.d(LOG_TAG, "Kedvencek megnyomva!");
                startWatchlist();
                return true;

            case R.id.accountDetails:
                Log.d(LOG_TAG, "Fiókadatok megnyomva!");
                startAccountDetails();
                return true;

            case R.id.logout:
                Log.d(LOG_TAG, "Kijelentkezés megnyomva!");
                logout();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    // Árfolyam frissítése
    private void refreshPrices() {
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

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(CoinListActivity.this, "Hiba történt az árfolyam lekérdezése során: "
                        + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                coinRepository.updateCoin(coin);
            }
            for (Coin coin : mWatchlistData) {
                JSONObject priceObject = jsonObject.getJSONObject(coin.getCoinGeckoId());
                String priceString = String.valueOf(priceObject.get("usd"));
                String percentageChangeString = String.valueOf(priceObject.get("usd_24h_change"));

                coin.setPrice(Double.parseDouble(priceString));
                coin.setPercentageChange(Double.parseDouble(percentageChangeString));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}