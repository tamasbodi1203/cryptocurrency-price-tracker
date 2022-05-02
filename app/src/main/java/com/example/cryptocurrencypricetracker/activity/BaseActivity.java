package com.example.cryptocurrencypricetracker.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cryptocurrencypricetracker.NotificationHelper;
import com.example.cryptocurrencypricetracker.entity.Coin;
import com.example.cryptocurrencypricetracker.CoinAdapter;
import com.example.cryptocurrencypricetracker.R;
import com.example.cryptocurrencypricetracker.entity.UserAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class BaseActivity extends AppCompatActivity {

    protected static final String LOG_TAG = BaseActivity.class.getName();
    protected static final int SECRET_KEY = 99;

    protected static NotificationHelper mNotificationHelper;
    protected static UserAccount userAccount;
    protected static FirebaseAuth mAuth;
    protected static FirebaseUser mUser;
    protected static FirebaseFirestore mFireStore;
    protected static CollectionReference mItems;
    protected static CollectionReference mAccounts;
    protected static ArrayList<Coin> mItemsData;
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
        mAccounts = mFireStore.collection("UserAccounts");
        if (mUser != null) {
            Log.d(LOG_TAG, "Autentikált felhasználó!");
        } else {
            Log.e(LOG_TAG, "Nem autentikált felhasználó!");
            finish();
        }
        mNotificationHelper = new NotificationHelper(this);
    }

    public void showProgressDialog(Context context) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setMessage("Árfolyamok frissítése...");
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

}