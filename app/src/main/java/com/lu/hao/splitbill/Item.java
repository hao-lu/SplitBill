package com.lu.hao.splitbill;

import java.io.Serializable;
import java.text.NumberFormat;

public class Item implements Serializable {
    private String name;
    private double price;
    NumberFormat currency_format = NumberFormat.getCurrencyInstance();

    public Item() {

    }
    public Item(String n, double p) {
        this.name = n;
        this.price = p;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getPriceString() {
        return Double.toString(price);
    }

    public void setName(String n) {
        name = n;
    }

    public void setPrice(double p) {
        price = p;
    }

    public void setPrice(String p) {
        price = Double.parseDouble(p);
    }

    public String getPriceCurrencyFormat() {
        return currency_format.format(price);
    }

    public String getPriceCurrencyFormatNoSymbol() {
        return currency_format.format(price).substring(1);
    }

}
