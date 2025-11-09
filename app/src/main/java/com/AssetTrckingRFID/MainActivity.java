package com.AssetTrckingRFID;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.AssetTrckingRFID.Activities.HomeActivity;
import com.AssetTrckingRFID.Activities.ServerConfigActivity;
import com.AssetTrckingRFID.Tables.Users;


public class MainActivity extends AppCompatActivity {
    private EditText usernameEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
    }

    private void initViews() {
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button loginButton = findViewById(R.id.loginButton);
        TextView linkTextView = findViewById(R.id.linkTextView);

        linkTextView.setOnClickListener(v -> startActivity(new Intent(this, ServerConfigActivity.class)));

        passwordEditText.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
               doLogin();
                return true;
            }
            else {
                return false;
            }
        });
        loginButton.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (validateUser(username, password)) {
            int userId = App.get().getDB().usersDao().getUserByUsernameAndPassword(username, password).getUserID();
//            userId = 1;
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            intent.putExtra("USERNAME", " " + username);
//            intent.putExtra("USERNAME", " " + "admin");
            intent.putExtra("USERID", userId);
            startActivity(intent);
       //     finish();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.login_failed), Toast.LENGTH_LONG).show();
        }
    }

    private boolean validateUser(String username, String password) {
        Users user = App.get().getDB().usersDao().getUserByUsernameAndPassword(username, password);
        return user != null;
    }

}