package com.example.addresschoose;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.example.chooselibrary.dialog.AddressSelectorDialog;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AddressSelectorDialog.newInstance()
                .setSelected(null, null, null, null)
                .setSelectorCompleteListener((province, city, county, town, detailStr) -> {
                    Toast.makeText(this, detailStr, Toast.LENGTH_SHORT).show();
                })
                .show(getSupportFragmentManager());
//        selectorDialog.setSelected(null, null, null, null);
    }
}
