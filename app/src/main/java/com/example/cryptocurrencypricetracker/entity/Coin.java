package com.example.cryptocurrencypricetracker.entity;

public class Coin {

    private String id;
    private String coinGeckoId;
    private String symbol;
    private double price;
    private double percentageChange;
    private int imageResource;

    public Coin() {}

    public Coin(String coinGeckoId, String symbol, double price,double percentageChange, int imageResource) {
        this.coinGeckoId = coinGeckoId;
        this.symbol = symbol;
        this.price = price;
        this.percentageChange = percentageChange;
        this.imageResource = imageResource;
    }

    public String _getId() {return id;}
    public String getCoinGeckoId() {return coinGeckoId;}
    public String getSymbol() {return symbol;}
    public double getPrice() {return price;}
    public double getPercentageChange() {return percentageChange;}
    public int getImageResource() {return imageResource;}

    public void setId(String id) {
        this.id = id;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public void setPercentageChange(double percentageChange) {this.percentageChange = percentageChange;}

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Coin){
            Coin coin = (Coin) obj;
            if(coin != null && this.coinGeckoId.equals(coin.coinGeckoId)){
                return true;
            }
        }
        return false;
    }
}