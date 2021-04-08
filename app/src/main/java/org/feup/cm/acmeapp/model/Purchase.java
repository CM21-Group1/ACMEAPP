package org.feup.cm.acmeapp.model;

import java.util.Date;

public class Purchase {
    private String userId;
    private Product[] products;
    private double totalPrice;
    private Date date;

    public Purchase() {
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setProducts(Product[] products) {
        this.products = products;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public Product[] getProducts() {
        return products;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public Date getDate() {
        return date;
    }
}
