package com.example.mdm_ycnt;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.Context.MODE_PRIVATE;
import static android.security.KeyStore.getApplicationContext;
import static java.lang.Thread.sleep;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class UniversalFunction {

    public static String GetServerIP(Context mContext){


        String url_domain = "http://192.168.89.140/mdm/";

        /*String url_domain = mContext.getSharedPreferences("mdm_ycnt", MODE_PRIVATE)
                            .getString("server_ip", mContext.getResources().getString(R.string.mdm_default_url));*/

        return url_domain;

    }

    public static ArrayList<String> getInstalledControlAppList(Context mContext){

        String[] control_app_arr = mContext.getResources().getStringArray(R.array.control_app_array);

        ArrayList<String> packageNameArr = new ArrayList<String>();

        for (String packageName:control_app_arr) {

            PackageManager pm = mContext.getPackageManager();
            PackageInfo packageInfo = null;
            try {
                packageInfo = pm.getPackageInfo(packageName, 0);

            } catch (PackageManager.NameNotFoundException e) {
                //packageInfo = null;

            }

            if (packageInfo != null) {
                packageNameArr.add(packageName);
            }

        }

        return packageNameArr;
    }

    public static void downloadApp(String apkDownloadUr ,String apkName ,String defaultPath ,String subPath ,boolean isFinishActivity ,Context mContext ,Activity activity){


        String apkPath = defaultPath + subPath + "/" + apkName;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                ProgressDialog pd = new ProgressDialog(mContext,R.style.BlueAlertDialogStyle);

                pd.setTitle("下載中...");
                pd.setCancelable(false);
                pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pd.setMax(100);
                pd.show();

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        OutputStream os = null;
                        InputStream is = null;
                        URLConnection conn = null;

                        try {
                            URL url = new URL(apkDownloadUr);

                            //打開連接
                            conn = url.openConnection();

                            //打開輸入流
                            is = conn.getInputStream();

                            //獲得長度
                            int contentLength = conn.getContentLength();
                            //Log.v("Tag", "contentLength = " + contentLength);

                            //String path = "/iCast/Apk";
                            String[] subPathArr = subPath.split("/");
                            //String defaultPath = String.valueOf(mContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS));

                            String checkPatch = defaultPath;

                            for (String str : subPathArr){
                                checkPatch = checkPatch + "/" + str;
                                File file = new File(checkPatch);
                                if (!file.exists()) {
                                    file.mkdir();
                                }
                            }


                            //String DownloadPath = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)+"/iCast/Apk/";
                            //String ApkName = "iCastUpgrade.apk";

                            //下載後的文件名
                            //String fileName = defaultPath + subPath + "/" + apkName;
                            File apkFile = new File(apkPath);
                            if (apkFile.exists()) {
                                apkFile.delete();
                            }

                            //創建字節流
                            byte[] bs = new byte[1024];
                            int len;
                            long total = 0;
                            os = new FileOutputStream(apkPath);

                            //寫數據
                            while ((len = is.read(bs)) != -1) {
                                total += len;
                                os.write(bs, 0, len);

                                int val = (int) ((total * 100) / contentLength);
                                ProgressDialogSetProgress(val ,activity ,pd);


                            }

                        } catch (Exception e) {
                            e.printStackTrace();

                        }finally {
                            //clean up
                            //完成後關閉流
                            try {
                                if(os!=null) {
                                    os.close();
                                }
                                if(is!=null){
                                    is.close();
                                }

                                if(pd.isShowing()){
                                    pd.dismiss();
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if(conn!=null) {
                                //conn.disconnect();
                                ((HttpURLConnection) conn).disconnect();
                            }

                            installApk(apkPath ,isFinishActivity ,activity);
                        }

                    }
                }).start();

            }
        });





    }

    public static void installApk(String apkPath ,boolean isFinishActivity ,Activity activity){

        File LocalFile = new File(apkPath);//DownloadPath + ApkName
        Intent intent = new Intent(Intent.ACTION_VIEW);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", LocalFile);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //intent.setAction(PACKAGE_INSTALLED_ACTION);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");


        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                getApplicationContext().startActivity(intent);

                if(isFinishActivity){
                    activity.finish();
                }

            }
        });

    }

    private static void ProgressDialogSetProgress(int val, Activity activity, ProgressDialog pd){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pd.setProgress(val);
            }
        });
    }

    public static boolean checkPermission(Context mContext ,String permission) {

        //WRITE_EXTERNAL_STORAGE

        int WriteExternalStorage = ContextCompat.checkSelfPermission(mContext.getApplicationContext(), permission);

        return WriteExternalStorage == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean requestPermission(Activity activity ,Context mContext ,String permission ,int requestCode) {

        //Manifest.permission.WRITE_EXTERNAL_STORAGE
        boolean hasPermission = checkPermission(mContext ,permission);

        //WRITE_EXTERNAL_STORAGE - requestCode: 0 不重新onCreate
        //WRITE_EXTERNAL_STORAGE - requestCode: 100 重新onCreate

        if(!hasPermission){
            ActivityCompat.requestPermissions(activity,new String[]{permission},requestCode);
        }

        return hasPermission;

    }

    public static void getInstallPermission(Context mContext , Activity activity){

        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + mContext.getPackageName()));

        //intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        activity.startActivityForResult(intent, 1);

    }

    ///懸浮視窗權限
    public static void getOverlayPermission(Context mContext , Activity activity){
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + mContext.getPackageName()));

        //intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        activity.startActivityForResult(intent, 2);

    }

    public static String getAppVersionName(String packageName){
        try {

            PackageManager pm = getApplicationContext().getPackageManager();
            PackageInfo pInfo = pm.getPackageInfo(packageName, 0);

            return pInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace();

            return "無法取得版本";
        }
    }

    public static int getAppVersionCode(String packageName){
        try {

            PackageManager pm = getApplicationContext().getPackageManager();
            PackageInfo pInfo = pm.getPackageInfo(packageName, 0);

            return pInfo.versionCode;

        } catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace();

            return -1;
        }
    }

    public static boolean hasInstalledThisApp(String packageName){
        try {

            PackageManager pm = getApplicationContext().getPackageManager();
            PackageInfo pInfo = pm.getPackageInfo(packageName, 0);

            return true;

        } catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace();

            return false;
        }
    }

    public static JSONObject HttpPostData(String phpUrl ,JSONObject jsonObjectData){

        OutputStream os = null;
        InputStream is = null;
        HttpURLConnection conn = null;

        JSONObject resultJson = null;

        try {

            String url_domain = UniversalFunction.GetServerIP(getApplicationContext());

            //String phpUrl = url_domain + HttpPost_updateDeviceModelAndGetDownloadUrl;

            String fullUrl = url_domain + phpUrl;

            URL url = new URL(fullUrl);
            /*JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", F_get_system_name());
            jsonObject.put("device_model", deviceModel);*/

            String message = jsonObjectData.toString();

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

        }
        finally {
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

    //getAndroidID
    public static String F_getDeviceId(){
        String device_id = getApplicationContext().getSharedPreferences("mdm_ycnt", MODE_PRIVATE).getString("device_id", "noID");

        return device_id;
    }

    //生成QRcode
    public static void F_show_qrcode(ImageView img_qrcode, String url){
        try{
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();

            Map map = new HashMap();
            //  設定容錯率 L>M>Q>H 等級越高掃描時間越長,準確率越高
            map.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            //設定字元集
            map.put(EncodeHintType.CHARACTER_SET,"utf-8");
            //設定外邊距
            map.put(EncodeHintType.MARGIN,0);

            Bitmap bitmap = barcodeEncoder.encodeBitmap(url, BarcodeFormat.QR_CODE,400,400,map);
            img_qrcode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject check_imds_app_version(){

        //newest_ver:最新版
        //safe_ver:安全範圍內 但不是最新
        //higher_than_max_ver:版本過高
        //lower_than_min_ver:版本過低
        //unregistered:未註冊
        //error:錯誤

        JSONObject getJson = null;
        String phpUrl = "php/app_php/mdm_get_imds_app_update_info.php";

        int nowAppVersionCode = UniversalFunction.getAppVersionCode(BuildConfig.APPLICATION_ID);

        if(nowAppVersionCode != -1){
            try {

                JSONObject jsonObjectData = new JSONObject();
                jsonObjectData.put("id", UniversalFunction.F_getDeviceId());
                jsonObjectData.put("nowAppVersionCode", nowAppVersionCode);

                getJson = UniversalFunction.HttpPostData(phpUrl ,jsonObjectData);


            } catch (JSONException e) {
                e.printStackTrace();

            }
        }


        return getJson;
    }

    public static JSONObject check_control_app_version(){

        //newest_ver:最新版
        //higher_than_max_ver:版本過高
        //lower_than_min_ver:版本過低
        //error:錯誤

        JSONObject getJson = null;
        String phpUrl = "php/app_php/mdm_get_control_app_update_info.php";

        String controlAppPackageName = getApplicationContext().getSharedPreferences("mdm_ycnt",MODE_PRIVATE).getString("control_app_package_name",null);

        if(controlAppPackageName != null){

            int nowAppVersionCode = UniversalFunction.getAppVersionCode(controlAppPackageName);

            if(nowAppVersionCode != -1){

                try {

                    JSONObject jsonObjectData = new JSONObject();
                    jsonObjectData.put("packageName", controlAppPackageName);
                    jsonObjectData.put("nowAppVersionCode", nowAppVersionCode);

                    getJson = UniversalFunction.HttpPostData(phpUrl ,jsonObjectData);


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }


        return getJson;
    }

    public static boolean isServiceRunning(Context mContext, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(30);
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
