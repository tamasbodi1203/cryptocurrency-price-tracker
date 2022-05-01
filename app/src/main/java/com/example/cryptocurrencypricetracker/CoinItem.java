package com.example.cryptocurrencypricetracker;

public class CoinItem {

    private String id;
    private String coinGeckoId;
    private String symbol;
    private double price;
    private int imageResource;

    public CoinItem() {}

    public CoinItem(String coinGeckoId, String symbol, double price, int imageResource) {
        this.coinGeckoId = coinGeckoId;
        this.symbol = symbol;
        this.price = price;
        this.imageResource = imageResource;
    }

    public String _getId() {return id;}
    public String getCoinGeckoId() {return coinGeckoId;}
    public String getSymbol() {return symbol;}
    public double getPrice() {return price;}
    public int getImageResource() {return imageResource;}

    public void setId(String id) {
        this.id = id;
    }
    public void setPrice(double price) {
        this.price = price;
    }
}
