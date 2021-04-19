package org.feup.cm.acmeapp.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import org.feup.cm.acmeapp.R;
import org.feup.cm.acmeapp.model.Product;
import java.util.List;


public class ProductListCustomAdapter extends ArrayAdapter<Product> {

    List<Product> productList;

    public ProductListCustomAdapter(Context context, int i, List<Product> list) {
        super(context, i, list);
        productList = list;
    }

    /*@Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = getLayoutInflater().inflate(R.layout.row, parent, false);

        ((TextView) row.findViewById(R.id.product_name)).setText(productList.get(position).getName());
        ((TextView) row.findViewById(R.id.product_price)).setText(String.valueOf(productList.get(position).getPrice()) + "€");

        return (row);
    }*/

    public void setProductList(List<Product> productList) {
        this.productList = productList;
    }
}
