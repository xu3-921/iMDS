package com.example.mdm_ycnt;

import static android.Manifest.permission.SYSTEM_ALERT_WINDOW;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.example.mdm_ycnt.Function_Get_device_state.F_getLocalIpAddress;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    String HttpPost_isSignUP_url = "php/app_php/mdm_get_device_isSignUp.php";
    String HttpPost_device_model_url = "php/app_php/mdm_get_device_model.php";
    String HttpPost_getManufacturerAndDeviceModelList = "php/app_php/mdm_get_manufacturer_device_model.php";
    String HttpPost_updateDeviceModelAndGetDownloadUrl = "php/app_php/mdm_update_device_model_and_get_download_url.php";

    private TimerTask task;
    private Timer timer;
    private ProgressDialog loadingPd;


    ArrayAdapter<String> adapterDeviceManufacturerItems;
    ArrayAdapter<String> adapterDeviceModelItems;

    boolean isFirstTimeOpen = true;
    boolean isToReload = false;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean hasWriteExternalStorage = UniversalFunction.requestPermission(MainActivity.this , this, WRITE_EXTERNAL_STORAGE ,100);

        //Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        //startActivity(intent);

        //Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        //boolean hasOver = UniversalFunction.requestPermission(MainActivity.this , this, SYSTEM_ALERT_WINDOW ,100);

        if(hasWriteExternalStorage){

            if((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) && !getPackageManager().canRequestPackageInstalls()){

                createAlertDialogGetInstallPermission();

            }
            else if((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) && !Settings.canDrawOverlays(MainActivity.this)){

                createAlertDialogGetDrawOverlaysPermission();

            }
            else {

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        is_update_imds_app();

                    }

                }).start();

            }
        }

    }

    private void is_update_imds_app(){

        JSONObject getJson = UniversalFunction.check_imds_app_version();

        try {
            String isLastVersion = null;

            if(getJson == null){
                isLastVersion = "error";
            }else {
                isLastVersion = getJson.getString("is_last_version");
            }

            //newest_ver:最新版
            //safe_ver:安全範圍內 但不是最新
            //higher_than_max_ver:版本過高
            //lower_than_min_ver:版本過低
            //unregistered:未註冊
            //error:錯誤

            if(isLastVersion.equals("newest_ver") || isLastVersion.equals("unregistered") || isLastVersion.equals("error")){

                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        createMainActivity();
                    }
                });*/

                is_update_control_app();

            }
            else if(isLastVersion.equals("safe_ver")){

                String isForcedUpdating = getJson.getString("is_forced_updating");

                if(isForcedUpdating.equals("T")){

                    String lastVersionName = getJson.getString("package_version_name");
                    String packagePath = getJson.getString("ad_mdm_device_control_app_package_path");
                    String appName = getJson.getString("ad_mdm_device_app_name");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this , R.style.BlueThemeDialog);
                            alertDialogBuilder.setTitle("請更新iMDS");
                            alertDialogBuilder.setMessage("iMDS最新版本為"+lastVersionName);

                            alertDialogBuilder.setPositiveButton("更新",((dialog, which) -> {
                                //更新

                                String serverIp = UniversalFunction.GetServerIP(MainActivity.this);
                                String defaultPath = String.valueOf(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS));
                                String subPath = "/Apk";

                                String downloadUrl = serverIp + packagePath + appName + "_" + lastVersionName + ".apk";
                                String apkName = appName + "_" + lastVersionName + ".apk";

                                isToReload = true;
                                UniversalFunction.downloadApp(downloadUrl ,apkName ,defaultPath ,subPath ,false ,MainActivity.this ,MainActivity.this);

                            }));

                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.setCancelable(false);
                            alertDialog.setCanceledOnTouchOutside(false);

                            alertDialog.show();

                        }
                    });

                }
                else{
                    /*runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            createMainActivity();
                        }
                    });*/
                    is_update_control_app();
                }

            }
            else if(isLastVersion.equals("higher_than_max_ver") || isLastVersion.equals("lower_than_min_ver")){

                String lastVersionName = getJson.getString("package_version_name");
                String packagePath = getJson.getString("ad_mdm_device_control_app_package_path");
                String appName = getJson.getString("ad_mdm_device_app_name");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this , R.style.BlueThemeDialog);
                        alertDialogBuilder.setTitle("請更新iMDS");
                        alertDialogBuilder.setMessage("iMDS建議版本為"+lastVersionName);

                        alertDialogBuilder.setPositiveButton("更新",((dialog, which) -> {
                            //更新

                            String serverIp = UniversalFunction.GetServerIP(MainActivity.this);
                            String defaultPath = String.valueOf(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS));
                            String subPath = "/Apk";

                            String downloadUrl = serverIp + packagePath + appName + "_" + lastVersionName + ".apk";
                            String apkName = appName + "_" + lastVersionName + ".apk";

                            isToReload = true;
                            UniversalFunction.downloadApp(downloadUrl ,apkName ,defaultPath ,subPath ,false ,MainActivity.this ,MainActivity.this);

                        }));

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.setCancelable(false);
                        alertDialog.setCanceledOnTouchOutside(false);

                        alertDialog.show();

                    }
                });
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void is_update_control_app(){

        JSONObject getJson = UniversalFunction.check_control_app_version();

        try {

            //newest_ver:最新版
            //higher_than_max_ver:版本過高
            //lower_than_min_ver:版本過低
            //error:錯誤

            String isLastVersion = null;

            if(getJson == null){
                isLastVersion = "error";
            }else {
                isLastVersion = getJson.getString("is_last_version");
            }

            if(isLastVersion.equals("newest_ver") || isLastVersion.equals("error")){

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        createMainActivity();
                    }
                });
            }
            else {

                String lastVersionName = getJson.getString("package_version_name");
                String packagePath = getJson.getString("device_control_app_package_path");
                String appName = getJson.getString("device_app_name");

                String setTitle = null;
                String setMessage = null;
                String setIsForcedUpdating = null;


                if(isLastVersion.equals("higher_than_max_ver")){
                    setTitle = "請更新控制APP";
                    setMessage = "控制APP建議版本為"+lastVersionName;
                    setIsForcedUpdating = "T";

                }else if(isLastVersion.equals("lower_than_min_ver")){
                    setTitle = "請問是否更新控制APP";
                    setMessage = "控制APP最新版本為"+lastVersionName;
                    setIsForcedUpdating = "F";
                }

                String alertTitle = setTitle;
                String alertMessage = setMessage;
                String isForcedUpdating = setIsForcedUpdating;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this ,R.style.BlueThemeDialog);
                        alertDialogBuilder.setTitle(alertTitle);
                        alertDialogBuilder.setMessage(alertMessage);

                        if(isForcedUpdating.equals("F")){
                            alertDialogBuilder.setNegativeButton("取消",((dialog, which) -> {
                                createMainActivity();
                            }));
                        }

                        alertDialogBuilder.setPositiveButton("更新",((dialog, which) -> {
                            //更新

                            String serverIp = UniversalFunction.GetServerIP(MainActivity.this);
                            String defaultPath = String.valueOf(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS));
                            String subPath = "/Apk";

                            String downloadUrl = serverIp + packagePath + appName + "_" + lastVersionName + ".apk";
                            String apkName = appName + "_" + lastVersionName + ".apk";

                            isToReload = true;
                            UniversalFunction.downloadApp(downloadUrl ,apkName ,defaultPath ,subPath ,false ,MainActivity.this ,MainActivity.this);

                        }));

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.setCancelable(false);
                        alertDialog.setCanceledOnTouchOutside(false);

                        alertDialog.show();

                    }
                });

            }



        }catch (JSONException e){
            e.printStackTrace();
        }

    }

    @SuppressLint("SetTextI18n")
    private void createMainActivity(){

        boolean isDeviceSignUp = getSharedPreferences("mdm_ycnt", MODE_PRIVATE)
                                .getBoolean("isDeviceSignUp", false);

        if(isDeviceSignUp){//已註冊
            setContentView(R.layout.activity_main_registered);

            TextView text_url = findViewById(R.id.text_url);
            TextView text_IP = findViewById(R.id.text_IP);
            TextView text_app_version = findViewById(R.id.text_app_version);
            ImageButton btn_setting = findViewById(R.id.btn_setting);


            setToSettingBtn(btn_setting);

            loadingPd = new ProgressDialog(MainActivity.this , R.style.BlueLoadingAlertDialogStyle);
            loadingPd.setMessage("iMDS資料確認中...");
            loadingPd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            loadingPd.setCancelable(false);
            loadingPd.setCanceledOnTouchOutside(false);
            loadingPd.show();


            String url_domain = UniversalFunction.GetServerIP(MainActivity.this);

            text_url.setText("伺服器網址 : "+url_domain);
            text_IP.setText("設備IP : "+F_getLocalIpAddress());
            text_app_version.setText("iMDS App版本 : "+BuildConfig.VERSION_NAME);


            task = new TimerTask (){
                public void run() {

                    boolean isDeviceSignUp = getSharedPreferences("mdm_ycnt", MODE_PRIVATE)
                                            .getBoolean("isDeviceSignUp", false);

                    if(!isDeviceSignUp){
                        F_reload_to_registered();
                    }

                    String get_url_domain = UniversalFunction.GetServerIP(MainActivity.this);

                    String og_url_domain =  text_url.getText().toString();
                    og_url_domain = og_url_domain.replace("伺服器網址 : ","");

                    if(!og_url_domain.equals(get_url_domain)){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run(){
                                text_url.setText("伺服器網址 : "+get_url_domain);
                            }
                        });

                    }

                }
            };

            timer = new Timer();
            timer.schedule(task, 0,1000 * 10);


            new Thread(() -> {

                try {
                    setDeviceControlMethodProcess();

                } catch (InterruptedException | JSONException e) {
                    e.printStackTrace();
                }

            }).start();


        }
        else{//未註冊
            setContentView(R.layout.activity_main);

            SharedPreferences pref = getSharedPreferences("mdm_ycnt", MODE_PRIVATE);
            pref.edit().remove("control_app_package_name").apply();

            ImageView img_qrcode=findViewById(R.id.img_qrcode);
            TextView textView_showID=findViewById(R.id.textView_showID);

            String id = F_get_system_name();

            textView_showID.setText(id);

            String url_domain = UniversalFunction.GetServerIP(getApplicationContext());


            byte[] DeviceIdData = new byte[0];
            try {
                DeviceIdData = id.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String DeviceId_base64 = Base64.encodeToString(DeviceIdData, Base64.DEFAULT);


            String url = url_domain + "iMDS_Register/index.php?" + DeviceId_base64;//"get_device_qrcode.php?"
            UniversalFunction.F_show_qrcode(img_qrcode,url);


            task = new TimerTask (){
                public void run() {

                    HttpPost_isSignUp(id);

                }
            };

            timer = new Timer();
            timer.schedule(task, 0,1000 * 5);

        }
    }

    @Override
    protected void onResume() {

        super.onResume();
        //Log.e("ttt","onResume");

        if(isFirstTimeOpen){
            isFirstTimeOpen = false;
        }else{
            isToReload = true;
        }


        if(isToReload){

            F_reload_to_registered();
            isToReload = false;
        }

    }

    @Override
    protected void onPause() {

        //Log.e("tttt","onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        //Log.e("tttt","onDestroy");
        super.onDestroy();

    }


    @SuppressLint("SetTextI18n")
    private void setDeviceControlMethodProcess() throws InterruptedException, JSONException {

        String control_app_package_name = getSharedPreferences("mdm_ycnt", MODE_PRIVATE)
                                        .getString("control_app_package_name", null);

        JSONObject getDeviceModelVal = HttpPost_getDeviceModelInfo(F_get_system_name());

        String controlAppPackageName = getDeviceModelVal.getString("ad_mdm_device_control_app_package_name");

        boolean isSuccessEnter = true;

        if(control_app_package_name == null || control_app_package_name.equals("null")){

            isSuccessEnter = false;

        }
        else if(control_app_package_name.equals("otherDevicePackageName")){

            if(control_app_package_name.equals(controlAppPackageName)){

                isSuccessEnter = true;

            }else{

                isSuccessEnter = false;

            }

        }
        else{

            if(controlAppPackageName.equals("otherDevicePackageName")){

                isSuccessEnter = false;


            }else{
                boolean hasInstalledControlApp = UniversalFunction.hasInstalledThisApp(controlAppPackageName);

                isSuccessEnter = hasInstalledControlApp;

            }


        }


        if(isSuccessEnter){

            //成功進入

            if(loadingPd.isShowing()){
                loadingPd.dismiss();
            }

            //寫入packageName
            SharedPreferences pref = getSharedPreferences("mdm_ycnt", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();

            String deviceManufacturerName = getDeviceModelVal.getString("ad_mdm_device_manufacturer");
            String deviceModelName = getDeviceModelVal.getString("ad_mdm_device_model_name");

            editor.putString("device_manufacturer_name",deviceManufacturerName);
            editor.putString("device_model_name",deviceModelName);
            editor.putString("control_app_package_name",controlAppPackageName);

            editor.apply();

            F_start_ResidentService();

        }
        else{

            ArrayList<String> ControlAppList = UniversalFunction.getInstalledControlAppList(this);
            boolean isControlAppListHasVal = ControlAppList.size() != 0;

            try {

                String deviceModelId = getDeviceModelVal.getString("ad_mdm_device_model");

                if(deviceModelId.equals("notFindDevice")){

                    F_reload_to_registered();

                }else if(deviceModelId.equals("notSet")){
                    //選擇型號
                    showSelectDeviceAlert(null, null);

                }else{

                    if(isControlAppListHasVal){

                        if(ControlAppList.contains(controlAppPackageName)){

                            //寫入packageName
                            SharedPreferences pref = getSharedPreferences("mdm_ycnt", MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();

                            String deviceManufacturerName = getDeviceModelVal.getString("ad_mdm_device_manufacturer");
                            String deviceModelName = getDeviceModelVal.getString("ad_mdm_device_model_name");

                            editor.putString("device_manufacturer_name",deviceManufacturerName);
                            editor.putString("device_model_name",deviceModelName);
                            editor.putString("control_app_package_name",controlAppPackageName);

                            editor.apply();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loadingPd.dismiss();
                                }
                            });

                            F_start_ResidentService();

                        }
                        /*else if(controlAppPackageName.equals("otherDevicePackageName")){
                            showSelectDeviceAlert(deviceModelId,controlAppPackageName);

                        }*/
                        else {
                            //選擇型號
                            //showSelectDeviceAlert(null, null);
                            showSelectDeviceAlert(deviceModelId,controlAppPackageName);
                        }

                    }else{
                        //選擇型號 帶入拿回來的型號
                        showSelectDeviceAlert(deviceModelId,controlAppPackageName);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

    private void showSelectDeviceAlert(String deviceModelId, String controlAppPackageName){


        JSONObject manufacturerNameJsonObject = new JSONObject();
        JSONObject deviceModelJsonObject = new JSONObject();
        JSONObject deviceModelInfoJsonObject = new JSONObject();
        JSONObject deviceModeNameToIdJsonObject = new JSONObject();

        ArrayList<String> manufacturerNameArrList = new ArrayList();

        JSONObject returnJsonObject = HttpPost_getManufacturerAndDeviceModelList();

        try {

            JSONArray get_device_manufacturer = returnJsonObject.getJSONArray("ad_mdm_device_manufacturer");
            JSONArray get_device_name = returnJsonObject.getJSONArray("ad_mdm_device_model");

            for (int i = 0; i < get_device_manufacturer.length(); i++) {

                JSONObject getVal = get_device_manufacturer.getJSONObject(i);

                String manufacturer_id = getVal.getString("ad_mdm_device_manufacturer_id");
                String manufacturer_name = getVal.getString("ad_mdm_device_manufacturer_name");

                manufacturerNameArrList.add(manufacturer_name);
                manufacturerNameJsonObject.put(manufacturer_name, manufacturer_id);
                deviceModelJsonObject.put(manufacturer_id, new JSONArray());

            }

            for (int i = 0; i < get_device_name.length(); i++){

                JSONObject getVal = get_device_name.getJSONObject(i);

                String ad_mdm_device_model = getVal.getString("ad_mdm_device_model");
                String ad_mdm_device_model_name = getVal.getString("ad_mdm_device_model_name");
                String ad_mdm_device_manufacturer = getVal.getString("ad_mdm_device_manufacturer");
                String ad_mdm_device_control_app_package_name = getVal.getString("ad_mdm_device_control_app_package_name");

                JSONArray getJsonArr = deviceModelJsonObject.getJSONArray(ad_mdm_device_manufacturer).put(ad_mdm_device_model_name);

                deviceModelJsonObject.put(ad_mdm_device_manufacturer,getJsonArr);

                JSONObject deviceInfo = new JSONObject();
                deviceInfo.put("ad_mdm_device_model_name",ad_mdm_device_model_name);
                deviceInfo.put("ad_mdm_device_manufacturer",ad_mdm_device_manufacturer);
                deviceInfo.put("ad_mdm_device_control_app_package_name",ad_mdm_device_control_app_package_name);

                deviceModelInfoJsonObject.put(ad_mdm_device_model,deviceInfo);
                deviceModeNameToIdJsonObject.put(ad_mdm_device_model_name,ad_mdm_device_model);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }



        runOnUiThread(new Runnable() {
            @Override
            public void run(){

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialogCustomColorBlue);
                alertDialog.setTitle("請選擇電視型號");
                alertDialog.setCancelable(false);
                View layout = getLayoutInflater().inflate(R.layout.alert_select_divice_model,null);
                alertDialog.setView(layout);
                //alertDialog.setNegativeButton("取消",((dialog, which) -> {}));
                alertDialog.setPositiveButton("確定",(((dialog, which) -> {})));

                AlertDialog dialog = alertDialog.create();

                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                AutoCompleteTextView textViewDeviceManufacturer;
                AutoCompleteTextView textViewDeviceModel;

                //TextInputLayout layoutDeviceManufacturer = layout.findViewById(R.id.layout_device_manufacturer);
                TextInputLayout layoutDeviceModel = layout.findViewById(R.id.layout_device_model);


                textViewDeviceManufacturer = layout.findViewById(R.id.select_list_device_manufacturer);
                textViewDeviceModel = layout.findViewById(R.id.select_list_device_model);
                adapterDeviceManufacturerItems = new ArrayAdapter<String>(MainActivity.this,R.layout.support_simple_spinner_dropdown_item,manufacturerNameArrList);


                textViewDeviceManufacturer.setInputType(InputType.TYPE_NULL);
                textViewDeviceModel.setInputType(InputType.TYPE_NULL);

                textViewDeviceManufacturer.setAdapter(adapterDeviceManufacturerItems);

                if(deviceModelId != null){

                    try {

                        JSONObject getDeviceInfo = deviceModelInfoJsonObject.getJSONObject(deviceModelId);

                        String manufacturerId = getDeviceInfo.getString("ad_mdm_device_manufacturer");
                        String manufacturerName = null;
                        String deviceModelName = getDeviceInfo.getString("ad_mdm_device_model_name");

                        Iterator<?> keys = manufacturerNameJsonObject.keys();

                        while( keys.hasNext() ) {
                            String key = (String) keys.next();

                            String getVal = manufacturerNameJsonObject.getString(key);

                            if(getVal.equals(manufacturerId)){

                                manufacturerName = key;
                            }
                        }

                        JSONArray deviceModelJSONArr = deviceModelJsonObject.getJSONArray(manufacturerId);
                        ArrayList<String> deviceModelJSONArrList = new ArrayList<String>();
                        for (int j = 0; j < deviceModelJSONArr.length(); j++) {
                            deviceModelJSONArrList.add(deviceModelJSONArr.getString(j));
                        }

                        adapterDeviceModelItems = new ArrayAdapter<String>(MainActivity.this,R.layout.support_simple_spinner_dropdown_item,deviceModelJSONArrList);

                        textViewDeviceModel.setAdapter(adapterDeviceModelItems);

                        textViewDeviceModel.setText(deviceModelName, false);
                        textViewDeviceManufacturer.setText(manufacturerName, false);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }else{
                    layoutDeviceModel.setEnabled(false);
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

                }

                textViewDeviceManufacturer.addTextChangedListener(new TextWatcher() {

                    String beforeText = null;
                    String afterText = null;

                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        if(String.valueOf(charSequence).length() == 0){
                            beforeText = null;
                        }else{
                            beforeText  = String.valueOf(charSequence);
                        }

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        if(String.valueOf(charSequence).length() == 0){
                            afterText = null;
                        }else{
                            afterText  = String.valueOf(charSequence);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                        if(!afterText.equals(beforeText)){

                            ArrayList<String> deviceModelJSONArrList = new ArrayList<String>();

                            try {
                                String manufacturerId = manufacturerNameJsonObject.getString(afterText);
                                JSONArray deviceModelJSONArr = deviceModelJsonObject.getJSONArray(manufacturerId);

                                for (int j = 0; j < deviceModelJSONArr.length(); j++) {
                                    deviceModelJSONArrList.add(deviceModelJSONArr.getString(j));
                                }

                                adapterDeviceModelItems = new ArrayAdapter<String>(MainActivity.this,R.layout.support_simple_spinner_dropdown_item,deviceModelJSONArrList);

                                textViewDeviceModel.setAdapter(adapterDeviceModelItems);
                                textViewDeviceModel.setText("", false);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                            layoutDeviceModel.setEnabled(true);

                            String DeviceManufacturerName = textViewDeviceManufacturer.getText().toString();
                            String DeviceModelName = textViewDeviceModel.getText().toString();

                            if(DeviceManufacturerName.length() != 0 && DeviceModelName.length() != 0){
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                            }else{
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                            }

                        }


                    }
                });


                textViewDeviceModel.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        //String item = adapterView.getItemAtPosition(i).toString();

                        String DeviceManufacturerName = textViewDeviceManufacturer.getText().toString();
                        String DeviceModelName = textViewDeviceModel.getText().toString();

                        if(DeviceManufacturerName.length() != 0 && DeviceModelName.length() != 0){
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        }else{
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        }

                    }

                });


                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((v ->{

                    String deviceManufacturerName = textViewDeviceManufacturer.getText().toString();
                    String deviceModelName = textViewDeviceModel.getText().toString();

                    Thread thread_update_info = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            try {

                                String deviceModelId = null;

                                deviceModelId = deviceModeNameToIdJsonObject.getString(deviceModelName);
                                JSONObject getJson = HttpPost_updateDeviceModelAndGetDownloadUrl(deviceModelId);
                                String isSuccess = getJson.getString("isSuccess");

                               if(isSuccess.equals("success")){

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            dialog.dismiss();
                                        }
                                    });

                                    JSONObject download_info = getJson.getJSONObject("download_info");
                                    String serverIp = UniversalFunction.GetServerIP(MainActivity.this);
                                    String controlAppPackageName = download_info.getString("controlAppPackageName");

                                    SharedPreferences pref = getSharedPreferences("mdm_ycnt", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putString("device_manufacturer_name",deviceManufacturerName);
                                    editor.putString("device_model_name",deviceModelName);
                                    editor.putString("control_app_package_name",controlAppPackageName);
                                    editor.apply();


                                    if(controlAppPackageName.equals("otherDevicePackageName")){

                                        F_start_ResidentService();

                                    }
                                    else{

                                        boolean hasInstalledApp = UniversalFunction.hasInstalledThisApp(controlAppPackageName);

                                        //int getAppVersionCode = UniversalFunction.getAppVersionCode(packageName);
                                        if(!hasInstalledApp){
                                            String appName = download_info.getString("app_name");
                                            String ad_mdm_device_control_app_package_version = download_info.getString("ad_mdm_device_control_app_package_version");
                                            String ad_mdm_device_control_app_package_path = download_info.getString("ad_mdm_device_control_app_package_path");

                                            String downloadUrl = serverIp+ad_mdm_device_control_app_package_path+
                                                    appName+"_"+ad_mdm_device_control_app_package_version+".apk";

                                            String apkName = appName+"_"+ad_mdm_device_control_app_package_version+".apk";

                                            create_download_alert(deviceModelName , downloadUrl ,apkName);

                                        }else {
                                            F_start_ResidentService();
                                        }

                                    }

                               }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });

                    thread_update_info.start();

                }));

                //dialog.getButton(Dialog.BUTTON_POSITIVE).setClickable(true);
                loadingPd.dismiss();

            }
        });


    }





    //生成device_id
    public String F_get_system_name(){
        //md5("d"+"9"+"2"+$android_id+"e")
        String DeviceId = null;
        String device_id= getSharedPreferences("mdm_ycnt", MODE_PRIVATE)
                            .getString("device_id", "noID");

        if(!device_id.equals("noID")){
            return device_id;

        }else{

            if(Build.VERSION.SDK_INT < 29){//低於android 10

                if(!getMacAddr_wlan0().equals("not_get_mac")){
                    DeviceId=getMacAddr_wlan0().replace(":","");

                }else if(!getMacAddr_eth0().equals("not_get_mac")){
                    DeviceId=getMacAddr_eth0().replace(":","");

                }else{
                    DeviceId=Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

                }

            }else{
                DeviceId=Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

            }

            String md5_DeviceId = F_md5(DeviceId);

            return md5_DeviceId;

        }


    }

    //md5加密
    public static String F_md5(String device_id){

        String id="d"+"9"+"2"+device_id+"e";

        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(id.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException",e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UnsupportedEncodingException", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10){
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    //getMacAddress_eth0
    public static String getMacAddr_eth0() {
        String Mac=null;
        try{
            String path="sys/class/net/eth0/address";
            FileInputStream fis_name = new FileInputStream(path);
            byte[] buffer_name = new byte[8192];
            int byteCount_name = fis_name.read(buffer_name);
            if(byteCount_name>0)
            {
                Mac = new String(buffer_name, 0, byteCount_name, "utf-8");
            }
            if(Mac.length()==0||Mac==null){
                return "not_get_mac";
            }
        }catch(Exception io){
            return "not_get_mac";
        }
        return Mac.trim().toUpperCase();
    }

    //getMacAddress_wlan0
    private static String getMacAddr_wlan0() {
        String Mac=null;
        try{
            String path="sys/class/net/wlan0/address";
            FileInputStream fis_name = new FileInputStream(path);
            byte[] buffer_name = new byte[8192];
            int byteCount_name = fis_name.read(buffer_name);
            if(byteCount_name>0)
            {
                Mac = new String(buffer_name, 0, byteCount_name, "utf-8");
            }
            if(Mac.length()==0||Mac==null){
                return "not_get_mac";
            }
        }catch(Exception io){
            return "not_get_mac";
        }
        return Mac.trim().toUpperCase();
    }

    private void HttpPost_isSignUp(String device_id){

        new Thread(new Runnable() {

            @Override
            public void run() {

                OutputStream os = null;
                InputStream is = null;
                HttpURLConnection conn = null;

                try {
                    String url_domain = UniversalFunction.GetServerIP(MainActivity.this);

                    String phpUrl = url_domain+HttpPost_isSignUP_url;
                    URL url = new URL(phpUrl);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", device_id);

                    String message = jsonObject.toString();

                    conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000); //milliseconds
                    conn.setConnectTimeout(15000); //milliseconds
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

                        String result1 = resultjson.getString("ad_mdm_device_register_enable");

                        if(result1.equals("T")){//註冊成功

                            SharedPreferences pref = getSharedPreferences("mdm_ycnt", MODE_PRIVATE);
                            pref.edit().putBoolean("isDeviceSignUp", true).commit();
                            pref.edit().putString("device_id", device_id).commit();

                            F_reload_to_registered();

                        }/*else if(result1.equals("F") && isDeviceSignUp){//解註冊


                        }*/


                    }
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

    public void F_reload_to_registered(){

        cancelTimer();

        Intent intent = getPackageManager().getLaunchIntentForPackage(getApplication().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        finish();

    }

    private void cancelTimer(){

        if (timer != null) {
            timer.cancel();
        }
        if (task != null) {
            task.cancel();
        }

    }

    private void setToSettingBtn(ImageButton btn_setting){

        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                //finish();

            }
        });
    }

    private JSONObject HttpPost_getDeviceModelInfo(String device_id){


        OutputStream os = null;
        InputStream is = null;
        HttpURLConnection conn = null;

        JSONObject returnVal = null;

        try {

            String url_domain = UniversalFunction.GetServerIP(getApplicationContext());

            String phpUrl = url_domain + HttpPost_device_model_url;

            URL url = new URL(phpUrl);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", device_id);

            String message = jsonObject.toString();

            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000); //milliseconds
            conn.setConnectTimeout(15000); //milliseconds
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

                returnVal = new JSONObject(sb.toString());

                //returnVal = resultJson.getString("ad_mdm_device_model");

            }
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

        return returnVal;

    }

    private JSONObject HttpPost_getManufacturerAndDeviceModelList(){

        OutputStream os = null;
        InputStream is = null;
        HttpURLConnection conn = null;

        JSONObject resultJson = null;

        try {

            String url_domain = UniversalFunction.GetServerIP(getApplicationContext());

            String phpUrl = url_domain + HttpPost_getManufacturerAndDeviceModelList;

            URL url = new URL(phpUrl);
            JSONObject jsonObject = new JSONObject();
            //jsonObject.put("id", device_id);

            String message = jsonObject.toString();

            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000); //milliseconds
            conn.setConnectTimeout(15000); //milliseconds
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

                resultJson = new JSONObject(sb.toString());

            }
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

        return resultJson;
    }

    private JSONObject HttpPost_updateDeviceModelAndGetDownloadUrl(String deviceModel){

        OutputStream os = null;
        InputStream is = null;
        HttpURLConnection conn = null;

        JSONObject resultJson = null;

        try {

            String url_domain = UniversalFunction.GetServerIP(getApplicationContext());

            String phpUrl = url_domain + HttpPost_updateDeviceModelAndGetDownloadUrl;

            URL url = new URL(phpUrl);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", F_get_system_name());
            jsonObject.put("device_model", deviceModel);

            String message = jsonObject.toString();

            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000); //milliseconds
            conn.setConnectTimeout(15000); //milliseconds
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

                resultJson = new JSONObject(sb.toString());

            }
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

        return resultJson;
    }

    private void create_download_alert(String deviceModelName , String downloadUrl ,String apkName){


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this,R.style.BlueThemeDialog);
        alertDialogBuilder.setTitle("安裝APP");
        alertDialogBuilder.setMessage("請問是否安裝 "+deviceModelName+" 控制APP");

        AlertDialog.Builder alertDialogBuilderExit = new AlertDialog.Builder(MainActivity.this,R.style.BlueThemeDialog);
        alertDialogBuilderExit.setTitle("未安裝控制APP將無法使用");
        alertDialogBuilderExit.setMessage("確認後將直接退出APP");


        alertDialogBuilder.setNegativeButton("取消",((dialog, which) -> {

            AlertDialog alertDialogExit = alertDialogBuilderExit.create();
            alertDialogExit.setCancelable(false);
            alertDialogExit.setCanceledOnTouchOutside(false);
            alertDialogExit.show();

        }));

        alertDialogBuilder.setPositiveButton("安裝",(((dialog, which) -> {

            new Thread(new Runnable() {
                @Override
                public void run() {

                    //是否有安裝權限
                    //是 - download
                    //否 - 拿權限

                    if((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) && !getPackageManager().canRequestPackageInstalls()){

                        createAlertDialogGetInstallPermission();

                    }else{
                        String defaultPath = String.valueOf(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS));
                        String subPath = "/Apk";

                        isToReload = true;
                        UniversalFunction.downloadApp(downloadUrl ,apkName ,defaultPath ,subPath ,false ,MainActivity.this ,MainActivity.this);
                    }




                }
            }).start();

        })));

        alertDialogBuilderExit.setNegativeButton("取消",((dialog, which) -> {

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();

        }));

        alertDialogBuilderExit.setPositiveButton("確認",(((dialog, which) -> {

            //退出APP
            //finish();

        })));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.setCancelable(false);
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();


            }
        });

    }

    //賦予權限的回傳值
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(permissions[0].equals("android.permission.WRITE_EXTERNAL_STORAGE")){

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                if(requestCode == 100){
                    F_reload_to_registered();
                }

            }else{

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder alertDialogBuilderHint = new AlertDialog.Builder(MainActivity.this,R.style.BlueThemeDialog);
                        alertDialogBuilderHint.setTitle("無法取得存取裝置的權限");
                        alertDialogBuilderHint.setMessage("沒有存取裝置的權限將無法使用iMDS");

                        alertDialogBuilderHint.setNegativeButton("退出",((dialog, which) -> {
                            finish();
                        }));
                        alertDialogBuilderHint.setPositiveButton("允許",((dialog, which) -> {
                            UniversalFunction.requestPermission(MainActivity.this ,MainActivity.this ,permissions[0] ,requestCode);
                        }));

                        AlertDialog alertDialogHint = alertDialogBuilderHint.create();
                        alertDialogHint.setCancelable(false);
                        alertDialogHint.setCanceledOnTouchOutside(false);
                        alertDialogHint.show();

                    }
                });
            }

        }

        /*if (requestCode == 0) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Log.e("tttaaa","1");

                if (!Settings.canDrawOverlays(MainActivity.this) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)) {
                    GetOverlayPermission(true);

                }else if((Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) && !getPackageManager().canRequestPackageInstalls()){
                    GetInstallPermission();

                } else {
                    StartMainActivity();

                }

            }else{
                //Log.e("tttaaa","0");

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this,R.style.BlueThemeDialog);
                alertDialog.setTitle("提示");
                alertDialog.setMessage("無法取得存儲權限，請先至設定中賦予");
                alertDialog.setPositiveButton(("去設定"),(((dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })));

                AlertDialog alert = alertDialog.create();
                alert.setCancelable(false);
                alert.setCanceledOnTouchOutside(false);
                alert.show();
            }
        }*/

    }

    //賦予權限的回傳值
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){

            case 1:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    if(!getPackageManager().canRequestPackageInstalls()){ //沒有權限

                        createAlertDialogGetInstallPermission();

                    }else{
                        F_reload_to_registered();

                    }

                }
                break;

            case 2:

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    if(!Settings.canDrawOverlays(this)){ //沒有權限
                        createAlertDialogGetDrawOverlaysPermission();

                    }else{
                        F_reload_to_registered();

                    }

                }
                break;

        }
    }

    private void createAlertDialogGetInstallPermission(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder alertDialogBuilderHint = new AlertDialog.Builder(MainActivity.this,R.style.BlueThemeDialog);
                alertDialogBuilderHint.setTitle("請允許安裝權限");
                alertDialogBuilderHint.setMessage("沒有安裝權限將無法使用iMDS");

                alertDialogBuilderHint.setNegativeButton("退出",((dialog, which) -> {
                    finish();
                }));
                alertDialogBuilderHint.setPositiveButton("允許",((dialog, which) -> {
                    UniversalFunction.getInstallPermission(MainActivity.this ,MainActivity.this);
                }));

                AlertDialog alertDialogHint = alertDialogBuilderHint.create();
                alertDialogHint.setCancelable(false);
                alertDialogHint.setCanceledOnTouchOutside(false);

                alertDialogHint.show();
            }
        });

    }

    private void createAlertDialogGetDrawOverlaysPermission(){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder alertDialogBuilderHint = new AlertDialog.Builder(MainActivity.this,R.style.BlueThemeDialog);
                alertDialogBuilderHint.setTitle("請允許懸浮視窗權限");
                alertDialogBuilderHint.setMessage("沒有懸浮視窗權限將無法使用iMDS");

                alertDialogBuilderHint.setNegativeButton("退出",((dialog, which) -> {
                    finish();
                }));
                alertDialogBuilderHint.setPositiveButton("允許",((dialog, which) -> {
                    UniversalFunction.getOverlayPermission(MainActivity.this ,MainActivity.this);
                }));

                AlertDialog alertDialogHint = alertDialogBuilderHint.create();
                alertDialogHint.setCancelable(false);
                alertDialogHint.setCanceledOnTouchOutside(false);

                alertDialogHint.show();
            }
        });

    }

    private void F_start_ResidentService(){

        //啟動服務
        if(!(UniversalFunction.isServiceRunning(getApplicationContext(),"com.example.mdm_ycnt.ResidentService"))){
            Intent service = new Intent(getApplicationContext(),ResidentService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getApplicationContext().startForegroundService(service);
            }else {
                getApplicationContext().startService(service);
            }
        }
    }






}