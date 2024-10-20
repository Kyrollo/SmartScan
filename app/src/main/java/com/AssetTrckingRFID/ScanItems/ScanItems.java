package com.AssetTrckingRFID.ScanItems;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.AssetTrckingRFID.Adapters.ItemAdapter;
import com.AssetTrckingRFID.App;

import com.AssetTrckingRFID.Bluetooth.BluetoothHandler;
import com.AssetTrckingRFID.R;
import com.AssetTrckingRFID.Tables.Category;
import com.AssetTrckingRFID.Tables.Inventory;
import com.AssetTrckingRFID.Tables.Item;
import com.AssetTrckingRFID.Tables.Location;
import com.google.android.material.tabs.TabLayout;
import com.zebra.rfid.api3.TagData;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ScanItems extends AppCompatActivity implements BluetoothHandler.RFIDHandlerBluetoothListener {
    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private List<Inventory>  allTagsList,unregistredInventoryList;//,inventoryList, , missingInventoryList, unregistredInventoryList, AllInventoryList;
    private List<Item> registeredItemsList, unregisteredItemsList, missingItemsList, allItemsList;
    private Set<String> uniqueTagIDs = new HashSet<>();
    private Set<String> unregisteredTags = new HashSet<>();
//    private RFIDHandlerItems rfidHandler;
    private BluetoothHandler rfidHandler;
    private Inventory randomInventory;
    TextView allCountData, registeredCountData, unregisteredCountData, missingCountData;
    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 100;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_WRITE_STORAGE = 300;
    public String locationID, startDateStr;
    public int userId, inventoryId;
    private TabLayout tabLayout;
    private Button btnEnd;
    private FrameLayout progressBarLayout;
    private BluetoothAdapter bluetoothAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_items);

//        rfidHandler = new RFIDHandlerItems();
        rfidHandler = new BluetoothHandler();

        retrieveData();
        initializePage();
        buildRecyclerView();
        addMissing();
        initializeRfid();
        initializeTabs();
        buttonEnd();

