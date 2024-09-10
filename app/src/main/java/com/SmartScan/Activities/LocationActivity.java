package com.SmartScan.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.SmartScan.App;
import com.SmartScan.DataBase.AppDataBase;
import com.SmartScan.R;
import com.SmartScan.ScanItems.ScanItems;
import com.SmartScan.Tables.Location;

import java.util.ArrayList;
import java.util.List;

public class LocationActivity extends AppCompatActivity {
    private Spinner spinner1, spinner2, spinner3, spinner4;
    private Button btnStartInventory;
    private List<Location> parentLocations, locations2, locations3, locations4;
    private List<String> locationSpinner1, locationSpinner2, locationSpinner3, locationSpinner4;
    private String lastChosenLocationId;
    private String startDateStr;
    private int userId, inventoryId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_location);

        initializePage();
        retrieveData();

        btnStartInventory.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScanItems.class);
            intent.putExtra("INVENTORYID", inventoryId);
            intent.putExtra("INVENTORYDATE", startDateStr);
            intent.putExtra("USERID", userId);
            intent.putExtra("locationId", lastChosenLocationId);
            startActivity(intent);
        });

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    spinner2.setVisibility(View.GONE);
                    spinner3.setVisibility(View.GONE);
                    spinner4.setVisibility(View.GONE);
                    btnStartInventory.setVisibility(View.GONE);
                    return;
                }
                String selectedItem = (String) parent.getItemAtPosition(position);
                Location selectedLocation = findLocationByDesc(parentLocations, selectedItem);
                lastChosenLocationId = selectedLocation.getLocationID();
                locations2 = App.get().getDB().locationDao().getAllLocationByParentID(selectedLocation.getLocationID());
                if (locations2 == null || locations2.isEmpty()) {
                    btnStartInventory.setVisibility(View.VISIBLE);
                    spinner2.setVisibility(View.GONE);
                    spinner3.setVisibility(View.GONE);
                    spinner4.setVisibility(View.GONE);
                    return;
                }
                locationSpinner2 = new ArrayList<>();
                locationSpinner2.add(getString(R.string.choose_location));
                for (Location location : locations2) {
                    locationSpinner2.add(location.getLocationDesc());
                }
                ArrayAdapter<String> adapter2 = new ArrayAdapter<>(LocationActivity.this, R.layout.spinner_text_color, locationSpinner2);
                adapter2.setDropDownViewResource(R.layout.spinner_text_color);
                spinner2.setAdapter(adapter2);

                spinner2.setVisibility(View.VISIBLE);
                spinner3.setVisibility(View.GONE);
                spinner4.setVisibility(View.GONE);
                btnStartInventory.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinner2.setVisibility(View.GONE);
                spinner3.setVisibility(View.GONE);
                spinner4.setVisibility(View.GONE);
                btnStartInventory.setVisibility(View.GONE);
            }
        });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    spinner3.setVisibility(View.GONE);
                    spinner4.setVisibility(View.GONE);
                    btnStartInventory.setVisibility(View.GONE);
                    return;
                }
                String selectedItem = (String) parent.getItemAtPosition(position);
                Location selectedLocation = findLocationByDesc(locations2, selectedItem);
                lastChosenLocationId = selectedLocation.getLocationID();
                locations3 = App.get().getDB().locationDao().getAllLocationByParentID(selectedLocation.getLocationID());
                if (locations3 == null || locations3.isEmpty()) {
                    btnStartInventory.setVisibility(View.VISIBLE);
                    spinner3.setVisibility(View.GONE);
                    spinner4.setVisibility(View.GONE);
                    return;
                }
                locationSpinner3 = new ArrayList<>();
                locationSpinner3.add(getString(R.string.choose_location));
                for (Location location : locations3) {
                    locationSpinner3.add(location.getLocationDesc());
                }
                ArrayAdapter<String> adapter3 = new ArrayAdapter<>(LocationActivity.this, R.layout.spinner_text_color, locationSpinner3);
                adapter3.setDropDownViewResource(R.layout.spinner_text_color);
                spinner3.setAdapter(adapter3);

                spinner3.setVisibility(View.VISIBLE);
                spinner4.setVisibility(View.GONE);
                btnStartInventory.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinner3.setVisibility(View.GONE);
                spinner4.setVisibility(View.GONE);
                btnStartInventory.setVisibility(View.GONE);
            }
        });

        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    spinner4.setVisibility(View.GONE);
                    btnStartInventory.setVisibility(View.GONE);
                    return;
                }
                String selectedItem = (String) parent.getItemAtPosition(position);
                Location selectedLocation = findLocationByDesc(locations3, selectedItem);
                lastChosenLocationId = selectedLocation.getLocationID();
                locations4 = App.get().getDB().locationDao().getAllLocationByParentID(selectedLocation.getLocationID());
                if (locations4 == null || locations4.isEmpty()) {
                    btnStartInventory.setVisibility(View.VISIBLE);
                    spinner4.setVisibility(View.GONE);
                    return;
                }
                locationSpinner4 = new ArrayList<>();
                locationSpinner4.add(getString(R.string.choose_location));
                for (Location location : locations4) {
                    locationSpinner4.add(location.getLocationDesc());
                }
                ArrayAdapter<String> adapter4 = new ArrayAdapter<>(LocationActivity.this, R.layout.spinner_text_color, locationSpinner4);
                adapter4.setDropDownViewResource(R.layout.spinner_text_color);
                spinner4.setAdapter(adapter4);

                spinner4.setVisibility(View.VISIBLE);
                btnStartInventory.setVisibility(View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinner4.setVisibility(View.GONE);
                btnStartInventory.setVisibility(View.GONE);
            }
        });

        spinner4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    btnStartInventory.setVisibility(View.GONE);
                    return;
                }
                String selectedItem = (String) parent.getItemAtPosition(position);
                Location selectedLocation = findLocationByDesc(locations4, selectedItem);
                lastChosenLocationId = selectedLocation.getLocationID();
                btnStartInventory.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                btnStartInventory.setVisibility(View.GONE);
            }
        });
    }

    private void initializePage(){
        spinner1 = findViewById(R.id.spinner1);
        spinner2 = findViewById(R.id.spinner2);
        spinner3 = findViewById(R.id.spinner3);
        spinner4 = findViewById(R.id.spinner4);
        btnStartInventory = findViewById(R.id.btnStartInventory);

        spinner2.setVisibility(View.GONE);
        spinner3.setVisibility(View.GONE);
        spinner4.setVisibility(View.GONE);
        btnStartInventory.setVisibility(View.GONE);

        parentLocations = App.get().getDB().locationDao().getAllParents();
        locationSpinner1 = new ArrayList<>();
        locationSpinner1.add(getString(R.string.choose_location));

        for (Location location : parentLocations) {
            locationSpinner1.add(location.getLocationDesc());
        }

        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, R.layout.spinner_text_color, locationSpinner1);
        adapter1.setDropDownViewResource(R.layout.spinner_text_color);
        spinner1.setAdapter(adapter1);

    }

    private void retrieveData() {
        Intent intent = getIntent();
        userId = intent.getIntExtra("USERID", -1);
        inventoryId = intent.getIntExtra("INVENTORYID", -1);
        startDateStr = intent.getStringExtra("INVENTORYDATE");
    }

    private Location findLocationByDesc(List<Location> locations, String desc) {
        for (Location location : locations) {
            if (location.getLocationDesc().equals(desc)) {
                return location;
            }
        }
        return null;
    }
}