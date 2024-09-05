package com.SmartScan.Activities;


import android.content.Context;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.SmartScan.ApiClasses.UserResponse;
import com.SmartScan.API.APIService;
import com.SmartScan.App;
import com.SmartScan.API.Retrofit;
import com.SmartScan.DataBase.AppDataBase;
import com.SmartScan.Server.ServerConfig;
import com.SmartScan.R;
import com.SmartScan.Tables.Users;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServerConfigActivity extends AppCompatActivity {
    private EditText ipEditText, portEditText;
    private Button testConnectionButton, saveButton;
    private boolean isTested = false;
    private APIService apiService;
    private ServerConfig config = new ServerConfig();
    private SharedPreferences sharedPreferences;
    private List<Users> users;
    private String ip;
    private int port;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_config);

        initViews();
        getServerCredentials();
        

//        sharedPreferences = getSharedPreferences("ServerConfig", Context.MODE_PRIVATE);
//
//        ip = sharedPreferences.getString("ip", "");
//        port = sharedPreferences.getInt("port", -1);
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("http://" + ip + ":" + port + "/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();



       // loadServerConfig();


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
           // App.get().getDB().usersDao().resetPrimaryKey();
            App.get().getDB().usersDao().insertAll(users);
//            for (UserResponse userResponse : users) {
//                Users user = new Users();
//                user.setUsername(userResponse.getUserName());
//                user.setPassword(userResponse.getPassword());
//                db.usersDao().insert(user);
//            }
        }
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void loadServerConfig() {
        String ip = sharedPreferences.getString("ip", "");
        int port = sharedPreferences.getInt("port", -1);
        if (!ip.isEmpty() && port != -1) {
            ipEditText.setText(ip);
            portEditText.setText(String.valueOf(port));
            config.setIp(ip);
            config.setPort(port);
        }
    }

    private void saveServerConfig(ServerConfig config) {
        if (config.getIp().isEmpty() || config.getPort() == -1) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (config != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("ip", config.getIp());
            editor.putInt("port", config.getPort());
            editor.apply();
            Toast.makeText(this, "Server configuration saved", Toast.LENGTH_SHORT).show();
        } else{
            Toast.makeText(this, "Failed to save server configuration", Toast.LENGTH_SHORT).show();
        }
    }
}