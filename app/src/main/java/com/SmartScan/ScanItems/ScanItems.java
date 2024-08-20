package com.SmartScan.ScanItems;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SmartScan.API.APIService;
import com.SmartScan.Adapters.ItemAdapter;
import com.SmartScan.Adapters.UnregisteredTagsAdapter;
import com.SmartScan.ApiClasses.ItemResponse;
import com.SmartScan.DataBase.AppDataBase;
import com.SmartScan.R;
import com.SmartScan.Tables.Item;
import com.zebra.rfid.api3.TagData;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ScanItems extends AppCompatActivity implements RFIDHandlerItems.RFIDHandlerListener {
    private AppDataBase db;
    private APIService apiService;
    private Button btnDownload, btnDeleteAll;
    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private RecyclerView unregisteredTagsRecyclerView;
    private UnregisteredTagsAdapter unregisteredTagsAdapter;
    private List<Item> itemList;
    private Set<String> uniqueTagIDs = new HashSet<>();
    private Set<String> unregisteredTags = new HashSet<>();
    private RFIDHandlerItems rfidHandler;
    private RadioGroup radioGroupTags;
    private Item randomItem;
    TextView uniqueCountData, registeredCountData, unregisteredCountData;
    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 100;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    private static final int REQUEST_IMAGE_CAPTURE = 1;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_items);

        btnDownload = findViewById(R.id.btnDownload);
        btnDeleteAll = findViewById(R.id.btnDeleteAll);
        recyclerView = findViewById(R.id.recyclerViewItems);
        rfidHandler = new RFIDHandlerItems();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.77:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(APIService.class);

        initializePage();

        radioGroupTags.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioRegisteredTags) {
                updateRecyclerView();
                recyclerView.setVisibility(View.VISIBLE);
                unregisteredTagsRecyclerView.setVisibility(View.GONE);
            } else if (checkedId == R.id.radioUnregisteredTags) {
                updateUnregisteredTagsRecyclerView();
                recyclerView.setVisibility(View.GONE);
                unregisteredTagsRecyclerView.setVisibility(View.VISIBLE);
            }
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, BLUETOOTH_PERMISSION_REQUEST_CODE);
            }else{
                rfidHandler.onCreate(this);
            }

        }else{
            rfidHandler.onCreate(this);
        }

    }

    private void initializePage() {
        db = AppDataBase.getDatabase(this);

        itemList = db.itemDao().getAllItems();

        radioGroupTags = findViewById(R.id.radioGroupTags);

        recyclerView = findViewById(R.id.recyclerViewItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemAdapter = new ItemAdapter(itemList);
        recyclerView.setAdapter(itemAdapter);

        unregisteredTagsRecyclerView = findViewById(R.id.recyclerViewUnregisteredTags);
        unregisteredTagsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        unregisteredTagsAdapter = new UnregisteredTagsAdapter(unregisteredTags);
        unregisteredTagsRecyclerView.setAdapter(unregisteredTagsAdapter);

        uniqueCountData = findViewById(R.id.uniqueCountData);
        registeredCountData = findViewById(R.id.registeredCountData);
        unregisteredCountData = findViewById(R.id.unregisteredCountData);

        btnDeleteAll.setOnClickListener(view -> showDeleteDialog());
        btnDownload.setOnClickListener(view -> testConnection(this::fetchItems));

        recyclerView.setVisibility(View.VISIBLE);
        unregisteredTagsRecyclerView.setVisibility(View.GONE);
        updateCountTextViews();
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.deleteAllDialog))
                .setMessage(getString(R.string.confirmDeleteDialog))
                .setPositiveButton(getString(R.string.yesDeleteDialog), (dialog, which) -> {
                    deleteAllData();
                })
                .setNegativeButton(getString(R.string.noDeleteDialog), (dialog, which) -> {
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    private void deleteAllData() {
        db.itemDao().deleteAll();
        db.itemDao().resetPrimaryKey();
        updateRecyclerView();
        unregisteredTags.clear();
        updateUnregisteredTagsRecyclerView();
        Toast.makeText(this, "All Data Deleted", Toast.LENGTH_SHORT).show();
    }

    private void testConnection(Runnable onSuccess) {
        apiService.testConnection().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && "Success".equals(response.body())) {
                    onSuccess.run();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to connect! Check your internet.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Failed to connect! Check your internet.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchItems() {
        apiService.getItems().enqueue(new Callback<List<ItemResponse>>() {
            @Override
            public void onResponse(Call<List<ItemResponse>> call, Response<List<ItemResponse>> response) {
                List<ItemResponse> items = response.body();
                if (response.isSuccessful()) {
                    insertItems(items);
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to get items", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<ItemResponse>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Failed to get items", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void insertItems(List<ItemResponse> items) {
        for (ItemResponse itemResponse : items) {
            if (itemResponse.getOPT3() != null) {
                Item item = new Item();
                item.setItemBarCode(itemResponse.getItemBarCode());
                item.setItemDesc(itemResponse.getItemDesc());
                item.setRemark(itemResponse.getRemark());
                item.setOpt3(itemResponse.getOPT3());
                item.setStatus("Missing");
                db.itemDao().insert(item);
            }
        }
        updateRecyclerView();
    }

    private void updateRecyclerView() {
        itemList = db.itemDao().getAllItems();
        itemAdapter.updateData(itemList);
        updateCountTextViews();
    }

    private void updateUnregisteredTagsRecyclerView() {
        runOnUiThread(() -> {
            unregisteredTagsAdapter = new UnregisteredTagsAdapter(unregisteredTags);
            unregisteredTagsRecyclerView.setAdapter(unregisteredTagsAdapter);
            updateCountTextViews();
        });
    }

    private void updateCountTextViews() {
        int registeredCount = itemList.size();
        int unregisteredCount = unregisteredTags.size();
        int uniqueCount = registeredCount + unregisteredCount;

        uniqueCountData.setText(String.valueOf(uniqueCount));
        registeredCountData.setText(String.valueOf(registeredCount));
        unregisteredCountData.setText(String.valueOf(unregisteredCount));
    }

    private void changeItemStatus(String tagId) {
        Item item = db.itemDao().getItemsByOPT3(tagId);

        if (item == null) {
            unregisteredTags.add(tagId);
            updateUnregisteredTagsRecyclerView();
        } else if (item != null && item.getStatus() != "Found") {
            db.itemDao().updateItemStatusToFound(tagId);
        }
    }

    private void showValidationDialog() {
        if (itemList == null || itemList.isEmpty()) {
            finish();
            return;
        }

        randomItem = itemList.get(new Random().nextInt(itemList.size()));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Validation Required");
        builder.setMessage("Please take a photo of the following item:\n" +
                "Item Barcode:  " + randomItem.getItemBarCode() + "\n" +
                "Item Description:  " + randomItem.getItemDesc() + "\n" +
                "Item Status:   " + randomItem.getStatus());

        builder.setPositiveButton("Take Photo", (dialog, which) -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_IMAGE_CAPTURE);

            }
        });

//        builder.create().show();
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    Bitmap bitmap = data.getParcelableExtra("data");
                    if (bitmap != null) {
                        byte[] imageData = bitmapToByteArray(bitmap);
                        db.itemDao().updateItemImage(imageData, randomItem.getItemBarCode());
                        finish();
                    }
                } else {
                    showValidationDialog();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Photo not taken", Toast.LENGTH_SHORT).show();
                showValidationDialog();
            }
        }
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        showValidationDialog();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                rfidHandler.onCreate(this);
            }
            else {
                Toast.makeText(this, "Bluetooth Permissions not granted", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                openCamera();
                startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_IMAGE_CAPTURE);
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onPause() {super.onPause();}

    @Override
    protected void onPostResume() {
        super.onPostResume();
        rfidHandler.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rfidHandler.onDestroy();
    }

    @Override
    public void handleTagdata(TagData[][] tagDataArray) {
        String tagId;

        for (TagData[] tagData : tagDataArray) {
            for (int index = 0; index < tagData.length; index++) {
                tagId = tagData[index].getTagID();

                if (uniqueTagIDs.add(tagId)) {
                    changeItemStatus(tagId);
                }
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateRecyclerView();
            }
        });
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        if (pressed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateRecyclerView();
                }
            });
            rfidHandler.performInventory();
        } else {
            rfidHandler.stopInventory();
        }
    }

    @Override
    public void sendToast(String val) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ScanItems.this, val, Toast.LENGTH_SHORT).show();
            }
        });
    }
}