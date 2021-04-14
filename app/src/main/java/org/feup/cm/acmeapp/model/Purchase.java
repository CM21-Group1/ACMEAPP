package org.feup.cm.acmeapp.model;

import java.util.Date;
import java.util.List;

public class Purchase {
    private String userId;
    private List<Product> products;
    private double totalPrice;
    private Date date;

    private Voucher voucher;

    public Purchase() {
    }

    public Purchase(String userId, List<Product> products, double totalPrice) {
        this.userId = userId;
        this.products = products;
        this.totalPrice = totalPrice;
    }

    public Purchase(String userId, List<Product> products, double totalPrice, Voucher voucher) {
        this.userId = userId;
        this.products = products;
        this.totalPrice = totalPrice;
        this.voucher = voucher;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {

        if(voucher != null){
            return "{" +
                    "\"userId\": \"" + userId + '\"' +
                    ", \"products\": " + products +
                    ", \"totalPrice\": " + totalPrice +
                    ", \"voucherId\": " + voucher +
                    '}';
        }

        return "{" +
                "\"userId\": \"" + userId + '\"' +
                ", \"products\": " + products +
                ", \"totalPrice\": " + totalPrice +
                '}';

    }
}
