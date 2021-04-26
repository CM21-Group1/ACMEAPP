package org.feup.cm.acmeapp.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.feup.cm.acmeapp.Constants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class ProductDecrypter {
    String encryptedMessage;
    String supermaketPublicKey;
    Product product;

    public ProductDecrypter(String supermaketPublicKey, String encryptedMessage) {
        this.supermaketPublicKey = supermaketPublicKey;
        this.encryptedMessage = encryptedMessage;
        product = null;
        createProduct();
    }

    private void createProduct() {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(supermaketPublicKey);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey pub =keyFactory.generatePublic(spec);

            Cipher cipher = Cipher.getInstance(Constants.ENC_ALGO);
            cipher.init(Cipher.DECRYPT_MODE, pub);
            String s = new String(cipher.doFinal(Base64.getDecoder().decode(encryptedMessage)), "UTF-8");

            System.out.println("QR=" +s);

            JSONArray json = new JSONArray(s);
            product = new Product(json.get(0).toString(),json.get(1).toString(), Float.parseFloat(json.get(2).toString()),json.get(3).toString() );
        } catch (Exception e) {
            System.out.println(e);
        }
    }


    public Product getProduct() {
        return product;
    }
}
