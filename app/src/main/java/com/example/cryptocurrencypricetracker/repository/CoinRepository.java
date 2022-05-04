package com.example.cryptocurrencypricetracker.repository;

import com.example.cryptocurrencypricetracker.entity.Coin;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class CoinRepository {

    private static final String LOG_TAG = CoinRepository.class.getName();
    private CollectionReference mCoins;
    private FirebaseAuth mAuth;

    public CoinRepository() {
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mCoins = mFirestore.collection("Coins");

    }

    public void addCoin(Coin coin) {
        mCoins.add(coin);
    }

    public Task<QuerySnapshot> getCoins() {
        return mCoins.orderBy("symbol").get();
    }

    public void updateCoin(Coin coin) {
        mCoins.document(coin._getId()).update("price", coin.getPrice());
        mCoins.document(coin._getId()).update("percentageChange",coin.getPercentageChange());
    }

}
