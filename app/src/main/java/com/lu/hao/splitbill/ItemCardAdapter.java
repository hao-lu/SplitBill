package com.lu.hao.splitbill;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemCardAdapter extends RecyclerView.Adapter<ItemCardAdapter.MyViewHolder>{

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

    private List<Item> items;
    private Context context;
    private String current = "";
    private FloatingActionButton fab;
    NumberFormat currency_format = NumberFormat.getCurrencyInstance();

    public ItemCardAdapter(List<Item> items, Context context) {
        this.items = items;
        this.context = context;
        this.fab = (FloatingActionButton)((Activity)context).findViewById(R.id.fab);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private CardView card_view;
        private EditText name;
        private EditText price;

        public MyViewHolder(View view) {
            super(view);
            card_view = (CardView)view.findViewById(R.id.card_view);
            name = (EditText)view.findViewById(R.id.name_input);
            price = (EditText)view.findViewById(R.id.price_input);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_price_card, parent, false);
        // TODO: hide fab on keyboard
//        ViewGroup grandparent = (ViewGroup)parent.getParent();
//        fab = (FloatingActionButton)grandparent.findViewById(R.id.fab);
//        Toast.makeText(context, Integer.toString(grandparent.getChildCount()), Toast.LENGTH_SHORT).show();
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Log.v("ON BIND", Integer.toString(position));
        setTextChangeListener(holder.name, position, true);
        setTextChangeListener(holder.price, position, false);
        setPriceInputFilter(holder.price);
        holder.name.setText(items.get(position).getName());
        if (items.get(position).getPriceCurrencyFormatNoSymbol().compareTo("0.00") != 0) {
            holder.price.setText(items.get(position).getPriceCurrencyFormatNoSymbol());
        }
        setOnTouchListener(holder.name);
        setOnTouchListener(holder.price);
        setOnClickListener(holder.name);
        setOnClickListener(holder.price);

        // Hides fab and shows keyboard after clicking done for price input
        holder.price.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    fab.show();
                    InputMethodManager inputManager = (InputMethodManager)
                            context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.toggleSoftInput(0, 0);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setOnTouchListener(final EditText editText) {
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (fab.isShown() && hasFocus) {
                    fab.hide();
                }
            }
        });
    }

    // TODO: check for keyboard, swipe to delete triggers onclick if in focus (might be ontouch)
    public void setOnClickListener(final EditText editText) {
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputManager = (InputMethodManager)
                        context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (fab.isShown()) {

                    Toast.makeText(context, Integer.toString(fab.getRootView().getBottom()), Toast.LENGTH_SHORT).show();
                    Toast.makeText(context, Integer.toString(fab.getBottom()), Toast.LENGTH_SHORT).show();
                    fab.hide();
                }
            }
        });
    }

    // TODO: check for . input as first character in price and extra cost
    public void setTextChangeListener(final EditText editText, final int position, final boolean isName) {
        editText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    editText.removeTextChangedListener(this);
                    // Do not need to change, noticeable lag
                    // sub_cost.setText(s.toString());

                    if (isName) {
                        items.get(position).setName(s.toString());
                    }
                    else {
                        if (s.length() > 0 && s.toString().compareTo(".") !=0
                                && s.toString().compareTo("") !=0) {
                            items.get(position).setPrice(s.toString());
                        }
                        else {
                            items.get(position).setPrice(0);
                        }
                    }
                    current = s.toString();
                    editText.setSelection(editText.length());
                    editText.addTextChangedListener(this);
                }
            }
        });
    }

    public List<Item> getItemList() {
        return items;
    }

    public void setPriceInputFilter(EditText price) {
        if (price != null) {
            price.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(5,2)});
        }
    }

}
