package com.example.mdm_ycnt;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.WINDOW_SERVICE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class Function_set_cmd {

    int defaultTime = 180;

    /**
     * iMDS 生成 無聲廣播-跑馬燈
     *
     * @author J Lee
     * @since 1.0.5
     */
    public void F_set_marquee(String getValue, Context mContext, String nowTime){

        //Log.e("test03",getValue);

        //{"set-marquee":"{\"text\":\"請班長到教務處找高主任\",\"time\":\"300\"}"}

        /*
        * set-marquee - type
        *
        * text : 無聲廣播內容
        * timeType : 播放時間類型(designatedTime-指定時間 / durationTime-持續時間)
        * designatedTime 讀 designatedTime
        * durationTime 讀 time持續時間(s)
        *
        * mediaId : 廣播的ID
        * textSize : 字體大小
        * textColor : 字體顏色
        * backgroundColor : 背景顏色
        * setPosition : 無聲廣播位置 (top / center /bottom)
        * */


        try {
            JSONObject getJson = new JSONObject(getValue);

            String getText = getJson.getString("text");//.replaceAll("\\R", "  ")
            int mediaId = getJson.getInt("mediaId");
            int playingSecond = getMediaTimeTypeAndCalculate(getJson, nowTime);

            int textSize = getJson.optInt("textSize");
            String textColor = getJson.optString("textColor");
            String backgroundColor = getJson.optString("backgroundColor");
            String setPosition = getJson.optString("setPosition");


            Intent intent = new Intent(mContext, FloatingWindowService.class);
            //必須
            intent.putExtra("action", "add");
            intent.putExtra("windowType", "silentBroadcast");
            intent.putExtra("mediaType", "silentBroadcast");
            intent.putExtra("viewId", mediaId);
            intent.putExtra("textInfo", getText);
            intent.putExtra("time", playingSecond);

            //選填
            if(textSize != 0){
                intent.putExtra("textSize", textSize);
            }

            if(textColor.length() > 0){
                intent.putExtra("textColor", textColor);
            }

            if(backgroundColor.length() > 0){
                intent.putExtra("backgroundColor", backgroundColor);
            }

            if(setPosition.length() > 0){
                intent.putExtra("setPosition", setPosition);
            }


            mContext.startService(intent);


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     *
     * iMDS 使用瀏覽器開啟一個指定的URL
     *
     * @param getValue webUrl
     * @param mContext Context
     *
     * @author J Lee
     * @since 1.0.6
     */
    public void F_set_open_web(String getValue, Context mContext){

        try {
            JSONObject getJson = new JSONObject(getValue);
            String getUrl = getJson.getString("url");

            Uri uri = Uri.parse(getUrl);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //1.0.6
    public void F_set_audio(String getValue, Context mContext, String nowTime){

        /*
         * set-audio - type
         *
         * url : 音檔url
         * time : 持續時間
         * mediaId : 廣播的ID
         * width : 寬 (dp)
         * height : 高 (dp)
         * x : 向右偏移最左邊的值 (dp)
         * y : 向下偏移最上方的值 (dp)
         * */

        try {
            JSONObject getJson = new JSONObject(getValue);

            String url = getJson.getString("url");
            int mediaId = getJson.getInt("mediaId");
            int playingSecond = getMediaTimeTypeAndCalculate(getJson, nowTime);

            int width = getJson.optInt("width");
            int height = getJson.optInt("height");

            int x = getJson.optInt("x");
            int y = getJson.optInt("y");

            width = width != 0 ? width : 300;
            height = height != 0 ? height : 200;

            x = x != 0 ? x : 20;
            y = y != 0 ? y : 90;

            Intent intent = new Intent(mContext, FloatingWindowService.class);
            //必填
            intent.putExtra("action", "add");
            intent.putExtra("windowType", "web");
            intent.putExtra("mediaType", "audio");
            intent.putExtra("viewId", mediaId);
            intent.putExtra("url", url);
            intent.putExtra("time", playingSecond);
            //選填
            intent.putExtra("width", width);
            intent.putExtra("height", height);
            intent.putExtra("x", x);
            intent.putExtra("y", y);

            mContext.startService(intent);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //1.0.6
    public void F_set_video(String getValue, Context mContext, String nowTime){

        /*
         * set-vidoe - type
         *
         * url : 影片url
         * time : 持續時間
         * mediaId : 廣播的ID
         * width : 寬 (dp)
         * height : 高 (dp)
         * x : 向右偏移最左邊的值 (dp)
         * y : 向下偏移最上方的值 (dp)
         * */

        try {
            JSONObject getJson = new JSONObject(getValue);

            String url = getJson.getString("url");

            int mediaId = getJson.getInt("mediaId");
            int playingSecond = getMediaTimeTypeAndCalculate(getJson, nowTime);

            int width = getJson.optInt("width");
            int height = getJson.optInt("height");

            width = width != 0 ? width : -1;
            height = height != 0 ? height : -1;

            int x = getJson.optInt("x");
            int y = getJson.optInt("y");


            Intent intent = new Intent(mContext, FloatingWindowService.class);
            //必填
            intent.putExtra("action", "add");
            intent.putExtra("windowType", "web");
            intent.putExtra("mediaType", "video");
            intent.putExtra("viewId", mediaId);
            intent.putExtra("url", url);
            intent.putExtra("time", playingSecond);
            //選填
            intent.putExtra("width", width);
            intent.putExtra("height", height);
            intent.putExtra("x", x);
            intent.putExtra("y", y);

            mContext.startService(intent);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //1.0.6
    public void F_set_image(String getValue, Context mContext, String nowTime){

        /*
         * set-photo - type
         *
         * url : 圖片url
         * time : 持續時間
         * mediaId : 廣播的ID
         * width : 寬 (dp)
         * height : 高 (dp)
         * x : 向右偏移最左邊的值 (dp)
         * y : 向下偏移最上方的值 (dp)
         * */

        try {

            JSONObject getJson = new JSONObject(getValue);

            String url = getJson.getString("url");
            int mediaId = getJson.getInt("mediaId");
            int playingSecond = getMediaTimeTypeAndCalculate(getJson, nowTime);

            int width = getJson.optInt("width");
            int height = getJson.optInt("height");

            width = width != 0 ? width : -1;
            height = height != 0 ? height : -1;

            int x = getJson.optInt("x");
            int y = getJson.optInt("y");

            Intent intent = new Intent(mContext, FloatingWindowService.class);
            //必填
            intent.putExtra("action", "add");
            intent.putExtra("windowType", "image");
            intent.putExtra("mediaType", "image");
            intent.putExtra("viewId", mediaId);
            intent.putExtra("url", url);
            intent.putExtra("time", playingSecond);
            //選填
            intent.putExtra("width", width);
            intent.putExtra("height", height);
            intent.putExtra("x", x);
            intent.putExtra("y", y);

            mContext.startService(intent);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //1.0.6
    public void F_stop_floating_window(String getValue, Context mContext){

        try {
            JSONObject getJson = null;
            getJson = new JSONObject(getValue);

            int mediaId = getJson.getInt("mediaId");

            Intent removeIntent = new Intent(mContext, FloatingWindowService.class);
            removeIntent.putExtra("action", "remove");
            removeIntent.putExtra("viewId", mediaId);
            mContext.startService(removeIntent);

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public void F_set_install_app(String getJson, Context mContext){

        try {
            JSONObject jsonObject = new JSONObject(getJson);

            String apkUrl = jsonObject.getString("path");

            String componentsType = jsonObject.optString("componentsType");
            String componentsName = jsonObject.optString("componentsName");
            String packageName = jsonObject.optString("packageName");

            F_download_and_install_apk(apkUrl, componentsType, componentsName, packageName, mContext);

        } catch (JSONException e) {
            Log.e("test09","testERR");
            e.printStackTrace();
        }

    }

    public void F_download_and_install_apk(String apkUrl, String componentsType,
                                           String componentsName, String packageName, Context mContext){

        String defaultPath = String.valueOf(mContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS));
        int lastIndex = apkUrl.lastIndexOf('/');
        String apkName = apkUrl.substring(lastIndex + 1);

        String subPath = "";

        try {

            downloadApp(apkUrl ,apkName ,defaultPath ,subPath);

            ResidentService instance = new ResidentService();

            String G_control_app_package_name = mContext.getSharedPreferences("mdm_ycnt", MODE_PRIVATE)
                                                .getString("control_app_package_name", "no_val");


            JSONObject cmdJson = new JSONObject();
            JSONObject dataJson = new JSONObject();

            String apkPath = defaultPath + subPath + "/" + apkName;

            dataJson.put("path",apkPath);

            if(componentsType != null){
                dataJson.put("componentsType",componentsType);
            }

            if(componentsName != null){
                dataJson.put("componentsName",componentsName);
            }

            if(packageName != null){
                dataJson.put("packageName",packageName);
            }

            cmdJson.put("set-install-apk",dataJson);

            instance.F_sendCMD(G_control_app_package_name, String.valueOf(cmdJson), mContext);
//            instance.F_sendCMD(G_control_app_package_name,
//                    "{\"set-install-apk\":\""+defaultPath + subPath + "/" + apkName+"\"}"
//                    , mContext);

        } catch (InterruptedException | JSONException e) {
            Log.e("test09","testERR02");
            e.printStackTrace();

        }

    }

    private void downloadApp(String apkDownloadUr , String apkName , String defaultPath , String subPath)
            throws InterruptedException {

        String apkPath = defaultPath + subPath + "/" + apkName;

        Thread thread_download_apk = new Thread(new Runnable() {
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

                    String[] subPathArr = subPath.split("/");

                    String checkPatch = defaultPath;

                    for (String str : subPathArr){
                        checkPatch = checkPatch + "/" + str;
                        File file = new File(checkPatch);
                        if (!file.exists()) {
                            file.mkdir();
                        }
                    }


                    //下載後的文件名
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
                        //Log.e("test0344",String.valueOf(val));
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

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if(conn!=null) {
                        //conn.disconnect();
                        ((HttpURLConnection) conn).disconnect();
                    }

                }

            }
        });

        thread_download_apk.start();
        thread_download_apk.join();

    }


    private int getMediaTimeTypeAndCalculate(JSONObject dataJson, String nowTime) throws JSONException {

        int playingSecond = defaultTime;

        String timeType = dataJson.getString("timeType");

        if(timeType.equals("designatedTime") && nowTime != null){

            String designatedTime = dataJson.optString("designatedTime");
            designatedTime = (designatedTime != null && !designatedTime.isEmpty()) ? designatedTime : "00:00:00";

            long secondsDifference = calculateSecondsDifference(nowTime, designatedTime);
            playingSecond = (int)secondsDifference;
        }
        else if(timeType.equals("durationTime")){

            playingSecond = dataJson.optInt("time");
            playingSecond = playingSecond != 0 ? playingSecond : defaultTime;

        }

        return playingSecond;
    }


    private long calculateSecondsDifference(String dateTimeWithDate, String timeWithoutDate){

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        LocalDateTime dateTimeWithDateObj = LocalDateTime.parse(dateTimeWithDate, dateTimeFormatter);
        LocalTime timeWithoutDateObj = LocalTime.parse(timeWithoutDate, timeFormatter);

        // 判斷是否為同一天或第二天
        LocalDateTime endDateTime;
        if (timeWithoutDateObj.isAfter(dateTimeWithDateObj.toLocalTime()) || timeWithoutDateObj.equals(dateTimeWithDateObj.toLocalTime())) {
            // 同一天
            endDateTime = dateTimeWithDateObj.toLocalDate().atTime(timeWithoutDateObj);
        } else {
            // 第二天
            endDateTime = dateTimeWithDateObj.toLocalDate().plusDays(1).atTime(timeWithoutDateObj);
        }

        // 計算時間差
        Duration duration = Duration.between(dateTimeWithDateObj, endDateTime);
        return duration.getSeconds();
    }

}
