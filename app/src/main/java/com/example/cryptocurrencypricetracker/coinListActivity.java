package com.example.cryptocurrencypricetracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.math.BigDecimal;
import java.util.ArrayList;

public class coinListActivity extends AppCompatActivity {

    private static final String LOG_TAG = coinListActivity.class.getName();
    private static final String PREF_KEY = MainActivity.class.getPackage().toString();
    private static final int SECRET_KEY = 99;

    private RecyclerView mRecyclerView;
    private ArrayList<CoinItem> mItemsData;
    private CoinItemAdapter mAdapter;
    private FirebaseUser user;
    private int gridNumber = 1;
    private SharedPreferences preferences;
    private boolean viewRow = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_list);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d(LOG_TAG, "Autentikált felhasználó!");
        } else {
            Log.e(LOG_TAG, "Nem autentikált felhasználó!");
            finish();
        }

        mRecyclerView = findViewById(R.id.recycleView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, gridNumber));
        mItemsData = new ArrayList<>();

        mAdapter = new CoinItemAdapter(this, mItemsData);
        mRecyclerView.setAdapter(mAdapter);

        initializeData();
    }

    private void initializeData() {
        String[] itemSymbol = getResources().getStringArray(R.array.cryptocurrency_item_symbols);
        String[] itemsPrice = getResources().getStringArray(R.array.cryptocurrency_item_prices);
        TypedArray itemsImageResource = getResources().obtainTypedArray(R.array.cryptocurrency_item_images);

        mItemsData.clear();

        for (int i = 0; i < itemSymbol.length; i++) {
            mItemsData.add(new CoinItem(itemSymbol[i], new BigDecimal(itemsPrice[i]), itemsImageResource.getResourceId(i, 0)));
        }

        itemsImageResource.recycle();

        mAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.watchlist_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
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
            case R.id.logout:
                Log.d(LOG_TAG, "Kijelentkezés megnyomva!");
                return true;
            case R.id.settings:
                Log.d(LOG_TAG, "Beállítások megnyomva!");
                return true;
            case R.id.watchlist:
                Log.d(LOG_TAG, "Kedvencek megnyomva!");
                return true;
            //case R.id.viewSelector:

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