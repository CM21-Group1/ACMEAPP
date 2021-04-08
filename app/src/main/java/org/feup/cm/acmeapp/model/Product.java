package org.feup.cm.acmeapp.model;

public class Product {
    private String productCode;
    private String name;
    private double price;

    public Product() {
    }

    public Product(String productCode, String name, double price) {
        this.productCode = productCode;
        this.name = name;
        this.price = price;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "{" +
                "\"_id\": \"" + productCode + '\"' +
                ", \"name\": \"" + name + '\"' +
                ", \"price\": \"" + price +
                '}';
    }
}
