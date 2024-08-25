package com.example.budgettracker;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;


import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;


public class MainActivity extends AppCompatActivity {
    BottomNavigationView nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nav = findViewById(R.id.nav);


        loadFragment(new homeFragment());
        nav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.home) {
                    loadFragment(new homeFragment());
                } else if (id == R.id.setting) {
                    loadFragment(new SettingFragment());
                } else if (id == R.id.profile) {
                    loadFragment(new profileFragment());
                }
                return true;
            }

        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.frame, fragment).commit();

    }
}