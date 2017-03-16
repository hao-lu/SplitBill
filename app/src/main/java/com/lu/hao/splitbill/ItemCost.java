package com.lu.hao.splitbill;

/**
 * Created by Hao on 4/23/2016.
 */
public class ItemCost {
    private String name;
    private double price;
    private double tax;
    private double tip;
    private double total;

    public ItemCost() {
    }

    public ItemCost(String n, double p, double tx, double tp, double tl) {
        this.name = n;
        this.price = p;
        this.tax = tx;
        this.tip = tp;
        this.total = tl;
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

    public double getTax() {
        return tax;
    }

    public String getTaxString() {
        return Double.toString(tax);
    }

    public double geTip() {
        return tip;
    }

    public String getTipString() {
        return Double.toString(tip);
    }

    public double getTotal() {
        return total;
    }

    public String getTotalString() {
        return Double.toString(total);
    }

}
