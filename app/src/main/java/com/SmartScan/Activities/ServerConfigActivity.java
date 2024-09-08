package com.SmartScan.Activities;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.SmartScan.API.APIService;
import com.SmartScan.App;
import com.SmartScan.API.Retrofit;

import com.SmartScan.Server.ServerConfig;
import com.SmartScan.R;
import com.SmartScan.Tables.Users;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ServerConfigActivity extends AppCompatActivity {
    private EditText ipEditText, portEditText;
    private Button testConnectionButton, saveButton;
    private boolean isTested = false;
    private APIService apiService;
    private ServerConfig config = new ServerConfig();
    private SharedPreferences sharedPreferences;
    private List<Users> users;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_config);

        initViews();
        getServerCredentials();

    }

    private void getServerCredentials() {
        ipEditText.setText(App.get().getServerIP());
        portEditText.setText(App.get().getPortNo());
    }

    private void initViews() {
        ipEditText = findViewById(R.id.ipEditText);
        portEditText = findViewById(R.id.portEditText);
        testConnectionButton = findViewById(R.id.testConnectionButton);
        saveButton = findViewById(R.id.saveButton);
        progressBar = findViewById(R.id.progressBar);

        testConnectionButton.setOnClickListener(v -> {
            showProgressBar();
            App.get().setServerCredentials(ipEditText.getText().toString(),portEditText.getText().toString());

            try
            {
                apiService = Retrofit.getRetrofit().create(APIService.class);

            }
            catch (Exception ex)
            {
                Toast.makeText(this, R.string.invalid_url_host,Toast.LENGTH_SHORT).show();
                hideProgressBar();
                return;
            }

            testConnection(this::fetchUsers);
        });

        saveButton.setOnClickListener(v -> {
            if (!isTested) {
                Toast.makeText(this, getString(R.string.test_connection_first), Toast.LENGTH_SHORT).show();
                return;
            }
            App.get().setServerCredentials(ipEditText.getText().toString(),portEditText.getText().toString());
            Toast.makeText(this, getString(R.string.server_configuration_saved), Toast.LENGTH_SHORT).show();
        });
    }

    private void testConnection(Runnable onSuccess) {
        apiService.testConnection().enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
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
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(getApplicationContext(), getString(R.string.failed_to_connect_check_your_internet), Toast.LENGTH_LONG).show();
                hideProgressBar();
            }
        });
    }

    private void fetchUsers() {
        apiService.getUsers().enqueue(new Callback<List<Users>>() {
            @Override
            public void onResponse(Call<List<Users>> call, Response<List<Users>> response) {
                if (response.isSuccessful()) {
                    showProgressBar();
                    users = response.body();
                    insertUsers();
                    hideProgressBar();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.failed_to_connect_check_your_internet), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Users>> call, Throwable t) {
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

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

}