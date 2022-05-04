package com.example.cryptocurrencypricetracker.entity;

import java.util.ArrayList;

public class UserAccount {

    private String id;
    private String username;
    private String emailAddress;
    private String phoneNumber;
    private ArrayList<Coin> watchlistItems;

    public UserAccount() {}

    public UserAccount(String username,String emailAddress, String phoneNumber, ArrayList<Coin> watchlistItems) {
        this.username = username;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.watchlistItems = watchlistItems;
    }

    public String _getId() {return id;}
    public String getUsername() {return username;}
    public String getEmailAddress() {return emailAddress;}
    public String getPhoneNumber() {return phoneNumber;}
    public ArrayList<Coin> getWatchlistItems() {return watchlistItems;}

    public void setId(String id) {
        this.id = id;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
