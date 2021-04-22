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
       /*String message = this.toString();
       int nr = message.length();
       byte[] messageAux = message.getBytes();*/

       ArrayList<String> sels = new ArrayList<>();
       sels.add(this.toString());

       System.out.println(sels);

       int nr = sels.get(0).length();
       System.out.println("Nr "+ nr);
       ByteBuffer bb = ByteBuffer.allocate((nr+4)+Constants.KEY_SIZE/8);
       bb.putInt(nr);

       System.out.println("Sells get[0]");
       System.out.println(sels.get(0));
       System.out.println(bb.position());

       bb.put(sels.get(0).getBytes());
       System.out.println(bb.position());

       System.out.println(bb);

       byte[] message = bb.array();

       String s = null;

       try {
           KeyStore ks = KeyStore.getInstance(Constants.ANDROID_KEYSTORE);
           ks.load(null);
           KeyStore.Entry entry = ks.getEntry(Constants.keyname, null);
           PrivateKey pri = ((KeyStore.PrivateKeyEntry)entry).getPrivateKey();
           Signature sg = Signature.getInstance(Constants.SIGN_ALGO);
           sg.initSign(pri);
           sg.update(message, 0, nr+4);
           int sz = sg.sign(message, nr+4, Constants.KEY_SIZE/8);
           System.out.println(message);
           System.out.println("Sign size = " + sz + " bytes.");

           s = new String(message, "UTF-8");


           String error = "";
           boolean validated = false;
           //StringBuilder sb = new StringBuilder();

           message = s.getBytes();

           byte[] aux = new byte[4];
           aux[0] = message[0];
           aux[1] = message[1];
           aux[2] = message[2];
           aux[3] = message[3];

           nr = ByteBuffer.wrap(aux).getInt() ;                                           // get the nr of different products (first position)

           System.out.println("New nr " + nr);

           /*for (int k=4; k<=nr; k++) {
               sb.append(new String(message[k]));                              // get the name of each product from the type
           }*/

           String ss = new String(Arrays.copyOfRange(message, 4, nr ));

           System.out.println(ss);

           KeyFactory keyFactory = KeyFactory.getInstance("RSA");        // to build a key object we need a KeyFactory object
           // the key raw values (as BigIntegers) are used to build an appropriate KeySpec
           RSAPublicKeySpec RSAPub = new RSAPublicKeySpec(new BigInteger(getPubKey().getModulus()), new BigInteger("65537"));
           pubKey = keyFactory.generatePublic(RSAPub);                   // the KeyFactory is used to build the key object from the key spec

           if (pubKey == null)
               System.out.println("Missing key");
               //sb.append("\nMissing pub key+.");
           else {
               byte[] mess = new byte[nr+1];                                // extract the order and the signature from the all message
               byte[] sign = new byte[Constants.KEY_SIZE/8];
               ByteBuffer bb1 = ByteBuffer.wrap(message);
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
          /* sb.append("\nValidated = ");
           sb.append(validated);
           sb.append(error);
           System.out.println(sb.toString());   */                                 // show order and validation
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
