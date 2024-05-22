package com.example.mdm_ycnt;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.Context.MODE_PRIVATE;

import static java.lang.Thread.sleep;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.mdm_ycnt.aidlBindHelper.AsyncAidlTool;
import com.example.mdm_ycnt.aidlBindHelper.ServiceBinderHelper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public class UniversalFunction {

    public static String GetServerIP(Context mContext){

        String url_domain = mContext.getSharedPreferences("mdm_ycnt", MODE_PRIVATE)
                            .getString("server_ip", mContext.getResources().getString(R.string.mdm_default_url));

//        String url_domain = "http://192.168.89.233/mdm/";
//        String url_domain = "https://imds.tw/imdsTest/";

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

    public static void downloadApp(String apkDownloadUr,
                                   String apkName,
                                   String defaultPath,
                                   String subPath,
                                   boolean isFinishActivity,
                                   Context mContext,
                                   Activity activity){


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

                            installApk(apkPath,isFinishActivity,activity,mContext);
                        }

                    }
                }).start();

            }
        });

    }

    public static void installApk(String apkPath ,boolean isFinishActivity ,Activity activity, Context mContext){

        File LocalFile = new File(apkPath);//DownloadPath + ApkName
        Intent intent = new Intent(Intent.ACTION_VIEW);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider", LocalFile);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //intent.setAction(PACKAGE_INSTALLED_ACTION);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");


        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mContext.startActivity(intent);

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

        int WriteExternalStorage = ContextCompat.checkSelfPermission(mContext, permission);

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

    public static String getAppVersionName(String packageName, Context mContext){
        try {

            PackageManager pm = mContext.getPackageManager();
            PackageInfo pInfo = pm.getPackageInfo(packageName, 0);

            return pInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace();

            return "無法取得版本";
        }
    }

    public static int getAppVersionCode(String packageName, Context mContext){
        try {

            PackageManager pm = mContext.getPackageManager();
            PackageInfo pInfo = pm.getPackageInfo(packageName, 0);

            return pInfo.versionCode;

        } catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace();

            return -1;
        }
    }

    public static boolean hasInstalledThisApp(String packageName, Context mContext){
        try {

            PackageManager pm = mContext.getPackageManager();
            PackageInfo pInfo = pm.getPackageInfo(packageName, 0);

            return true;

        } catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace();

            return false;
        }
    }

    public static JSONObject HttpPostData(String phpUrl ,JSONObject jsonObjectData, Context mContext){

        OutputStream os = null;
        InputStream is = null;
        HttpURLConnection conn = null;

        JSONObject resultJson = null;

        try {

            String url_domain = UniversalFunction.GetServerIP(mContext);

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
    public static String F_getDeviceId(Context mContext){
        String deviceId = mContext.getSharedPreferences("mdm_ycnt", MODE_PRIVATE)
                            .getString("device_id", "noID");

        return deviceId;
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

    public static JSONObject check_imds_app_version(Context mContext){

        //newest_ver:最新版
        //safe_ver:安全範圍內 但不是最新
        //higher_than_max_ver:版本過高
        //lower_than_min_ver:版本過低
        //unregistered:未註冊
        //error:錯誤

        JSONObject getJson = null;
        String phpUrl = "php/app_php/mdm_get_imds_app_update_info.php";

        int nowAppVersionCode = UniversalFunction.getAppVersionCode(BuildConfig.APPLICATION_ID, mContext);

        if(nowAppVersionCode != -1){
            try {

                JSONObject jsonObjectData = new JSONObject();
                jsonObjectData.put("id", UniversalFunction.F_getDeviceId(mContext));
                jsonObjectData.put("nowAppVersionCode", nowAppVersionCode);

                getJson = UniversalFunction.HttpPostData(phpUrl ,jsonObjectData, mContext);


            } catch (JSONException e) {
                e.printStackTrace();

            }
        }


        return getJson;
    }

    public static JSONObject check_control_app_version(Context mContext){

        //newest_ver:最新版
        //higher_than_max_ver:版本過高
        //lower_than_min_ver:版本過低
        //error:錯誤

        JSONObject getJson = null;
        String phpUrl = "php/app_php/mdm_get_control_app_update_info.php";

        String controlAppPackageName = mContext.getSharedPreferences("mdm_ycnt",MODE_PRIVATE).getString("control_app_package_name",null);

        if(controlAppPackageName != null && controlAppPackageName != "otherDevicePackageName"){

            int nowAppVersionCode = UniversalFunction.getAppVersionCode(controlAppPackageName, mContext);

            if(nowAppVersionCode != -1){

                try {

                    JSONObject jsonObjectData = new JSONObject();
                    jsonObjectData.put("packageName", controlAppPackageName);
                    jsonObjectData.put("nowAppVersionCode", nowAppVersionCode);

                    getJson = UniversalFunction.HttpPostData(phpUrl ,jsonObjectData, mContext);


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

    public JSONObject httpPostData(JSONObject jsonData, String phpUrl){

        OutputStream os = null;
        InputStream is = null;
        HttpURLConnection conn = null;


        try {

            URL url = new URL(phpUrl);

//            jsonObject.put("id", DeviceId);
//            jsonObject.put("update", info_json);

            String postData = jsonData.toString();

            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10 * 1000);
            conn.setConnectTimeout(15 * 1000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setFixedLengthStreamingMode(postData.getBytes().length);

            //make some HTTP header nicety
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");

            //open
            conn.connect();

            //setup send
            os = new BufferedOutputStream(conn.getOutputStream());
            os.write(postData.getBytes());

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

                if(sb.toString().length() == 0){
                    return null;
                }else{
                    return new JSONObject(sb.toString());
                }

            }

        }
        catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        finally {
            //clean up
            try {
                if(os != null) {
                    os.close();
                }
                if(is != null){
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(conn != null) {
                conn.disconnect();
            }
        }

        return null;
    }

    public String generateRandomString(int textLength){

        final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        SecureRandom random = new SecureRandom();
        StringBuilder stringBuilder = new StringBuilder(textLength);

        for (int i = 0; i < textLength; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            stringBuilder.append(CHARACTERS.charAt(randomIndex));
        }

        return stringBuilder.toString();
    }

    public int compareTimes(String time1, String time2) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            Date date1 = format.parse(time1);
            Date date2 = format.parse(time2);

            return date1.compareTo(date2);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean isSameDay(String time1, String time2) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        try {
            Date date1 = dateFormat.parse(time1);
            Date date2 = dateFormat.parse(time2);

            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(date1);
            cal2.setTime(date2);

            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public long getDaysDifference(String time1, String time2) {
        //如果 date1 晚於 date2，函數將傳回負數；如果 date1 早於 date2，則傳回正數。 如果兩者相等，則傳回 0。

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            Date date1 = dateFormat.parse(time1);
            Date date2 = dateFormat.parse(time2);

            //long diffInMillies = Math.abs(date2.getTime() - date1.getTime());
            long diffInMillies = date2.getTime() - date1.getTime();
            return TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

        } catch (ParseException e) {
            e.printStackTrace();
            return -1; // 或者可以根据需要处理异常
        }
    }

    public String convertToBase64String(String imagePath) {

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

            if(bitmap != null){

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);
                byte[] imageBytes = baos.toByteArray();

                return Base64.encodeToString(imageBytes, Base64.DEFAULT);
            }else {

                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 生成device_id
    @SuppressLint("HardwareIds")
    public String F_get_system_name(Context mContext){

        String getDeviceId =
                mContext.getSharedPreferences("mdm_ycnt", MODE_PRIVATE)
                        .getString("device_id", "noID");


        if(!getDeviceId.equals("noID")){

            return getDeviceId;

        }else{

            String deviceId = null;

            // 查詢是否有支援的設備和是否有安裝App可以取得eth0
            try {
                String aidlControlMapStr = loadJSONFromAsset(mContext, "aidlControlMap.json");
                JSONObject aidlControlMap = new JSONObject(aidlControlMapStr);

                Iterator<String> aidlControlMapKeys = aidlControlMap.keys();

                while(aidlControlMapKeys.hasNext()) {
                    String packageName = aidlControlMapKeys.next();
                    JSONObject value = aidlControlMap.getJSONObject(packageName);

                    int versioncode = (int) checkAppVersion(mContext, packageName);
                    int supportVersionCode = value.getInt("supportVersion");

                    if(versioncode >= supportVersionCode){

                        String serviceAction = value.getString("serviceAction");
                        String interfaceCanonicalName = value.getString("interfaceCanonicalName");

                        String result = aidlToGetDeviceMac(
                                mContext,
                                serviceAction,
                                packageName,
                                interfaceCanonicalName // AIDL介面的完整類別名
                         );

                        if(result != null || !result.equals("error") || result.equals("")){
                            deviceId = result.replace(":","");
                        }

                        break;
                    }

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(deviceId == null || deviceId.equals("error")){

                String getEth0 = getMacAddr_eth0();

                if(!getEth0.equals("not_get_mac")){
                    deviceId = getEth0.replace(":","");
                }

            }

            if(deviceId == null || deviceId.equals("error")){

                deviceId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);

            }

            String deviceIdAddStr = "d"+"9"+"2"+deviceId+"e";
            String md5DeviceId = F_md5(deviceIdAddStr);

            SharedPreferences pref = mContext.getSharedPreferences("mdm_ycnt", MODE_PRIVATE);
            pref.edit().putString("device_id", md5DeviceId).apply();

            return md5DeviceId;

        }

    }

    private String aidlToGetDeviceMac(
            Context mContext, String serviceAction, String servicePackageName, String interfaceCanonicalName
    ){

        AsyncAidlTool asyncAidlTool = new AsyncAidlTool();

        CompletableFuture<Object> future = asyncAidlTool.bindServiceAsyncWithResult(
                mContext,
                serviceAction,
                servicePackageName,
                interfaceCanonicalName // AIDL接口的完整類名
        );

        CompletableFuture<String> resultFuture = new CompletableFuture<>();

        future.thenAccept(serviceConnectionResult -> {

            AsyncAidlTool.ServiceConnectionResult result =(AsyncAidlTool.ServiceConnectionResult) serviceConnectionResult;
            // 處理服務綁定成功的狀況

            // 從結果中取得service,从结果中获取服务对象和ServiceBinderHelper
            Object service = result.getService();
            ServiceBinderHelper<?> binderHelper = result.getBinderHelper();

            // 暫時先寫死com.ycnt.genetouch_v5_control.IMDSControlInterface
            // 往不同App要用不同的class
            com.ycnt.genetouch_v5_control.IMDSControlInterface iMDSInterface =
                    (com.ycnt.genetouch_v5_control.IMDSControlInterface) service;
            try {
                String getEth0 = iMDSInterface.getEth0();

                resultFuture.complete(getEth0);

                binderHelper.unbindService();

            } catch (Exception e) {
//                resultFuture.completeExceptionally(e);
                resultFuture.complete("error");
                e.printStackTrace();
            }

        }).exceptionally(e -> {

            // 處理服務綁定失敗的情況
            e.printStackTrace();
            resultFuture.complete("error");

            return null;
        });

        return resultFuture.join();
    }

    //md5加密
    public static String F_md5(String deviceId){

//        String id = "d"+"9"+"2"+device_id+"e";

        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(deviceId.getBytes("UTF-8"));
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
//    private static String getMacAddr_wlan0() {
//        String Mac=null;
//        try{
//            String path="sys/class/net/wlan0/address";
//            FileInputStream fis_name = new FileInputStream(path);
//            byte[] buffer_name = new byte[8192];
//            int byteCount_name = fis_name.read(buffer_name);
//            if(byteCount_name>0)
//            {
//                Mac = new String(buffer_name, 0, byteCount_name, "utf-8");
//            }
//            if(Mac.length()==0||Mac==null){
//                return "not_get_mac";
//            }
//        }catch(Exception io){
//            return "not_get_mac";
//        }
//        return Mac.trim().toUpperCase();
//    }

    public interface RegistrationCallback {
        void onResult(boolean isRegistered);
    }

    // 非同步檢查設備是否已註冊
    public void isDeviceRegistered(Context mContext, RegistrationCallback callback) throws JSONException {

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    String url_domain = UniversalFunction.GetServerIP(mContext);
                    String HttpPost_isSignUP_url = "php/app_php/mdm_get_device_isSignUp.php";
                    String getDeviceId = F_get_system_name(mContext);
                    String phpUrl = url_domain + HttpPost_isSignUP_url;

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", getDeviceId);

                    JSONObject getJson = httpPostData(jsonObject, phpUrl);

                    String isRegistered = getJson.getString("ad_mdm_device_register_enable");

                    // 使用回调返回结果
                    if (callback != null) {

                        if(isRegistered.equals("T")){
                            callback.onResult(true);
                        }else {
                            callback.onResult(false);
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (callback != null) {
                        callback.onResult(false);
                    }
                }
            }
        }).start();
    }

    public boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDirectory(new File(dir, children[i]));
                if (!success) {
                    return false; // 如果无法删除子文件或目录
                }
            }
        }
        // 删除空目录或文件
        return dir.delete();
    }


    public void clearDirectory(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                File childFile = new File(dir, child);
                if (childFile.isDirectory()) {
                    clearDirectory(childFile); // 递归删除子目录
                }
                childFile.delete(); // 删除文件或空目录
            }
        }
    }

    /**
     * 檢視特定套件名稱的應用程式是否已安裝，並嘗試取得其 versionCode。
     *
     * @param context 上下文
     * @param packageName B 应用的包名
     * @return 如果应用安装了，返回其 versionCode；如果应用未安装，返回 -1。
     */
    public static long checkAppVersion(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                // 对于 Android P 及以上版本
                return packageInfo.getLongVersionCode();
            } else {
                // 对于旧版本的 Android
                return packageInfo.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            // 应用未安装
            return -1;
        }
    }

    public static String loadJSONFromAsset(Context context, String fileName) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    /**
     * 獲得簽名訊息
     *
     * @param mContext Context
     *
     * @return SignatureInfo Map<String, String> 包含签名信息的字符串数组 key [SHA-256,SubjectDN]
     *
     * @since 1.0.9
     * @author J Lee
     */
    public Map<String, String> getSignatureInfo(Context mContext){

        Map<String, String> signatureInfoMap = new HashMap<>();

        try {

            // 取得包名
            String packageName = mContext.getPackageName();

            // 取得包的訊息
            PackageInfo packageInfo = null;
            packageInfo = mContext.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);

            // 获取签名信息
            Signature[] signatures = packageInfo.signatures;

            if (packageInfo.signatures != null) {

                for (Signature signature : signatures) {

                    byte[] signatureBytes = signature.toByteArray();

                    // SHA-256
                    String sha256Fingerprint = getFingerprint(signatureBytes, "SHA-256");

                    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                    X509Certificate cert = (X509Certificate) certFactory.generateCertificate(new java.io.ByteArrayInputStream(signatureBytes));

                    signatureInfoMap.put("SHA-256", sha256Fingerprint);
                    signatureInfoMap.put("SubjectDN", cert.getSubjectDN().toString());

                }

            }

        } catch (PackageManager.NameNotFoundException | CertificateException e) {
            e.printStackTrace();
        }

        return signatureInfoMap;
    }

    private static String getFingerprint(byte[] signature, String algorithm) {

        try {

            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(signature);
            byte[] digest = md.digest();
            return bytesToHex(digest);

        } catch (Exception e) {

            return null;
        }
    }

    private static String bytesToHex(byte[] bytes) {

        StringBuilder hexString = new StringBuilder();

        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString().toUpperCase(Locale.US);

    }

    public static class ClickCounter {

        private final int clickThreshold;
        private final long timeThreshold;

        private int clickCounter = 0;
        private final Handler handler;
        private final Runnable resetClickCounter;
        private final OnThresholdReachedListener listener;

        public interface OnThresholdReachedListener {
            void onThresholdReached();
        }

        public ClickCounter(int clickThreshold, long timeThreshold, OnThresholdReachedListener listener) {

            this.clickThreshold = clickThreshold;
            this.timeThreshold = timeThreshold;

            this.listener = listener;
            this.handler = new Handler();
            this.resetClickCounter = new Runnable() {
                @Override
                public void run() {
                    clickCounter = 0;
                }
            };
        }

        public boolean onClick() {

            clickCounter++;

            if (clickCounter == 1) {
                handler.postDelayed(resetClickCounter, timeThreshold);
            }

            if (clickCounter == clickThreshold) {
                clickCounter = 0;
                handler.removeCallbacks(resetClickCounter);
                if (listener != null) {
                    listener.onThresholdReached();
                }
                return true;
            }

            return false;
        }
    }


}
