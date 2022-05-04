package com.example.cryptocurrencypricetracker.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cryptocurrencypricetracker.repository.CoinRepository;
import com.example.cryptocurrencypricetracker.repository.UserAccountRepository;
import com.example.cryptocurrencypricetracker.entity.Coin;
import com.example.cryptocurrencypricetracker.adapter.CoinAdapter;
import com.example.cryptocurrencypricetracker.R;
import com.example.cryptocurrencypricetracker.entity.UserAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;

public class BaseActivity extends AppCompatActivity {

    protected static final String LOG_TAG = BaseActivity.class.getName();
    protected static final int SECRET_KEY = 99;

    protected static FirebaseAuth mAuth;
    protected static CollectionReference mCoins;

    protected static ArrayList<Coin> mCoinsData;
    protected static ArrayList<Coin> mWatchlistData;
    protected static UserAccount mUserAccountData;
    protected static UserAccountRepository userAccountRepository;
    protected static CoinRepository coinRepository;
    protected CoinAdapter mAdapter;

    public ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_list);

        mAuth = FirebaseAuth.getInstance();

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
        mUserAccountData = null;
    }

    public boolean isSignedInUser() {
        return mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getEmail() != null && !("").equals(mAuth.getCurrentUser().getEmail());
    }

    public void addToWatchlist(Coin coin) {
        mWatchlistData.add(coin);
        Collections.sort(mWatchlistData, (o1, o2) -> o1.getSymbol().compareTo(o2.getSymbol()));
        if (isSignedInUser()) {
            userAccountRepository.addToUsersWatchlist(mUserAccountData, mWatchlistData);
        }
    }

    public void removeFromWatchlist(Coin coin) {
        mWatchlistData.remove(coin);
        if (isSignedInUser()) {
            userAccountRepository.removeFromUsersWatchlist(mUserAccountData, mWatchlistData);
        }
    }

}