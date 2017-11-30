package com.example.mikew.rsr;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity {
    private static int splach_time_out = 1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run(){
            Intent home_intent = new Intent(MainActivity.this,HomeActivity.class);
            startActivity(home_intent);
            finish();
            }
        },splach_time_out);
    }
}
