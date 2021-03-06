package org.feup.cm.acmeapp;

import androidx.lifecycle.ViewModel;

import org.feup.cm.acmeapp.model.Product;
import org.feup.cm.acmeapp.model.Purchase;
import org.feup.cm.acmeapp.model.Voucher;

import java.util.List;

public class SharedViewModel extends ViewModel {
    private List<Product> productList;
    private Purchase purchase;

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

    public void resetValues(){
        productList = null;
        purchase = null;
    }

}
