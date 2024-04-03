package com.example.mdm_ycnt;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class WatchdogService extends Service {

    @Override
    public void onCreate() {

        // 獲取NotificationManager
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // 創建一個新的通知頻道
        NotificationChannel mChannel;
        String CHANNEL_ID_STRING = "com.example.mdm_ycnt.WatchdogService"; // 確保這是一個唯一的ID
        mChannel = new NotificationChannel(CHANNEL_ID_STRING, getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(mChannel);

        // 創建通知
        Notification.Builder builder = new Notification.Builder(WatchdogService.this, CHANNEL_ID_STRING);
        builder.setContentTitle("iMDS正在執行中"); // 設置通知的標題
        builder.setSmallIcon(R.drawable.imds_icon); // 設置通知的小圖標
        // 其他設置（可選）...

        // 建立通知
        Notification notification = builder.build();

        // 啟動前台服務
        startForeground(1027, notification);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //啟動服務
        if(!(UniversalFunction.isServiceRunning(
                WatchdogService.this,"com.example.mdm_ycnt.ResidentService"))){

            Intent service = new Intent(WatchdogService.this,ResidentService.class);
            WatchdogService.this.startForegroundService(service);

        }

        stopSelf();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}