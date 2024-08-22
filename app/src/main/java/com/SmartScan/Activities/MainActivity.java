package com.SmartScan.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.SmartScan.R;

public class MainActivity extends AppCompatActivity {
    private EditText usernameEditText, passwordEditText;
    private TextView linkTextView;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        linkTextView = findViewById(R.id.linkTextView);

        linkTextView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ServerConfigActivity.class);
            startActivity(intent);
        });
    }
}