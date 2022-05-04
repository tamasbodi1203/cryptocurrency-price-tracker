package com.example.cryptocurrencypricetracker.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.cryptocurrencypricetracker.entity.Coin;
import com.example.cryptocurrencypricetracker.entity.UserAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class UserAccountRepository {

    private static final String LOG_TAG = UserAccountRepository.class.getName();
    private CollectionReference mUserAccounts;
    private FirebaseAuth mAuth;

    public UserAccountRepository() {
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUserAccounts = mFirestore.collection("UserAccounts");
    }

    public Task<QuerySnapshot> getSignedInUserAccount() {
        return mUserAccounts.whereEqualTo("emailAddress", mAuth.getCurrentUser().getEmail()).get();
    }

    public void createUserAccount(UserAccount userAccount) {
        mUserAccounts.add(userAccount);

    }

    public void deleteUserAccount(UserAccount userAccount) {
        mAuth.getCurrentUser().delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mUserAccounts.document(userAccount._getId()).delete();
                            Log.d(LOG_TAG,  userAccount.getUsername() + "'s account deleted.");
                        }
                    }
                });
    }

    public void updateUserAccount(UserAccount userAccount) {
        mUserAccounts.document(userAccount._getId()).update("phoneNumber", userAccount.getPhoneNumber()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(LOG_TAG, userAccount.getUsername() + "'s account updated.");
            }
        });
    }

    public void addToUsersWatchlist(UserAccount userAccount, ArrayList<Coin> mWatchListData){
        mUserAccounts.document(userAccount._getId()).update("watchlistItems",  mWatchListData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
               Log.d(LOG_TAG, userAccount.getUsername() + "'s watchlist updated.");
            }
        });
    }

    public void removeFromUsersWatchlist(UserAccount userAccount, ArrayList<Coin> mWatchListData){
        mUserAccounts.document(userAccount._getId()).update("watchlistItems", mWatchListData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(LOG_TAG, userAccount.getUsername() + "'s watchlist updated.");
            }
        });
    }
}
