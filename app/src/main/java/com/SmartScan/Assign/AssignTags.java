package com.SmartScan.Assign;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.SmartScan.App;
import com.SmartScan.R;

import com.zebra.rfid.api3.TagData;

public class AssignTags extends AppCompatActivity implements RFIDHandlerAssign.RFIDHandlerListener {
    private RFIDHandlerAssign rfidHandler;
    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 100;
    private BroadcastReceiver myBroadcastReceiver;
    private EditText barcodeEditText;
    private TextView rfidTextView;
    private Button btnEnd;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_tags);

        barcodeEditText = findViewById(R.id.barcodeEditText);
        rfidTextView = findViewById(R.id.rfidTextView);
        btnEnd = findViewById(R.id.btnEnd);

        rfidHandler = new RFIDHandlerAssign();

        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (barcodeEditText.getText().toString().isEmpty()) {
                    Toast.makeText(AssignTags.this, R.string.please_scan_the_barcode, Toast.LENGTH_SHORT).show();
                } else if (rfidTextView.getText().toString().isEmpty()) {
                    Toast.makeText(AssignTags.this, R.string.please_scan_the_rfid, Toast.LENGTH_SHORT).show();
                } else {
                    assignTag(barcodeEditText.getText().toString(), rfidTextView.getText().toString());
                }
            }
        });

//        myBroadcastReceiverr = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                String action = intent.getAction();
//                if (action.equals(getResources().getString(R.string.activity_intent_filter_action))) {
//                    String scannedData = intent.getStringExtra(getResources().getString(R.string.datawedge_intent_data_String));
//                    barcodeEditText.setText(scannedData);
//                }
//            }
//        };

        barcodeEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                 if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        return true;
                    }
                 else
                 {
                     return false;
                 }
                    // Perform action on key press
//                    String scannedData = barcodeEditText.getText().toString();
//                    barcodeEditText.setText(scannedData);

                }

        });

        IntentFilter filter = new IntentFilter();
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.addAction(getResources().getString(R.string.activity_intent_filter_action));
        registerReceiver(myBroadcastReceiver, filter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, BLUETOOTH_PERMISSION_REQUEST_CODE);
            } else {
                rfidHandler.onCreate(this);
            }
        } else {
            rfidHandler.onCreate(this);
        }
    }

    private void assignTag(String barcode, String rfid) {
        App.get().getDB().itemDao().assignTag(barcode, rfid);
        Toast.makeText(this, R.string.tag_assigned_successfully, Toast.LENGTH_SHORT).show();
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

                // Update the UI with the scanned RFID
                String finalTagId = tagId;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rfidTextView.setText(finalTagId);
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
}