package com.example.cryptocurrencypricetracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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

public class CoinListActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<ArrayList<CoinItem>> {

    private static final String LOG_TAG = CoinListActivity.class.getName();

    Handler handler = new Handler();
    private OkHttpClient okHttpClient = new OkHttpClient();
    private RecyclerView mRecyclerView;
    private ArrayList<CoinItem> mItemsData;
    private CoinItemAdapter mAdapter;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private int gridNumber = 1;
    private boolean viewRow = true;

    private FirebaseFirestore mFireStore;
    private CollectionReference mItems;

    private Runnable runnableCode = new Runnable() {
        @Override
        public void run() {
            Log.d("Handlers", "Called on main thread");
            // Repeat this the same runnable code block again another 10 seconds
            refreshPrices();
            mAdapter.notifyDataSetChanged();
            // 'this' is referencing the Runnable object
            handler.postDelayed(this, 10000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_list);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user != null) {
            Log.d(LOG_TAG, "Autentikált felhasználó!");
        } else {
            Log.e(LOG_TAG, "Nem autentikált felhasználó!");
            finish();
        }

        showProgressDialog(this);
        mRecyclerView = findViewById(R.id.recycleView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, gridNumber));
        mItemsData = new ArrayList<>();

        mAdapter = new CoinItemAdapter(this, mItemsData);
        mRecyclerView.setAdapter(mAdapter);

        mFireStore = FirebaseFirestore.getInstance();
        mItems = mFireStore.collection("Items");
        queryData();

        // Periódikus árfolyam frissítés
        handler.post(runnableCode);
    }

    private void queryData() {
        mItemsData.clear();

        mItems.orderBy("symbol").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                CoinItem item = documentSnapshot.toObject(CoinItem.class);
                item.setId(documentSnapshot.getId());
                mItemsData.add(item);
            }

            if (mItemsData.size() == 0) {
                initializeData();
                queryData();
            }
            mAdapter.notifyDataSetChanged();
            hideProgressDialog();
        });
    }

    private void initializeData() {
        String[] itemCoinGeckoId = getResources().getStringArray(R.array.cryptocurrency_item_coin_gecko_ids);
        String[] itemSymbol = getResources().getStringArray(R.array.cryptocurrency_item_symbols);
        String[] itemsPrice = getResources().getStringArray(R.array.cryptocurrency_item_prices);
        TypedArray itemsImageResource = getResources().obtainTypedArray(R.array.cryptocurrency_item_images);

//        mItemsData.clear();

        for (int i = 0; i < itemSymbol.length; i++) {
//            mItemsData.add(new CoinItem(itemCoinGeckoId[i], itemSymbol[i], new BigDecimal(itemsPrice[i]), itemsImageResource.getResourceId(i, 0)));
            mItems.add(new CoinItem(itemCoinGeckoId[i], itemSymbol[i], Double.parseDouble(itemsPrice[i]), itemsImageResource.getResourceId(i, 0)));
        }

        itemsImageResource.recycle();

//        mAdapter.notifyDataSetChanged();

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
                logout();
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

    @NonNull
    @Override
    public Loader<ArrayList<CoinItem>> onCreateLoader(int id, @Nullable Bundle args) {
        return new PriceAsyncLoader(this, mItemsData);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<ArrayList<CoinItem>> loader, ArrayList<CoinItem> data) {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<ArrayList<CoinItem>> loader) {}

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
                Log.d(LOG_TAG, item.getSymbol() + " jelenlegi ár: $" + priceString);
                mItems.document(item._getId()).update("price", Double.parseDouble(priceString));

                item.setPrice(Double.parseDouble(priceString));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void logout() {
        mAuth.signOut();
        finishAffinity();
    }
}