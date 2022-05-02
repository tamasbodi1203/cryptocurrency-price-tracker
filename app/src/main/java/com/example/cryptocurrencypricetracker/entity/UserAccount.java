package com.example.cryptocurrencypricetracker.entity;

public class UserAccount {

    private String id;
    private String username;
    private String emailAddress;
    private String phoneNumber;

    public UserAccount() {}

    public UserAccount(String username,String emailAddress, String phoneNumber) {
        this.username = username;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
    }

    public String _getId() {return id;}
    public String getUsername() {return username;}
    public String getEmailAddress() {return emailAddress;}
    public String getPhoneNumber() {return phoneNumber;}

    public void setId(String id) {
        this.id = id;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
