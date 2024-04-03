package com.example.mdm_ycnt;


import static com.example.mdm_ycnt.Function_Get_device_state.F_getAvailableMemory;
import static com.example.mdm_ycnt.Function_Get_device_state.F_getAvailableRAM;
import static com.example.mdm_ycnt.Function_Get_device_state.F_getDHCPstatus;
import static com.example.mdm_ycnt.Function_Get_device_state.F_getLocalIpAddress;
import static com.example.mdm_ycnt.Function_Get_device_state.F_getTotalMemory;
import static com.example.mdm_ycnt.Function_Get_device_state.F_getTotalRAM;
import static com.example.mdm_ycnt.Function_Get_device_state.F_get_TimeSwitchPower_state;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import com.example.mdm_ycnt.aidlBindHelper.AsyncAidlTool;
import com.example.mdm_ycnt.aidlBindHelper.ServiceBinderHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class ResidentService extends Service {

    public final String CHANNEL_ID_STRING = "service_chatroom";
//    private boolean isFirstTime = true;

//    private final String HttpPost_update_info_and_get_cmd_url = "php/app_php/mdm_update_data_and_get_cmd.php";
//    private final String HttpPost_update_info_url = "php/app_php/mdm_update_data.php";
    private final String UPDATE_DEVICE_INFO_AND_GET_CMD_URL = "php/app_php/mdm_update_data_and_get_cmd.php";
    private final String UPDATE_DEVICE_INFO_URL = "php/app_php/mdm_update_data.php";

    private Control_Receiver receiver;

//    private TimerTask task;
//    private Timer timer;

    private TimerTask shareScreenTask;
    private Timer shareScreenTimer;

    private String start_date = "no_val";
    private String end_date = "no_val";

    private final Function_set_cmd function_set_cmd_instance = new Function_set_cmd();
    private final UniversalFunction universalFunction_instance = new UniversalFunction();
    private final Function_Get_device_state getDeviceStateInstance = new Function_Get_device_state();

    private String nowTime = null;

    private final String RANDOM_ID = universalFunction_instance.generateRandomString(10); // 隨機Id

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private boolean isSharingDeviceScreen = false;


    @Override
    public void onCreate() {

//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        NotificationChannel mChannel = null;
//
//        mChannel = new NotificationChannel(CHANNEL_ID_STRING, getString(R.string.app_name),
//                    NotificationManager.IMPORTANCE_LOW);
//        notificationManager.createNotificationChannel(mChannel);
//        Notification notification = new Notification.Builder(ResidentService.this, CHANNEL_ID_STRING).build();
//        startForeground(1025, notification);

        // 獲取NotificationManager
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // 創建一個新的通知頻道
        NotificationChannel mChannel;
        String CHANNEL_ID_STRING = "com.example.mdm_ycnt.ResidentService"; // 確保這是一個唯一的ID
        mChannel = new NotificationChannel(CHANNEL_ID_STRING, getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(mChannel);

        // 創建通知
        Notification.Builder builder = new Notification.Builder(ResidentService.this, CHANNEL_ID_STRING);
        builder.setContentTitle("iMDS正在執行中"); // 設置通知的標題
        // builder.setContentText("Your content text"); // 設置通知的內容
        builder.setSmallIcon(R.drawable.imds_icon); // 設置通知的小圖標
        // 其他設置（可選）...

        // 建立通知
        Notification notification = builder.build();

        // 啟動前台服務
        startForeground(1025, notification);


        //註冊廣播
        receiver = new Control_Receiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("get_ycnt_mdm_cmd");
        this.registerReceiver(receiver,filter);


        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(ResidentService.this);

        // 確認緊急回報按鈕是否出現
        boolean isEmergencyAlertBtnNowOn = (UniversalFunction.isServiceRunning(
                ResidentService.this,"com.example.mdm_ycnt.EmergencyAlertService"));


        boolean isSettingEmergencyAlertOn =
                sharedPreferences.getBoolean("setting_emergency_alert_btn", true);

        boolean isEmergencyAlertBtnOn = getSharedPreferences("mdm_ycnt", MODE_PRIVATE)
                .getBoolean("isEmergencyAlertBtnOn", false);


        if(!isEmergencyAlertBtnNowOn && (isSettingEmergencyAlertOn && isEmergencyAlertBtnOn)) {

            // 啟動緊急廣播按鈕
            Intent intent = new Intent(this, EmergencyAlertService.class);
            intent.putExtra("action", "create");
            this.startService(intent);

        }

        Singleton.getInstance().setSingletonData(new ArrayList<Integer>());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // 透過開機廣播啟動，重製設備的一些狀態
        boolean isStartFromBootBroadcast = false;

        if(intent != null){
            isStartFromBootBroadcast = intent.getBooleanExtra("isStartFromBootBroadcast", false);
        }

        try {

            boolean finalIsStartFromBootBroadcast = isStartFromBootBroadcast;

            universalFunction_instance.isDeviceRegistered(this, new UniversalFunction.RegistrationCallback() {
                @Override
                public void onResult(boolean isRegistered) {

                    if (isRegistered) {

                        startRegisteredFunction(finalIsStartFromBootBroadcast);

                    } else {
                        SharedPreferences pref = getSharedPreferences("mdm_ycnt", MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.remove("isDeviceSignUp");
                        editor.apply();

                        F_stop_service();
                    }
                }
            });
        }
        catch (JSONException e) {
            e.printStackTrace();
            F_stop_service();
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

//        cancelTimer();
        unregisterReceiver(receiver);

    }

    private void startRegisteredFunction(boolean isStartFromBootBroadcast){

        String G_control_app_package_name = getSharedPreferences("mdm_ycnt", MODE_PRIVATE)
                .getString("control_app_package_name", "no_val");

        if(!G_control_app_package_name.equals("no_val")){

            if(isStartFromBootBroadcast){
                resetStateToDefault(G_control_app_package_name);
            }

            F_sendCMD(G_control_app_package_name,"{\"get-TotalBootTime\":\"NaN\"}", ResidentService.this);
        }

        keepAliveiMdsSentinelService();

        Map<String, Supplier<Object>> deviceInfoMap = new HashMap<>();
        deviceInfoMap.put("ad_mdm_device_ip", Function_Get_device_state::F_getLocalIpAddress);

        if (Build.VERSION.SDK_INT < 33) { // Build.VERSION_CODES.TIRAMISU
            deviceInfoMap.put("ad_mdm_device_eth0", Function_Get_device_state::F_getLocalMacAddress_eth0);
            deviceInfoMap.put("ad_mdm_device_wlan0", Function_Get_device_state::F_getLocalMacAddress_wlan0);
        }

        deviceInfoMap.put("ad_mdm_device_phonemanufacturers", Function_Get_device_state::F_getPhoneManufacturers);
        deviceInfoMap.put("ad_mdm_device_phonemodel", Function_Get_device_state::F_getPhoneModel);
        deviceInfoMap.put("ad_mdm_device_androidversion", Function_Get_device_state::F_getAndroidVersion);
        deviceInfoMap.put("ad_mdm_device_totalram", () -> F_getTotalRAM(ResidentService.this));
        deviceInfoMap.put("ad_mdm_device_totalMemory", () -> F_getTotalMemory(ResidentService.this));
        deviceInfoMap.put("ad_mdm_device_app_version", () -> BuildConfig.VERSION_NAME);
        deviceInfoMap.put("ad_mdm_device_control_app_version",
                () -> getDeviceStateInstance.getControlAppVersion(ResidentService.this));

        JSONObject getDeviceInfoJson = new JSONObject();

        for (Map.Entry<String, Supplier<Object>> entry : deviceInfoMap.entrySet()) {

            String key = entry.getKey();

            Supplier<Object> supplier = entry.getValue();
            try {
                getDeviceInfoJson.put(key, supplier.get());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Thread firstToDoThread = new Thread(new Runnable() {
            @Override
            public void run() {

                String url_domain = UniversalFunction.GetServerIP(ResidentService.this);
                String phpUrl = url_domain + UPDATE_DEVICE_INFO_URL;

                JSONObject postData = new JSONObject();

                try {
                    postData.put("id",UniversalFunction.F_getDeviceId(ResidentService.this));
                    postData.put("update",getDeviceInfoJson);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONObject getJson = universalFunction_instance.httpPostData(postData, phpUrl);

            }
        });

        firstToDoThread.start();
        try {
            firstToDoThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        scheduleNext(0);

//        task = new TimerTask (){
//
//            public void run() {
//
//                JSONObject getDeviceInfoJson = new JSONObject();
//                try {
//
//                    getDeviceInfoJson.put("ad_mdm_device_BootTime",Function_Get_device_state.F_get_BootTime());
//                    getDeviceInfoJson.put("ad_mdm_device_ip", F_getLocalIpAddress());//IP
//                    getDeviceInfoJson.put("ad_mdm_device_availableram",F_getAvailableRAM(getApplicationContext()));//未使用的RAM
//                    getDeviceInfoJson.put("ad_mdm_device_availableMemory",F_getAvailableMemory(getApplicationContext()));//未使用的內存
//                    getDeviceInfoJson.put("ad_mdm_device_DHCP",F_getDHCPstatus());
//                    getDeviceInfoJson.put("ad_mdm_device_app_time_switch_power_state",F_get_TimeSwitchPower_state());
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                //HttpPost_update_info_and_get_cmd(UniversalFunction.F_getDeviceId() ,getDeviceInfoJson,ResidentService.this);
//
//                String url_domain = UniversalFunction.GetServerIP(ResidentService.this);
//                String phpUrl = url_domain + UPDATE_DEVICE_INFO_AND_GET_CMD_URL;
//
//                JSONObject postData = new JSONObject();
//
//                try {
//                    postData.put("id",UniversalFunction.F_getDeviceId());
//                    postData.put("update",getDeviceInfoJson);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                JSONObject getJson = universalFunction_instance.httpPostData(postData, phpUrl);
//                try {
//
//                    String getNowTime;
//
//                    if(getJson != null){
//
//                        processReceivedCommand(getJson);
//                        getNowTime = getJson.getString("now_time");
//
//                    }else{
//
//                        getNowTime = F_get_local_time();
//
//                    }
//
//                    nowTime = getNowTime;
//
//                    //設備定時開關休眠
//                    F_TimeSwitch_Power(getNowTime);
//
//                    logDeviceOpenTime();
//
////                    SharedPreferences pref = getSharedPreferences("mdm_ycnt", MODE_PRIVATE);
////                    SharedPreferences.Editor editor = pref.edit();
////                    editor.remove("deviceOpenTimeJson").apply();
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        };
//
//        timer = new Timer();
//        timer.schedule(task, 0,10 * 1000);

    }


    public void F_sendCMD(String packageName,String cmd, Context mContext){//另一个app的包名

        if(packageName.equals("otherDevicePackageName")){//其他廠牌的電視

//            try {
//
//                JSONObject deviceStateJson = new JSONObject();
//
//                deviceStateJson.put("ad_mdm_device_BootTime",Function_Get_device_state.F_get_BootTime());
//                deviceStateJson.put("ad_mdm_device_volume",Function_Get_device_state.F_get_volume());
//                deviceStateJson.put("ad_mdm_device_deviceName",Function_Get_device_state.F_get_deviceName());
//
//                //Log.e("deviceStateJson", String.valueOf(deviceStateJson));
//
//                HttpPost_update_info(UniversalFunction.F_getDeviceId() ,deviceStateJson,ResidentService.this);
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }

            new Thread(new Runnable() {
                @Override
                public void run() {




                    String url_domain = UniversalFunction.GetServerIP(ResidentService.this);
                    String phpUrl = url_domain + UPDATE_DEVICE_INFO_URL;

                    JSONObject deviceStateJson = new JSONObject();
                    JSONObject postData = new JSONObject();

                    //new
                    Object playing_media_id_list = Singleton.getInstance().getSingletonData();


                    try {
                        deviceStateJson.put("ad_mdm_device_BootTime",Function_Get_device_state.F_get_BootTime());
                        deviceStateJson.put("ad_mdm_device_volume",Function_Get_device_state.F_get_volume(ResidentService.this));
                        deviceStateJson.put("ad_mdm_device_deviceName",Function_Get_device_state.F_get_deviceName(ResidentService.this));

                        //new
                        deviceStateJson.put("playing_media_id_list",playing_media_id_list);

                        postData.put("id",UniversalFunction.F_getDeviceId(ResidentService.this));
                        postData.put("update",deviceStateJson);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    JSONObject getJson = universalFunction_instance.httpPostData(postData, phpUrl);
                }
            }).start();

        }
        else {

            Intent intent = new Intent();
            String className = packageName+".ResidentService"; //另一个app要啟動的组件的全路徑名
            intent.setClassName(packageName, className);

            intent.putExtra("cmd", cmd);
            intent.putExtra("promise_action", "get_ycnt_mdm_cmd");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mContext.startForegroundService(intent);
            } else {
                mContext.startService(intent);
            }

        }

    }

    //接收廣播
    public class Control_Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle bundle = intent.getExtras();

            String command = "null";
            command = bundle.getString("control_return");


//            try {
//
//                JSONObject resultJson = new JSONObject(command);
//
//                HttpPost_update_info(UniversalFunction.F_getDeviceId(), resultJson, ResidentService.this);
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }

            String url_domain = UniversalFunction.GetServerIP(ResidentService.this);
            String phpUrl = url_domain + UPDATE_DEVICE_INFO_URL;

            JSONObject postData = new JSONObject();

            try {
                JSONObject resultJson = new JSONObject(command);

                //new
                Object playing_media_id_list = Singleton.getInstance().getSingletonData();

                //new
                resultJson.put("playing_media_id_list",playing_media_id_list);

                postData.put("id",UniversalFunction.F_getDeviceId(ResidentService.this));
                postData.put("update",resultJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    JSONObject getJson = universalFunction_instance.httpPostData(postData, phpUrl);
                }
            }).start();
        }
    }


//    private void HttpPost_update_info_and_get_cmd(String DeviceId ,JSONObject info_json ,Context context){
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                OutputStream os = null;
//                InputStream is = null;
//                HttpURLConnection conn = null;
//
//                String G_now_time = "no_val";
//
//                try {
//
//                    String url_domain = UniversalFunction.GetServerIP(context);
//                    String phpUrl = url_domain + HttpPost_update_info_and_get_cmd_url;
//
//                    URL url = new URL(phpUrl);
//                    JSONObject jsonObject = new JSONObject();
//
//                    jsonObject.put("id", DeviceId);
//                    jsonObject.put("update", info_json);
//
//                    String message = jsonObject.toString();
//
//                    conn = (HttpURLConnection) url.openConnection();
//                    conn.setReadTimeout(10000);
//                    conn.setConnectTimeout(15000);
//                    conn.setRequestMethod("POST");
//                    conn.setDoInput(true);
//                    conn.setDoOutput(true);
//                    conn.setFixedLengthStreamingMode(message.getBytes().length);
//
//                    //make some HTTP header nicety
//                    conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
//                    conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
//
//                    //open
//                    conn.connect();
//
//                    //setup send
//                    os = new BufferedOutputStream(conn.getOutputStream());
//                    os.write(message.getBytes());
//
//                    //clean up
//                    os.flush();
//
//                    if (HttpURLConnection.HTTP_OK == conn.getResponseCode()) {
//                        //do somehting with response
//                        is = conn.getInputStream();
//                        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
//                        String line = null;
//                        StringBuilder sb = new StringBuilder();
//
//                        while ((line = br.readLine()) != null) {
//                            sb.append(line);
//                        }
//                        br.close();
//
//
//                        JSONObject resultjson = new JSONObject(sb.toString());
//
//                        String G_device_register_enable = resultjson.getString("ad_mdm_device_register_enable");
//
//                        SharedPreferences pref = getSharedPreferences("mdm_ycnt", MODE_PRIVATE);
//                        SharedPreferences.Editor editor = pref.edit();
//
//                        if(G_device_register_enable.equals("F")){
//
//                            editor.remove("isDeviceSignUp");
//                            editor.remove("device_id");
//
//                            F_stop_service();
//
//                        }
//                        else if(G_device_register_enable.equals("T")){
//
//                            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                            SharedPreferences.Editor defaultEditor = defaultSharedPreferences.edit();
//
//                            String G_server_ip = resultjson.getString("server_ip");
//                            String G_control_app_package_name = resultjson.getString("control_app_package_name");
//                            String G_cmd = resultjson.getString("ad_mdm_cmd_content");
//
//                            String G_time_switch_power = resultjson.getString("ad_mdm_device_app_time_switch_power");
//                            String G_time_switch_power_cmd = resultjson.getString("ad_mdm_device_app_time_switch_power_cmd");
//
//                            G_now_time = resultjson.getString("now_time");
//                            nowTime = G_now_time;
//
//                            String can_control_from_ipts = resultjson.getString("ad_mdm_app_control_from_iPTS");
//                            editor.putString("control_from_ipts",can_control_from_ipts);
//
//
//                            String server_ip = getSharedPreferences("mdm_ycnt", MODE_PRIVATE)
//                                                .getString("server_ip", getResources().getString(R.string.mdm_default_url));
//
//                            if(!F_isNull(G_server_ip) && !server_ip.equals(G_server_ip)){
//                                editor.putString("server_ip", G_server_ip);
//
//                            }
//
//                            if(!F_isNull(G_control_app_package_name)){
//
//                                F_tidy_get_cmd(G_control_app_package_name,G_cmd);
//
//                            }
//
//                            //寫入是否定時開關休眠
//                            if(!F_isNull(G_time_switch_power_cmd)){
//
//                                String[] cmd_arr=G_time_switch_power_cmd.split("\\|");
//
//                                if(cmd_arr[0].equals("on")){
//                                    defaultEditor.putBoolean("setting_TimeSwitch_open", true);
//                                }else if(cmd_arr[0].equals("off")){
//                                    defaultEditor.putBoolean("setting_TimeSwitch_open", false);
//                                }
//
//                                if(cmd_arr[1].equals("on")){
//                                    defaultEditor.putBoolean("setting_TimeSwitch_close", true);
//                                }else if(cmd_arr[1].equals("off")){
//                                    defaultEditor.putBoolean("setting_TimeSwitch_close", false);
//                                }
//
//                            }
//
//                            //寫入定時開關機時間
//                            if(F_isNull(G_time_switch_power)){
//                                editor.putString("device_TimeSwitch","no_val");
//                            }
//                            else{
//                                String TimeSwitch_str = getSharedPreferences("mdm_ycnt", MODE_PRIVATE)
//                                                        .getString("device_TimeSwitch", "no_val");
//
//                                if(!TimeSwitch_str.equals(G_time_switch_power)){
//                                    editor.putString("device_TimeSwitch",G_time_switch_power);
//                                    editor.putBoolean("device_TimeSwitch_NewOne",true);
//                                }else {
//                                    editor.putBoolean("device_TimeSwitch_NewOne",false);
//                                }
//
//                            }
//
//                            defaultEditor.apply();
//                        }
//
//                        editor.apply();
//
//                    }
//                    //String contentAsString = readIt(is,len);
//                } catch (IOException | JSONException e) {
//                    e.printStackTrace();
//                } finally {
//
//                    if(G_now_time.equals("no_val")){
//                        G_now_time = F_get_local_time();
//                    }
//                    //設備定時開關休眠
//                    F_TimeSwitch_Power(G_now_time);
//
//                    //clean up
//                    try {
//                        if(os!=null) {
//                            os.close();
//                        }
//                        if(is!=null){
//                            is.close();
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    if(conn!=null) {
//                        conn.disconnect();
//                    }
//                }
//            }
//        }).start();
//
//    }

//    //回傳設備狀態(沒有拿值回來)
//    private void HttpPost_update_info(String DeviceId ,JSONObject info_json ,Context context){
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//                OutputStream os = null;
//                InputStream is = null;
//                HttpURLConnection conn = null;
//
//
//                try {
//                    String url_domain = UniversalFunction.GetServerIP(context);
//                    String phpUrl = url_domain + UPDATE_DEVICE_INFO_URL;
//
//                    URL url = new URL(phpUrl);
//                    JSONObject jsonObject = new JSONObject();
//
//                    jsonObject.put("id", DeviceId);
//                    jsonObject.put("update", info_json);
//
//                    String message = jsonObject.toString();
//
//                    conn = (HttpURLConnection) url.openConnection();
//                    conn.setReadTimeout(10000);
//                    conn.setConnectTimeout(15000);
//                    conn.setRequestMethod("POST");
//                    conn.setDoInput(true);
//                    conn.setDoOutput(true);
//                    conn.setFixedLengthStreamingMode(message.getBytes().length);
//
//                    //make some HTTP header nicety
//                    conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
//                    conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
//
//                    //open
//                    conn.connect();
//
//                    //setup send
//                    os = new BufferedOutputStream(conn.getOutputStream());
//                    os.write(message.getBytes());
//
//                    //clean up
//                    os.flush();
//
//                    if (HttpURLConnection.HTTP_OK == conn.getResponseCode()) {
//                        //do somehting with response
//                        is = conn.getInputStream();
//                        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
//                        String line = null;
//                        StringBuilder sb = new StringBuilder();
//
//                        while ((line = br.readLine()) != null) {
//                            sb.append(line);
//                        }
//                        br.close();
//
//                    }
//                    //String contentAsString = readIt(is,len);
//                } catch (IOException | JSONException e) {
//                    e.printStackTrace();
//                }
//                finally {
//                    //clean up
//                    try {
//                        if(os!=null) {
//                            os.close();
//                        }
//                        if(is!=null){
//                            is.close();
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    if(conn!=null) {
//                        conn.disconnect();
//                    }
//                }
//            }
//        }).start();
//
//    }

    public void F_stop_service(){

//        cancelTimer();
        stopSelf();
//        Intent service = new Intent(this,ResidentService.class);
//        this.stopService(service);

    }

//    private void cancelTimer(){
//
//        if (timer != null) {
//            timer.cancel();
//        }
//        if (task != null) {
//            task.cancel();
//        }
//
//    }


    public boolean F_isNull(String val){

        return val == null || val.equals("") || val == "null" || val.equals("null");

    }

    //設備定時開關休眠
    public void F_TimeSwitch_Power(String DateAndTime) {

        String G_control_app_package_name = getSharedPreferences("mdm_ycnt", MODE_PRIVATE)
                                            .getString("control_app_package_name", "no_val");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean TimeSwitch_open = prefs.getBoolean("setting_TimeSwitch_open", false);
        boolean TimeSwitch_close = prefs.getBoolean("setting_TimeSwitch_close", false);


        String TimeSwitch = getSharedPreferences("mdm_ycnt", MODE_PRIVATE)
                            .getString("device_TimeSwitch", "no_val");

        boolean TimeSwitch_TF = getSharedPreferences("mdm_ycnt", MODE_PRIVATE)
                                .getBoolean("device_TimeSwitch_NewOne", false);


        if(TimeSwitch_TF){
            //清除時間
            start_date = "no_val";
            end_date = "no_val";

            SharedPreferences pref = getSharedPreferences("mdm_ycnt", MODE_PRIVATE);
            pref.edit().putBoolean("device_TimeSwitch_NewOne",false).apply();
        }


        if(!G_control_app_package_name.equals("no_val") && !TimeSwitch.equals("no_val") && (TimeSwitch_open || TimeSwitch_close)){


            String[] date_arr = DateAndTime.split(" ");
            String date = date_arr[0];
            String time = date_arr[1];

            time=time.replace(":","");

            String start_week = null;
            String end_week = null;
            String start_time = null;
            String end_time = null;

            String week = F_transform_date_to_week(date);

            TimeSwitch = TimeSwitch.substring(1, TimeSwitch.length() - 1);


            JSONObject json = null;
            try {
                json = new JSONObject(TimeSwitch);
                Iterator<String> it = json.keys();

                while(it.hasNext()){
                    String key = it.next();// 獲得key
                    String value = null;
                    value = json.getString(key);


                    switch (key){
                        case "start_week":
                            start_week = value;
                            break;

                        case "end_week":
                            end_week = value;
                            break;

                        case "start_time":
                            start_time = value;
                            break;

                        case "end_time":
                            end_time = value;
                            break;
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ////////////////////////////////////////////////////////////////////////////////////////////

            start_week=start_week.substring(1, start_week.length() - 1).replace("\"","");

            if(!F_isNull(start_week) && TimeSwitch_open && !start_date.equals(date)){
                String[] start_week_arr = start_week.split(",");

                boolean start_week_TF = Arrays.asList(start_week_arr).contains(week);

                if(start_week_TF){
                    start_time = start_time.replace(":","");
                    int start_time_int = Integer.parseInt(start_time);
                    int now_time_int = Integer.parseInt(time);

                    if(start_time_int <= now_time_int && (start_time_int+100) >= now_time_int){
                        start_date = date;
                        F_sendCMD(G_control_app_package_name,"{\"set-sleep\":\"off\"}", ResidentService.this);

                    }

                }
            }

            ////////////////////////////////////////////////////////////////////////////////////////////

            end_week = end_week.substring(1, end_week.length() - 1).replace("\"","");

            if(!F_isNull(end_week) && TimeSwitch_close && !end_date.equals(date)){
                String[] end_week_arr = end_week.split(",");

                boolean end_week_TF = Arrays.asList(end_week_arr).contains(week);

                if(end_week_TF){
                    end_time = end_time.replace(":","");
                    int end_time_int = Integer.parseInt(end_time);
                    int now_time_int = Integer.parseInt(time);

                    if(end_time_int <= now_time_int && (end_time_int+100) >= now_time_int){
                        end_date=date;
                        F_sendCMD(G_control_app_package_name,"{\"set-sleep\":\"on\"}", ResidentService.this);

                        //關機 開始休眠
                        //Log.e("ttt","time to close");
                    }

                }
            }

        }

    }


    public static String F_transform_date_to_week(String strDate){

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");// 定義日期格式
        Date date = null;
        try {
            date = format.parse(strDate);// 將字串轉換為日期
        } catch (ParseException e) {
            //System.out.println("輸入的日期格式不合理!");
        }

        //String[] weekDays = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
        String[] weekDays = { "0", "1", "2", "3", "4", "5", "6" };
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return weekDays[w];
    }

    public String F_get_local_time(){

        long system_time = System.currentTimeMillis();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1 = new Date(system_time);

        return format.format(d1);
    }

    private void F_tidy_get_cmd(String control_app_package_name,String getCmd) throws JSONException {

        if(getCmd.equals("nocmd")){

            F_sendCMD(control_app_package_name,getCmd, ResidentService.this);

        }else {

            JSONArray getCmdArray = new JSONArray(getCmd);

            for (int i = 0 ; i < getCmdArray.length() ; i++) {

                JSONObject jsonCmd = getCmdArray.getJSONObject(i);

                String[] android_cmd_name = {
                        "set-marquee",
                        "set-marquee-stop",
                        "set-open-web",

                        //2023-10-30 新增
                        "set-update-iMDS-app",
                        "set-update-control-app",
                        "set-install-apk",
                        //"set-uninstall-apk"
                        "set-audio",
                        "set-video",
                        "set-image",
                        "set-url",

                        //2023-12-12 新增
                        "set-stop-media"
                };

                JSONObject android_cmd_json = new JSONObject();
                JSONObject api_cmd_json = new JSONObject();

                //JSONObject jsonCmd = new JSONObject(getCmd);
                Iterator<String> it = jsonCmd.keys();

                while(it.hasNext()){

                    String key = it.next();// 獲得key
                    String value = jsonCmd.getString(key);
                    //Log.e("testGet","key: "+key+",value:"+value);

                    boolean isContains = false;

                    for (String str : android_cmd_name) {
                        if (str.equals(key)) {
                            isContains = true;
                            break;
                        }
                    }

                    if (isContains) {
                        //Log.e("testGet",key+" 包含");
                        android_cmd_json.put(key, value);

                    } else {
                        //Log.e("testGet",key+" 不包含");
                        api_cmd_json.put(key, value);

                    }
                }

                if(api_cmd_json.length() == 0){
                    F_sendCMD(control_app_package_name,"nocmd", ResidentService.this);
                }else{
                    F_sendCMD(control_app_package_name,api_cmd_json.toString(), ResidentService.this);
                }

                if(android_cmd_json.length() != 0){
                    F_todo_android_cmd(android_cmd_json);
                }

            }

        }


//        else{
//
//            try {
//
//                String[] android_cmd_name = {
//                        "set-marquee",
//                        "set-marquee-stop",
//                        "set-open-web",
//
//                        //2023-10-30 新增
//                        "set-update-iMDS-app",
//                        "set-update-control-app",
//                        "set-install-apk",
//                        //"set-uninstall-apk"
//                        "set-audio",
//                        "set-video",
//                        "set-image",
//                        "set-url"
//                };
//
//                JSONObject android_cmd_json = new JSONObject();
//                JSONObject api_cmd_json = new JSONObject();
//
//                JSONObject jsonCmd = new JSONObject(getCmd);
//                Iterator<String> it = jsonCmd.keys();
//
//                while(it.hasNext()){
//
//                    String key = it.next();// 獲得key
//                    String value = jsonCmd.getString(key);
//                    //Log.e("testGet","key: "+key+",value:"+value);
//
//                    boolean isContains = false;
//
//                    for (String str : android_cmd_name) {
//                        if (str.equals(key)) {
//                            isContains = true;
//                            break;
//                        }
//                    }
//
//                    if (isContains) {
//                        //Log.e("testGet",key+" 包含");
//                        android_cmd_json.put(key, value);
//
//                    } else {
//                        //Log.e("testGet",key+" 不包含");
//                        api_cmd_json.put(key, value);
//
//                    }
//                }
//
//                if(api_cmd_json.length() == 0){
//                    F_sendCMD(control_app_package_name,"nocmd", ResidentService.this);
//                }else{
//                    F_sendCMD(control_app_package_name,api_cmd_json.toString(), ResidentService.this);
//                }
//
//                if(android_cmd_json.length() != 0){
//                    F_todo_android_cmd(android_cmd_json);
//                }
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }

    }

    private void F_todo_android_cmd(JSONObject android_cmd_json){

        try {
            Iterator<String> it = android_cmd_json.keys();

            while(it.hasNext()){

                String key = it.next();// 獲得key
                String value = android_cmd_json.getString(key);

                F_switch_todo_cmd(key,value,ResidentService.this);
            }

        }catch (JSONException e){
            e.printStackTrace();
        }

    }

    private void F_switch_todo_cmd(String cmd,String value,Context mContext){

        switch (cmd){

            //1.0.5
            case "set-marquee":
                function_set_cmd_instance.F_set_marquee(value, mContext, nowTime);
                break;

//            case "set-marquee-stop":
//                function_set_cmd_instance.F_set_marquee_stop(mContext);
//                break;

            //1.0.6
            case "set-url":
                function_set_cmd_instance.F_set_open_web(value, mContext);
                break;

            case "set-update-iMDS-app":
            case "set-update-control-app":
            case "set-install-apk":
                function_set_cmd_instance.F_set_install_app(value, mContext);
                break;

//            case "set-update-control-app":
//                Function_set_cmd.F_set_open_web(value,mContext);
//                break;

            case "set-audio":
                function_set_cmd_instance.F_set_audio(value, mContext, nowTime);
                break;

            case "set-video":
                function_set_cmd_instance.F_set_video(value, mContext, nowTime);
                break;

            case "set-image":
                function_set_cmd_instance.F_set_image(value, mContext, nowTime);
                break;

            case "set-stop-media":
                function_set_cmd_instance.F_stop_floating_window(value, mContext);
                break;

        }

    }

    private void resetStateToDefault(String controlAppPackageName){

        String[] toResetStatePackageNameArr = {
                "com.ycnt.genetouch_v5_control"
        };

        boolean contains = Arrays.asList(toResetStatePackageNameArr).contains(controlAppPackageName);

        if(contains){

            try {

                JSONObject jsonCmd  = new JSONObject();

                jsonCmd.put("set-touch","on");
                jsonCmd.put("set-Physical-button","on");

                String resetStateCmd = String.valueOf(jsonCmd);

                F_sendCMD(controlAppPackageName,resetStateCmd, this);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


    }

    private void processReceivedCommand(JSONObject getJson) throws JSONException {
        String G_device_register_enable = getJson.getString("ad_mdm_device_register_enable");

        SharedPreferences pref = getSharedPreferences("mdm_ycnt", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        if(G_device_register_enable.equals("F")){

            editor.remove("isDeviceSignUp");
            editor.remove("device_id");
            // 新增
            editor.remove("server_ip");

            F_stop_service();

        }
        else if(G_device_register_enable.equals("T")){


            SharedPreferences defaultSharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(ResidentService.this);
            SharedPreferences.Editor defaultEditor = defaultSharedPreferences.edit();

            String G_server_ip = getJson.getString("server_ip");
            String G_control_app_package_name = getJson.getString("control_app_package_name");
            String G_cmd = getJson.getString("ad_mdm_cmd_content");

            String getDeviceName = getJson.getString("deviceName");

            if(!F_isNull(getDeviceName)){
                editor.putString("deviceName",getDeviceName);
            }

            String getSiteName = getJson.getString("siteName");

            if(!F_isNull(getSiteName)){
                editor.putString("siteName",getSiteName);
            }

            String G_time_switch_power = getJson.getString("ad_mdm_device_app_time_switch_power");
            String G_time_switch_power_cmd = getJson.getString("ad_mdm_device_app_time_switch_power_cmd");


            String can_control_from_ipts = getJson.getString("ad_mdm_app_control_from_iPTS");
            editor.putString("control_from_ipts",can_control_from_ipts);


            String server_ip = getSharedPreferences("mdm_ycnt", MODE_PRIVATE)
                                .getString("server_ip", getResources().getString(R.string.mdm_default_url));

            if(!F_isNull(G_server_ip) && !server_ip.equals(G_server_ip)){
                editor.putString("server_ip", G_server_ip);
            }

            if(!F_isNull(G_control_app_package_name)){
                F_tidy_get_cmd(G_control_app_package_name,G_cmd);
            }

            //寫入是否定時開關休眠
            if(!F_isNull(G_time_switch_power_cmd)){

                String[] cmd_arr=G_time_switch_power_cmd.split("\\|");

                if(cmd_arr[0].equals("on")){
                    defaultEditor.putBoolean("setting_TimeSwitch_open", true);
                }else if(cmd_arr[0].equals("off")){
                    defaultEditor.putBoolean("setting_TimeSwitch_open", false);
                }

                if(cmd_arr[1].equals("on")){
                    defaultEditor.putBoolean("setting_TimeSwitch_close", true);
                }else if(cmd_arr[1].equals("off")){
                    defaultEditor.putBoolean("setting_TimeSwitch_close", false);
                }

            }

            //寫入定時開關機時間
            if(F_isNull(G_time_switch_power)){
                editor.putString("device_TimeSwitch","no_val");
            }
            else{
                String TimeSwitch_str = getSharedPreferences("mdm_ycnt", MODE_PRIVATE)
                        .getString("device_TimeSwitch", "no_val");

                if(!TimeSwitch_str.equals(G_time_switch_power)){
                    editor.putString("device_TimeSwitch",G_time_switch_power);
                    editor.putBoolean("device_TimeSwitch_NewOne",true);
                }else {
                    editor.putBoolean("device_TimeSwitch_NewOne",false);
                }

            }

            // 確認緊急回報按鈕是否出現
            boolean isEmergencyAlertBtnNowOn = (UniversalFunction.isServiceRunning(
                    ResidentService.this,"com.example.mdm_ycnt.EmergencyAlertService"));

            boolean isSiteSetShowEmergencyAlert = getJson.getBoolean("showEmergencyAlertBtn");
//            Log.e("test01-2_isSiteSetShowEmergencyAlert", String.valueOf(isSiteSetShowEmergencyAlert));
            boolean isSettingEmergencyAlertOn =
                    defaultSharedPreferences.getBoolean("setting_emergency_alert_btn", true);


            if(!isEmergencyAlertBtnNowOn && isSiteSetShowEmergencyAlert && isSettingEmergencyAlertOn){
                Intent intent = new Intent(this, EmergencyAlertService.class);
                intent.putExtra("action", "create");
                this.startService(intent);
            }else if(isEmergencyAlertBtnNowOn && (!isSiteSetShowEmergencyAlert || !isSettingEmergencyAlertOn)){
                Intent intent = new Intent(this, EmergencyAlertService.class);
                intent.putExtra("action", "delete");
                this.startService(intent);
            }

            editor.putBoolean("isEmergencyAlertBtnOn",isSiteSetShowEmergencyAlert);

            defaultEditor.apply();
        }

        editor.apply();

    }

    private void logDeviceOpenTime() throws JSONException {

        SharedPreferences pref = getSharedPreferences("mdm_ycnt", MODE_PRIVATE);

        String deviceOpenTimeStr = pref.getString("deviceOpenTimeJson", "[]");
        JSONArray deviceOpenTimeJson = new JSONArray(deviceOpenTimeStr);


        SharedPreferences.Editor editor = pref.edit();

        String getBootTime = Function_Get_device_state.F_get_BootTime();


        //拿到空的正列
        if(deviceOpenTimeJson.length() == 0){

            deviceOpenTimeJson = insertNewDeviceOpenTime(deviceOpenTimeJson, getBootTime);
            editor.putString("deviceOpenTimeJson", String.valueOf(deviceOpenTimeJson)).apply();
            //寫入
        }
        else {

            /*
             * Id不一樣 -> 新增 -> 判斷'現在時間'是否大於'第一個json的開始時間' -> 是:新增 / 否:失敗
             *
             * Id一樣 -> 判斷'現在時間'和'更新時間'是否為同一天
             *  -> 同一天 -> 更新'更新時間' -> 判斷'現在時間'是否大於'更新時間' -> 是:更新 / 否:失敗
             *
             *  -> 不同天 -> 新增 -> 判斷'現在時間'是否大於'第一個json的開始時間' -> 是:新增 / 否:失敗
             *           -> 更新 -> 將前一筆的'更新時間'改為23:59:59
             * */

            //拿最後一筆資料
            int lastNum = deviceOpenTimeJson.length() - 1;
            JSONObject getLastJson = deviceOpenTimeJson.getJSONObject(lastNum);


            String getLastId = getLastJson.getString("deviceId");
            String getLastUpdateTime = getLastJson.getString("updateTime");


            if(!getLastId.equals(RANDOM_ID)){

                deviceOpenTimeJson = insertNewDeviceOpenTime(deviceOpenTimeJson, getBootTime);
                editor.putString("deviceOpenTimeJson", String.valueOf(deviceOpenTimeJson)).apply();

            }
            else{

                /*
                * int result = compareTimes(time1, time2);
                * if (result < 0) {
                *     System.out.println(time1 + " is earlier than " + time2);
                * } else if (result > 0) {
                *     System.out.println(time1 + " is later than " + time2);
                * } else {
                *     System.out.println(time1 + " is equal to " + time2);
                * }
                * */

                long daysDifference = universalFunction_instance.getDaysDifference(getLastUpdateTime, nowTime);


                if(daysDifference == 1){

                    //update last json updateTime to 23:59:59
                    //insert new one

                    getLastJson.put("updateTime","23:59:59");
                    getLastJson.put("bootTime",getBootTime);

                    deviceOpenTimeJson.put(deviceOpenTimeJson.length() - 1, getLastJson);
                    deviceOpenTimeJson = insertNewDeviceOpenTime(deviceOpenTimeJson, getBootTime);

                    editor.putString("deviceOpenTimeJson", String.valueOf(deviceOpenTimeJson)).apply();

                }
                else if(daysDifference == 0 &&
                        universalFunction_instance.compareTimes(getLastUpdateTime, nowTime) < 0){

                    //update last json updateTime

                    getLastJson.put("updateTime",nowTime);
                    getLastJson.put("bootTime",getBootTime);

                    deviceOpenTimeJson.put(deviceOpenTimeJson.length() - 1, getLastJson);

                    editor.putString("deviceOpenTimeJson", String.valueOf(deviceOpenTimeJson)).apply();

                }

            }

        }

    }

    private JSONArray insertNewDeviceOpenTime(JSONArray deviceOpenTime, String getBootTime) throws JSONException {

        JSONObject newDeviceOpenTime = new JSONObject();
        newDeviceOpenTime.put("startTime",nowTime);
        newDeviceOpenTime.put("updateTime",nowTime);
        newDeviceOpenTime.put("deviceId",RANDOM_ID);
        newDeviceOpenTime.put("bootTime",getBootTime);

        deviceOpenTime.put(newDeviceOpenTime);

        return deviceOpenTime;
    }

    private int startUpdateDeviceInfo() throws JSONException {

        SharedPreferences pref = getSharedPreferences("mdm_ycnt", MODE_PRIVATE);
        String deviceOpenTimeStr = pref.getString("deviceOpenTimeJson", "[]");
        JSONArray deviceOpenTimeJson = new JSONArray(deviceOpenTimeStr);


        JSONObject getDeviceInfoJson = new JSONObject();
        getDeviceInfoJson.put("ad_mdm_device_BootTime",Function_Get_device_state.F_get_BootTime());
        getDeviceInfoJson.put("ad_mdm_device_ip", F_getLocalIpAddress());//IP
        getDeviceInfoJson.put("ad_mdm_device_availableram",F_getAvailableRAM(ResidentService.this));//未使用的RAM
        getDeviceInfoJson.put("ad_mdm_device_availableMemory",F_getAvailableMemory(ResidentService.this));//未使用的內存
        getDeviceInfoJson.put("ad_mdm_device_DHCP",F_getDHCPstatus(ResidentService.this));
        getDeviceInfoJson.put("ad_mdm_device_app_time_switch_power_state",F_get_TimeSwitchPower_state(ResidentService.this));
        getDeviceInfoJson.put("deviceOpenTimeList",deviceOpenTimeJson);

//        Object playing_media_id_list = Singleton.getInstance().getSingletonData();
//        Log.e("test03-3", String.valueOf(playing_media_id_list));
//
//        getDeviceInfoJson.put("playing_media_id_list",playing_media_id_list);



        String url_domain = UniversalFunction.GetServerIP(ResidentService.this);
        String phpUrl = url_domain + UPDATE_DEVICE_INFO_AND_GET_CMD_URL;

        JSONObject postData = new JSONObject();
        postData.put("id",UniversalFunction.F_getDeviceId(ResidentService.this));
        postData.put("update",getDeviceInfoJson);

        JSONObject getJson = universalFunction_instance.httpPostData(postData, phpUrl);

        String getNowTime;
        int deviceLoopSecond = 10;

        if(getJson != null){

            processReceivedCommand(getJson);
            getNowTime = getJson.getString("now_time");

            deviceLoopSecond = getJson.optInt("deviceLoopSecond");
            deviceLoopSecond = deviceLoopSecond != 0 ? deviceLoopSecond : 10;

            boolean isLogDeviceRuntimeSuccess = getJson.optBoolean("isLogDeviceRuntimeSuccess", false);
            if(isLogDeviceRuntimeSuccess && deviceOpenTimeJson.length() > 1){
                cleanUpBootLogs(deviceOpenTimeJson);
            }

            boolean isToShareDeviceScreen = getJson.optBoolean("ad_mdm_device_return_screen", false);
            int screenShotSpeed = getJson.optInt("screenShotSpeed", 5);

            startToUploadDeviceScreen(isToShareDeviceScreen, screenShotSpeed);

        }else{
            getNowTime = F_get_local_time();
        }

        nowTime = getNowTime;

        //設備定時開關休眠
        F_TimeSwitch_Power(getNowTime);

        logDeviceOpenTime();

        return deviceLoopSecond;

    }

    private void scheduleNext(int delayInSeconds) {

        scheduler.schedule(new Runnable() {
            @Override
            public void run() {

                int nextDelay = 10;

                try {

                    nextDelay = startUpdateDeviceInfo();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

//                keepAliveiMdsSentinelService();

                scheduleNext(nextDelay);
            }
        }, delayInSeconds, TimeUnit.SECONDS);
    }

    private void cleanUpBootLogs(JSONArray deviceOpenTimeJson) throws JSONException {

        JSONObject getLastJson = deviceOpenTimeJson.getJSONObject(deviceOpenTimeJson.length() -1);


        JSONArray newJsonArray = new JSONArray();
        newJsonArray.put(0, getLastJson);

        SharedPreferences pref = getSharedPreferences("mdm_ycnt", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString("deviceOpenTimeJson", String.valueOf(newJsonArray)).apply();
    }

    @SuppressLint("SimpleDateFormat")
    private void startToUploadDeviceScreen(boolean isToShareDeviceScreen, int screenShotSpeed){

        String screenShotFolderName = "Q5213";
        String screenShotFolderPath = Environment.getExternalStorageDirectory() + "/"+ screenShotFolderName+ "/";

        if(isToShareDeviceScreen && !isSharingDeviceScreen){

            File screenShotFolder = new File(Environment.getExternalStorageDirectory(), screenShotFolderName);
            if (!screenShotFolder.exists()) {
                screenShotFolder.mkdir();
            }

            //啟動回傳螢幕畫面
            shareScreenTask = new TimerTask (){
                public void run() {

                    sendScreenShot(screenShotFolderPath);

                }
            };

            shareScreenTimer = new Timer();
            shareScreenTimer.schedule(shareScreenTask, 0,screenShotSpeed * 1000);

            isSharingDeviceScreen = true;
        }
        else if(!isToShareDeviceScreen && isSharingDeviceScreen){
            //停止回傳

            if (shareScreenTimer != null) {
                shareScreenTimer.cancel();
                shareScreenTimer.purge(); // 清除定時器的任務佇列
            }

            //remove folder
            universalFunction_instance.clearDirectory(new File(screenShotFolderPath));

            isSharingDeviceScreen = false;
        }


    }

    private void sendScreenShot(String folderPath){

        try {

            JSONObject setJson = new JSONObject();
            JSONObject dataJson = new JSONObject();

            //String filePath = Environment.getExternalStorageDirectory()+"/Download/";//Download/
            //String fileName = "screen_shot.jpg";

            String fileName = universalFunction_instance.generateRandomString(10) + ".jpg";

            String fullPath = folderPath + fileName;

            dataJson.put("path", fullPath);
            dataJson.put("returnPackageName", "com.example.mdm_ycnt");
            dataJson.put("returnClassName", "UploadMessageReceiver");
            setJson.put("set-ScreenShot", dataJson);


            String controlAppPackageName = getSharedPreferences("mdm_ycnt", MODE_PRIVATE)
                                            .getString("control_app_package_name", "no_val");


            Intent intent = new Intent();
            String className = controlAppPackageName+".ResidentService"; //另一个app要啟動的组件的全路徑名
            intent.setClassName(controlAppPackageName, className);

            intent.putExtra("cmd", String.valueOf(setJson));
            intent.putExtra("promise_action", "get_ycnt_mdm_cmd");
            intent.putExtra("isGetDeviceStates", false);

            ResidentService.this.startForegroundService(intent);


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

//    private void startAlarmManager(int min, Context mContext){
//
//        Log.e("test02","startAlarmManager");
//
//        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
//        Intent intent = new Intent(mContext, AlarmReceiver.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
//
//        // 设置重复的闹钟，每1分钟触发一次
//        long startTime = System.currentTimeMillis();
//        long intervalTime = min * 60 * 1000; // 1分钟
//
//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startTime, intervalTime, pendingIntent);
//
//    }

    private void keepAliveiMdsSentinelService(){

        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.ycnt.imds.sentinel",
                "com.ycnt.imds.sentinel.WatchdogService"));
        startForegroundService(intent);

    }


}