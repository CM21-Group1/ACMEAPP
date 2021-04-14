package org.feup.cm.acmeapp;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.feup.cm.acmeapp.model.Product;
import org.feup.cm.acmeapp.model.Purchase;
import org.feup.cm.acmeapp.model.Voucher;

import java.util.List;

public class SharedViewModel extends ViewModel {

    private MutableLiveData<String> name;
    private List<Product> productList;
    private List<Voucher> voucherList;
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

    public MutableLiveData<String> getName() {
        return name;
    }

    public void setName(MutableLiveData<String> name) {
        this.name = name;
    }

    public List<Voucher> getVoucherList() {
        return voucherList;
    }

    public void setVoucherList(List<Voucher> voucherList) {
        this.voucherList = voucherList;
    }

    public void setNameData(String nameData) {
        name.setValue(nameData);

/*
        If you are calling setNameData from a background thread use:
        name.postValue(nameData);
*/
    }

    public MutableLiveData<String> getNameData() {
        if (name == null) {
            name = new MutableLiveData<>();
        }

        return name;
    }
}
