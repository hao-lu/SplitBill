package com.lu.hao.splitbill;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<Item> mItems = new ArrayList<>();
    private SharedPreferences mSharedPreferences;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    protected ImageView mImageView;
    private String mReceiptImagePath;

    private static final String TAG = "SimpleAndroidOCR.java";
    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/SimpleAndroidOCR/";
    public static final String lang = "eng";
    public List<Item> mItemsFromImage = new ArrayList<>();

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        final ItemCardAdapter adapter = new ItemCardAdapter(mItems, MainActivity.this);
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Need to add the new card
                    recyclerView.setAdapter(adapter);
                    ArrayList<Item> update = (ArrayList<Item>) adapter.getItemList();
                    // Update info
                    for (int i = 0; i < mItems.size(); i++) {
                        mItems.get(i).setName(update.get(i).getName());
                        mItems.get(i).setPrice(update.get(i).getPrice());
                    }
                    // Add new row
                    mItems.add(new Item());
                }

            });
        }

        // Hide fab on scroll
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && fab.isShown())
                    fab.hide();
                else if (dy < 0 && !fab.isShown())
                    fab.show();
            }
        });

        // Swipe to delete card row
        recyclerView.addOnItemTouchListener(setSwipeableRecylcerViewTouchListener(recyclerView, adapter));

        mReceiptImagePath = Environment.getExternalStorageDirectory() + "/SimpleAndroidOCR/" + "ocr.jpg";

        createTessData();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)) {
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.show();
            // Toast.makeText(MainActivity.this, "DOWN", Toast.LENGTH_SHORT).show();
            // Log.d(this.getClass().getName(), "back button pressed");
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_done:
                // Check valid to proceed
                if (isRecyclerViewValid((RecyclerView) findViewById(R.id.recyclerView))) {
                    putValuesInArrays();
                    Intent intent = new Intent(this, ExtraCostsActivity.class);
                    intent.putExtra("items", (Serializable) mItems);
                    // Save tip preference
                    mSharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);

                    Bundle tax_tip = new Bundle();
                    tax_tip.putFloat("tax", Float.parseFloat(mSharedPreferences.getString("tax", "7.5")));
                    tax_tip.putFloat("tip", Float.parseFloat(mSharedPreferences.getString("tip", "15")));
                    intent.putExtras(tax_tip);
                    startActivity(intent);
                }
                break;
            case R.id.action_camera:
                dispatchTakePictureIntent();
                // Intent intent = new Intent(this, SimpleAndroidOCRActivity.class);
                // startActivity(intent);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void putValuesInArrays() {
        RecyclerView rv = (RecyclerView) findViewById(R.id.recyclerView);
        for (int i = 0; i < rv.getChildCount(); i++) {
            CardView cv = (CardView) rv.getChildAt(i);
            LinearLayout ll = (LinearLayout) cv.getChildAt(0);
            String name_input = ((EditText) ll.getChildAt(0)).getText().toString();
            String price_input = ((EditText) ll.getChildAt(1)).getText().toString();

            if (name_input.length() > 0 && price_input.length() > 0) {
                mItems.get(i).setName(name_input);
                mItems.get(i).setPrice(price_input);
            }

        }
    }

    public boolean isRecyclerViewValid(RecyclerView recycler) {
        // Check for no elements
        if (recycler.getChildCount() <= 0) {
            Toast.makeText(this, "Add a new item", Toast.LENGTH_SHORT).show();
            return false;
        }
        for (int i = 0; i < recycler.getChildCount(); i++) {
            CardView cv = (CardView) recycler.getChildAt(i);
            LinearLayout ll = (LinearLayout) cv.getChildAt(0);
            String name_input = ((EditText) ll.getChildAt(0)).getText().toString();
            String price_input = ((EditText) ll.getChildAt(1)).getText().toString();
            // Check if elements in rows are empty
            // TODO: can combine to one line return
            if (name_input.length() <= 0 || price_input.length() <= 0) {
                Toast.makeText(this, "Enter the name and price", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    public SwipeableRecyclerViewTouchListener setSwipeableRecylcerViewTouchListener(
            final RecyclerView recyclerView, final ItemCardAdapter adapter) {
        final CoordinatorLayout coordinator_layout =
                (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        SwipeableRecyclerViewTouchListener swipeTouchListener =
                new SwipeableRecyclerViewTouchListener(recyclerView,
                        new SwipeableRecyclerViewTouchListener.SwipeListener() {
                            @Override
                            public boolean canSwipeLeft(int position) {
                                return true;
                            }

                            @Override
                            public boolean canSwipeRight(int position) {
                                return true;
                            }

                            @Override
                            public void onDismissedBySwipeLeft(final RecyclerView recyclerView,
                                                               int[] reverseSortedPositions) {
                                Item temp = null;
                                int temp_pos = 0;
                                // Remove it
                                for (int position : reverseSortedPositions) {
                                    temp = mItems.remove(position);
                                    temp_pos = position;
                                    recyclerView.removeViewAt(position);
                                    adapter.notifyItemRemoved(position);
                                }
                                adapter.notifyDataSetChanged();

                                final Item final_temp_item = temp;
                                final int final_temp_pos = temp_pos;

                                Snackbar.make(recyclerView, "Item is deleted",
                                        Snackbar.LENGTH_LONG).setAction("UNDO", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mItems.add(final_temp_pos, final_temp_item);
                                        // Refresh the view
                                        recyclerView.setAdapter(adapter);
                                        ArrayList<Item> update = (ArrayList<Item>) adapter.getItemList();
                                        // Update info
                                        for (int i = 0; i < mItems.size(); i++) {
                                            mItems.get(i).setName(update.get(i).getName());
                                            mItems.get(i).setPrice(update.get(i).getPrice());
                                        }
                                    }
                                }).show();
                            }

                            @Override
                            public void onDismissedBySwipeRight(final RecyclerView recyclerView,
                                                                int[] reverseSortedPositions) {
                                Item temp = null;
                                int temp_pos = 0;
                                // Remove it
                                for (int position : reverseSortedPositions) {
                                    temp = mItems.remove(position);
                                    temp_pos = position;
                                    recyclerView.removeViewAt(position);
                                    adapter.notifyItemRemoved(position);
                                }
                                adapter.notifyDataSetChanged();

                                final Item final_temp_item = temp;
                                final int final_temp_pos = temp_pos;

                                Snackbar.make(recyclerView, "Item is deleted",
                                        Snackbar.LENGTH_LONG).setAction("UNDO", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mItems.add(final_temp_pos, final_temp_item);
                                        // Refresh the view
                                        recyclerView.setAdapter(adapter);
                                        ArrayList<Item> update = (ArrayList<Item>) adapter.getItemList();
                                        // Update info
                                        for (int i = 0; i < mItems.size(); i++) {
                                            mItems.get(i).setName(update.get(i).getName());
                                            mItems.get(i).setPrice(update.get(i).getPrice());
                                        }
                                    }
                                }).show();
                            }
                        });
        return swipeTouchListener;
    }

    // TODO: fix pressing back goes to progressdialog
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "resultCode: " + resultCode);

        if (resultCode == -1) {
            progressDialog = ProgressDialog.show(this, "Processing...",
                "Adding items to list...", true);
            new Thread(new Runnable() {
                @Override
                public void run()
                {
                    // TODO: going through onImageTaken doesn't update UI (causes bug)
                    // do the thing that takes a long time
//                    ArrayList<Item> to_be_added = onImageTaken();
//                    for (Item item : to_be_added) {
//                        // mItems.add(new Item(item.getName(), item.getPrice()));
//                        mItems.add(item);
//                    }
                    // addItems("HELLO");
                    // mItems.add(new Item("NEW ITEM", 10.99));
                    onImageTaken();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ItemCardAdapter adapter = new ItemCardAdapter(mItems, MainActivity.this);
                            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
                            recyclerView.setAdapter(adapter);
                            ArrayList<Item> update = (ArrayList<Item>) adapter.getItemList();
                            // Update info
                            for (int i = 0; i < mItems.size(); i++) {
                                mItems.get(i).setName(update.get(i).getName());
                                mItems.get(i).setPrice(update.get(i).getPrice());
                            }
                            // Add item
                            mItems.add(mItemsFromImage.get(0));

                            progressDialog.dismiss();
                            // Clear for next image
                            mItemsFromImage.clear();

                        }
                    });
                }
            }).start();

        } else {
            Log.v(TAG, "User cancelled");
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {
        switch (permsRequestCode) {
            case 200:
                boolean writeAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                boolean cameraAccepted = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                break;
        }
    }

    private boolean shouldAskPermission() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(23)
    private boolean hasPermission(String permission){
        if(shouldAskPermission()){
            return(checkSelfPermission(permission)==PackageManager.PERMISSION_GRANTED);
        }
        return true;
    }

    @TargetApi(23)
    private void dispatchTakePictureIntent() {
        if (shouldAskPermission()) {
            String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE",
                    "android.permission.READ_EXTERNAL_STORAGE",
                    "android.permission.CAMERA"};
            int permsRequestCode = 200;
            if(!hasPermission("android.permission.WRITE_EXTERNAL_STORAGE")) {
                requestPermissions(perms, permsRequestCode);
            }
        }
        File file = new File(mReceiptImagePath);
        Uri outputFileUri = Uri.fromFile(file);
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

        startActivityForResult(intent, 0);
    }

    public Bitmap rotateImage(Bitmap bitmap) {
        try {
            ExifInterface exif = new ExifInterface(mReceiptImagePath);
            int exifOrientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            Log.v(TAG, "Orient: " + exifOrientation);
            int rotate = 0;
            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
            }
            Log.v(TAG, "Rotation: " + rotate);
            if (rotate != 0) {
                // Getting width & height of the given image.
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();
                // Setting pre rotate
                Matrix mtx = new Matrix();
                mtx.preRotate(rotate);
                // Rotating Bitmap
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
            }
            // Convert to ARGB_8888, required by tess
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        } catch (IOException e) {
            Log.e(TAG, "Couldn't correct orientation: " + e.toString());
        }
        return bitmap;
    }

    public void onImageTaken() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap bitmap = BitmapFactory.decodeFile(mReceiptImagePath, options);
        bitmap = rotateImage(bitmap);

        Log.v(TAG, "Before baseApi");

        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.setDebug(true);
        baseApi.init(DATA_PATH, lang);
        baseApi.setImage(bitmap);

        String recognizedText = baseApi.getUTF8Text();
        baseApi.end();
        Log.v(TAG, "OCRED TEXT: " + recognizedText);
        if (lang.equalsIgnoreCase("eng") ) {
            // Puts everything on one line (replace anything that's not a-z or A-Z or 0-9 with space)

            // Use this causes date and time to be read
            // recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9$.\\n\\s]+", " ");
        }
        recognizedText = recognizedText.trim();
        addItems(recognizedText);

        // Crashes app for some reason with progess dialog
        // Toast.makeText(MainActivity.this, recognizedText, Toast.LENGTH_LONG).show();
    }

    public void addItems(String receipt) {
        String[] list_of_items = receipt.split("\\n");
        // Toast.makeText(MainActivity.this, Integer.toString(items.length), Toast.LENGTH_SHORT).show();
//        mItems.add(new Item(items[0], 10));
        // mItems.add(new Item("TEST", items.length));
        // mItems.add(new Item("NEW ITEM", 10.99));

        for (String item : list_of_items) {
            String item_name = "";
            double item_price = 0;
            String[] item_split = item.split(" ");
            if (item_split[0].matches("-?\\d+(\\.\\d+)?")) {
                Log.v(TAG, item_split[0]);
                for (int i = 1; i < item_split.length; i++) {
                    if (item_split[i].matches("[a-zA-Z]+")) {
                        item_name += item_split[i] + " ";
                        Log.v(TAG, item_name);
                    }
                    if (item_split[i].contains(".")) {
                        try {
                            item_price = Double.parseDouble(item_split[i]);
                        }
                        catch (Exception e) {
                            Log.v(TAG, "NUMBER FORMAT EXCEPTION");
                        }
                        Log.v(TAG, item_split[i]);
                    }
                }
                // mItems.add(new Item(item_name, item_price));
                mItemsFromImage.add(new Item(item_name, item_price));
                // mItemsFromImage.add(new Item("FETTY", 10));
            }
        }

        Log.v(TAG, Integer.toString(list_of_items.length));
        Log.v(TAG,receipt);

//        ArrayList<Item> test = new ArrayList<Item>();
//        test.add(new Item("FETTY", 10));
    }

    public void createTessData() {
        // Create tess data folder and eng file on phone
        String[] paths = new String[] {DATA_PATH, DATA_PATH + "tessdata/"};
        for (String path : paths) {
            File dir = new File(path);
            // Toast.makeText(SimpleAndroidOCRActivity.this, dir.toString(), Toast.LENGTH_SHORT).show();
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    // Log.v(TAG, "ERROR: Creation of directory " + path + " on sdcard failed");
                    Toast.makeText(this, " ERROR", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    // Log.v(TAG, "Created directory " + path + " on sdcard");
                    Toast.makeText(this, "CREATE", Toast.LENGTH_SHORT).show();
                }
            }

        }
        Toast.makeText(this, "AFTER CREATE DIR", Toast.LENGTH_SHORT).show();

        if (!(new File(DATA_PATH + "tessdata/" + lang + ".traineddata")).exists()) {
            try {
                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
                //GZIPInputStream gin = new GZIPInputStream(in);
                OutputStream out = new FileOutputStream(DATA_PATH
                        + "tessdata/" + lang + ".traineddata");

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
                out.close();

                Log.v(TAG, "Copied " + lang + " traineddata");
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + lang + " traineddata " + e.toString());
            }
        }
    }
}
