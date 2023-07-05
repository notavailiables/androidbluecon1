package com.example.androidbluecon1;

import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AlertDialog;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.util.Calendar;

public class Alert_activity extends MainActivity implements TimePickerDialog.OnTimeSetListener{
    private TextView mTextView;

    private EditText editTextMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_layout);

        editTextMessage = findViewById(R.id.edittext_message);
        mTextView =  findViewById(R.id.textView);

        Button button = (Button) findViewById(R.id.button_timepicker);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(), "time picker");
            }

        });

        ImageButton newbutton = findViewById(R.id.back_button);
        newbutton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });


        Button buttonCancelAlarm = findViewById(R.id.button_cancel);
        buttonCancelAlarm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View v) {
                cancelAlarm();
            }
        });
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);

        updateTimeText(c);
        startAlarm(c);
    }

    private void updateTimeText(Calendar c){
        String timeText = "다음 시간에 알람을 맞췄어요. \n";
        timeText += DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime());

        mTextView.setText(timeText);
    }

    private void startAlarm(Calendar c){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), AlertReceiver.class);
        intent.putExtra("name", editTextMessage.getText().toString());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, intent, PendingIntent.FLAG_MUTABLE);

        if(c.before((Calendar.getInstance()))){
            c.add(Calendar.DATE, 1);
        }
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
        //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), 1*60*1000 ,  pendingIntent);

    }

    private void cancelAlarm(){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, intent, PendingIntent.FLAG_MUTABLE);

        alarmManager.cancel(pendingIntent);
        mTextView.setText("Alarm canceled");
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