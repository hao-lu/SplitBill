package com.lu.hao.splitbill;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtraCostsActivity extends AppCompatActivity {
    private NumberFormat currency_format = NumberFormat.getCurrencyInstance();
    private SharedPreferences mSharedPreferences;
    private String mDiscounts = "";
    private List<Item> mItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extra_costs);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Get data from the other activity
        Intent activityThatCalled = getIntent();
        mItems = (ArrayList<Item>)activityThatCalled.getSerializableExtra("items");
        Bundle tax_tip = activityThatCalled.getExtras();
        float tax = tax_tip.getFloat("tax") / 100;
        float tip = tax_tip.getFloat("tip") / 100;

        // ((TextView) findViewById(R.id.input_subtotal)).setText(Float.toString(subtotal(price_of_items)));
        ((TextView) findViewById(R.id.input_subtotal)).setText(currency_format.format(subtotal()).substring(1));

        tip();
        discounts();
        gratuity();
        tax();

        setPriceInputFilter((EditText)findViewById(R.id.input_discounts));
        setPriceInputFilter((EditText)findViewById(R.id.input_gratuity));
        setPriceInputFilter((EditText)findViewById(R.id.input_tax));

        ((TextView)findViewById(R.id.input_tip)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTaxTipDialog();
            }
        });

        total();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_extra_costs, menu);
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
            case R.id.next_button:
                Intent intent = new Intent(this, SplitBillActivity.class);
                intent.putExtra("items", (Serializable)mItems);

                Bundle extra_costs = new Bundle();
                String subtotal = ((TextView)findViewById(R.id.input_subtotal)).getText().toString();
                String tax = ((EditText)findViewById(R.id.input_tax)).getText().toString();
                String discounts = ((EditText)findViewById(R.id.input_discounts)).getText().toString();
                String gratuity = ((EditText)findViewById(R.id.input_gratuity)).getText().toString();
                String tip = ((TextView)findViewById(R.id.input_tip)).getText().toString();

                if (subtotal.length() > 0) {
                    extra_costs.putFloat("subtotal", Float.parseFloat(subtotal));
                }
                else {
                    extra_costs.putFloat("subtotal", 0);
                }
                if (tax.length() > 0) {
                    extra_costs.putFloat("tax", Float.parseFloat(tax));
                }
                else {
                    extra_costs.putFloat("tax", 0);
                }
                if (discounts.length() > 0) {
                    extra_costs.putFloat("discounts", Float.parseFloat(discounts));
                }
                else {
                    extra_costs.putFloat("discounts", 0);
                }
                if (gratuity.length() > 0) {
                    extra_costs.putFloat("gratuity", Float.parseFloat(gratuity));
                }
                else {
                    extra_costs.putFloat("gratuity", 0);
                }
                if (tip.length() > 0) {
                    extra_costs.putFloat("tip", Float.parseFloat(tip));
                }
                else {
                    extra_costs.putFloat("tip", 0);
                }
                intent.putExtras(extra_costs);
                startActivity(intent);
                break;

        }

        return super.onOptionsItemSelected(item);
    }


    public float subtotal() {
        float subtotal_price = 0;
        for (Item item : mItems) {
            subtotal_price += item.getPrice();
        }
        return subtotal_price;
    }

    public void discounts() {
        EditText input_discounts = (EditText) findViewById(R.id.input_discounts);
        setTextChangeListener(input_discounts);
    }

    public void gratuity() {
        EditText input_gratuity = (EditText) findViewById(R.id.input_gratuity);
        setTextChangeListener(input_gratuity);
    }

    public void tip() {
        mSharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        TextView tip_view = (TextView)findViewById(R.id.input_tip);
        float tip_percent = Float.parseFloat(mSharedPreferences.getString("tip", "15"));
        float tip = subtotal()  * (tip_percent / 100);
        tip_view.setText(currency_format.format(tip).substring(1));

    }

    public void tax() {
        EditText input_tax = (EditText) findViewById(R.id.input_tax);
        setTextChangeListener(input_tax);
    }


    public void total() {
        float total, subtotal, discounts, gratuity, tip, tax;
        TextView total_price = (TextView)findViewById(R.id.input_total);
        String subtotal_string = ((TextView)findViewById(R.id.input_subtotal)).getText().toString();
        subtotal = Float.parseFloat(subtotal_string);

        String discounts_string = ((EditText)findViewById(R.id.input_discounts)).getText().toString();
        if (discounts_string.length() > 0 && discounts_string.compareTo(".") != 0) {
            discounts = Float.parseFloat(discounts_string);
        }
        else {
            discounts = 0;
        }
        String gratuity_string = ((EditText)findViewById(R.id.input_gratuity)).getText().toString();
        if (gratuity_string.length() > 0 && gratuity_string.compareTo(".") != 0) {
            gratuity = Float.parseFloat(gratuity_string);
        }
        else {
            gratuity = 0;
        }
        String tip_string = ((TextView)findViewById(R.id.input_tip)).getText().toString();
        tip = Float.parseFloat(tip_string);

        String tax_string = ((EditText)findViewById(R.id.input_tax)).getText().toString();
        if (tax_string.length() > 0 && tax_string.compareTo(".") != 0) {
            tax = Float.parseFloat(tax_string);
        }
        else {
            tax = 0;
        }

        if (gratuity_string.length() > 0) {
            total = subtotal - discounts + gratuity + tax;
        }
        else {
            total = subtotal - discounts + tip + tax;
        }

        total_price.setText(currency_format.format(total).substring(1));
    }

    public void setTextChangeListener(EditText subCost) {
        final EditText sub_cost = subCost;
        sub_cost.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(mDiscounts)) {
                    sub_cost.removeTextChangedListener(this);
                    // Update the current total
                    total();
                    mDiscounts = s.toString();
                    sub_cost.setSelection(s.toString().length());
                    sub_cost.addTextChangedListener(this);
                }
            }
        });
    }

    public void setPriceInputFilter(EditText price) {
        if (price != null) {
            price.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(5,2)});
        }
    }

    public void showTaxTipDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_tax_and_tip, null);
        final EditText tip_input = (EditText)dialogView.findViewById(R.id.tip_edit_text);
        dialogBuilder.setTitle("Set tip");

        // Save use tax and tip
        mSharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        tip_input.setHint(mSharedPreferences.getString("tip", "15"));

        dialogBuilder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                if (tip_input.getText().toString().length() > 0) {
                    editor.putString("tip", tip_input.getText().toString());
                    TextView tip = (TextView)findViewById(R.id.input_tip);
                    float new_tip = subtotal() * ((Float.parseFloat(tip_input.getText().toString())/100));
                    tip.setText(currency_format.format(new_tip).substring(1));
                    // Update the total price
                    total();
                }
                editor.commit();
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // do nothing
            }
        });
        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    public class DecimalDigitsInputFilter implements InputFilter {
        Pattern mPattern;
        public DecimalDigitsInputFilter(int digitsBeforeZero,int digitsAfterZero) {
            mPattern= Pattern.compile("[0-9]{0," + (digitsBeforeZero-1) + "}+((\\.[0-9]{0," + (digitsAfterZero-1) + "})?)||(\\.)?");
        }
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Matcher matcher=mPattern.matcher(dest);
            if(!matcher.matches())
                return "";
            return null;
        }
    }
}
