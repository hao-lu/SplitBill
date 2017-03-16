package com.lu.hao.splitbill;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.text.NumberFormat;
import java.util.List;

public class SplitBillActivity extends AppCompatActivity {
    private List<ItemCost> mItemCost = new ArrayList<>();
    private List<Item> mItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splitbill);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get data from the other activity
        Intent activityThatCalled = getIntent();
        mItems = (ArrayList<Item>)activityThatCalled.getSerializableExtra("items");


        Bundle extra_costs = activityThatCalled.getExtras();
        float subtotal = extra_costs.getFloat("subtotal");
        float tax = extra_costs.getFloat("tax");
        // TODO: implement how discounts will affect price
        float discounts = extra_costs.getFloat("discounts");
        float gratuity = extra_costs.getFloat("gratuity");
        float tip = extra_costs.getFloat("tip");

        // Toast.makeText(SplitBillActivity.this, Float.toString(tip), Toast.LENGTH_SHORT).show();

        float tax_rate = tax / subtotal;
        float tip_rate = tip / 100;


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerViewShow);
        LinearLayoutManager layoutManager = new LinearLayoutManager(SplitBillActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        PriceItemCardAdapter adapter = new PriceItemCardAdapter(mItemCost, SplitBillActivity.this);
        recyclerView.setAdapter(adapter);

        for (int i = 0; i < mItems.size(); i++) {
            String item_name = mItems.get(i).getName();
            double item_price = mItems.get(i).getPrice();
            double item_tip = mItems.get(i).getPrice() * tip_rate;
            double item_tax = item_price * tax_rate;
            double item_total = item_price + item_tip + item_tax;
            mItemCost.add(new ItemCost(item_name, item_price, item_tax, item_tip, item_total));
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splitbill_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        // Toast.makeText(this, "pressed back button", Toast.LENGTH_SHORT).show();
        //((TableLayout) findViewById(R.id.billTableLayout)).removeAllViews();
        finish();
    }

    public void roundToNearestDollar(TableLayout table) {
        NumberFormat currency_format = NumberFormat.getCurrencyInstance();
        for (int i = 0; i < table.getChildCount(); i++) {
            TableRow row = (TableRow)table.getChildAt(i);
            TextView item_price = (TextView)row.getChildAt(1);
            String item_price_string = item_price.getText().toString();
            String omit_symbol = item_price_string.substring(1);
            int item_rounded = (int)(Double.parseDouble(omit_symbol) + .99);
            item_price.setText(currency_format.format(item_rounded));

        }
    }
}
