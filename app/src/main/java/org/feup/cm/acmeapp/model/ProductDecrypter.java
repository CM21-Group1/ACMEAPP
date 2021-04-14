package org.feup.cm.acmeapp.model;

import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class ProductDecrypter {
    String publKey, encryptedMessage;
    Product product;

    public ProductDecrypter(String publKey, String encryptedMessage) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException, InvalidKeyException, SignatureException {
        this.publKey = publKey;
        this.encryptedMessage = encryptedMessage;
        createProduct();
    }

    private void createProduct() throws InvalidKeySpecException, SignatureException, NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        //Falta fazer a desencriptação
        System.out.println(encryptedMessage);
        Gson g = new Gson();
        product = g.fromJson(teste(), Product.class);
    }

    private String teste() throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        byte[] b1 = Base64.getDecoder().decode(publKey);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(b1);
        KeyFactory kf = KeyFactory.getInstance("RSA");

        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(kf.generatePrivate(spec));
        privateSignature.update(encryptedMessage.getBytes("UTF-8"));
        byte[] s = privateSignature.sign();
        return Base64.getEncoder().encodeToString(s);
    }


    public Product getProduct(){
        return product;
    }

}

/*{
  "productCode" : "12345",
  "name" : "Teste",
  "price" : 25
}*/