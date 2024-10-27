package com.AssetTrckingRFID.Assign;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.AssetTrckingRFID.Adapters.TagAdapter;
import com.AssetTrckingRFID.App;
import com.AssetTrckingRFID.Bluetooth.BluetoothHandler;
import com.AssetTrckingRFID.R;

import com.AssetTrckingRFID.Tables.Item;
import com.zebra.rfid.api3.TagData;

import java.util.ArrayList;
import java.util.List;

public class AssignTags extends AppCompatActivity implements BluetoothHandler.RFIDHandlerBluetoothListener {
    private BluetoothHandler rfidHandler;
    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 100;
    private BroadcastReceiver myBroadcastReceiver;
    private EditText barcodeEditText;
    private EditText rfidTextView;
    private RecyclerView recyclerView;
    private TagAdapter tagAdapter;
    private List<Tag> tagList;
    private FrameLayout progressBarContainer;
    private BluetoothAdapter bluetoothAdapter;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_tags);

        initializePage();

//        initializeBroadcastReceiver();

        initializeOnKey();

        initializeRfid();
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
                rfidHandler.onCreate(this);
            }
        } else {
//            rfidHandler.onCreate(this);
            rfidHandler.onCreate(this);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        rfidHandler.onDestroy();
        finish();
    }

    public void showProgressBar() {
        progressBarContainer.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        progressBarContainer.setVisibility(View.GONE);
    }

    private void initializePage() {
        barcodeEditText = findViewById(R.id.barcodeEditText);
        rfidTextView = findViewById(R.id.rfidTextView);
        progressBarContainer = findViewById(R.id.progressBarContainer);

//        rfidHandler = new RFIDHandlerAssign();
        rfidHandler = new BluetoothHandler();

        recyclerView = findViewById(R.id.recyclerViewAssignTags);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tagList = new ArrayList<>();
        tagAdapter = new TagAdapter(tagList, new TagAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(Tag tag) {
                updateOpt3(tag.getItemBarcode());
            }
        });
        recyclerView.setAdapter(tagAdapter);

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothStateReceiver, filter);
        rfidTextView.requestFocus();
    }

    private void initializeBroadcastReceiver() {
        myBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(getResources().getString(R.string.activity_intent_filter_action))) {
                    String scannedData = intent.getStringExtra(getResources().getString(R.string.datawedge_intent_data_String));
                    barcodeEditText.setText(scannedData);
                    handleBarcodeScan(scannedData);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(getResources().getString(R.string.activity_intent_filter_action));
        registerReceiver(myBroadcastReceiver, filter);
    }

    private void initializeOnKey() {
        barcodeEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    handleBarcodeScan(barcodeEditText.getText().toString());
                    return true;
                }
                else {
                    return false;
                }
            }
        });
    }

    private void updateOpt3(String barcode) {
        App.get().getDB().itemDao().deleteOPT3(barcode);
        Toast.makeText(this, R.string.opt3_deleted, Toast.LENGTH_SHORT).show();

        // Remove the tag from the list
        for (int i = 0; i < tagList.size(); i++) {
            if (tagList.get(i).getItemBarcode().equals(barcode)) {
                tagList.remove(i);
                tagAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    private void assignTag(String barcode, String rfid) {
        App.get().getDB().itemDao().assignTag(barcode, rfid);
        Toast.makeText(this, R.string.tag_assigned_successfully, Toast.LENGTH_SHORT).show();
    }

    private boolean isRFIDExist(String rfid) {
        for (Tag tag : tagList) {
            if (tag.getTagId().equals(rfid)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBarcodeExist(String barcode) {
        for (Tag tag : tagList) {
            if (tag.getItemBarcode().equals(barcode)) {
                return true;
            }
        }
        return false;
    }

    private void handleBarcodeScan(String barcode) {
        String rfid = rfidTextView.getText().toString();

        if (rfid.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_scan_rfid_first), Toast.LENGTH_SHORT).show();
            return;
        }

        if (barcode.isEmpty()) {
            Toast.makeText(this, getString(R.string.please_scan_barcode), Toast.LENGTH_SHORT).show();
            return;
        }

        Item itemByBarcode = App.get().getDB().itemDao().getItemByBarcode(barcode);
        if (itemByBarcode == null) {
            Toast.makeText(this, getString(R.string.invalid_barcode), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!rfid.isEmpty() && !barcode.isEmpty()) {
            if (isRFIDExist(rfid)) {
                Toast.makeText(this, getString(R.string.rfid_tag_is_already_assigned), Toast.LENGTH_SHORT).show();
                return;
            }
            if (isBarcodeExist(barcode)) {
                Toast.makeText(this, getString(R.string.barcode_is_already_assigned), Toast.LENGTH_SHORT).show();
                return;
            }

            Item itemsByOPT3 = App.get().getDB().itemDao().getItemsByOPT3(rfid);
            if (itemsByOPT3 == null) {
                assignTag(barcode, rfid);
                tagList.add(new Tag(barcode, rfid));
                tagAdapter.notifyDataSetChanged();

                rfidTextView.setText("");
                barcodeEditText.setText("");
                rfidTextView.requestFocus();

                // Toast.makeText(this, getString(R.string.invalid_opt3), Toast.LENGTH_SHORT).show();
                // return;
            }
            else
            {
                Toast.makeText(this, getString(R.string.rfid_tag_is_already_assigned), Toast.LENGTH_SHORT).show();
                barcodeEditText.setText("");
            }


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                rfidHandler.onCreate(this);
                rfidHandler.onCreate(this);
            } else {
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPostResume() {
        rfidHandler.onDestroy();
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

                // Update the UI with the scanned RFID
                String finalTagId = tagId;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        rfidTextView.setText(finalTagId);
                        barcodeEditText.requestFocus();
                    }
                });
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        if (pressed) {
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
                Toast.makeText(AssignTags.this, val, Toast.LENGTH_SHORT).show();
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
//                        rfidHandler.onCreate(AssignTags.this);
                        rfidHandler.onCreate(AssignTags.this);
                        break;
                }
            }
        }
    };
}