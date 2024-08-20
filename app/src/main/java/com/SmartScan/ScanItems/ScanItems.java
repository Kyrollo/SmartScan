package com.SmartScan.ScanItems;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
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
import com.SmartScan.ApiClasses.ItemResponse;
import com.SmartScan.DataBase.AppDataBase;
import com.SmartScan.R;
import com.SmartScan.Tables.Item;
import com.zebra.rfid.api3.TagData;

import java.util.HashSet;
import java.util.List;
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
    private List<Item> itemList;
    public TextView statusTextViewRFID = null;
    private Set<String> uniqueTagIDs = new HashSet<>();
    private Set<String> unregisteredTags = new HashSet<>();
    private RFIDHandlerItems rfidHandler;
    private RadioGroup radioGroupTags;
    private TextView unregisteredTagsTextView;
    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 100;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_items);

        btnDownload = findViewById(R.id.btnDownload);
        btnDeleteAll = findViewById(R.id.btnDeleteAll);
        recyclerView = findViewById(R.id.recyclerViewItems);
        statusTextViewRFID = (TextView) findViewById(R.id.textViewStatusrfid);
        rfidHandler = new RFIDHandlerItems();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.77:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(APIService.class);

        initializePage();

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

        radioGroupTags.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioRegisteredTags) {
                recyclerView.setVisibility(View.VISIBLE);
                unregisteredTagsTextView.setVisibility(View.GONE);
                updateRecyclerView();
            } else if (checkedId == R.id.radioUnregisteredTags) {
                recyclerView.setVisibility(View.GONE);
                unregisteredTagsTextView.setVisibility(View.VISIBLE);
                displayUnregisteredTags();
            }
        });
    }

    private void initializePage() {
        db = AppDataBase.getDatabase(this);

        recyclerView = findViewById(R.id.recyclerViewItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemList = db.itemDao().getAllItems();
        itemAdapter = new ItemAdapter(itemList);
        recyclerView.setAdapter(itemAdapter);
        radioGroupTags = findViewById(R.id.radioGroupTags);
        unregisteredTagsTextView = findViewById(R.id.unregisteredTagsTextView);

        btnDeleteAll.setOnClickListener(view -> showDeleteDialog());
        btnDownload.setOnClickListener(view -> testConnection(this::fetchItems));
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
    }

    private void changeItemStatus(String tagId) {
        Item item = db.itemDao().getItemsByOPT3(tagId);

        if (item == null) {
            unregisteredTags.add(tagId);
            displayUnregisteredTags();
        }
        else if (item != null && item.getStatus() != "Found") {
            db.itemDao().updateItemStatusToFound(tagId);
        }
    }

    private void displayUnregisteredTags() {
        StringBuilder tagsText = new StringBuilder();
        for (String tag : unregisteredTags) {
            tagsText.append(tag).append("\n");
        }
        if (tagsText.length() == 0) {
            unregisteredTagsTextView.setText("No Unregistered Tags Scanned");
            return;
        }
        unregisteredTagsTextView.setText(tagsText.toString());
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

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        String result = rfidHandler.onResume();
        statusTextViewRFID.setText(result);
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