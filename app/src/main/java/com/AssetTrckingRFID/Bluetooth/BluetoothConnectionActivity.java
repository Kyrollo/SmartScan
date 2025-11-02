package com.AssetTrckingRFID.Bluetooth;

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
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.AssetTrckingRFID.R;
import com.AssetTrckingRFID.Utilities.LoadingDialog;
import com.zebra.rfid.api3.TagData;

public class BluetoothConnectionActivity extends AppCompatActivity implements BluetoothHandler.RFIDHandlerBluetoothListener {
    public TextView textViewStatusRFID;
    private ImageView refreshConnection;
    private BluetoothHandler rfidHandler;
    private BluetoothAdapter bluetoothAdapter;
    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 100;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    private static final int REQUEST_ENABLE_BLUETOOTH = 300;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private LoadingDialog loadingDialog;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connection);

        initViews();
        registerBluetoothReceiver();
        initializeRfid();
    }

    private void initViews() {
        textViewStatusRFID = findViewById(R.id.textViewStatusRFID);
        refreshConnection = findViewById(R.id.refreshConnection);

        loadingDialog = new LoadingDialog(this);

        rfidHandler = new BluetoothHandler();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        refreshConnection.setOnClickListener((v -> reconnectToRFID()));
    }

    private void registerBluetoothReceiver() {
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothStateReceiver, filter);
    }

    private void initializeRfid() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                        BLUETOOTH_PERMISSION_REQUEST_CODE);
            } else {
                checkBluetoothAndConnect();
            }
        } else {
            checkBluetoothAndConnect();
        }
    }

    private void checkBluetoothAndConnect() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
            updateRFIDStatus("Bluetooth Not Supported");
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
//            showBluetoothDialog();
            updateRFIDStatus("Bluetooth is Off");
            Toast.makeText(this, R.string.bluetooth_is_required_for_scanning, Toast.LENGTH_SHORT).show();

        } else {
            connectToRfid();
        }
    }

    private void showBluetoothDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Bluetooth Required")
                .setMessage("Bluetooth is required to connect to the RFID reader. Do you want to enable Bluetooth?")
                .setPositiveButton("Yes", (dialog, which) -> enableBluetooth())
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                    updateRFIDStatus("Bluetooth Off");
                    Toast.makeText(this, "Bluetooth is required for scanning", Toast.LENGTH_SHORT).show();
                })
                .setCancelable(false)
                .show();
    }

    private void enableBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
            }
        } else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
        }
    }

    private void connectToRfid() {
        showProgressBar();
        rfidHandler.onCreate(this);
    }

    public void showProgressBar() {
        loadingDialog.startLoadingDialog();
    }

    public void hideProgressBar() {
        loadingDialog.dismissDialog();
    }

    private void reconnectToRFID() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
//            showBluetoothDialog();
            updateRFIDStatus("Bluetooth is Off");
            Toast.makeText(this, R.string.bluetooth_is_required_for_scanning, Toast.LENGTH_SHORT).show();
            return;
        }

        showProgressBar();

        new Thread(() -> {
            try {
                rfidHandler.onDestroy();
                Thread.sleep(500);
            } catch (Exception e) {
                // ignore
            }

            runOnUiThread(this::connectToRfid);
        }).start();
    }

    public void updateRFIDStatus(String status) {
        runOnUiThread(() -> textViewStatusRFID.setText(status));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, getString(R.string.bluetooth_enabled), Toast.LENGTH_SHORT).show();
                connectToRfid();
            } else {
                updateRFIDStatus("Bluetooth Off");
                Toast.makeText(this, getString(R.string.bluetooth_is_required_for_scanning), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        rfidHandler.removeContext(this);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkBluetoothAndConnect();
            } else {
                Toast.makeText(this, getString(R.string.bluetooth_permissions_not_granted), Toast.LENGTH_SHORT).show();
                updateRFIDStatus("Permission Denied");
            }
        }

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_IMAGE_CAPTURE);
            } else {
                Toast.makeText(this, getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        rfidHandler.updateContext(BluetoothConnectionActivity.this);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        rfidHandler.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        hideProgressBar();
        rfidHandler.removeContext(this);
        try {
            unregisterReceiver(bluetoothStateReceiver);
        } catch (IllegalArgumentException ignored) {}
        super.onDestroy();
    }

    @Override
    public void handleTagdata(TagData[][] tagDataArray) {
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
        runOnUiThread(() -> Toast.makeText(this, val, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onConnectionStatusChanged(boolean isConnected, boolean isFailed) {
        runOnUiThread(() -> {
            hideProgressBar();
            if (isConnected) {
                Toast.makeText(this, getString(R.string.rfid_connected), Toast.LENGTH_SHORT).show();
            } else if (isFailed) {
                Toast.makeText(this, getString(R.string.rfid_not_connected), Toast.LENGTH_SHORT).show();
            }
        });
    }

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
                        updateRFIDStatus(getString(R.string.disconnected));
                        rfidHandler.onDestroy();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        connectToRfid();
                        break;
                }
            }
        }
    };
}