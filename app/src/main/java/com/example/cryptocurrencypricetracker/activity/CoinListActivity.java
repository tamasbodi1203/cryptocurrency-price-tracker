package com.example.cryptocurrencypricetracker.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cryptocurrencypricetracker.R;
import com.example.cryptocurrencypricetracker.adapter.CoinAdapter;
import com.example.cryptocurrencypricetracker.repository.CoinRepository;
import com.google.firebase.auth.FirebaseAuth;

public class CoinListActivity extends BaseActivity {

    private static final String LOG_TAG = CoinListActivity.class.getName();

    Handler handler = new Handler();

    private final Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            Log.d("Handlers", "Called on main thread");
            CoinRepository.getInstance().refreshPrices();
            mAdapter.notifyDataSetChanged();
            handler.postDelayed(this, 10000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Log.d(LOG_TAG, "Autentikált felhasználó!");
        } else {
            Log.e(LOG_TAG, "Nem autentikált felhasználó!");
            finish();
        }
        setContentView(R.layout.activity_coin_list);

        RecyclerView mRecyclerView = findViewById(R.id.recycleView);
        int gridNumber = 1;
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, gridNumber));

        mAdapter = new CoinAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setCoins(viewModel.getCoinsData());
        mAdapter.setWatchlist(viewModel.getUserAccountData().getWatchlist());
        mAdapter.notifyDataSetChanged();

        // Periódikus árfolyam frissítés
        handler.post(runnableCode);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        logout();
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

}