//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
//        }
    }

    private void addMissing() {

        List<Inventory> checkIfLocationInsertedBefore =
                App.get().getDB().inventoryDao().getAllInLocation(locationID);

        allTagsList = new ArrayList<>();
        unregistredInventoryList = new ArrayList<>();
//        if (inventoryList == null) {
//            inventoryList = new ArrayList<>();
//        }
//        if (registredInventoryList == null) {
//            registredInventoryList = new ArrayList<>();
//        }
//        if (missingInventoryList == null) {
//            missingInventoryList = new ArrayList<>();
//        }
//
        try
        {
        if (checkIfLocationInsertedBefore.size() == 0) {
            List<Item> itemList = App.get().getDB().itemDao().getAllItemsByParentID(locationID);
            for (Item item : itemList) {
                Category categoryDesc = App.get().getDB().categoryDao().getCategoryByID(item.getCategoryID());
                Location loc = App.get().getDB().locationDao().getAllLocationByLocationID(item.getLocationID());
                Inventory inventory = new Inventory(inventoryId, startDateStr, userId, item.getItemID(), item.getItemBarCode(), item.getOpt3(),
                        item.getRemark(), item.getCategoryID(), categoryDesc.getCategoryDesc(), item.getStatusID(), locationID,
                        loc.getLocationDesc(), loc.getFullLocationDesc());
                App.get().getDB().inventoryDao().insert(inventory);
            }
        }
//
//            inventoryList = App.get().getDB().inventoryDao().getAllInventoriesByLocationId(locationID);
//            registredInventoryList = App.get().getDB().inventoryDao().getRegistered(locationID);
//            missingInventoryList = App.get().getDB().inventoryDao().getInventoriesByMissingStatus(locationID, true);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
//
//        allItemsList = new ArrayList<>();

        // Open the missing tab by default
        openTab(2);
        missingTrx();

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothStateReceiver, filter);
    }

    private void buildRecyclerView() {
        itemAdapter = new ItemAdapter(allItemsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(itemAdapter);
    }

    public void showProgressBar() {
        progressBarLayout.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        progressBarLayout.setVisibility(View.GONE);
    }

    private void buttonEnd() {
        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showValidationDialog();
                rfidHandler.onDestroy();
                finish();
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

    private void initializeRfid() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, getString(R.string.bluetooth_disabled), Toast.LENGTH_SHORT).show();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, BLUETOOTH_PERMISSION_REQUEST_CODE);
            } else {
//                rfidHandler.onCreate(this);
    //            rfidHandler.onDestroy();
                rfidHandler.onCreate(this);
            }
        } else {
//          rfidHandler.onCreate(this);
//            rfidHandler.onDestroy();
            rfidHandler.onCreate(this);
        }
    }

    private void initializePage() {

        recyclerView = findViewById(R.id.recyclerViewItems);
        tabLayout = findViewById(R.id.tabLayout);
        allCountData = findViewById(R.id.allCountData);
        registeredCountData = findViewById(R.id.registeredCountData);
        missingCountData = findViewById(R.id.missingCountData);
        unregisteredCountData = findViewById(R.id.unregisteredCountData);
        btnEnd = findViewById(R.id.btnEnd);
        progressBarLayout = findViewById(R.id.progressBarLayout);

        recyclerView.setVisibility(View.VISIBLE);
    }

    private void initializeTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        allTrx();
                        break;
                    case 1:
                        registeredTrx();
                        break;
                    case 2:
                        missingTrx();
                        break;
                    case 3:
                        unRegisteredTrx();
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
       // updateCountTextViews();
    }

    private void updateCountTextViews() {
        allCountData.setText(Integer.toString(App.get().getDB().inventoryDao().getAll(locationID).size()));
        registeredCountData.setText(Integer.toString(App.get().getDB().inventoryDao().getRegistered(locationID).size()));
        missingCountData.setText(Integer.toString(App.get().getDB().inventoryDao().getInventoriesByMissingStatus(locationID, true).size()));
        unregisteredCountData.setText(Integer.toString(App.get().getDB().inventoryDao().getUnRegistered(locationID).size()));
    }

    private void changeItemInventoryStatus(String tagId) {
        if (!isItemInInventory(tagId)) {
            addUnregisteredItem(tagId);

            // Open the unregistered tab
            runOnUiThread(() -> openTab(3));
            //return;
        }
        else {

            Inventory inventory = App.get().getDB().inventoryDao().getInventoryByOPT3(tagId);
            if (inventory != null && inventory.isMissing()) {
                App.get().getDB().inventoryDao().updateItemStatusToFound(inventory.getItemBarcode());

//                missingInventoryList = App.get().getDB().inventoryDao().getInventoriesByMissingStatus(locationID, true);
//                registredInventoryList.add(inventory);
            }
            // Open the registered tab
            runOnUiThread(() -> openTab(1));
        }
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
            }
        } else {
            unregistredInventoryList.add(unregisteredInventory);
           // unRegisteredTrx();
        }
    }

    private void getItemAndAddToInventory(String tagId) {
        Item item = App.get().getDB().itemDao().getItemsByOPT3(tagId);
        if (item != null) {
            Category categoryDesc = App.get().getDB().categoryDao().getCategoryByID(item.getCategoryID());
            Location NewLoc = App.get().getDB().locationDao().getAllLocationByLocationID(locationID);
            Location OldLoc = App.get().getDB().locationDao().getAllLocationByLocationID(item.getLocationID());

            Inventory inventory = new Inventory(inventoryId, startDateStr, userId, item.getItemID(), item.getItemBarCode(),tagId, item.getRemark(),
                    item.getCategoryID(), categoryDesc.getCategoryDesc(), item.getStatusID(), locationID, NewLoc.getLocationDesc(),
                    NewLoc.getFullLocationDesc(), item.getLocationID(), OldLoc.getLocationDesc(), OldLoc.getFullLocationDesc());

            App.get().getDB().inventoryDao().insert(inventory);
        }
    }

    private void showValidationDialog() {
//        if (registredInventoryList == null || registredInventoryList.isEmpty()) {
//            finish();
//            return;
//        }
//
//        randomInventory = registredInventoryList.get(new Random().nextInt(registredInventoryList.size()));

        Item item = App.get().getDB().itemDao().getItemsByItemID(randomInventory.getItemID());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString((R.string.validation_required)));
        builder.setMessage(getString(R.string.please_take_a_photo_of_the_following_item) +
                getString(R.string.item_barcode) + item.getItemBarCode() + "\n\n" +
                getString(R.string.item_description) + item.getItemDesc() + "\n\n" +
                getString(R.string.item_opt3) + item.getOpt3());

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

    public void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Uri uri = Uri.parse("package:com.SmartScan");
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
                    startActivity(intent);
                 //   Toast.makeText(this, getString(R.string.storage_permission_accepted), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, getString(R.string.storage_permission_denied), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void saveBitmapToFolder(Bitmap bitmap) {
        File directory = new File(Environment.getExternalStorageDirectory(), "SmartScanImages");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = "IMG_" + System.currentTimeMillis() + ".png";
        File file = new File(directory, fileName);

        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            Toast.makeText(this, "Image Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
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
                        App.get().getDB().itemDao().SetItemImage(imageData, randomInventory.getItemID());   // Save the image to the item in format array of bytes
                        saveBitmapToFolder(bitmap);          // Save bitmap to folder in the mobile
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
      //  showValidationDialog();
        rfidHandler.onDestroy();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                rfidHandler.onCreate(this);
                rfidHandler.onCreate(this);
            } else {
                Toast.makeText(this, R.string.bluetooth_permissions_not_granted, Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_IMAGE_CAPTURE);
            } else {
                Toast.makeText(this, getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                requestPermission();
            } else {
                requestPermission();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();}

    @Override
    protected void onPostResume() {
        super.onPostResume();
        rfidHandler.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothStateReceiver);
//        rfidHandler.onDestroy();
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
                        allTrx();
                        break;
                    case 1:
                        registeredTrx();
                        break;
                    case 2:
                        missingTrx();
                        break;
                    case 3:
                        unRegisteredTrx();
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
                          allTrx();
                            break;
                        case 1:
                            registeredTrx();
                            break;
                        case 2:
                            missingTrx();
                            break;
                        case 3:
                           unRegisteredTrx();
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

    private BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        // When Bluetooth turned off
                        rfidHandler.onDestroy();
                        Toast.makeText(context, getString(R.string.bluetooth_turned_off), Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        // When Bluetooth turned on
//                        rfidHandler.onCreate(this);
                        rfidHandler.onCreate(ScanItems.this);
                        break;
                }
            }
        }
    };
    private void registeredTrx() {
        allTagsList.clear();
        allTagsList.addAll(App.get().getDB().inventoryDao().getRegistered(locationID));
       updateRecyclerView(allTagsList, allItemsList);
        updateCountTextViews();
        // registeredCountData.setText(Integer.toString(App.get().getDB().inventoryDao().getRegistered(locationID).size()));
//        if(Integer.parseInt(registeredCountData.getText().toString()) == 0)
//        {
//            unRegisteredTrx();
//        }

    }

    private void unRegisteredTrx() {
        allTagsList.clear();
        allTagsList.addAll(App.get().getDB().inventoryDao().getUnRegistered(locationID));
        updateRecyclerView(allTagsList, unregisteredItemsList);
        updateCountTextViews();
        //  unregisteredCountData.setText(Integer.toString(App.get().getDB().inventoryDao().getUnRegistered(locationID).size()));

    }
    private void missingTrx() {
        allTagsList.clear();
        allTagsList.addAll(App.get().getDB().inventoryDao().getInventoriesByMissingStatus(locationID, true));
        updateRecyclerView(allTagsList, missingItemsList);
        updateCountTextViews();
        // missingCountData.setText(Integer.toString(App.get().getDB().inventoryDao().getInventoriesByMissingStatus(locationID, true).size()));
//        if(Integer.parseInt(missingCountData.getText().toString()) == 0)
//        {
//            registeredTrx();
//        }

    }
    private void allTrx() {
        allTagsList.clear();
        allTagsList.addAll(App.get().getDB().inventoryDao().getAll(locationID));
        updateRecyclerView(allTagsList, registeredItemsList);
        updateCountTextViews();
      //  allCountData.setText(Integer.toString(App.get().getDB().inventoryDao().getAll(locationID).size()));

    }
}