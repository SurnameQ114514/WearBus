package com.Sumeru.WearBus;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity{
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maininterface);
        Button Bus = findViewById(R.id.bus);
        Bus.setOnClickListener(v -> {
            Intent intent=new Intent(MainActivity.this,Bus.class);
            startActivity(intent);
        });
        Button Settings = findViewById(R.id.settings);
        Settings.setOnClickListener(v -> {
            Intent intent=new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(intent);
        });
    }
}