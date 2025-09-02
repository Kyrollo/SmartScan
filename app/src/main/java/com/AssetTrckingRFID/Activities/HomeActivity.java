package com.AssetTrckingRFID.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
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

import com.AssetTrckingRFID.API.APIService;
import com.AssetTrckingRFID.API.Retrofit;
import com.AssetTrckingRFID.API.UploadInventory;
import com.AssetTrckingRFID.API.UploadItems;
import com.AssetTrckingRFID.ApiClasses.*;

import com.AssetTrckingRFID.App;
import com.AssetTrckingRFID.Assign.AssignTags;
import com.AssetTrckingRFID.Bluetooth.BluetoothConnectionActivity;
import com.AssetTrckingRFID.R;
import com.AssetTrckingRFID.Tables.*;
import com.AssetTrckingRFID.Utilities.LoadingDialog;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private Toolbar toolbar;
    private TextView sessionName, startDate, endDate;
    private Button btnStart;
    private DrawerLayout drawerLayout;
    private APIService apiService;
    private List<Users> users;
    private List<ItemResponse> items;
    private List<CategoryResponse> categories;
    private List<LocationResponse> locations;
    private List<StatusResponse> allStatus;
    private List<InventoryH_Response> inventoriesH;
    private FrameLayout progressBarDownload;
    private ProgressBar progressBar, progressBarUpload;
    private TextView tvStatus;
    private Handler handler = new Handler();
    private int progress = 0;
    private String username, startDateStr;
    private int userId, inventoryId;
    private boolean isUploadItems = false, isUploadInventories = false;
    private LoadingDialog loadingDialog;

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
        progressBarDownload = findViewById(R.id.progressBarDownload);
        progressBarUpload = findViewById(R.id.progressBarUpload);

        loadingDialog = new LoadingDialog(this);


        apiService = Retrofit.getRetrofit().create(APIService.class);

        btnStart.setOnClickListener(v -> {
            List<InventoryH> inventoryHS = App.get().getDB().inventoryH_dao().getAllInventoryHs();
            InventoryH inventoryH = inventoryHS.get(0);
            inventoryId = inventoryH.getInventoryID();
            startDateStr = inventoryH.getStartDate();
            Intent intent = new Intent(this, LocationActivity.class);
            intent.putExtra("INVENTORYID", inventoryId);
            intent.putExtra("INVENTORYDATE", startDateStr);
            intent.putExtra("USERID", userId);
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
        username = intent.getStringExtra("USERNAME");
        userId = intent.getIntExtra("USERID", -1);

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
//        Intent intent = getIntent();
//        username = intent.getStringExtra("USERNAME");
//        userId = intent.getIntExtra("USERID", -1);
//        intent.putExtra("USERNAME", username);
//        intent.putExtra("PASSWORD", userId);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//        finish();

        recreate();
    }

    private void fetchItems() {
        apiService.getItems().enqueue(new Callback<List<ItemResponse>>() {
            @Override
            public void onResponse(Call<List<ItemResponse>> call, Response<List<ItemResponse>> response) {
                if (response.isSuccessful()) {
                    items = response.body();
                    if (items != null) {
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

    private String cleanOpt3(String opt3) {
        if (opt3 == null) return null;
        return opt3.replace("\r", "").replace("\n", "").trim();
    }

    private void insertItems(List<ItemResponse> items) {
        for (ItemResponse item : items) {
            String opt3Clean = cleanOpt3(item.getOPT3());
            Item newItem = new Item(item.getItemBarCode(),
                    item.getItemDesc(),
                    item.getRemark(),
//                    item.getOPT3(),
                    opt3Clean,
                    item.getItemID(),
                    item.getCategoryID(),
                    item.getLocationID(),
                    item.getStatusID(),
                    item.getItemSN());
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

    private void fetchUsers() {
        apiService.getUsers().enqueue(new Callback<List<Users>>() {
            @Override
            public void onResponse(Call<List<Users>> call, Response<List<Users>> response) {
                if (response.isSuccessful()) {
                    users = response.body();
                    insertUsers();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.failed_to_connect_check_your_internet), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Users>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), getString(R.string.failed_to_connect_check_your_internet), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void insertUsers() {
        if (users != null && !users.isEmpty()) {
            App.get().getDB().usersDao().deleteAll();
            App.get().getDB().usersDao().insertAll(users);
        }
    }

    private void testConnection(Runnable onSuccess) {
        if (!NetworkUtils.isNetworkConnected(this)) {
            Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
            return;
        }
        apiService.testConnection().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && "Success".equals(response.body())) {
                    onSuccess.run();
                    Toast.makeText(getApplicationContext(), getString(R.string.connection_succeeded), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.failed_to_connect_check_your_internet), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(getApplicationContext(), getString(R.string.failed_to_connect_check_your_internet), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void downloadData(){
        List<Inventory> allInventory = App.get().getDB().inventoryDao().getAllInventories();

        if (allInventory.size() == 0){
            deleteAllData();
            download();
        } else {
            showDownloadDialog();
        }
    }

    private void showDownloadDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.downloadDataDialog))
                .setMessage(getString(R.string.confirmDownloadDialog))
                .setPositiveButton(getString(R.string.yesDownloadDialog), (dialog, which) ->{
                    deleteAllData();
                    download();
                })
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

    private void download() {
        showProgressBar();
        progress = 0;
        fetchUsers();
        fetchItems();
        fetchCategories();
        fetchLocations();
        fetchStatus();
        fetchInventoryH();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!NetworkUtils.isNetworkConnected(HomeActivity.this)) {
                    deleteAllData();
                    hideProgressBar();
                    Toast.makeText(getApplicationContext(), getString(R.string.internet_disconnected_while_downloading), Toast.LENGTH_LONG).show();
                } else {
                    hideProgressBar();
                    Toast.makeText(getApplicationContext(), getString(R.string.data_downloaded_successfully), Toast.LENGTH_LONG).show();
                }
            }
        }, 5000);
    }

    public void showProgressBar() {
        loadingDialog.startLoadingDialog();
    }

    public void hideProgressBar() {
        loadingDialog.dismissDialog();
    }



    @SuppressLint("ClickableViewAccessibility")
    private void showPasswordCard() {
        View cardView = getLayoutInflater().inflate(R.layout.card_password_input, null);
        EditText editTextPassword = cardView.findViewById(R.id.editTextPassword);

        editTextPassword.setOnTouchListener((v, event) -> {
            final int DRAWABLE_END = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                Drawable drawableEnd = editTextPassword.getCompoundDrawables()[DRAWABLE_END];
                if (drawableEnd != null && event.getRawX() >= (editTextPassword.getRight() - drawableEnd.getBounds().width())) {
                    if (editTextPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                        editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        editTextPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_open, 0);
                    } else {
                        editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        editTextPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_closed, 0);
                    }
                    editTextPassword.setSelection(editTextPassword.getText().length());
                    return true;
                }
            }
            return false;
        });

        new AlertDialog.Builder(this)
                .setView(cardView)
                .setTitle(getString(R.string.validation_required))
                .setPositiveButton(getString(R.string.confirm), (dialog, which) -> {
                    String password = editTextPassword.getText().toString();
                    if ("P@$$w0rd".equals(password)) {
                        showDeleteDialog();
                    } else {
                        Toast.makeText(this, getString(R.string.incorrect_password), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.deleteAllDialog))
                .setMessage(getString(R.string.confirmDeleteDialog))
                .setPositiveButton(getString(R.string.yesDeleteDialog), (dialog, which) -> {
                    deleteAllData();
                    Toast.makeText(HomeActivity.this, getString(R.string.dataDeleted), Toast.LENGTH_SHORT).show();
                })
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
        App.get().getDB().statusDao().resetPrimaryKey();
        App.get().getDB().inventoryH_dao().resetPrimaryKey();
        App.get().getDB().inventoryDao().deleteAll();
        App.get().getDB().inventoryDao().resetPrimaryKey();
        updateSessionData();
    }

    private void uploadData() {
        if (!NetworkUtils.isNetworkConnected(this)) {
            Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
            return;
        }

        List<Item> test = App.get().getDB().itemDao().getAllItems2();
        List<Item> allItems = App.get().getDB().itemDao().getAllItems();
        if (allItems.isEmpty()) {
            Toast.makeText(getApplicationContext(), getString(R.string.no_data_to_upload), Toast.LENGTH_LONG).show();
            return;
        }

        showProgressBar();

        upload();
    }

    private void upload() {
        isUploadItems = false;
        isUploadInventories = false;

        new Thread(() -> {
            List<Inventory> allInventory = App.get().getDB().inventoryDao().getAllInventories();
            List<Item> allItems = App.get().getDB().itemDao().getAllItems();

            runOnUiThread(() -> {
                if (!allInventory.isEmpty()) {
                    uploadInventory();
                }
                if (!allItems.isEmpty()) {
                    uploadItems();
                } else {
                    hideProgressBar();
                    Toast.makeText(getApplicationContext(), getString(R.string.no_data_to_upload), Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    private  void printUploadMessage() {
        if (isUploadItems) {
            Toast.makeText(getApplicationContext(), getString(R.string.data_uploaded_successfully), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.failed_to_upload_data), Toast.LENGTH_LONG).show();
        }

        hideProgressBar();
    }

    private List getItemsUpload (){
        List<Item> allItems = App.get().getDB().itemDao().getAllItems();
        List <UploadItems> uploadData = new ArrayList<>();
        for (Item item : allItems) {
            UploadItems uploadItem = new UploadItems(item.ItemID,item.getItemBarCode(), item.getItemBarcodeAbb(),
                    item.getItemDesc(), item.getItemSN(), item.getVendorID(),item.getInsurerID(), item.getPurchaseDate(),
                    item.getWarrentyPeriod(), item.getCategoryID(), item.getLocationID(), item.getStatusID(),
                    item.getItemCost(), item.getItemPrice(), item.getPONumber(), item.getItemLifeTime(), item.getItemUsagePeriod(),
                    item.getItemSalvage(), item.getFactor(), item.getItemFirstInventoryDate(), item.getItemLastInventoryDate(),
                    item.getItemQty(), item.getRemark(), item.getOpt1(), item.getOpt2(), item.getOpt3());
            uploadData.add(uploadItem);
        }
        return uploadData;
    }

    private List getInevntoryUpload (){
        List<Inventory> allInventory = App.get().getDB().inventoryDao().getAllInventories();
        List <UploadInventory> uploadData = new ArrayList<>();
        for (Inventory inventory : allInventory) {
            UploadInventory uploadItem = new UploadInventory(inventory.getInventoryID(), inventory.getInventoryDate(), inventory.getUserID(),
                    inventory.getItemID(), inventory.getItemBarcode(), inventory.getRemark(), inventory.getCategoryId(),
                    inventory.getCategoryDesc(), inventory.getStatusID(), inventory.getLocationID(), inventory.getLocationDesc(),
                    inventory.getFullLocationDesc(), inventory.isScanned(), inventory.isMissing(), inventory.isManual(), inventory.isReallocated(),
                    inventory.getOldLocationID(), inventory.getOldLocationDesc(), inventory.getOldFullLocationDesc(), inventory.isStatusUpdated(),
                    inventory.isReallocatedApplied(), inventory.isStatusApplied(), inventory.isMissingApplied(), inventory.IsChecked(),
                    inventory.isRegistered(), inventory.getCreatedBy(), inventory.getCreationDate(), inventory.getModifiedBy(),
                    inventory.getModificationDate(), inventory.getReasonID());
            uploadData.add(uploadItem);
        }
        return uploadData;
    }

    private void uploadItems() {
        new Thread(() -> {
            List<UploadItems> itemsUpload = getItemsUpload();
            runOnUiThread(() -> {
                apiService.uploadAssignedAssetsTag(itemsUpload).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful() && "Success".equals(response.body())) {
                            isUploadItems = true;
                        }
                        checkUploadCompletion();
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Toast.makeText(getApplicationContext(), getString(R.string.internet_disconnected_while_uploading), Toast.LENGTH_LONG).show();
                        hideProgressBar();
                    }
                });
            });
        }).start();
    }

    private void uploadInventory() {
        new Thread(() -> {
            List<UploadInventory> inventoryUpload = getInevntoryUpload();
            runOnUiThread(() -> {
                apiService.uploadInventory(inventoryUpload).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful() && "Success".equals(response.body())) {
                            isUploadInventories = true;
                        }
                        checkUploadCompletion();
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Toast.makeText(getApplicationContext(), getString(R.string.internet_disconnected_while_uploading), Toast.LENGTH_LONG).show();
                        hideProgressBar();
                    }
                });
            });
        }).start();
    }

    private void checkUploadCompletion() {
        if (isUploadItems || isUploadInventories) {
            printUploadMessage();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_download) {
            testConnection(this::downloadData);
        }
        else if (id == R.id.nav_upload) {
            uploadData();
        }
        else if (id == R.id.nav_delete) {
            showPasswordCard();
        }
        else if (id == R.id.nav_bluetooth) {
            Intent intent = new Intent(this, BluetoothConnectionActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_server_config) {
            Intent intent = new Intent(this, ServerConfigActivity.class);
            startActivity(intent);
        }
//        else if (id == R.id.nav_assign_tags) {
//            Intent intent = new Intent(this, AssignTags.class);
//            startActivity(intent);
//        }
        else if (id == R.id.nav_logout) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    public static class NetworkUtils {
        public static boolean isNetworkConnected(Context context) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
    }
}
