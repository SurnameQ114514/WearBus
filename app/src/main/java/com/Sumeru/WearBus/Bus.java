package com.Sumeru.WearBus;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class Bus extends AppCompatActivity{
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.businterface);
        Button Nearby = findViewById(R.id.nearby);
        Button Search = findViewById(R.id.search);
        Nearby.setOnClickListener(v -> {
            Intent intent=new Intent(Bus.this,Nearby.class);
            startActivity(intent);
        });
        Search.setOnClickListener(v -> {
            Intent intent1 = new Intent(Bus.this, search.class);
            startActivity(intent1);
        });
    }
}
