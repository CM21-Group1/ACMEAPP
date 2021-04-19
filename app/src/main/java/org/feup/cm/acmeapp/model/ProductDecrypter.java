package org.feup.cm.acmeapp.model;

import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class ProductDecrypter {
    String encryptedMessage;
    Product product;
    PublicKey supermaketPublicKey;

    public ProductDecrypter(PublicKey supermaketPublicKey, String encryptedMessage) throws BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, UnsupportedEncodingException, NoSuchPaddingException, InvalidKeyException {
        this.supermaketPublicKey = supermaketPublicKey;
        this.encryptedMessage = encryptedMessage;
        createProduct();
    }

    private void createProduct() throws BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {

        Cipher cipher = null;
        cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, supermaketPublicKey);
        String s = new String(cipher.doFinal(encryptedMessage.getBytes("UTF-8")), "UTF-8");

        Gson g = new Gson();
        product = g.fromJson(s , Product.class);
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
