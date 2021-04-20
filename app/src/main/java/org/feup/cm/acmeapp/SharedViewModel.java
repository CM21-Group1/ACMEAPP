package org.feup.cm.acmeapp;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.feup.cm.acmeapp.model.Product;
import org.feup.cm.acmeapp.model.Purchase;
import org.feup.cm.acmeapp.model.Voucher;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

public class SharedViewModel extends ViewModel {

    private List<Product> productList;
    private List<Voucher> voucherList;
    private Purchase purchase;
    private PublicKey personalPublicKey;
    private PrivateKey personalPrivateKey;

    public PrivateKey getPersonalPrivateKey() {
        return personalPrivateKey;
    }

    public void setPersonalPrivateKey(PrivateKey personalPrivateKey) {
        this.personalPrivateKey = personalPrivateKey;
    }

    public PublicKey getPersonalPublicKey() {
        return personalPublicKey;
    }

    public void setPersonalPublicKey(PublicKey personalPublicKey) {
        this.personalPublicKey = personalPublicKey;
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
