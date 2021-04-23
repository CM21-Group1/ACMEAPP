package org.feup.cm.acmeapp.model;

import android.util.Log;

import org.feup.cm.acmeapp.Constants;
import org.feup.cm.acmeapp.Security.KeyPart;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;

public class Purchase {
    private static PublicKey pubKey = null;
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

   public String QRCodeString(){
        //Obtem a mensagem
        String mensagem = this.toString();

        //Obtem o tamanho da mensagem
        int nr = mensagem.length();
        System.out.println("Nr "+ nr);

        //Cria o buffer e coloca nos primeiros 4 bytes (int) o tamanho da mensagem
        ByteBuffer bb = ByteBuffer.allocate( (nr+4)+ Constants.KEY_SIZE/8);
        bb.putInt(nr);

        //Coloca a mensagem no buffer
        bb.put(mensagem.getBytes());

        System.out.println("Mensagem");
        System.out.println(mensagem);
        System.out.println("Tamanho da buffer ( 4+"+nr+"): " + bb.position());


        byte[] message = bb.array();

        byte[] aux = new byte[4];
        aux[0] = message[0];
        aux[1] = message[1];
        aux[2] = message[2];
        aux[3] = message[3];

       nr = ByteBuffer.wrap(aux).getInt() ;
       System.out.println("New nr verificações" + nr);

       String s = null;

       try {
           KeyStore ks = KeyStore.getInstance(Constants.ANDROID_KEYSTORE);
           ks.load(null);
           KeyStore.Entry entry = ks.getEntry(Constants.keyname, null);
           PrivateKey pri = ((KeyStore.PrivateKeyEntry)entry).getPrivateKey();
           Signature sg = Signature.getInstance(Constants.SIGN_ALGO);
           sg.initSign(pri);
           sg.update(message, 0, nr+4);
           sg.sign(message, nr+4, Constants.KEY_SIZE/8);

           //mensagem criada
           s = new String(message);
           System.out.println(s);


           String error = "";
           boolean validated = false;
           byte[] message2 = s.getBytes();

           byte[] aux2 = new byte[4];
           aux2[0] = message2[0];
           aux2[1] = message2[1];
           aux2[2] = message2[2];
           aux2[3] = message2[3];

           nr = ByteBuffer.wrap(aux2).getInt() ;                                           // get the nr of different products (first position)

           System.out.println("New nr " + nr);


           String ss = new String(Arrays.copyOfRange(message2, 4, nr+4));

           System.out.println(ss);

           KeyFactory keyFactory = KeyFactory.getInstance("RSA");        // to build a key object we need a KeyFactory object
           // the key raw values (as BigIntegers) are used to build an appropriate KeySpec
           RSAPublicKeySpec RSAPub = new RSAPublicKeySpec(new BigInteger(getPubKey().getModulus()), new BigInteger(getPubKey().getExponent()));
           pubKey = keyFactory.generatePublic(RSAPub);                   // the KeyFactory is used to build the key object from the key spec

           if (pubKey == null)
               System.out.println("Missing key");
           else {
               byte[] mess = new byte[nr+1];                                // extract the order and the signature from the all message
               byte[] sign = new byte[Constants.KEY_SIZE/8];
               ByteBuffer bb1 = ByteBuffer.wrap(message2);
               bb1.get(mess, 0, nr+1);
               bb1.get(sign, 0, Constants.KEY_SIZE/8);
               try {
                   Signature sg1 = Signature.getInstance("SHA256WithRSA");      // verify the signature with the public key
                   sg1.initVerify(pubKey);
                   sg1.update(mess);
                   validated = sg1.verify(sign);
                   System.out.println(validated);
               }
               catch (Exception ex) {
                   error = "\n" + ex.getMessage();
               }
           }
       }catch (Exception e){
            System.out.println(e + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }


        return s;
    }

    KeyPart getPubKey() {
        KeyPart pkey = null;
        try {
            KeyStore ks = KeyStore.getInstance(Constants.ANDROID_KEYSTORE);
            ks.load(null);
            KeyStore.Entry entry = ks.getEntry(Constants.keyname, null);
            PublicKey pub = ((KeyStore.PrivateKeyEntry)entry).getCertificate().getPublicKey();
            pkey = new KeyPart(((RSAPublicKey)pub).getModulus().toByteArray(), ((RSAPublicKey)pub).getPublicExponent().toByteArray());
        }
        catch (Exception ex) {
            System.out.println(ex);
        }
        return pkey;
    }
}
