package org.feup.cm.acmeapp.model;

import org.feup.cm.acmeapp.Constants;

import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
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

    public String getTotalPrice() {
        return String.format("%.02f", totalPrice);
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
                    ",\"totalPrice\":\"" + String.format("%.02f", totalPrice) + '\"' +
                    ",\"voucherId\":" + voucher +
                    '}';
        }

        return "{" +
                "\"userId\":\"" + userId + '\"' +
                ",\"products\":" + products +
                ",\"totalPrice\":\"" + String.format("%.02f", totalPrice) + '\"' +
                '}';

    }

    public byte[] QRCodeString() {
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

            byte[] completeMessage = s.getBytes(Constants.ISO_SET);
            byte[] message2 = new byte[completeMessage.length - Constants.KEY_SIZE / 8];
            byte[] signature = new byte[Constants.KEY_SIZE / 8];

            ByteBuffer bb1 = ByteBuffer.wrap(completeMessage);
            bb1.get(message2, 0, completeMessage.length - Constants.KEY_SIZE / 8);
            bb1.get(signature, 0, Constants.KEY_SIZE / 8);

            System.out.println("Mensagem!" + new String(message2, Constants.ISO_SET));
            System.out.println("Mensagem!" + Base64.getEncoder().encodeToString(message2));
            System.out.println("Signature!" + Base64.getEncoder().encodeToString(signature));

        } catch (Exception e) {
            System.out.println(e);
        }

        return message;
    }
}
