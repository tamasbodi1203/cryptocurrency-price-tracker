package com.example.cryptocurrencypricetracker.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cryptocurrencypricetracker.NotificationHelper;
import com.example.cryptocurrencypricetracker.entity.Coin;
import com.example.cryptocurrencypricetracker.adapter.CoinAdapter;
import com.example.cryptocurrencypricetracker.R;
import com.example.cryptocurrencypricetracker.entity.UserAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;

public class BaseActivity extends AppCompatActivity {

    protected static final String LOG_TAG = BaseActivity.class.getName();
    protected static final int SECRET_KEY = 99;

    protected static NotificationHelper mNotificationHelper;
    protected static UserAccount userAccount;
    protected static FirebaseAuth mAuth;
    protected static FirebaseUser mUser;
    protected static FirebaseFirestore mFireStore;
    protected static CollectionReference mItems;
    protected static CollectionReference mUserAccounts;

    protected static ArrayList<Coin> mItemsData;
    protected static ArrayList<Coin> mWatchlistData;
    protected CoinAdapter mAdapter;

    public ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_list);

        mFireStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        mItems = mFireStore.collection("Items");
        mUserAccounts = mFireStore.collection("UserAccounts");
        mNotificationHelper = new NotificationHelper(this);
    }

    public void showProgressDialog(Context context, String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setMessage(message);
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }

    protected void startWatchlist() {
        Intent intent = new Intent(this, WatchlistActivity.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
    }

    protected void startAccountDetails() {
        Intent intent = new Intent(this, UserAccountActivity.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
    }

    protected void logout() {
        mAuth.signOut();
        finishAffinity();
        startActivity(new Intent(this, MainActivity.class));
        userAccount = null;
    }

    public boolean isSignedInUser() {
        return mUser != null && mUser.getEmail() != null && !("").equals(mUser.getEmail());
    }

    public void addToWatchlist(Coin coin) {
        if (isSignedInUser()) {
            mUserAccounts.document(userAccount._getId()).update("watchlistItems", FieldValue.arrayUnion(coin));
        }
        mWatchlistData.add(coin);
        Collections.sort(mWatchlistData, (o1, o2) -> o1.getSymbol().compareTo(o2.getSymbol()));
    }

    public void removeFromWatchlist(Coin coin) {
        if (isSignedInUser()) {
            mUserAccounts.document(userAccount._getId()).update("watchlistItems", FieldValue.arrayRemove(coin));
        }
        mWatchlistData.remove(coin);
    }

}