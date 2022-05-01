package com.example.cryptocurrencypricetracker;

import java.math.BigDecimal;

public class CoinItem {
    private String symbol;
    private BigDecimal price;
    private final int imageResource;

    public CoinItem(String name, BigDecimal price, int imageResource) {
        this.symbol = name;
        this.price = price;
        this.imageResource = imageResource;
    }

    String getSymbol() {return symbol;}
    BigDecimal getPrice() {return price;}
    public int getImageResource() {return imageResource;}
}
