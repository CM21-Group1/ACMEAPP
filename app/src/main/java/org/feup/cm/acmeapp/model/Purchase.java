package org.feup.cm.acmeapp.model;

import org.feup.cm.acmeapp.Constants;
import org.feup.cm.acmeapp.Security.KeyPart;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
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

        if (voucher != null) {
            return "{" +
                    "\"userId\":\"" + userId + '\"' +
                    ",\"products\":" + products +
                    ",\"totalPrice\":\"" + totalPrice + '\"' +
                    ",\"voucherId\":" + voucher +
                    '}';
        }

        return "{" +
                "\"userId\":\"" + userId + '\"' +
                ",\"products\":" + products +
                ",\"totalPrice\":\"" + totalPrice + '\"' +
                '}';

    }

    public byte[] QRCodeString() {
        getPubKey();
        //Obtem a mensagem
        String mensagem = this.toString();
        mensagem.replace(" ", "");

        //Obtem o tamanho da mensagem
        int nr = mensagem.length();


        //Cria o buffer e coloca nos primeiros 4 bytes (int) o tamanho da mensagem
        ByteBuffer bb = ByteBuffer.allocate((nr) + Constants.KEY_SIZE / 8);

        //Coloca a mensagem no buffer
        bb.put(mensagem.getBytes());

        byte[] message = bb.array();

        try {
            KeyStore ks = KeyStore.getInstance(Constants.ANDROID_KEYSTORE);
            ks.load(null);
            KeyStore.Entry entry = ks.getEntry(Constants.keyname, null);
            PrivateKey pri = ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
            Signature sg = Signature.getInstance(Constants.SIGN_ALGO);
            sg.initSign(pri);
            sg.update(message, 0, nr);
            sg.sign(message, nr, Constants.KEY_SIZE / 8);

            //mensagem criada
            String s = new String(message, Constants.ISO_SET);

            byte[] completeMessage =  s.getBytes(Constants.ISO_SET);
            byte[] message2 = new byte[completeMessage.length - Constants.KEY_SIZE / 8];
            byte[] signature = new byte[Constants.KEY_SIZE / 8];

            ByteBuffer bb1 = ByteBuffer.wrap(completeMessage);
            bb1.get(message2, 0, completeMessage.length - Constants.KEY_SIZE / 8);
            bb1.get(signature, 0, Constants.KEY_SIZE / 8);

            System.out.println("Mensagem!"+ new String(message2, Constants.ISO_SET));
            System.out.println("Mensagem!"+ Base64.getEncoder().encodeToString(message2));
            System.out.println("Signature!"+ Base64.getEncoder().encodeToString(signature));


            //System.out.println(s);
            //System.out.println("Tamanho da string criada: " + s.length());


            /*boolean validated = false;
            byte[] message2 = s.getBytes(StandardCharsets.ISO_8859_1);


            KeyFactory keyFactory = KeyFactory.getInstance("RSA");        // to build a key object we need a KeyFactory object
            // the key raw values (as BigIntegers) are used to build an appropriate KeySpec
            RSAPublicKeySpec RSAPub = new RSAPublicKeySpec(new BigInteger(getPubKey().getModulus()), new BigInteger(getPubKey().getExponent()));
            pubKey = keyFactory.generatePublic(RSAPub);                   // the KeyFactory is used to build the key object from the key spec
            //with 0x30 (ASN.1 SEQUENCE and CONSTRUCTED), so there is no leading 0x00 to drop.
            String base64Encoded = Base64.encodeToString(pubKey.getEncoded(), Base64.DEFAULT);
            System.out.println(base64Encoded);

            if (pubKey == null)
                System.out.println("Missing key");
            else {
                byte[] mess = new byte[message2.length - Constants.KEY_SIZE / 8];                                // extract the order and the signature from the all message
                byte[] sign = new byte[Constants.KEY_SIZE / 8];
                ByteBuffer bb1 = ByteBuffer.wrap(message2);
                bb1.get(mess, 0, message2.length - Constants.KEY_SIZE / 8);
                bb1.get(sign, 0, Constants.KEY_SIZE / 8);
                System.out.println(new String(mess, Constants.ISO_SET));
                try {
                    Signature sg1 = Signature.getInstance(Constants.SIGN_ALGO);      // verify the signature with the public key
                    sg1.initVerify(pubKey);
                    sg1.update(mess);
                    validated = sg1.verify(sign);
                    System.out.println(validated);
                    String encoded = java.util.Base64.getEncoder().encodeToString(sign);
                    System.out.println(encoded);
                } catch (Exception ex) {
                    System.out.println(ex);//error = "\n" + ex.getMessage();
                }
            }*/
        } catch (Exception e) {
            System.out.println(e);
        }

        return message;
    }


    private KeyPart getPubKey() {
        KeyPart pkey = null;
        try {
            KeyStore ks = KeyStore.getInstance(Constants.ANDROID_KEYSTORE);
            ks.load(null);
            KeyStore.Entry entry = ks.getEntry(Constants.keyname, null);
            PublicKey pub = ((KeyStore.PrivateKeyEntry) entry).getCertificate().getPublicKey();
            System.out.println("PublicKey:"+Base64.getEncoder().encodeToString(pub.getEncoded()));
            pkey = new KeyPart(((RSAPublicKey) pub).getModulus().toByteArray(), ((RSAPublicKey) pub).getPublicExponent().toByteArray());

        } catch (Exception ex) {
            System.out.println(ex);
        }
        return pkey;
    }
}
