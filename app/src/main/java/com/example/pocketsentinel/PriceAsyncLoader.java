package com.example.pocketsentinel;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.example.pocketsentinel.repository.CoinRepository;

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
        CoinRepository.getInstance().refreshPrices();
        handler.post(runnableCode);
        return null;
    }

    private final Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            Log.d("Handlers", "Called on main thread");
            CoinRepository.getInstance().refreshPrices();
            handler.postDelayed(this, 10000);
        }
    };
}
