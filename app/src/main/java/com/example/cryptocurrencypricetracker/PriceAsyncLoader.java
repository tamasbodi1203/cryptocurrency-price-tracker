package com.example.cryptocurrencypricetracker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PriceAsyncLoader extends AsyncTaskLoader<ArrayList<CoinItem>> {

    private OkHttpClient okHttpClient = new OkHttpClient();
    private ArrayList<CoinItem> mItemsData;

    public PriceAsyncLoader(@NonNull Context context, ArrayList<CoinItem> mItemsData) {
        super(context);
        this.mItemsData = mItemsData;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();

        forceLoad();
    }

    @Nullable
    @Override
    public ArrayList<CoinItem> loadInBackground() {

        refreshPrices();
        return this.mItemsData;
    }

    // Árfolyam frissítése
    private void refreshPrices() {
        String ids = "";
        Iterator<CoinItem> iterator = mItemsData.iterator();
        while (iterator.hasNext()) {
            CoinItem item = iterator.next();
            ids = ids + item.getCoinGeckoId() + "%2C";
            if (!iterator.hasNext()) {
                ids = ids + item.getCoinGeckoId();
            }
        }

        Request request = new Request.Builder()
                .url("https://api.coingecko.com/api/v3/simple/price?ids=" + ids + "&vs_currencies=USD")
                .build();

        CountDownLatch countDownLatch = new CountDownLatch(1);
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
//                Toast.makeText(MainActivity.this, "Error during BPI loading : "
//                        + e.getMessage(), Toast.LENGTH_SHORT).show();
                countDownLatch.countDown();
            }

            @Override
            public void onResponse(Call call, Response response)
                    throws IOException {
                final String body = response.body().string();
                parseResponse(body);
                countDownLatch.countDown();
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void parseResponse(String body) {
        try {
            JSONObject jsonObject = new JSONObject(body);
            for (CoinItem item : mItemsData) {
                JSONObject priceObject = jsonObject.getJSONObject(item.getCoinGeckoId());
                String priceString = String.valueOf(priceObject.get("usd"));

                item.setPrice(new BigDecimal(priceString));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}