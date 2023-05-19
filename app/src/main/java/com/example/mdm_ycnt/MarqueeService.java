package com.example.mdm_ycnt;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextPaint;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.mdm_ycnt.FloatingWindow.FloatingLayout;
import com.example.mdm_ycnt.FloatingWindow.callback.FloatingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;


public class MarqueeService extends Service {

    public FloatingLayout floatingLayout = null;

    private FloatingListener floatingListener = new FloatingListener() {
        @Override
        public void onCreateListener(View view) {

            Button btn = view.findViewById(R.id.btn_close);
            FrameLayout floating_xml = view.findViewById(R.id.root_container);
            TextView textview_marquee = view.findViewById(R.id.textview_marquee);

            SharedPreferences pref = getSharedPreferences("mdm_ycnt", MODE_PRIVATE);

            //String silentBroadcastType = pref.getString("silentBroadcastType","text");
            String silentBroadcastInfo = pref.getString("silentBroadcastInfo",null);
            int silentBroadcastDurationTime = pref.getInt("silentBroadcastDurationTime",-100);

            //設定TextView字體大小
            textview_marquee.setTextSize(TypedValue.COMPLEX_UNIT_PX,75);

            //TextView字體大小(px)
            float textSize = textview_marquee.getTextSize();

            //螢幕寬度（px）
            DisplayMetrics displayMetrics = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
            int getDeviceWidthPixels = displayMetrics.widthPixels;

            //TextView內容
            textview_marquee.setText(silentBroadcastInfo);

            //TextView寬度 (px)
            String dt = textview_marquee.getText().toString();
            Rect bounds = new Rect();
            TextPaint paint = textview_marquee.getPaint();
            paint.getTextBounds(dt, 0, dt.length(), bounds);
            int mTextWidth = bounds.width();

            //設定跑馬燈layout寬高
            floating_xml.getLayoutParams().width = getDeviceWidthPixels;
            floating_xml.getLayoutParams().height = Math.round(textSize) + 40;
            textview_marquee.getLayoutParams().width = (mTextWidth * 2) + getDeviceWidthPixels;

            StartMarquee(getDeviceWidthPixels,mTextWidth,textview_marquee);

            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        Log.e("testGetTime","startSleep");

                        Thread.sleep(silentBroadcastDurationTime * 1000);

                        Log.e("testGetTime","end");

                        stopSelf();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        stopSelf();
                    }

                }
            }).start();



            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //結束跑馬燈 刪除正在播放ID
                    //SharedPreferences pref = getSharedPreferences("icastapp", MODE_PRIVATE);
                    //String marquee_play = getSharedPreferences("icastapp", MODE_PRIVATE).getString("marquee_play", "NotPlaying");
                    //get_silent_broadcast_device(marquee_play);

                    //pref.edit().putString("marquee_play", "NotPlaying").commit();
                    //MarqueeService.floatingLayout.destroy();

                    stopSelf();


                }
            });
        }

        @Override
        public void onCloseListener() {

            stopSelf();
        }
    };

    public static final String CHANNEL_ID_STRING = "service_chatroom";

    private int theRoll = 0;
    private Handler handler = null;
    private Runnable runnable = null;


    @Override
    public void onCreate() {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel mChannel = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(CHANNEL_ID_STRING, getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(mChannel);
            Notification notification = new Notification.Builder(getApplicationContext(), CHANNEL_ID_STRING).build();
            startForeground(1, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("testGetTime","start");
        showFloating();
        //Log.e("testGet","showFloatingStart");

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

        if(floatingLayout != null){
            floatingLayout.destroy();
            floatingLayout = null;
        }

        if(floatingListener != null){
            floatingListener = null;
        }

        if(handler != null){
            handler.removeCallbacks(runnable);
            handler = null;
        }

        //Log.e("tttt","onDestroy");
    }

    private void showFloating() {

        floatingLayout = new FloatingLayout(this, R.layout.floatingwindow_marquee);
        floatingLayout.setFloatingListener(floatingListener);
        floatingLayout.create();

    }

    private void StartMarquee(int getDeviceWidthPixels, int mTextWidth, TextView textview_marquee){

        runnable = new Runnable() {
            @Override
            public void run() {
                if (theRoll < 0){
                    theRoll = getDeviceWidthPixels + mTextWidth;
                }
                theRoll = theRoll-2;
                //通過設置下面的四個參數可以起到控制滾動方向的作用
                textview_marquee.setPadding(theRoll,0,0,0);
                handler.postDelayed(this,10);//調整速度
            }
        };
        handler = new Handler();
        handler.postDelayed(runnable,0);
    }


    /*private void get_silent_broadcast_device(String palying_style){

        new Thread(new Runnable() {
            @Override
            public void run() {

                OutputStream os = null;
                InputStream is = null;
                HttpURLConnection conn = null;


                String DeviceId= getSharedPreferences("icastapp3", MODE_PRIVATE).getString("icastdeviceid", "");

                //String SystemModel =getSystemModel();
                byte[] DeviceIddata = new byte[0];
                try {
                    DeviceIddata = DeviceId.getBytes("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String DeviceId_base64 = Base64.encodeToString(DeviceIddata, Base64.DEFAULT);

                try {

                    String url_str = getSharedPreferences("icastapp2", MODE_PRIVATE)
                            .getString("icastserverip", getResources().getString(R.string.icast_default_url));

                    String phpurl=url_str+get_silent_broadcast_device_url;

                    URL url = new URL(phpurl);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", DeviceId_base64);

                    String message = jsonObject.toString();

                    conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setFixedLengthStreamingMode(message.getBytes().length);

                    //make some HTTP header nicety
                    conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                    conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");

                    //open
                    conn.connect();

                    //setup send
                    os = new BufferedOutputStream(conn.getOutputStream());
                    os.write(message.getBytes());

                    //clean up
                    os.flush();

                    if (HttpURLConnection.HTTP_OK == conn.getResponseCode()) {
                        //do somehting with response
                        is = conn.getInputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                        String line = null;
                        StringBuilder sb = new StringBuilder();

                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        br.close();

                        JSONObject resultjson = new JSONObject(sb.toString());
                        String styles = resultjson.getString("ad_icastapp_device_apply_style");
                        JSONArray array = new JSONArray(styles);

                        JSONObject object = array.getJSONObject(0);
                        styles = object.getString("ad_icastapp_device_apply_style");

                        String[] styles_arr=styles.split("/");

                        String new_styles="null";
                        boolean first=false;

                        for(int j=0;j<styles_arr.length;j++){
                            if(!styles_arr[j].equals(palying_style)){
                                if(!first){
                                    new_styles=styles_arr[j];
                                    first=true;
                                }else{
                                    new_styles=new_styles+"/"+styles_arr[j];
                                }
                            }
                        }

                        update_silent_broadcast_device(new_styles);

                    }
                    //String contentAsString = readIt(is,len);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                } finally {
                    //clean up
                    try {
                        if(os!=null) {
                            os.close();
                        }
                        if(is!=null){
                            is.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(conn!=null) {
                        conn.disconnect();
                    }
                }
            }
        }).start();

    }
*/
}
