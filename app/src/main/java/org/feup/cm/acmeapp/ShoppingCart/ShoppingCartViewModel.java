package org.feup.cm.acmeapp.ShoppingCart;

import androidx.lifecycle.ViewModel;

import org.feup.cm.acmeapp.model.Product;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCartViewModel extends ViewModel {
    // TODO: Implement the ViewModel
    List<Product> productList = new ArrayList<>();

    public ShoppingCartViewModel() {
    }

    public void setProductList(List<Product> productList) {
        this.productList = productList;
    }

    public List<Product> getProductList() {
        return productList;
    }
}