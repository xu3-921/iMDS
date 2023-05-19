package com.example.mdm_ycnt;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.WINDOW_SERVICE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import org.json.JSONException;
import org.json.JSONObject;

public class Function_set_cmd {

    public static void F_set_marquee(String value, Context mContext){

        //Log.e("testGetAA","value:"+value);

        try {
            JSONObject getJson = new JSONObject(value);

            String textVal = getJson.getString("text");
            textVal = textVal.replaceAll("\\R", "  ");

            int durationTime = getJson.getInt("time");

            /*Log.e("testGet",textVal);
            Log.e("testGet", String.valueOf(durationTime));*/


            SharedPreferences pref = mContext.getSharedPreferences("mdm_ycnt", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();

            editor.putString("silentBroadcastType","text");
            editor.putString("silentBroadcastInfo",textVal);
            editor.putInt("silentBroadcastDurationTime",durationTime);

            editor.apply();


            Intent service = new Intent(mContext,MarqueeService.class);
            if((UniversalFunction.isServiceRunning(mContext,"com.example.mdm_ycnt.MarqueeService"))){

                mContext.stopService(service);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mContext.startForegroundService(service);
            } else {
                mContext.startService(service);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public static void F_set_marquee_stop(Context mContext){

        Intent service = new Intent(mContext,MarqueeService.class);
        if((UniversalFunction.isServiceRunning(mContext,"com.example.mdm_ycnt.MarqueeService"))){

            mContext.stopService(service);
        }

    }

    public static void F_set_open_web(String getUrl, Context mContext){
        Uri uri = Uri.parse(getUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

}
