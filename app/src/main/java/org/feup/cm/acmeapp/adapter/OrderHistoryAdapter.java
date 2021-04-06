package org.feup.cm.acmeapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import org.feup.cm.acmeapp.R;
import org.feup.cm.acmeapp.model.OrderHistory;
import org.feup.cm.acmeapp.model.Product;

import java.util.List;


public class OrderHistoryAdapter extends BaseAdapter {

    private Context context;
    private List<OrderHistory> orderHistoryList;
    private String date;
    private double totalPrice;
    private int status;

    public OrderHistoryAdapter(Context context, List<OrderHistory> orderHistoryList){
        this.context = context;
        this.orderHistoryList = orderHistoryList;
    }

    @Override
    public int getCount() {
        return orderHistoryList.size();
    }

    @Override
    public Object getItem(int position) {
        return orderHistoryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            view = new View(context);
            view = inflater.inflate(R.layout.view_row, null);

        } else {
            view = (View) convertView;
        }

        date = orderHistoryList.get(position).getDate();
        totalPrice = orderHistoryList.get(position).getPriceTotal();
        status = orderHistoryList.get(position).getStatus();

        RelativeLayout rlOrderHistory = (RelativeLayout) view.findViewById(R.id.rlOrderHistory);
        TextView tvDate = (TextView) view.findViewById(R.id.date);
        TextView tvTotalPrice = (TextView) view.findViewById(R.id.totalPrice);

        if (status == 0){
            rlOrderHistory.setBackgroundColor(context.getResources().getColor(R.color.status0));
        }else if (status == 1){
            rlOrderHistory.setBackgroundColor(context.getResources().getColor(R.color.status1));
            tvDate.setTextColor(context.getResources().getColor(R.color.white));
            tvTotalPrice.setTextColor(context.getResources().getColor(R.color.white));
        }else if (status == 2){
            rlOrderHistory.setBackgroundColor(context.getResources().getColor(R.color.status2));
            tvDate.setTextColor(context.getResources().getColor(R.color.white));
            tvTotalPrice.setTextColor(context.getResources().getColor(R.color.white));
        }

        tvDate.setText(date);
        tvTotalPrice.setText(String.valueOf(totalPrice+ " TL "));

        return view;
    }
}
