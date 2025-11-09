package com.AssetTrckingRFID.Activities;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.AssetTrckingRFID.API.APIService;
import com.AssetTrckingRFID.App;
import com.AssetTrckingRFID.API.Retrofit;
import com.AssetTrckingRFID.R;
import com.AssetTrckingRFID.Tables.Users;
import com.AssetTrckingRFID.Utilities.BluetoothHandler;
import com.AssetTrckingRFID.Utilities.LoadingDialog;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServerConfigActivity extends AppCompatActivity {
    private EditText ipEditText, portEditText;
    private boolean isTested = false;
    private APIService apiService;
    private List<Users> users;
    FrameLayout progressBarContainer;
    private LoadingDialog loadingDialog;
    private BluetoothHandler rfidHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_config);

        initViews();
        getServerCredentials();
        registerBluetoothReceiver();
    }

    private void getServerCredentials() {
        ipEditText.setText(App.get().getServerIP());
        portEditText.setText(App.get().getPortNo());
    }

    private void initViews() {
        ipEditText = findViewById(R.id.ipEditText);
        portEditText = findViewById(R.id.portEditText);
        Button testConnectionButton = findViewById(R.id.testConnectionButton);
        Button saveButton = findViewById(R.id.saveButton);
        progressBarContainer = findViewById(R.id.progressBarContainer);

        rfidHandler = new BluetoothHandler();

        loadingDialog = new LoadingDialog(this);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isTested = false;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        ipEditText.addTextChangedListener(textWatcher);
        portEditText.addTextChangedListener(textWatcher);

        testConnectionButton.setOnClickListener(v -> {
            if (ipEditText.getText().toString().isEmpty()) {
                Toast.makeText(this, getString(R.string.ip_validation), Toast.LENGTH_SHORT).show();
                return;
            }
            if (portEditText.getText().toString().isEmpty()) {
                Toast.makeText(this, getString(R.string.port_validation), Toast.LENGTH_SHORT).show();
                return;
            }
            showProgressBar();
            App.get().setServerCredentials(ipEditText.getText().toString(), portEditText.getText().toString());

            try {
                apiService = Retrofit.getRetrofit().create(APIService.class);
            } catch (Exception ex) {
                Toast.makeText(this, R.string.invalid_url_host, Toast.LENGTH_SHORT).show();
                hideProgressBar();
                return;
            }

            testConnection(this::fetchUsers);
        });

        saveButton.setOnClickListener(v -> {
//            if (!isTested) {
//                Toast.makeText(this, getString(R.string.test_connection_first), Toast.LENGTH_SHORT).show();
//                return;
//            }
            if (ipEditText.getText().toString().isEmpty()) {
                Toast.makeText(this, getString(R.string.ip_validation), Toast.LENGTH_SHORT).show();
                isTested = false;
                return;
            }
            if (portEditText.getText().toString().isEmpty()) {
                Toast.makeText(this, getString(R.string.port_validation), Toast.LENGTH_SHORT).show();
                isTested = false;
                return;
            }

            App.get().setServerCredentials(ipEditText.getText().toString(), portEditText.getText().toString());
            Toast.makeText(this, getString(R.string.server_configuration_saved), Toast.LENGTH_SHORT).show();
        });
    }

    private void testConnection(Runnable onSuccess) {
        apiService.testConnection().enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful() && "Success".equals(response.body())) {
                    isTested = true;
                    onSuccess.run();
                    Toast.makeText(getApplicationContext(), getString(R.string.connection_succeeded), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.failed_to_connect_check_your_internet), Toast.LENGTH_LONG).show();
                }
                hideProgressBar();
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(getApplicationContext(), getString(R.string.failed_to_connect_check_your_internet), Toast.LENGTH_LONG).show();
                hideProgressBar();
            }
        });
    }

    private void fetchUsers() {
        apiService.getUsers().enqueue(new Callback<List<Users>>() {
            @Override
            public void onResponse(@NonNull Call<List<Users>> call, @NonNull Response<List<Users>> response) {
                if (response.isSuccessful()) {
                    showProgressBar();
                    users = response.body();
                    insertUsers();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.failed_to_connect_check_your_internet), Toast.LENGTH_LONG).show();
                }
                hideProgressBar();
            }

            @Override
            public void onFailure(@NonNull Call<List<Users>> call, @NonNull Throwable t) {
                Toast.makeText(getApplicationContext(), getString(R.string.failed_to_connect_check_your_internet), Toast.LENGTH_LONG).show();
                hideProgressBar();
            }
        });
    }

    private void insertUsers() {

        if (users != null && !users.isEmpty()) {
            App.get().getDB().usersDao().deleteAll();
            App.get().getDB().usersDao().insertAll(users);
        }
    }

    private void showProgressBar() {
        loadingDialog.startLoadingDialog();
    }

    private void hideProgressBar() {
        loadingDialog.dismissDialog();
    }

    private void registerBluetoothReceiver() {
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothStateReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(bluetoothStateReceiver);
        } catch (IllegalArgumentException ignored) {}
        super.onDestroy();
    }

    private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Toast.makeText(context, getString(R.string.bluetoth_off_disconnect), Toast.LENGTH_SHORT).show();
                        rfidHandler.closeAndResetConnection();
                        break;
                }
            }
        }
    };
}