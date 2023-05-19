package com.example.mdm_ycnt;


import static com.example.mdm_ycnt.Function_Get_device_state.F_getAndroidVersion;
import static com.example.mdm_ycnt.Function_Get_device_state.F_getAvailableMemory;
import static com.example.mdm_ycnt.Function_Get_device_state.F_getAvailableRAM;
import static com.example.mdm_ycnt.Function_Get_device_state.F_getDHCPstatus;
import static com.example.mdm_ycnt.Function_Get_device_state.F_getLocalIpAddress;
import static com.example.mdm_ycnt.Function_Get_device_state.F_getLocalMacAddress_eth0;
import static com.example.mdm_ycnt.Function_Get_device_state.F_getLocalMacAddress_wlan0;
import static com.example.mdm_ycnt.Function_Get_device_state.F_getPhoneManufacturers;
import static com.example.mdm_ycnt.Function_Get_device_state.F_getPhoneModel;
import static com.example.mdm_ycnt.Function_Get_device_state.F_getTotalMemory;
import static com.example.mdm_ycnt.Function_Get_device_state.F_getTotalRAM;
import static com.example.mdm_ycnt.Function_Get_device_state.F_get_TimeSwitchPower_state;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class ResidentService extends Service {

    public final String CHANNEL_ID_STRING = "service_chatroom";
    private boolean isFirstTime = true;

    private final String HttpPost_update_info_and_get_cmd_url = "php/app_php/mdm_update_data_and_get_cmd.php";
    private final String HttpPost_update_info_url = "php/app_php/mdm_update_data.php";

    private Control_Receiver receiver;

    private TimerTask task;
    private Timer timer;

    private String start_date = "no_val";
    private String end_date = "no_val";

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

        //廣播
        receiver = new Control_Receiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("get_ycnt_mdm_cmd");
        this.registerReceiver(receiver,filter);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        boolean isDeviceSignUp = getSharedPreferences("mdm_ycnt", MODE_PRIVATE)
                                .getBoolean("isDeviceSignUp", false);

        if(!isDeviceSignUp){
            F_stop_service();
        }


        if(isFirstTime) {

            JSONObject object = new JSONObject();

            try {
                object.put("ad_mdm_device_ip", F_getLocalIpAddress());//IP
            }catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                object.put("ad_mdm_device_eth0", F_getLocalMacAddress_eth0());//有線MAC
            }catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                object.put("ad_mdm_device_wlan0", F_getLocalMacAddress_wlan0());//無線MAC
            }catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                object.put("ad_mdm_device_phonemanufacturers", F_getPhoneManufacturers());//手機廠商
            }catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                object.put("ad_mdm_device_phonemodel", F_getPhoneModel());//手機型號
            }catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                object.put("ad_mdm_device_androidversion", F_getAndroidVersion());//系統版本號
            }catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                object.put("ad_mdm_device_totalram", F_getTotalRAM(getApplicationContext()));//全部的RAM
            }catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                object.put("ad_mdm_device_totalMemory", F_getTotalMemory(getApplicationContext()));//全部的內存
            } catch (JSONException e) {
                e.printStackTrace();
            }


            try {
                object.put("ad_mdm_device_app_version", BuildConfig.VERSION_NAME);//APP版本
            } catch (JSONException e) {
                e.printStackTrace();
            }

            HttpPost_update_info(UniversalFunction.F_getDeviceId() ,object ,ResidentService.this);


            isFirstTime = false;
        }

        task = new TimerTask (){
            public void run() {

                JSONObject object = new JSONObject();
                try {
                    //object.put("ad_mdm_device_opentime",F_getOpenTime());
                    object.put("ad_mdm_device_availableram",F_getAvailableRAM(getApplicationContext()));//未使用的RAM
                    object.put("ad_mdm_device_availableMemory",F_getAvailableMemory(getApplicationContext()));//未使用的內存
                    object.put("ad_mdm_device_DHCP",F_getDHCPstatus());
                    object.put("ad_mdm_device_app_time_switch_power_state",F_get_TimeSwitchPower_state());

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                HttpPost_update_info_and_get_cmd(UniversalFunction.F_getDeviceId() ,object,ResidentService.this);
            }
        };
        timer = new Timer();
        timer.schedule(task, 0,10 * 1000);

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        cancelTimer();
        unregisterReceiver(receiver);
    }


    public void F_sendCMD(String packageName,String cmd){//另一个app的包名

        if(packageName.equals("otherDevicePackageName")){

            /*if(cmd.equals("nocmd")){
            }*/

            try {

                /*try {
                    int brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);

                    int brightnessSettingMaximumId = getResources().getIdentifier("config_screenBrightnessSettingMaximum", "integer", "android");
                    int brightnessSettingMaximum = getResources().getInteger(brightnessSettingMaximumId);

                    int brightnessSettingMinimumId = getResources().getIdentifier("config_screenBrightnessSettingMinimum", "integer", "android");
                    int brightnessSettingMinimum = getResources().getInteger(brightnessSettingMinimumId);

                    float brightnessPercentage = ((brightness - brightnessSettingMinimum) / (float)(brightnessSettingMaximum - brightnessSettingMinimum)) * 100;


                    Log.e("brightness", String.valueOf(brightness));
                    Log.e("brightness", String.valueOf(brightness));
                    Log.e("brightnessSettingMax", String.valueOf(brightnessSettingMaximum));
                    Log.e("brightnessSettingMin", String.valueOf(brightnessSettingMinimum));

                    Log.e("brightness%", String.valueOf(brightnessPercentage));

                } catch (Settings.SettingNotFoundException e) {
                    e.printStackTrace();
                }*/

                //Log.e("getCpuTemperature", String.valueOf(Function_Get_device_state.getCpuTemperature()));
                //Log.e("brightness", Function_Get_device_state.F_get());

                JSONObject deviceStateJson = new JSONObject();

                deviceStateJson.put("ad_mdm_device_BootTime",Function_Get_device_state.F_get_BootTime());
                deviceStateJson.put("ad_mdm_device_volume",Function_Get_device_state.F_get_volume());
                deviceStateJson.put("ad_mdm_device_deviceName",Function_Get_device_state.F_get_deviceName());

                //Log.e("deviceStateJson", String.valueOf(deviceStateJson));

                HttpPost_update_info(UniversalFunction.F_getDeviceId() ,deviceStateJson,ResidentService.this);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        else {

            Intent intent = new Intent();
            String className = packageName+".ResidentService"; //另一个app要啟動的组件的全路徑名
            intent.setClassName(packageName, className);
            //intent.putExtra("device", "TV");
            intent.putExtra("cmd", cmd);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getApplicationContext().startForegroundService(intent);
            } else {
                getApplicationContext().startService(intent);
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

            try {
                JSONObject resultjson = new JSONObject(command);
                HttpPost_update_info(UniversalFunction.F_getDeviceId() ,resultjson,ResidentService.this);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            //Log.e("tttt","-----------------------------------------------------------");
        }
    }

    private void HttpPost_update_info_and_get_cmd(String DeviceId ,JSONObject info_json ,Context context){

        new Thread(new Runnable() {
            @Override
            public void run() {

                OutputStream os = null;
                InputStream is = null;
                HttpURLConnection conn = null;

                String G_now_time = "no_val";

                try {

                    String url_domain = UniversalFunction.GetServerIP(context);
                    String phpUrl = url_domain+HttpPost_update_info_and_get_cmd_url;

                    URL url = new URL(phpUrl);
                    JSONObject jsonObject = new JSONObject();

                    jsonObject.put("id", DeviceId);
                    jsonObject.put("update", info_json);

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

                        String G_device_register_enable = resultjson.getString("ad_mdm_device_register_enable");

                        SharedPreferences pref = getSharedPreferences("mdm_ycnt", MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();

                        if(G_device_register_enable.equals("F")){

                            editor.remove("isDeviceSignUp");
                            editor.remove("device_id");

                            F_stop_service();

                        }
                        else if(G_device_register_enable.equals("T")){

                            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor defaultEditor = defaultSharedPreferences.edit();

                            String G_server_ip = resultjson.getString("server_ip");
                            String G_control_app_package_name = resultjson.getString("control_app_package_name");
                            String G_cmd = resultjson.getString("ad_mdm_cmd_content");


                            String G_time_switch_power = resultjson.getString("ad_mdm_device_app_time_switch_power");
                            String G_time_switch_power_cmd = resultjson.getString("ad_mdm_device_app_time_switch_power_cmd");


                            G_now_time = resultjson.getString("now_time");


                            String can_control_from_ipts = resultjson.getString("ad_mdm_app_control_from_iPTS");
                            editor.putString("control_from_ipts",can_control_from_ipts);


                            String server_ip = getSharedPreferences("mdm_ycnt", MODE_PRIVATE)
                                                .getString("server_ip", getResources().getString(R.string.mdm_default_url));

                            if(!F_isNull(G_server_ip) && !server_ip.equals(G_server_ip)){
                                editor.putString("server_ip", G_server_ip);

                            }

                            if(!F_isNull(G_control_app_package_name)){

                                //G_cmd = "{\"set-marquee\":\"{\\\"text\\\":\\\"test123\\\\niMDS\\\\n20230508\\\",\\\"time\\\":\\\"300\\\"}\",\"set-backlight-value\":\"100\"}";

                                F_tidy_get_cmd(G_control_app_package_name,G_cmd);

                                editor.putString("control_app_package_name",G_control_app_package_name);

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

                            defaultEditor.apply();
                        }

                        editor.apply();

                    }
                    //String contentAsString = readIt(is,len);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                } finally {

                    if(G_now_time.equals("no_val")){
                        G_now_time=F_get_local_time();
                    }
                    //設備定時開關休眠
                    F_TimeSwitch_Power(G_now_time);

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

    //回傳設備狀態(沒有拿值回來)
    private void HttpPost_update_info(String DeviceId ,JSONObject info_json ,Context context){

        new Thread(new Runnable() {
            @Override
            public void run() {

                OutputStream os = null;
                InputStream is = null;
                HttpURLConnection conn = null;


                try {
                    String url_domain = UniversalFunction.GetServerIP(context);
                    String phpUrl = url_domain+HttpPost_update_info_url;

                    URL url = new URL(phpUrl);
                    JSONObject jsonObject = new JSONObject();

                    jsonObject.put("id", DeviceId);
                    jsonObject.put("update", info_json);

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

    public void F_stop_service(){

        cancelTimer();
        /*Intent service = new Intent(this,ResidentService.class);
        this.stopService(service);*/
        stopSelf();
    }

    private void cancelTimer(){

        if (timer != null) {
        timer.cancel();
        }
        if (task != null) {
        task.cancel();
        }

    }


    public boolean F_isNull(String val){
        if(val!= null && !val.equals("") && val!= "null" && !val.equals("null")){
            return false;
        }else{
            return true;
        }
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

        boolean TimeSwitch_TF= getSharedPreferences("mdm_ycnt", MODE_PRIVATE)
                                .getBoolean("device_TimeSwitch_NewOne", false);


        if(TimeSwitch_TF){
            //清除時間
            start_date="no_val";
            end_date="no_val";

            SharedPreferences pref = getSharedPreferences("mdm_ycnt", MODE_PRIVATE);
            pref.edit().putBoolean("device_TimeSwitch_NewOne",false).commit();
        }


        if(!G_control_app_package_name.equals("no_val") && !TimeSwitch.equals("no_val") && (TimeSwitch_open || TimeSwitch_close)){


            String[] date_arr=DateAndTime.split(" ");
            String date=date_arr[0];
            String time=date_arr[1];

            time=time.replace(":","");

            String start_week=null;
            String end_week=null;
            String start_time=null;
            String end_time=null;

            String week=F_transform_date_to_week(date);

            TimeSwitch=TimeSwitch.substring(1, TimeSwitch.length() - 1);


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
                            start_week=value;
                            break;

                        case "end_week":
                            end_week=value;
                            break;

                        case "start_time":
                            start_time=value;
                            break;

                        case "end_time":
                            end_time=value;
                            break;
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ////////////////////////////////////////////////////////////////////////////////////////////

            start_week=start_week.substring(1, start_week.length() - 1).replace("\"","");

            if(!F_isNull(start_week) && TimeSwitch_open && !start_date.equals(date)){
                String[] start_week_arr=start_week.split(",");

                boolean start_week_TF= Arrays.asList(start_week_arr).contains(week);

                if(start_week_TF){
                    start_time=start_time.replace(":","");
                    int start_time_int=Integer.parseInt(start_time);
                    int now_time_int=Integer.parseInt(time);

                    if(start_time_int <= now_time_int && (start_time_int+100) >= now_time_int){
                        start_date=date;
                        F_sendCMD(G_control_app_package_name,"{\"set-sleep\":\"off\"}");

                    }

                }
            }

            ////////////////////////////////////////////////////////////////////////////////////////////

            end_week=end_week.substring(1, end_week.length() - 1).replace("\"","");

            if(!F_isNull(end_week) && TimeSwitch_close && !end_date.equals(date)){
                String[] end_week_arr=end_week.split(",");

                boolean end_week_TF= Arrays.asList(end_week_arr).contains(week);

                if(end_week_TF){
                    end_time=end_time.replace(":","");
                    int end_time_int=Integer.parseInt(end_time);
                    int now_time_int=Integer.parseInt(time);

                    if(end_time_int <= now_time_int && (end_time_int+100) >= now_time_int){
                        end_date=date;
                        F_sendCMD(G_control_app_package_name,"{\"set-sleep\":\"on\"}");

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

        long system_time=System.currentTimeMillis();//long now = android.os.SystemClock.uptimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1 = new Date(system_time);
        String DateAndTime = format.format(d1);

        return DateAndTime;
    }

    private void F_tidy_get_cmd(String control_app_package_name,String getCmd){

        if(getCmd.equals("nocmd")){

            F_sendCMD(control_app_package_name,getCmd);

        }
        else{

            try {

                String[] android_cmd_name = {"set-marquee","set-marquee-stop","set-open-web"};

                JSONObject android_cmd_json = new JSONObject();
                JSONObject api_cmd_json = new JSONObject();

                JSONObject jsonCmd = new JSONObject(getCmd);
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
                    F_sendCMD(control_app_package_name,"nocmd");
                }else{
                    F_sendCMD(control_app_package_name,api_cmd_json.toString());
                }

                if(android_cmd_json.length() != 0){
                    F_todo_android_cmd(android_cmd_json);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        //Log.e("testGet", "------------------------------------------------------");

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

        //Log.e("testGetAA","key: "+cmd+",value:"+value);

        switch (cmd){

            case "set-marquee"://1.0.5
                Function_set_cmd.F_set_marquee(value,mContext);
                break;

            case "set-marquee-stop"://1.0.5
                Function_set_cmd.F_set_marquee_stop(mContext);
                break;

            case "set-open-web"://1.0.5
                Function_set_cmd.F_set_open_web(value,mContext);
                break;

        }

    }




}
