package org.feup.cm.acmeapp.model;

import com.google.gson.Gson;

public class ProductDecrypter {
    String publKey, encryptedMessage;
    Product product;

    public ProductDecrypter(String publKey, String encryptedMessage) {
        this.publKey = publKey;
        this.encryptedMessage = encryptedMessage;
        createProduct();
    }

    private void createProduct(){
        //Falta fazer a desencriptação
        System.out.println(encryptedMessage);
        Gson g = new Gson();
        product = g.fromJson(encryptedMessage, Product.class);
    }

    /*
    {
      "productCode" : "12345",
      "name" : "Teste",
      "price" : 25
    }
 */

    public Product getProduct(){
        return product;
    }
}
