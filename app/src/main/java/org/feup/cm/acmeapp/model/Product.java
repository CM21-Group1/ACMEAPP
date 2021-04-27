package org.feup.cm.acmeapp.model;

public class Product {
    private String productCode;
    private String name;
    private double price;
    private String imageUrl;
    private int quantity;

    public Product(String productCode, String name, double price, String imageUrl) {
        this.productCode = productCode;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        quantity = 1;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getProductCode() {
        return productCode;
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


    public void increaseQuantity(){
        quantity++;
    }

    public void decreaseQuantity(){
        if (quantity > 1){
            quantity--;
        }
    }

    public int getQuantity(){
        return quantity;
    }

    @Override
    public String toString() {
        return "{" +
                "\"_id\": \"" + productCode + '\"' +
                ", \"name\": \"" + name + '\"' +
                ", \"price\": " + price +
                ", \"quantity\": " + quantity+
                '}';
    }
}
