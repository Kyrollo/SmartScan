package com.SmartScan.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.SmartScan.API.APIService;
import com.SmartScan.API.Retrofit;
import com.SmartScan.ApiClasses.*;

import com.SmartScan.App;
import com.SmartScan.Bluetooth.BluetoothConnectionActivity;
import com.SmartScan.DataBase.AppDataBase;
import com.SmartScan.R;
import com.SmartScan.ScanAndCount.ScanAndCount;
import com.SmartScan.Tables.*;
import com.google.android.material.navigation.NavigationView;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private Toolbar toolbar;
    private TextView sessionName, startDate, endDate;
    private Button btnStart;
    private DrawerLayout drawerLayout;
    LinearLayout invSession;
  //  private AppDataBase db;;
    private APIService apiService;
    private List<ItemResponse> items;
    private List<CategoryResponse> categories;
    private List<LocationResponse> locations;
    private List<StatusResponse> allStatus;
    private List<InventoryH_Response> inventoriesH;
    private SharedPreferences sharedPreferences;
    private String ip;
    private int port;
    private FrameLayout progressBarLayout;
    private ProgressBar progressBar;
    private TextView tvStatus;
    private Handler handler = new Handler();
    private int progress = 0;
    private AlertDialog progressDialog;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupNavigationDrawer();
        setupLanguageSwitching();

        updateSessionData();

    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        sessionName = findViewById(R.id.sessionName);
        startDate = findViewById(R.id.startDate);
        endDate = findViewById(R.id.endDate);
        btnStart = findViewById(R.id.btnStart);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);
        progressBarLayout = findViewById(R.id.progressBarLayout);


        apiService = Retrofit.getRetrofit().create(APIService.class);

        btnStart.setOnClickListener(v -> {

            Intent intent = new Intent(this, LocationActivity.class);
            startActivity(intent);
        });
    }

    private void updateSessionData() {
        List<InventoryH> inventoryHS = App.get().getDB().inventoryH_dao().getAllInventoryHs();
        if (inventoryHS.size() > 0) {
            InventoryH inventoryH = inventoryHS.get(0);
            sessionName.setText(inventoryH.getInventoryName());
            startDate.setText(inventoryH.getStartDate().replace("T00:00:00",""));
            if (inventoryH.isClosed()) {
                btnStart.setVisibility(View.GONE);
                if (inventoryH.getEndDate() != null){
                    endDate.setVisibility(View.VISIBLE);
                    endDate.setText(inventoryH.getEndDate().replace("T00:00:00",""));
                }
            } else {
                btnStart.setVisibility(View.VISIBLE);
                endDate.setVisibility(View.GONE);
            }
        } else {
            btnStart.setVisibility(View.GONE);
            endDate.setVisibility(View.GONE);
            sessionName.setText("");
            startDate.setText("");
        }
    }

    private void setupNavigationDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Retrieve the username from the intent
        Intent intent = getIntent();
        String username = intent.getStringExtra("USERNAME");

        // Find the TextView and set the username
        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderTitle = headerView.findViewById(R.id.nav_header_title);
        navHeaderTitle.setText(getString(R.string.welcome) + username);
    }

    private void setupLanguageSwitching() {
        ImageView iconArabic = findViewById(R.id.icon_arabic);
        ImageView iconEnglish = findViewById(R.id.icon_english);

        String currentLocale = getResources().getConfiguration().locale.getLanguage();

        if (currentLocale.equals("ar")) {
            iconArabic.setVisibility(View.GONE);
        } else if (currentLocale.equals("en")) {
            iconEnglish.setVisibility(View.GONE);
        }

        iconArabic.setOnClickListener(view -> setLocale("ar"));
        iconEnglish.setOnClickListener(view -> setLocale("en"));
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = new Configuration();
        config.setLocale(locale);
        DisplayMetrics dm = resources.getDisplayMetrics();
        resources.updateConfiguration(config, dm);

        // Restart the current activity
        Intent intent = getIntent();
        String username = intent.getStringExtra("USERNAME");
        intent.putExtra("USERNAME", username);
        startActivity(intent);
    }

    private void fetchItems() {
        apiService.getItems().enqueue(new Callback<List<ItemResponse>>() {
            @Override
            public void onResponse(Call<List<ItemResponse>> call, Response<List<ItemResponse>> response) {
                if (response.isSuccessful()) {
                    items = response.body();
                    if (items != null) {
                        App.get().getDB().itemDao().deleteAll();
                        App.get().getDB().itemDao().resetPrimaryKey();
                        insertItems(items);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.failed_to_connect_check_your_internet), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<ItemResponse>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), getString(R.string.failed_to_connect_check_your_internet), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void insertItems(List<ItemResponse> items) {
        for (ItemResponse item : items) {
            Item newItem = new Item(item.getItemBarCode(), item.getItemDesc(), item.getRemark(), item.getOPT3(),
                    item.getItemID(), item.getCategoryID(), item.getLocationID(), item.getStatusID(), item.getItemSN());
            App.get().getDB().itemDao().insert(newItem);
        }
        updateProgress();
    }

    private void fetchCategories() {
        apiService.getCategories().enqueue(new Callback<List<CategoryResponse>>() {
            @Override
            public void onResponse(Call<List<CategoryResponse>> call, Response<List<CategoryResponse>> response) {
                if (response.isSuccessful()) {
                    categories = response.body();
                    if (categories != null) {
                        App.get().getDB().locationDao().deleteAll();
                        App.get().getDB().locationDao().resetPrimaryKey();
                        insertCategories(categories);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.failed_to_connect_check_your_internet), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<CategoryResponse>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), getString(R.string.failed_to_connect_check_your_internet), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void insertCategories(List<CategoryResponse> categories) {
        for (CategoryResponse category : categories) {
            Category newCategory = new Category(category.getCategoryID(), category.getCategoryDesc());
            App.get().getDB().categoryDao().insert(newCategory);
        }
        updateProgress();
    }

    private void fetchLocations() {
        apiService.getLocation().enqueue(new Callback<List<LocationResponse>>() {
            @Override
            public void onResponse(Call<List<LocationResponse>> call, Response<List<LocationResponse>> response) {
                if (response.isSuccessful()) {
                    locations = response.body();
                    if (locations != null) {
                        App.get().getDB().locationDao().deleteAll();
                        App.get().getDB().locationDao().resetPrimaryKey();
                        insertLocations(locations);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.failed_to_connect_check_your_internet), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<LocationResponse>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), getString(R.string.failed_to_connect_check_your_internet), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void insertLocations(List<LocationResponse> locations) {
        for (LocationResponse location : locations) {
            Location newLocation = new Location(location.getLocationID(), location.getLocationBarCode(), location.getLocationDesc(),
                    location.isHasParent(), location.getLocationParentID(), location.getFullLocationDesc());
            App.get().getDB().locationDao().insert(newLocation);
        }
        updateProgress();
    }

    private void fetchStatus() {
        apiService.getStatus().enqueue(new Callback<List<StatusResponse>>() {
            @Override
            public void onResponse(Call<List<StatusResponse>> call, Response<List<StatusResponse>> response) {
                if (response.isSuccessful()) {
                    allStatus = response.body();
                    if (allStatus != null) {
                        App.get().getDB().locationDao().deleteAll();
                        App.get().getDB().locationDao().resetPrimaryKey();
                        insertStatus(allStatus);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.failed_to_connect_check_your_internet), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<StatusResponse>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), getString(R.string.failed_to_connect_check_your_internet), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void insertStatus(List<StatusResponse> allStatus) {
        for (StatusResponse status : allStatus) {
            Status newStatus = new Status(status.getStatusID(), status.getStatusDesc());
            App.get().getDB().statusDao().insert(newStatus);
        }
        updateProgress();
    }

    private void fetchInventoryH() {
        apiService.getInventoryH().enqueue(new Callback<List<InventoryH_Response>>() {
            @Override
            public void onResponse(Call<List<InventoryH_Response>> call, Response<List<InventoryH_Response>> response) {
                if (response.isSuccessful()) {
                    inventoriesH = response.body();
                    if (inventoriesH != null) {
                        App.get().getDB().locationDao().deleteAll();
                        App.get().getDB().locationDao().resetPrimaryKey();
                        insertInventoryH(inventoriesH);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.failed_to_connect_check_your_internet), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<InventoryH_Response>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), getString(R.string.failed_to_connect_check_your_internet), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void insertInventoryH(List<InventoryH_Response> inventoriesH) {
        for (InventoryH_Response inventory : inventoriesH) {
            InventoryH newInventory = new InventoryH(inventory.getInventoryID(), inventory.getInventoryName(),
                    inventory.getStartdate(), inventory.getEnddate(), inventory.isClosed());
            App.get().getDB().inventoryH_dao().insert(newInventory);
        }
        updateProgress();
        updateSessionData();
    }

    private void showDownloadDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.downloadDataDialog))
                .setMessage(getString(R.string.confirmDownloadDialog))
                .setPositiveButton(getString(R.string.yesDownloadDialog), (dialog, which) -> downloadData())
                .setNegativeButton(getString(R.string.noDownloadDialog), (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void updateProgress() {
        runOnUiThread(() -> {
            progress += 20;
            if (progress <= 100) {
                progressBar.setProgress(progress);
                tvStatus.setText(progress + getString(R.string.progress_bar_process));
            }
        });
    }

    private void downloadData() {
        showProgressBar();
        progress = 0;
        fetchItems();
        fetchCategories();
        fetchLocations();
        fetchStatus();
        fetchInventoryH();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideProgressBar();
                Toast.makeText(getApplicationContext(), getString(R.string.data_downloaded_successfully), Toast.LENGTH_LONG).show();
            }
        }, 5000);
    }

    private void showProgressBar() {
        progressBarLayout.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBarLayout.setVisibility(View.GONE);
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.deleteAllDialog))
                .setMessage(getString(R.string.confirmDeleteDialog))
                .setPositiveButton(getString(R.string.yesDeleteDialog), (dialog, which) -> deleteAllData())
                .setNegativeButton(getString(R.string.noDeleteDialog), (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void deleteAllData() {
        App.get().getDB().itemDao().deleteAll();
        App.get().getDB().categoryDao().deleteAll();
        App.get().getDB().locationDao().deleteAll();
        App.get().getDB().statusDao().deleteAll();
        App.get().getDB().inventoryH_dao().deleteAll();
        App.get().getDB().itemDao().resetPrimaryKey();
        App.get().getDB().categoryDao().resetPrimaryKey();
        App.get().getDB().locationDao().resetPrimaryKey();
        App.get().getDB().statusDao().resetPrimaryKey();
        App.get().getDB().inventoryH_dao().resetPrimaryKey();
        Toast.makeText(HomeActivity.this, getString(R.string.dataDeleted), Toast.LENGTH_SHORT).show();
        updateSessionData();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_download) {
            showDownloadDialog();
//            hideProgressBar();
        }
        else if (id == R.id.nav_upload) {
            // Handle upload data action
        }
        else if (id == R.id.nav_delete) {
            showDeleteDialog();
        }
        else if (id == R.id.nav_bluetooth) {
            Intent intent = new Intent(this, BluetoothConnectionActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_server_config) {
            Intent intent = new Intent(this, ServerConfigActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_logout) {
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}