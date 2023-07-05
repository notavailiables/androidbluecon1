package com.example.androidbluecon1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class instructions extends MainActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instructions);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        inputPane.setVisibility(View.GONE);
        search_on.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);
        textView2.setVisibility(View.VISIBLE);
        textView3.setVisibility(View.VISIBLE);
        first_connect.setVisibility(View.GONE);
        button1.setVisibility(View.VISIBLE);
        button2.setVisibility(View.VISIBLE);
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(intent);
    }
}
