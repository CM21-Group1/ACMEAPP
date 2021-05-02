package org.feup.cm.acmeapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyboardShortcutGroup;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.feup.cm.acmeapp.model.Product;
import org.feup.cm.acmeapp.model.Voucher;

import java.util.List;

public class ProductsDialog extends Dialog implements android.view.View.OnClickListener{
    public Activity c;
    private List<Product> productList;
    private CustomArrayAdapter adapter;
    private ListView list;
    private Voucher voucher;

    public ProductsDialog(Activity a, List<Product> productList) {
        super(a);
        this.c = a;
        this.productList = productList;
    }

    public ProductsDialog(Activity a, List<Product> productList, Voucher voucher) {
        super(a);
        this.c = a;
        this.productList = productList;
        this.voucher = voucher;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog);

        if(voucher != null){
            ((TextView) findViewById(R.id.voucher_name)).setText("Voucher used (ID) " + voucher.get_id());
        }else{
            ((TextView) findViewById(R.id.voucher_name)).setVisibility(View.GONE);
        }

        list = findViewById(R.id.product_array);
        adapter = new CustomArrayAdapter(c.getApplicationContext(), 0, productList);

        adapter.setProductList(productList);
        list.setAdapter(adapter);

    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void onProvideKeyboardShortcuts(List<KeyboardShortcutGroup> data, @Nullable Menu menu, int deviceId) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }

    private class CustomArrayAdapter extends ArrayAdapter<Product> {

        List<Product> productList;

        public CustomArrayAdapter(Context context, int i, List<Product> list) {
            super(context, i, list);
            productList = list;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = getLayoutInflater().inflate(R.layout.row_product_purchase, parent, false);

            System.out.println(productList.get(position).getName());
            ((TextView) row.findViewById(R.id.product_name)).setText(productList.get(position).getName());
            ((TextView) row.findViewById(R.id.product_price)).setText(String.format("%.02f", productList.get(position).getPrice()*productList.get(position).getQuantity() )+ "€");

            return (row);
        }

        public void setProductList(List<Product> productList) {
            this.productList = productList;
        }
    }
}
