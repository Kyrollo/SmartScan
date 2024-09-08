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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.SmartScan.Adapters.ItemAdapter;
import com.SmartScan.App;

import com.SmartScan.R;
import com.SmartScan.Tables.Category;
import com.SmartScan.Tables.Inventory;
import com.SmartScan.Tables.Item;
import com.SmartScan.Tables.Location;
import com.google.android.material.tabs.TabLayout;
import com.zebra.rfid.api3.TagData;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ScanItems extends AppCompatActivity implements RFIDHandlerItems.RFIDHandlerListener {
    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private List<Inventory> inventoryList, registredInventoryList, missingInventoryList, allTagsList, unregistredInventoryList, AllInventoryList;
    private List<Item> registeredItemsList, unregisteredItemsList, missingItemsList, allItemsList;
    private Set<String> uniqueTagIDs = new HashSet<>();
    private Set<String> unregisteredTags = new HashSet<>();
    private RFIDHandlerItems rfidHandler;
    private Inventory randomInventory;
    TextView allCountData, registeredCountData, unregisteredCountData, missingCountData;
    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 100;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String locationID, startDateStr;
    private int userId, inventoryId;
    private TabLayout tabLayout;
    private Button btnEnd;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_items);

        recyclerView = findViewById(R.id.recyclerViewItems);
        rfidHandler = new RFIDHandlerItems();

        retrieveData();

        initializePage();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        allTagsList = new ArrayList<>();
                        if (allTagsList != null) {
                            allTagsList.clear();
                            allTagsList.addAll(registredInventoryList);
                            allTagsList.addAll(unregistredInventoryList);
                        }
                        allTagsList.addAll(registredInventoryList);
                        allTagsList.addAll(unregistredInventoryList);
                        updateRecyclerView(allTagsList, allItemsList);
                        break;
                    case 1:
                        updateRecyclerView(registredInventoryList, registeredItemsList);
                        break;
                    case 2:
                        updateRecyclerView(missingInventoryList, missingItemsList);
                        break;
                    case 3:
                        updateRecyclerView(unregistredInventoryList, unregisteredItemsList);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Do nothing
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Do nothing
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

        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showValidationDialog();
            }
        });

    }

    private void retrieveData() {
        Intent intent = getIntent();
        userId = intent.getIntExtra("USERID", -1);
        inventoryId = intent.getIntExtra("INVENTORYID", -1);
        startDateStr = intent.getStringExtra("INVENTORYDATE");
        locationID = intent.getStringExtra("locationId");
    }

    private void initializePage() {
        inventoryList = App.get().getDB().inventoryDao().getAllInventoriesByLocationId(locationID);
        registredInventoryList = App.get().getDB().inventoryDao().getInventoriesByMissingStatus(locationID, false);
        missingInventoryList = App.get().getDB().inventoryDao().getInventoriesByMissingStatus(locationID, true);
        AllInventoryList = App.get().getDB().inventoryDao().getAllInventories();
        unregistredInventoryList = new ArrayList<>();

        if (inventoryList == null) {
            inventoryList = new ArrayList<>();
        }
        if (registredInventoryList == null) {
            registredInventoryList = new ArrayList<>();
        }
        if (missingInventoryList == null) {
            missingInventoryList = new ArrayList<>();
        }

        if (inventoryList.size() == 0) {
            List<Item> itemList = App.get().getDB().itemDao().getAllItemsByParentID(locationID);
            for (Item item : itemList) {
                Category categoryDesc = App.get().getDB().categoryDao().getCategoryByID(item.getCategoryID());
                Location loc = App.get().getDB().locationDao().getAllLocationByLocationID(item.getLocationID());
                Inventory inventory = new Inventory(inventoryId, startDateStr, userId, item.getItemID(), item.getOpt3(),
                        item.getRemark(), item.getCategoryID(), categoryDesc.getCategoryDesc(), item.getStatusID(), locationID,
                        loc.getLocationDesc(), loc.getFullLocationDesc());
                App.get().getDB().inventoryDao().insert(inventory);
            }
            inventoryList = App.get().getDB().inventoryDao().getAllInventoriesByLocationId(locationID);
            registredInventoryList = App.get().getDB().inventoryDao().getInventoriesByMissingStatus(locationID, false);
            missingInventoryList = App.get().getDB().inventoryDao().getInventoriesByMissingStatus(locationID, true);
        }

        allItemsList = new ArrayList<>();
//        for (Inventory inventory : inventoryList) {
//            Item item = App.get().getDB().itemDao().getItemsByItemID(inventory.getItemID());
//            if (item != null) {
//                allItemsList.add(item);
//            }
//        }

        recyclerView = findViewById(R.id.recyclerViewItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemAdapter = new ItemAdapter(allItemsList);
        recyclerView.setAdapter(itemAdapter);

        tabLayout = findViewById(R.id.tabLayout);

        allCountData = findViewById(R.id.allCountData);
        registeredCountData = findViewById(R.id.registeredCountData);
        missingCountData = findViewById(R.id.missingCountData);
        unregisteredCountData = findViewById(R.id.unregisteredCountData);
        btnEnd = findViewById(R.id.btnEnd);

        recyclerView.setVisibility(View.VISIBLE);

        // Open the missing tab by default
        openTab(2);
        updateRecyclerView(missingInventoryList, missingItemsList);

        updateCountTextViews();
    }

    private void openTab(int tabPosition) {
        TabLayout.Tab tab = tabLayout.getTabAt(tabPosition);
        if (tab != null) {
            tab.select();
        }
    }

    private void updateRecyclerView(List<Inventory> invlist, List<Item> itemList) {
        if (invlist == null) {
            invlist = new ArrayList<>();
        }
        if (itemList == null) {
            itemList = new ArrayList<>();
        }

        List<Inventory> invlistCopy = new ArrayList<>(invlist);

        for (Inventory inventory : invlistCopy) {
            if (inventory != null) {
                Item item = App.get().getDB().itemDao().getItemsByItemID(inventory.getItemID());
                if (item != null) {
                    itemList.add(item);
                }
            }
        }

        itemAdapter.updateData(itemList);
        updateCountTextViews();
    }

    private void updateCountTextViews() {
        int registeredCount = registredInventoryList.size();
        int missingCount = missingInventoryList.size();
        int unregisteredCount = unregistredInventoryList.size();
        int allTagsCount = registeredCount + unregisteredCount;

        allCountData.setText(String.valueOf(allTagsCount));
        registeredCountData.setText(String.valueOf(registeredCount));
        missingCountData.setText(String.valueOf(missingCount));
        unregisteredCountData.setText(String.valueOf(unregisteredCount));
    }

    private void changeItemInventoryStatus(String tagId) {
        if (!isItemInInventory(tagId)) {
            addUnregisteredItem(tagId);
            // Open the unregistered tab
            runOnUiThread(() -> openTab(3));
            return;
        }

        Inventory inventory = App.get().getDB().inventoryDao().getInventoryByOPT3(tagId);
            if (inventory != null && inventory.isMissing()) {
            App.get().getDB().inventoryDao().updateItemStatusToFound(inventory.getItemID());

            missingInventoryList = App.get().getDB().inventoryDao().getInventoriesByMissingStatus(locationID, true);
            registredInventoryList.add(inventory);
        }
        // Open the registered tab
        runOnUiThread(() -> openTab(1));
    }

    private boolean isItemInInventory(String tagId) {
        return App.get().getDB().inventoryDao().getInventoryByOPT3AndLocationID(tagId, locationID) != null;
    }

    private void addUnregisteredItem(String tagId) {
        unregisteredTags.add(tagId);
        Inventory unregisteredInventory = App.get().getDB().inventoryDao().getInventoryByOPT3(tagId);
        if (unregisteredInventory == null) {
            getItemAndAddToInventory(tagId);
            Inventory newInsertedInventory = App.get().getDB().inventoryDao().getInventoryByOPT3(tagId);

            if (newInsertedInventory != null){
                unregistredInventoryList.add(newInsertedInventory);
            } else {
                runOnUiThread(() -> Toast.makeText(this, getString(R.string.invalid_rfid), Toast.LENGTH_SHORT).show());
//                Toast.makeText(this, getString(R.string.invalid_rfid), Toast.LENGTH_SHORT).show();
            }
        } else {
            unregistredInventoryList.add(unregisteredInventory);
        }
    }

    private void getItemAndAddToInventory(String tagId) {
        Item item = App.get().getDB().itemDao().getItemsByOPT3(tagId);
        if (item != null) {
            Category categoryDesc = App.get().getDB().categoryDao().getCategoryByID(item.getCategoryID());
            Location NewLoc = App.get().getDB().locationDao().getAllLocationByLocationID(locationID);
            Location OldLoc = App.get().getDB().locationDao().getAllLocationByLocationID(item.getLocationID());

            Inventory inventory = new Inventory(inventoryId, startDateStr, userId, item.getItemID(), tagId, item.getRemark(),
                    item.getCategoryID(), categoryDesc.getCategoryDesc(), item.getStatusID(), locationID, NewLoc.getLocationDesc(),
                    NewLoc.getFullLocationDesc(), item.getLocationID(), OldLoc.getLocationDesc(), OldLoc.getFullLocationDesc());

            App.get().getDB().inventoryDao().insert(inventory);
        }
    }

    private void showValidationDialog() {
        if (inventoryList == null || inventoryList.isEmpty()) {
            finish();
            return;
        }

        randomInventory = inventoryList.get(new Random().nextInt(inventoryList.size()));

        Item item = App.get().getDB().itemDao().getItemsByItemID(randomInventory.getItemID());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Validation Required");
        builder.setMessage("Please take a photo of the following item:\n\n" +
                "Item Barcode:       " + item.getItemBarCode() + "\n\n" +
                "Item Description:  " + item.getItemDesc() + "\n\n" +
                "Item OPT3:           " + item.getOpt3());

        builder.setPositiveButton("Take Photo", (dialog, which) -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_IMAGE_CAPTURE);
            }
        });

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
                        App.get().getDB().itemDao().updateItemImage(imageData, randomInventory.getItemBarcode());
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
            } else {
                Toast.makeText(this, "Bluetooth Permissions not granted", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
                    changeItemInventoryStatus(tagId);
                }
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int selectedTabPosition = tabLayout.getSelectedTabPosition();
                switch (selectedTabPosition) {
                    case 0:
                        allTagsList = new ArrayList<>();
                        if (allTagsList != null) {
                            allTagsList.clear();
                            allTagsList.addAll(registredInventoryList);
                            allTagsList.addAll(unregistredInventoryList);
                        }
                        allTagsList.addAll(registredInventoryList);
                        allTagsList.addAll(unregistredInventoryList);
                        break;
                    case 1:
                        updateRecyclerView(registredInventoryList, registeredItemsList);
                        break;
                    case 2:
                        updateRecyclerView(missingInventoryList, missingItemsList);
                        break;
                    case 3:
                        updateRecyclerView(unregistredInventoryList, unregisteredItemsList);
                        break;
                }
            }
        });
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        if (pressed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int selectedTabPosition = tabLayout.getSelectedTabPosition();
                    switch (selectedTabPosition) {
                        case 0:
                            allTagsList = new ArrayList<>();
                            if (allTagsList != null) {
                                allTagsList.clear();
                                allTagsList.addAll(registredInventoryList);
                                allTagsList.addAll(unregistredInventoryList);
                            }
                            allTagsList.addAll(registredInventoryList);
                            allTagsList.addAll(unregistredInventoryList);
                            break;
                        case 1:
                            updateRecyclerView(registredInventoryList, registeredItemsList);
                            break;
                        case 2:
                            updateRecyclerView(missingInventoryList, missingItemsList);
                            break;
                        case 3:
                            updateRecyclerView(unregistredInventoryList, unregisteredItemsList);
                            break;
                    }
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