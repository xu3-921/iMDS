package com.example.mdm_ycnt;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;

import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class BootBroadcastReceiver extends BroadcastReceiver {
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(ACTION)) {
            //開啟ResidentService
            if(!(isServiceRunning(context,"com.example.mdm_ycnt.ResidentService"))){

                Intent service = new Intent(context,ResidentService.class);
                service.putExtra("isStartFromBootBroadcast", true);

                context.startForegroundService(service);

            }

        }

    }

    public static boolean isServiceRunning(Context mContext, String className) {

        boolean isRunning = false;

        ActivityManager activityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                .getRunningServices(30);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

}
