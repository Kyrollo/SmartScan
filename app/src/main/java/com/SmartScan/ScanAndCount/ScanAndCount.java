package com.SmartScan.ScanAndCount;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.SmartScan.R;
import com.zebra.rfid.api3.TagData;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

public class ScanAndCount extends AppCompatActivity implements RFIDHandlerScan.ResponseHandlerInterface {
    private Set<String> uniqueTagIDs = new HashSet<>();
    public TextView statusTextViewRFID = null;
    private TextView textrfid , scanResult, itemCount;
    private ImageView refreshConnection;
    private Button btnExport;
    private RFIDHandlerScan rfidHandlerScan;
    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 100;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_and_count);

        // RFID Handler
        statusTextViewRFID = (TextView) findViewById(R.id.textViewStatusrfid); //Device name
        textrfid = (TextView) findViewById(R.id.edittextrfid);
        scanResult = (TextView) findViewById(R.id.scanResult);
        itemCount = (TextView) findViewById(R.id.itemCount);
        refreshConnection = (ImageView) findViewById(R.id.refreshConnection);
        btnExport = (Button) findViewById(R.id.btnExport);
        rfidHandlerScan = new RFIDHandlerScan();

        btnExport.setOnClickListener(v -> createExcelFiles());

        refreshConnection.setOnClickListener((v -> reconnectToRFID()));

        //Scanner Initializations
        //Handling Runtime BT permissions for Android 12 and higher
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, BLUETOOTH_PERMISSION_REQUEST_CODE);
            }else{
                rfidHandlerScan.onCreate(this);
            }

        }else{
            rfidHandlerScan.onCreate(this);
        }
    }

    private void updateItemCount() {
        int items = uniqueTagIDs.size();
        itemCount.setText("Unique Tags: " + items);
    }

    private void reconnectToRFID() {
        rfidHandlerScan.onDestroy();
        rfidHandlerScan.onCreate(this);
    }

    public void createExcelFiles() {
        requestPermission();

        String fileName = "Unique Tags.xlsx";
        File file = new File(Environment.getExternalStorageDirectory(), fileName);

        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Tags");

            XSSFRow headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Tag ID");

            int rowNum = 1;
            for (String tag : uniqueTagIDs) {
                XSSFRow row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(tag);
            }

            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();

            Toast.makeText(ScanAndCount.this, "File created successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(ScanAndCount.this, "Failed to create excel file", Toast.LENGTH_SHORT).show();
        }

    }

    public void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, 2296);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ScanAndCount.this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2296);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2296) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    createExcelFiles();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                rfidHandlerScan.onCreate(this);
            }
            else {
                Toast.makeText(this, "Bluetooth Permissions not granted", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == PackageManager.PERMISSION_GRANTED) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createExcelFiles();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }



    @Override
    protected void onPause() {
        super.onPause();
        //rfidHandler.onPause();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        String result = rfidHandlerScan.onResume();
        statusTextViewRFID.setText(result);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rfidHandlerScan.onDestroy();
    }

    public void StartInventory(View view) {
        textrfid.setText("");
        rfidHandlerScan.performInventory();
    }

    public void scanCode(View view){
        rfidHandlerScan.scanCode();
    }

    public void StopInventory(View view){
        rfidHandlerScan.stopInventory();
    }

    @Override
    public void handleTagdata(TagData[][] tagDataArray) {
        final StringBuilder sb = new StringBuilder();
        String tagId;

        for (TagData[] tagData : tagDataArray) {
            for (int index = 0; index < tagData.length; index++) {
                tagId = tagData[index].getTagID();

                if (uniqueTagIDs.add(tagId)) {
                    sb.append(tagId + " ,   " + tagData[index].getPeakRSSI() + "\n");
                }
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textrfid.append(sb.toString());
                updateItemCount();
            }
        });
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        if (pressed) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    uniqueTagIDs.clear();
                    textrfid.setText("");
                    updateItemCount();
                }
            });
            rfidHandlerScan.performInventory();
        } else
            rfidHandlerScan.stopInventory();
    }

    @Override
    public void barcodeData(String val) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scanResult.setText("Scan Result : "+ val);
            }
        });
    }

    @Override
    public void sendToast(String val) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ScanAndCount.this,val,Toast.LENGTH_SHORT).show();
            }
        });
    }
}