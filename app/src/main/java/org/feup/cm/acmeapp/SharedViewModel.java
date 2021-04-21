package org.feup.cm.acmeapp;

import androidx.lifecycle.ViewModel;

import org.feup.cm.acmeapp.Security.Key;
import org.feup.cm.acmeapp.model.Product;
import org.feup.cm.acmeapp.model.Purchase;
import org.feup.cm.acmeapp.model.Voucher;

import java.util.List;

public class SharedViewModel extends ViewModel {
    private List<Product> productList;
    private List<Voucher> voucherList;
    private Purchase purchase;
    private Key pubKey, privkey;

    public Key getPrivateKey() {
        return privkey;
    }

    public void setPrivateKey ( Key privkey) {
        System.out.println("Set private key" + privkey );
        this.privkey = privkey;
    }

    public Key getPubKey() {
        return pubKey;
    }

    public void setPublicKey (Key privkey) {
        System.out.println("Set public key" + privkey );
        this.privkey = privkey;
    }

    public List<Product> getProductList() {
        return productList;
    }

    public void setProductList(List<Product> productList) {
        this.productList = productList;
    }

    public Purchase getPurchase() {
        return purchase;
    }

    public void setPurchase(Purchase purchase) {
        this.purchase = purchase;
    }

    public List<Voucher> getVoucherList() {
        return voucherList;
    }

    public void setVoucherList(List<Voucher> voucherList) {
        this.voucherList = voucherList;
    }

}
