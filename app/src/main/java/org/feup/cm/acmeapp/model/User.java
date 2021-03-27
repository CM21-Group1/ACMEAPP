package org.feup.cm.acmeapp.model;

import com.google.gson.annotations.SerializedName;

public class User {
    private String name;
    private String username;
    private String password;
    private String payment_card;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(String name, String username, String password, String payment_card) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.payment_card = payment_card;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getPayment_card() {
        return payment_card;
    }
}
