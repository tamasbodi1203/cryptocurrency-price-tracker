package com.example.cryptocurrencypricetracker;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.example.cryptocurrencypricetracker.repository.CoinRepository;

public class PriceAsyncLoader extends AsyncTaskLoader<Void> {

    Handler handler = new Handler();

    public PriceAsyncLoader(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();

        forceLoad();
    }

    @Nullable
    @Override
    public Void loadInBackground() {
//        handler.post(runnableCode);
        CoinRepository.getInstance().refreshPrices();
        return null;
    }

    private final Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this, 30000);
            Log.d("Handlers", "Refreshing prices...");
            CoinRepository.getInstance().refreshPrices();
        }
    };
}
