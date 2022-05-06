package com.example.cryptocurrencypricetracker.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.cryptocurrencypricetracker.repository.UserAccountRepository;
import com.example.cryptocurrencypricetracker.entity.Coin;
import com.example.cryptocurrencypricetracker.adapter.CoinAdapter;
import com.example.cryptocurrencypricetracker.R;
import com.example.cryptocurrencypricetracker.view.ViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;

public class BaseActivity extends AppCompatActivity {

    protected static final String LOG_TAG = BaseActivity.class.getName();
    protected static final int SECRET_KEY = 99;
    protected CoinAdapter mAdapter;
    protected ViewModel viewModel;

    public ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coin_list);

        viewModel = new ViewModelProvider(this).get(ViewModel.class);
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
        finishAffinity();
        FirebaseAuth.getInstance().signOut();
        UserAccountRepository.getInstance().clear();
        viewModel = null;
        startActivity(new Intent(this, MainActivity.class));
        Toast.makeText(BaseActivity.this, "Kijelentkezve.", Toast.LENGTH_SHORT).show();
    }

    public boolean isSignedInUser() {
        return FirebaseAuth.getInstance().getCurrentUser() != null
                && FirebaseAuth.getInstance().getCurrentUser().getEmail() != null
                && !("").equals(FirebaseAuth.getInstance().getCurrentUser().getEmail());
    }

    public void addToWatchlist(Coin coin) {
        ArrayList<Coin> mWatchlistData = viewModel.getUserAccountData().getWatchlist();
        mWatchlistData.add(coin);
        Collections.sort(mWatchlistData, (o1, o2) -> o1.getSymbol().compareTo(o2.getSymbol()));
        if (isSignedInUser()) {
            viewModel.addToWatchlist(mWatchlistData);
        }
    }

    public void removeFromWatchlist(Coin coin) {
        ArrayList<Coin> mWatchlistData = viewModel.getUserAccountData().getWatchlist();
        mWatchlistData.remove(coin);
        if (isSignedInUser()) {
            viewModel.removeFromWatchlist(mWatchlistData);
        }
    }

}