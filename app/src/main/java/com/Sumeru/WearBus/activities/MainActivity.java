package com.Sumeru.WearBus.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.Sumeru.WearBus.R;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkFirstRun() {
        getSharedPreferences("FirstRun", MODE_PRIVATE)
                .edit()
                .putBoolean("First", false)
                .apply();

        Intent intent = new Intent(MainActivity.this, guideSplashActivity.class);
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maininterface);
        
        Button Bus = findViewById(R.id.bus);
        Bus.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Bus.class);
            startActivity(intent);
        });
        
        Button whereTo = findViewById(R.id.where_to);
        whereTo.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, WhereToActivity.class));
        });
        
        Button Settings = findViewById(R.id.settings);
        Settings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        
        Button ads = findViewById(R.id.ads);
        ads.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ad.class);
            startActivity(intent);
        });
        
        Button about = findViewById(R.id.about);
        about.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        });
        
        Button exit = findViewById(R.id.exit);
        exit.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, exit.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
        
        boolean isFirstRunFlag = getSharedPreferences("FirstRun", MODE_PRIVATE)
                .getBoolean("First", true);
        if (isFirstRunFlag) {
            checkFirstRun();
        }
    }
}
