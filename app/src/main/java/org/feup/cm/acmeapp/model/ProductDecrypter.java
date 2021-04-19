package org.feup.cm.acmeapp.model;

import com.google.gson.Gson;

import java.security.PublicKey;

public class ProductDecrypter {
    String encryptedMessage;
    Product product;
    PublicKey supermaketPublicKey;

    public ProductDecrypter(PublicKey supermaketPublicKey, String encryptedMessage) {
        this.supermaketPublicKey = supermaketPublicKey;
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
