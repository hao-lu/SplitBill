package com.lu.hao.splitbill;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.List;

public class PriceItemCardAdapter extends RecyclerView.Adapter<PriceItemCardAdapter.MyViewHolder> {

    private List<ItemCost> items;
    private Context context;
    NumberFormat currency_format = NumberFormat.getCurrencyInstance();

    public PriceItemCardAdapter(List<ItemCost> items, Context context) {
        this.items = items;
        this.context = context;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private CardView card_view;
        private TextView name, price, tax, tax_price, tip, tip_price, total_price;

        public MyViewHolder(View view) {
            super(view);
            card_view = (CardView)view.findViewById(R.id.card_total_cost);
            name = (TextView)view.findViewById(R.id.item_name_text);
            price = (TextView) view.findViewById(R.id.item_price_text);
            tax_price = (TextView) view.findViewById(R.id.tax_price_text);
            tip_price = (TextView) view.findViewById(R.id.tip_price_text);
            total_price = (TextView) view.findViewById(R.id.total_price_text);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sum_costs_card, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.name.setText(items.get(position).getName());
        holder.price.setText(currency_format.format(items.get(position).getPrice()));
        holder.tax_price.setText(currency_format.format(items.get(position).getTax()));
        holder.tip_price.setText(currency_format.format(items.get(position).geTip()));
        holder.total_price.setText(currency_format.format(items.get(position).getTotal()));

//        holder.price.setText(currency_format.format(items.get(position).getPrice()).substring(1));
//        holder.tax_price.setText(currency_format.format(items.get(position).getTax()).substring(1));
//        holder.tip_price.setText(currency_format.format(items.get(position).geTip()).substring(1));
//        holder.total_price.setText(currency_format.format(items.get(position).getTotal()).substring(1));
//        holder.price.setText(items.get(position).getPriceString());
//        holder.tax_price.setText(items.get(position).getTaxString());
//        holder.tip_price.setText(items.get(position).getTipString());
//        holder.total_price.setText(items.get(position).getTotalString());

    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
