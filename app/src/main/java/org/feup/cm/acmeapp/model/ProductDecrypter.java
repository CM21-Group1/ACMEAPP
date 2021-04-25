package org.feup.cm.acmeapp.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.feup.cm.acmeapp.Constants;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class ProductDecrypter {
    String encryptedMessage;
    Product product;
    PublicKey supermaketPublicKey;

    public ProductDecrypter(PublicKey supermaketPublicKey, String encryptedMessage) throws BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, UnsupportedEncodingException, NoSuchPaddingException, InvalidKeyException, JSONException {
        this.supermaketPublicKey = supermaketPublicKey;
        this.encryptedMessage = encryptedMessage;
        createProduct();
    }

    private void createProduct() throws BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException, JSONException {
        Cipher cipher = Cipher.getInstance(Constants.KEY_ALGO);
        cipher.init(Cipher.DECRYPT_MODE, supermaketPublicKey);

        String s = new String(cipher.doFinal(Base64.getDecoder().decode(encryptedMessage)),"UTF-8");

        JSONObject json = new JSONObject(s);
        System.out.println(json);

        product = new Product(json.getString("_id"),json.getString("name"), json.getInt("price"));
    }


    public Product getProduct(){
        return product;
    }
}
