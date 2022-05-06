package com.example.pocketsentinel.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.pocketsentinel.entity.Coin;
import com.example.pocketsentinel.entity.UserAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class UserAccountRepository {

    private static final String LOG_TAG = UserAccountRepository.class.getName();
    private static UserAccountRepository INSTANCE;
    private CollectionReference mUserAccounts;
    private UserAccount mUserAccountData;


    private UserAccountRepository() {
        mUserAccounts = FirebaseFirestore.getInstance().collection("UserAccounts");
        mUserAccountData = new UserAccount(null, null, null, new ArrayList<>());

    }

    public static UserAccountRepository getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new UserAccountRepository();
        }

        return INSTANCE;
    }

    public void clear() {
        this.mUserAccountData = new UserAccount(null, null, null, new ArrayList<>());
    }

    public UserAccount getUserAccountData() {
        return this.mUserAccountData;
    }

    public Task<QuerySnapshot> getUserAccount() {
        return mUserAccounts.whereEqualTo("emailAddress", FirebaseAuth.getInstance().getCurrentUser().getEmail()).get();
    }

    public Task<QuerySnapshot> initUserAccount() {
        return mUserAccounts.whereEqualTo("emailAddress", FirebaseAuth.getInstance().getCurrentUser().getEmail()).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = (QuerySnapshot) task.getResult();
                            for (DocumentSnapshot documentSnapshot : querySnapshot.getDocuments()) {
                                mUserAccountData = documentSnapshot.toObject(UserAccount.class);
                                mUserAccountData.setId(documentSnapshot.getId());
                                if (mUserAccountData.getWatchlist() == null) {
                                    mUserAccountData.setWatchlist(new ArrayList<>());
                                }
                            }
                        }
                    }
                });
    }

    public void createUserAccount(UserAccount userAccount) {
        mUserAccounts.add(userAccount);

    }

    public Task<Void> deleteUserAccount() {
        return FirebaseAuth.getInstance().getCurrentUser().delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mUserAccounts.document(mUserAccountData._getId()).delete();
                        Log.d(LOG_TAG,  mUserAccountData.getUsername() + "'s account deleted.");
                    }
                });
    }

    public Task<Void> updateUserAccount() {
        return mUserAccounts.document(mUserAccountData._getId()).update("phoneNumber", mUserAccountData.getPhoneNumber()).addOnSuccessListener(
                queryDocumentSnapshot -> Log.d(LOG_TAG, mUserAccountData.getUsername() + "'s account updated."));
    }

    public void addToWatchlist(ArrayList<Coin> mWatchListData){
        mUserAccounts.document(mUserAccountData._getId()).update("watchlist",  mWatchListData).addOnSuccessListener(
                queryDocumentSnapshot -> Log.d(LOG_TAG, mUserAccountData.getUsername() + "'s watchlist updated."));
    }

    public void removeFromWatchlist(ArrayList<Coin> mWatchListData){
        mUserAccounts.document(mUserAccountData._getId()).update("watchlist", mWatchListData).addOnSuccessListener(
                queryDocumentSnapshot -> Log.d(LOG_TAG, mUserAccountData.getUsername() + "'s watchlist updated."));
    }
}
