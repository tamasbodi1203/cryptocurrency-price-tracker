package com.example.pocketsentinel.entity;

import java.util.ArrayList;

public class UserAccount {

    private String id;
    private String username;
    private String emailAddress;
    private String phoneNumber;
    private ArrayList<Coin> watchlist;

    public UserAccount() {}

    public UserAccount(String username,String emailAddress, String phoneNumber, ArrayList<Coin> watchlist) {
        this.username = username;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.watchlist = watchlist;
    }

    public String _getId() {return id;}
    public String getUsername() {return username;}
    public String getEmailAddress() {return emailAddress;}
    public String getPhoneNumber() {return phoneNumber;}
    public ArrayList<Coin> getWatchlist() {return watchlist;}

    public void setId(String id) {
        this.id = id;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public void setWatchlist(ArrayList<Coin> watchlist) { this.watchlist = watchlist; }
}
