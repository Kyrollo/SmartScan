package com.SmartScan.Bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.SmartScan.R;

public class BluetoothConnectionActivity extends AppCompatActivity implements BluetoothHandler.BluetoothHandlerHandler {
    private TextView textrfid;
    public TextView textViewStatusRFID;
    private ImageView refreshConnection;
    private BluetoothHandler rfidHandler;

    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 100;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    private static final int REQUEST_IMAGE_CAPTURE = 1;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connection);

        textrfid = findViewById(R.id.textrfid);
        textViewStatusRFID = findViewById(R.id.textViewStatusRFID);
        refreshConnection = findViewById(R.id.refreshConnection);

        rfidHandler = new BluetoothHandler();

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

        refreshConnection.setOnClickListener((v -> reconnectToRFID()));

    }

    private void reconnectToRFID() {
        rfidHandler.onDestroy();
        rfidHandler.onCreate(this);
    }

    public void updateRFIDStatus(String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewStatusRFID.setText(status);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        finish();
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
        String result = rfidHandler.onResume();
        textViewStatusRFID.setText(result);
    }

    @Override
    public void sendToast(String val) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BluetoothConnectionActivity.this, val, Toast.LENGTH_SHORT).show();
            }
        });
    }
}