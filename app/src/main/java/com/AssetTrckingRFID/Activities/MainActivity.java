package com.AssetTrckingRFID.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.AssetTrckingRFID.App;
import com.AssetTrckingRFID.R;
import com.AssetTrckingRFID.Tables.Users;


public class MainActivity extends AppCompatActivity {
    private EditText usernameEditText, passwordEditText;
    private TextView linkTextView;
    private Button loginButton;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
    }

    private void initViews() {
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        linkTextView = findViewById(R.id.linkTextView);

        linkTextView.setOnClickListener(v -> {
            startActivity(new Intent(this, ServerConfigActivity.class));
        });

        passwordEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                   doLogin();
                    return true;
                }
                else {
                    return false;
                }
            }
        });
        loginButton.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (validateUser(username, password)) {
            userId = App.get().getDB().usersDao().getUserByUsernameAndPassword(username, password).getUserID();
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
//        Users user = new Users();
//        user.setUserName("admin");
//        user.setPassword("123");
        return user != null;
    }

}