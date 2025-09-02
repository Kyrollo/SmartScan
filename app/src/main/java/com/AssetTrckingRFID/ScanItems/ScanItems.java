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
import android.os.AsyncTask;
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

import com.AssetTrckingRFID.Adapters.InventoryAdapter;
import com.AssetTrckingRFID.App;
import com.AssetTrckingRFID.Bluetooth.BluetoothHandler;
import com.AssetTrckingRFID.R;
import com.AssetTrckingRFID.Tables.Category;
import com.AssetTrckingRFID.Tables.Inventory;
import com.AssetTrckingRFID.Tables.Item;
import com.AssetTrckingRFID.Tables.Location;
import com.AssetTrckingRFID.Utilities.LoadingDialog;
import com.google.android.material.tabs.TabLayout;
import com.zebra.rfid.api3.TagData;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScanItems extends AppCompatActivity implements BluetoothHandler.RFIDHandlerBluetoothListener {
    // UI
    private RecyclerView recyclerView;
    private InventoryAdapter inventoryAdapter;
    private TextView allCountData, registeredCountData, unregisteredCountData, missingCountData;
    private TabLayout tabLayout;
    private Button btnEnd;
    private FrameLayout progressBarLayout;

    // RFID
    private BluetoothHandler rfidHandler;
    private BluetoothAdapter bluetoothAdapter;

    // Navigation data
    public String locationID, startDateStr;
    public int userId, inventoryId;

    // In-memory fast structures
    private final Set<String> registeredTagIds = new HashSet<>();
    private final Set<String> unregisteredTagIds = new HashSet<>();
    private final Set<String> missingTagIds = new HashSet<>();
    private final Set<String> otherLocationTagIds = new HashSet<>();
    private final Map<String, Inventory> tagIdToInventory = new HashMap<>();

    // Scan duplicated Tags ID
    private final Set<String> uniqueTagIDs = new HashSet<>();

    // Pending DB changes (Updated on End)
    private final Set<String> pendingFoundBarcodes = new HashSet<>();
    private final Map<String, String> pendingRelocationOldLocationByBarcode = new HashMap<>();

    // Adapter initial list holder
    private final List<Inventory> emptyListForAdapter = new ArrayList<>();

    private Inventory randomInventory;

    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 100;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_WRITE_STORAGE = 300;

    private LoadingDialog loadingDialog;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_items);

        initializePage();

        retrieveData();
        buildRecyclerView();

        addMissing();

        initializeRfid();
        initializeTabs();
        buttonEnd();
    }

    private void addMissing() { new AddMissingTask().execute(); }

    private class AddMissingTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() { showProgressBar(); }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                List<Inventory> existing = App.get().getDB().inventoryDao().getAllInLocation(locationID);
                if (existing == null || existing.isEmpty()) {
                    List<Item> itemList = App.get().getDB().itemDao().getAllItems();

                    App.get().getDB().runInTransaction(() -> {

                        for (Item item : itemList) {
                            Category categoryDesc = App.get().getDB().categoryDao().getCategoryByID(item.getCategoryID());
                            Location loc = App.get().getDB().locationDao().getAllLocationByLocationID(item.getLocationID());

                            Inventory inventory = new Inventory(
                                    inventoryId,
                                    startDateStr,
                                    userId,
                                    item.getItemID(),
                                    item.getItemBarCode(),
                                    item.getOpt3(),
                                    item.getRemark(),
                                    item.getCategoryID(),
                                    categoryDesc != null ? categoryDesc.getCategoryDesc() : null,
                                    item.getStatusID(),
                                    item.getLocationID(),
                                    loc != null ? loc.getLocationDesc() : null,
                                    loc != null ? loc.getFullLocationDesc() : null
                            );

                            App.get().getDB().inventoryDao().insert(inventory);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            fetchDataFromDatabase();
            hideProgressBar();

            // Default to Missing tab
            openTab(2);
            missingTrx();

            // Register Bluetooth state listener
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(bluetoothStateReceiver, filter);
        }
    }

    private void buildRecyclerView() {
        inventoryAdapter = new InventoryAdapter(emptyListForAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(inventoryAdapter);
    }

    public void showProgressBar() {
        loadingDialog.startLoadingDialog();
    }

    public void hideProgressBar() {
        loadingDialog.dismissDialog();
    }

    private void buttonEnd() {
        btnEnd.setOnClickListener(v -> {
            new FlushPendingUpdatesTask(this::finish).execute();
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
                showProgressBar();
                rfidHandler.onCreate(ScanItems.this);
            }
        } else {
            showProgressBar();
            rfidHandler.onCreate(ScanItems.this);
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

        loadingDialog = new LoadingDialog(this);

        rfidHandler = new BluetoothHandler();
    }

    private void initializeTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: allTrx(); break;
                    case 1: registeredTrx(); break;
                    case 2: missingTrx(); break;
                    case 3: unRegisteredTrx(); break;
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void openTab(int tabPosition) {
        TabLayout.Tab tab = tabLayout.getTabAt(tabPosition);
        if (tab != null) tab.select();
    }

    private void fetchDataFromDatabase() { new FetchDataFromDatabaseTask().execute(); }

    private class FetchDataFromDatabaseTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() { showProgressBar(); }

        @Override
        protected Void doInBackground(Void... voids) {
            registeredTagIds.clear();
            unregisteredTagIds.clear();
            missingTagIds.clear();
            otherLocationTagIds.clear();
            tagIdToInventory.clear();

            List<Inventory> otherLoc = App.get().getDB().inventoryDao().getAllOtherLocationTagsID(locationID);
            List<Inventory> missing = App.get().getDB().inventoryDao().getAllUMissingTagsID(locationID);
            List<Inventory> registered = App.get().getDB().inventoryDao().getAllURegisteredTagsID(locationID);
            List<Inventory> unregistered = App.get().getDB().inventoryDao().getAllUnregisteredTagsID(locationID);

            for (Inventory inv : registered) {
                String tag = inv.getTagId();
                if (tag != null && !tag.isEmpty()) {
                    registeredTagIds.add(tag);
                    tagIdToInventory.put(tag, inv);
                }
            }

            for (Inventory inv : unregistered) {
                String tag = inv.getTagId();
                if (tag != null && !tag.isEmpty()) {
                    unregisteredTagIds.add(tag);
                    tagIdToInventory.put(tag, inv);
                }
            }

            for (Inventory inv : missing) {
                String tag = inv.getTagId();
                if (tag != null && !tag.isEmpty()) {
                    missingTagIds.add(tag);
                    tagIdToInventory.put(tag, inv);
                }
            }

            for (Inventory inv : otherLoc) {
                String tag = inv.getTagId();
                if (tag != null && !tag.isEmpty()) {
                    otherLocationTagIds.add(tag);
                    tagIdToInventory.put(tag, inv);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            hideProgressBar();
            updateCountTextViews();

            int pos = tabLayout.getSelectedTabPosition();
            if (pos == 0) allTrx();
            else if (pos == 1) registeredTrx();
            else if (pos == 2) missingTrx();
            else if (pos == 3) unRegisteredTrx();
        }
    }

    // Flush DB updates when End is pressed
    private interface FlushCallback { void onDone(); }

    private class FlushPendingUpdatesTask extends AsyncTask<Void, Void, Void> {
        private final FlushCallback callback;
        FlushPendingUpdatesTask(FlushCallback cb) { this.callback = cb; }

        @Override
        protected void onPreExecute() { showProgressBar(); }

        @Override
        protected Void doInBackground(Void... voids) {
            try {

                // Found items
                for (String barcode : new HashSet<>(pendingFoundBarcodes)) {
                    App.get().getDB().inventoryDao().updateItemStatusToFound(barcode);
                }

                // Relocated items (old -> current locationID)
                for (Map.Entry<String, String> e : new HashMap<>(pendingRelocationOldLocationByBarcode).entrySet()) {
                    App.get().getDB().inventoryDao().updateItemMissingFound(e.getKey(), e.getValue(), locationID);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            pendingFoundBarcodes.clear();
            pendingRelocationOldLocationByBarcode.clear();
            hideProgressBar();
            if (callback != null) callback.onDone();
        }
    }

    // Scan logic: move within sets only (no DB writes)
    public interface CheckItemCallback { void onCheckComplete(boolean result); }

    private void DoInventory(String tagIdRaw) {
        final String tagId = tagIdRaw;
        if (tagId == null || tagId.isEmpty()) return;

        checkIfItemIsMissing(tagId, foundInMissing -> {
            if (!foundInMissing) {
                checkIfItemIsOnOtherLocation(tagId, foundInOtherLoc -> {
                    // If needed, handle unknown tags here
                });
            }
        });
    }

    private void checkIfItemIsMissing(String tagId, CheckItemCallback callback) { new CheckIfItemIsMissingTask(callback).execute(tagId); }

    private class CheckIfItemIsMissingTask extends AsyncTask<String, Void, Boolean> {
        private final CheckItemCallback callback;
        CheckIfItemIsMissingTask(CheckItemCallback callback) { this.callback = callback; }

        @Override
        protected Boolean doInBackground(String... params) {
            String tagId = params[0];
            if (tagId != null && missingTagIds.remove(tagId)) {
                registeredTagIds.add(tagId);
                Inventory inv = tagIdToInventory.get(tagId);
                if (inv != null && inv.getItemBarcode() != null) {
                    pendingFoundBarcodes.add(inv.getItemBarcode());
                }
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) { if (callback != null) callback.onCheckComplete(result); }
    }

    private void checkIfItemIsOnOtherLocation(String tagId, CheckItemCallback callback) { new CheckIfItemIsOnOtherLocationTask(callback).execute(tagId); }

    private class CheckIfItemIsOnOtherLocationTask extends AsyncTask<String, Void, Boolean> {
        private final CheckItemCallback callback;
        CheckIfItemIsOnOtherLocationTask(CheckItemCallback callback) { this.callback = callback; }

        @Override
        protected Boolean doInBackground(String... params) {
            String tagId = params[0];
            if (tagId != null && otherLocationTagIds.remove(tagId)) {
                unregisteredTagIds.add(tagId);
                Inventory inv = tagIdToInventory.get(tagId);
                if (inv != null && inv.getItemBarcode() != null) {
                    pendingRelocationOldLocationByBarcode.put(inv.getItemBarcode(), inv.getLocationID());
                }
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {if (callback != null) callback.onCheckComplete(result); }
    }

    private void registeredTrx() { new RenderSetTask(registeredTagIds).execute(); }
    private void unRegisteredTrx() { new RenderSetTask(unregisteredTagIds).execute(); }
    private void missingTrx() { new RenderSetTask(missingTagIds).execute(); }
    private void allTrx() {
        Set<String> all = new HashSet<>(registeredTagIds);
        all.addAll(unregisteredTagIds);
        new RenderSetTask(all).execute();
    }

    private class RenderSetTask extends AsyncTask<Void, Void, List<Inventory>> {
        private final Set<String> source;
        RenderSetTask(Set<String> source) { this.source = source; }

        @Override
        protected List<Inventory> doInBackground(Void... voids) {
            List<Inventory> out = new ArrayList<>(source.size());
            for (String tag : source) {
                Inventory inv = tagIdToInventory.get(tag);
                if (inv != null) out.add(inv);
            }
            return out;
        }

        @Override
        protected void onPostExecute(List<Inventory> result) {
            inventoryAdapter.updateData(result);
            updateCountTextViews();
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateCountTextViews() {
        int registeredCount = registeredTagIds.size();
        int unregisteredCount = unregisteredTagIds.size();
        int missingCount = missingTagIds.size();
        int allCount = registeredCount + unregisteredCount;

        allCountData.setText(String.valueOf(allCount));
        registeredCountData.setText(String.valueOf(registeredCount));
        missingCountData.setText(String.valueOf(missingCount));
        unregisteredCountData.setText(String.valueOf(unregisteredCount));
    }

    private void showValidationDialog() {
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
                        App.get().getDB().itemDao().SetItemImage(imageData, randomInventory.getItemID());
                        saveBitmapToFolder(bitmap);
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
//        rfidHandler.onDestroy();
        rfidHandler.removeContext(this);
        new FlushPendingUpdatesTask(this::finish).execute();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showProgressBar();
                rfidHandler.assignScanItemsContext(this);
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
                // no-op
            } else {
                requestPermission();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        rfidHandler.onResume();
        rfidHandler.assignScanItemsContext(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        rfidHandler.updateContext(ScanItems.this);
    }

    @Override
    protected void onDestroy() {
        rfidHandler.removeContext(this);
        super.onDestroy();
        try {
            // Flush pending updates
            new FlushPendingUpdatesTask(this::finish).execute();
            unregisterReceiver(bluetoothStateReceiver);
        } catch (IllegalArgumentException ignored) {}
    }

    @Override
    public void handleTagdata(TagData[][] tagDataArray) {
        for (TagData[] td : tagDataArray) {
            for (int i = 0; i < td.length; i++) {
                String tagId = td[i].getTagID();
                if (tagId == null || tagId.isEmpty()) continue;
                if (uniqueTagIDs.add(tagId)) {
                    DoInventory(tagId);
                }
            }
        }

        runOnUiThread(() -> {
            int selectedTabPosition = tabLayout.getSelectedTabPosition();
            switch (selectedTabPosition) {
                case 0: allTrx(); break;
                case 1: registeredTrx(); break;
                case 2: missingTrx(); break;
                case 3: unRegisteredTrx(); break;
            }
        });
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        if (pressed) {
            runOnUiThread(() -> {
                int selectedTabPosition = tabLayout.getSelectedTabPosition();
                switch (selectedTabPosition) {
                    case 0: allTrx(); break;
                    case 1: registeredTrx(); break;
                    case 2: missingTrx(); break;
                    case 3: unRegisteredTrx(); break;
                }
            });
            rfidHandler.performInventory();
        } else {
            rfidHandler.stopInventory();
        }
    }

    @Override
    public void sendToast(String val) { runOnUiThread(() -> Toast.makeText(ScanItems.this, val, Toast.LENGTH_SHORT).show()); }

    private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        hideProgressBar();
                        Toast.makeText(context, getString(R.string.bluetooth_turned_off), Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        showProgressBar();
                        rfidHandler.onCreate(ScanItems.this);
                        break;
                }
            }
        }
    };
}