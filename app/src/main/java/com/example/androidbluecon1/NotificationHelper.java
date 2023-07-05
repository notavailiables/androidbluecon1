package com.example.androidbluecon1;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

public class NotificationHelper extends ContextWrapper {
    public static final String channel1ID = "channel1ID";
    public static final String channel1Name = "channel 1 ";
    public static final String channel2ID = "channel2ID";
    public static final String channel2Name = "channel 2 ";

    private NotificationManager mManager;

    public NotificationHelper(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannels();
        }

    }

    public void createChannels(){
        NotificationChannel channel1 = new NotificationChannel(channel1ID,channel1Name, NotificationManager.IMPORTANCE_DEFAULT);
        channel1.enableLights(true);
        channel1.enableVibration(true);
        channel1.setLightColor(R.color.colorPrimary);
        channel1.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        getManager().createNotificationChannel(channel1);

        NotificationChannel channel2 = new NotificationChannel(channel2ID,channel2Name, NotificationManager.IMPORTANCE_DEFAULT);
        channel2.enableLights(true);
        channel2.enableVibration(true);
        channel2.setLightColor(R.color.colorPrimary);
        channel2.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(channel2);

    }

    public NotificationManager getManager() {
        if (mManager == null){
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return mManager;
    }


    public NotificationCompat.Builder getChannel1Notification (String message){

        return new NotificationCompat.Builder(getApplicationContext(), channel1ID)
                .setContentTitle("오늘 챙겨야 할 물건이 있어요!")
                .setContentText("챙겨야 할 물건:" +message)
                .setSmallIcon(R.drawable.app_icon);
    }

    public NotificationCompat.Builder getChannel2Notification(String title, String message){
        return new NotificationCompat.Builder(getApplicationContext(), channel2ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.app_icon);
    }

    public NotificationCompat.Builder getChannelNotification(String name){
        return new NotificationCompat.Builder(getApplicationContext(), channel1ID)
                .setContentTitle("오늘 챙겨야 할 물건이 있어요!")
                .setContentText("챙겨야 할 물건:" +name)
                .setSmallIcon(R.drawable.app_icon);
    }

    public void setChannel1Notification(String message) {
        NotificationCompat.Builder builder = getChannel1Notification(message);
        getManager().notify(1, builder.build());
    }
}