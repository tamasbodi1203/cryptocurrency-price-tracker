package com.example.pocketsentinel.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pocketsentinel.R;
import com.example.pocketsentinel.adapter.CoinAdapter;
import com.example.pocketsentinel.entity.Coin;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class WatchlistActivity extends BaseActivity {

    private static final String LOG_TAG = WatchlistActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Log.d(LOG_TAG, "Autentikált felhasználó!");
        } else {
            Log.e(LOG_TAG, "Nem autentikált felhasználó!");
            finish();
        }
        setContentView(R.layout.activity_watchlist);

        RecyclerView mRecyclerView = findViewById(R.id.recycleView);
        TextView mEmptyListTextView = findViewById(R.id.emptyListTextView);
        int gridNumber = 1;
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, gridNumber));

        ArrayList<Coin> watchlist = viewModel.getUserAccountData().getWatchlist();
        if (!watchlist.isEmpty()) {
            mEmptyListTextView.setVisibility(View.INVISIBLE);
        } else {
            mEmptyListTextView.setVisibility(View.VISIBLE);
        }
        mAdapter = new CoinAdapter(this);
        mAdapter.setCoins(watchlist);
        mAdapter.setWatchlist(watchlist);
        mAdapter.setIsWatchlist(true);
        mRecyclerView.setAdapter(mAdapter);
    }
}