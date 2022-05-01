package com.example.cryptocurrencypricetracker;

import java.math.BigDecimal;

public class CoinItem {
    private String coinGeckoId;
    private String symbol;
    private BigDecimal price;
    private final int imageResource;

    public CoinItem(String coinGeckoId, String symbol, BigDecimal price, int imageResource) {
        this.coinGeckoId = coinGeckoId;
        this.symbol = symbol;
        this.price = price;
        this.imageResource = imageResource;
    }

    public String getCoinGeckoId() {return coinGeckoId;}
    public String getSymbol() {return symbol;}
    public BigDecimal getPrice() {return price;}
    public int getImageResource() {return imageResource;}

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
