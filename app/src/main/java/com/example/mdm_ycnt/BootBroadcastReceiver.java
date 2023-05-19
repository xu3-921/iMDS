package com.example.mdm_ycnt;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(service);

                } else {
                    context.startService(service);

                }
            }

            /*if(bootbroadcastopenapp){
                //Intent mainActivityIntent = new Intent(context, MainActivity.class);  // 要启动的Activity
                //1.如果自启动APP，参数为需要自动启动的应用包名
                String package_name="com.example.icast_front_end_app";
                PackageManager packageManager = context.getPackageManager();
                intent = packageManager.getLaunchIntentForPackage(package_name);
                //下面这句话必须加上才能开机自动运行app的界面
                //mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //2.如果自启动Activity
                //context.startActivity(mainActivityIntent);
                //3.如果自启动服务
                //context.startService(mainActivityIntent);
                //intent = new Intent(context, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                isbootbroadcasttimes=1;
                context.startActivity(intent);

            }*/

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
