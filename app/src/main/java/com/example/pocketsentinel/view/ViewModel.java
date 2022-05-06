package com.example.pocketsentinel.view;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import com.example.pocketsentinel.entity.Coin;
import com.example.pocketsentinel.entity.UserAccount;
import com.example.pocketsentinel.repository.CoinRepository;
import com.example.pocketsentinel.repository.UserAccountRepository;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

public class ViewModel extends AndroidViewModel {

    private CoinRepository coinRepository;
    private UserAccountRepository userAccountRepository;

    public ViewModel (Application application) {
        super(application);

        this.coinRepository = CoinRepository.getInstance();
        this.userAccountRepository = UserAccountRepository.getInstance();
    }

    public ArrayList<Coin> getCoinsData() {
        return coinRepository.getCoinsData();
    }

    public UserAccount getUserAccountData() {
        return userAccountRepository.getUserAccountData();
    }

    public void createUserAccount(UserAccount userAccount) {
        userAccountRepository.createUserAccount(userAccount);
    }

    public Task<Void> updateUserAccount() {
        return userAccountRepository.updateUserAccount();
    }

    public void addToWatchlist(ArrayList<Coin> watchlist) {
        userAccountRepository.addToWatchlist(watchlist);
    }

    public void removeFromWatchlist(ArrayList<Coin> watchlist) {
        userAccountRepository.removeFromWatchlist(watchlist);
    }

}
