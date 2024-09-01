package com.SmartScan.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.SmartScan.App;
import com.SmartScan.DataBase.AppDataBase;
import com.SmartScan.R;
import com.SmartScan.Tables.Users;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText usernameEditText, passwordEditText;
    private TextView linkTextView;
    private Button loginButton;
    //private AppDataBase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();


    //    db = AppDataBase.getDatabase(this);


    }

    private void initViews() {
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        linkTextView = findViewById(R.id.linkTextView);

        linkTextView.setOnClickListener(v -> {
            startActivity(new Intent(this, ServerConfigActivity.class));
        });

        loginButton.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (validateUser(username, password)) {
          //  Toast.makeText(getApplicationContext(), getString(R.string.login_successfully), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            intent.putExtra("USERNAME", " " + username);
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.login_failed), Toast.LENGTH_LONG).show();
        }
    }

    private boolean validateUser(String username, String password) {
        List<Users> test = App.get().getDB().usersDao().getAllUsers();
        Users user = App.get().getDB().usersDao().getUserByUsernameAndPassword(username, password);
        return user != null;
    }
}