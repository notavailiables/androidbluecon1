package com.example.androidbluecon1;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

public class Manual_registration extends MainActivity {
    Button save, refresh;
    EditText name, amount;
    ArrayAdapter arrayAdapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle readdInstanceState) {
        super.onCreate(readdInstanceState);
        setContentView(R.layout.manual_regis);
        final DatabaseHelper helper = new DatabaseHelper(this);
        final ArrayList array_list = helper.getAllCotacts();
        name = findViewById(R.id.name);
        amount = findViewById(R.id.amount);
        listView = findViewById(R.id.listView);
        arrayAdapter = new ArrayAdapter(Manual_registration.this, android.R.layout.simple_list_item_1, array_list);
        listView.setAdapter(arrayAdapter);

        findViewById(R.id.Delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (helper.delete()) {
                    Toast.makeText(Manual_registration.this, "삭제되었습니다.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(Manual_registration.this, "오류가 발생하였습니다.", Toast.LENGTH_LONG).show();
                }
            }
        });

        findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                array_list.clear();
                array_list.addAll(helper.getAllCotacts());
                arrayAdapter.notifyDataSetChanged();
                listView.invalidateViews();
                listView.refreshDrawableState();
            }
        });
        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!name.getText().toString().isEmpty() && !amount.getText().toString().isEmpty()) {
                    if (helper.insert(name.getText().toString(), amount.getText().toString())) {
                        Toast.makeText(Manual_registration.this, "등록되었습니다.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(Manual_registration.this, "오류가 발생하였습니다.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    name.setError("물품을 입력하세요!");
                    amount.setError("수량을 입력하세요!");
                }
            }
        });
    }

}
