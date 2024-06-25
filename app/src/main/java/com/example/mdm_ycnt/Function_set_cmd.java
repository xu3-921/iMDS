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
     * 生成無聲廣播 - 跑馬燈
     *
     * @param getValue 無聲廣播參數-JSON格式
     *                 參數TYPE:
     *                 - text: 無聲廣播內容
     *                 - timeType: 播放時間類型 (designatedTime-指定時間 / durationTime-持續時間)
     *                 - designatedTime: 讀 designatedTime
     *                 - durationTime: 讀 time持續時間(s)
     *                 - mediaId: 廣播的ID
     *                 - textSize: 字體大小
     *                 - textColor: 字體顏色
     *                 - backgroundColor: 背景顏色
     *                 - setPosition: 無聲廣播位置 (top / center / bottom)
     *
     * @param mContext 上下文
     * @param nowTime 當前時間 yyyy-MM-dd HH:mm:ss
     *
     * @since 1.0.5
     * @author J Lee
     */
    public void setSilentBroadcast(String getValue, Context mContext, String nowTime){

        try {
            JSONObject getJson = new JSONObject(getValue);

            int playingSecond = getMediaTimeTypeAndCalculate(getJson, nowTime);

            getJson.put("windowType", "silentBroadcast");
            getJson.put("mediaType", "silentBroadcast");
            getJson.put("time", playingSecond);

            Intent intent = new Intent(mContext, FloatingWindowService.class);
            intent.putExtra("action", "add");
            intent.putExtra("data", String.valueOf(getJson));

            mContext.startService(intent);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * 生成無聲廣播 - 文字
     *
     * @param getValue 無聲廣播參數-JSON格式
     *                 參數TYPE:
     *                 - text: 無聲廣播內容
     *                 - timeType: 播放時間類型 (designatedTime-指定時間 / durationTime-持續時間)
     *                 - designatedTime: 讀 designatedTime
     *                 - durationTime: 讀 time持續時間(s)
     *                 - mediaId: 廣播的ID
     *                 - textSize: 字體大小
     *                 - textColor: 字體顏色
     *                 - backgroundColor: 背景顏色
     *
     * @param mContext 上下文
     * @param nowTime 當前時間 yyyy-MM-dd HH:mm:ss
     *
     * @since 1.0.9
     * @author J Lee
     */
    public void setTextBroadcast(String getValue, Context mContext, String nowTime){

        try {
            JSONObject getJson = new JSONObject(getValue);

            int playingSecond = getMediaTimeTypeAndCalculate(getJson, nowTime);

            getJson.put("windowType", "text");
            getJson.put("mediaType", "text");
            getJson.put("time", playingSecond);

            Intent intent = new Intent(mContext, FloatingWindowService.class);
            intent.putExtra("action", "add");
            intent.putExtra("data", String.valueOf(getJson));

            mContext.startService(intent);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * 使用瀏覽器開啟一個指定的URL
     *
     * @param getValue 取得的網址
     * @param mContext 上下文
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

    /**
     * 生成聲音廣播 - 透過webView開啟
     *
     * @param getValue 聲音廣播參數-JSON格式
     *                 參數TYPE:
     *                 - url : 音檔url
     *                 - timeType: 播放時間類型 (designatedTime-指定時間 / durationTime-持續時間)
     *                 - designatedTime: 讀 designatedTime
     *                 - durationTime: 讀 time持續時間(s)
     *                 - mediaId : 廣播的ID
     *                 - width : 寬 (dp)
     *                 - height : 高 (dp)
     *                 - x : 向右偏移最左邊的值 (dp)
     *                 - y : 向下偏移最上方的值 (dp)
     *
     * @param mContext 上下文
     * @param nowTime 當前時間 yyyy-MM-dd HH:mm:ss
     *
     * @since 1.0.6
     * @author J Lee
     */
    public void F_set_audio(String getValue, Context mContext, String nowTime){

        try {

            JSONObject getJson = new JSONObject(getValue);

            int playingSecond = getMediaTimeTypeAndCalculate(getJson, nowTime);

            getJson.put("windowType", "web");
            getJson.put("mediaType", "audio");
            getJson.put("time", playingSecond);

            Intent intent = new Intent(mContext, FloatingWindowService.class);
            intent.putExtra("action", "add");
            intent.putExtra("data", String.valueOf(getJson));

            mContext.startService(intent);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成影片廣播 - 透過webView開啟
     *
     * @param getValue 影片廣播參數-JSON格式
     *                 參數TYPE:
     *                 - url : 影片url
     *                 - timeType: 播放時間類型 (designatedTime-指定時間 / durationTime-持續時間)
     *                 - designatedTime: 讀 designatedTime
     *                 - durationTime: 讀 time持續時間(s)
     *                 - mediaId : 廣播的ID
     *                 - width : 寬 (dp)
     *                 - height : 高 (dp)
     *                 - x : 向右偏移最左邊的值 (dp)
     *                 - y : 向下偏移最上方的值 (dp)
     *
     * @param mContext 上下文
     * @param nowTime 當前時間 yyyy-MM-dd HH:mm:ss
     *
     * @since 1.0.6
     * @author J Lee
     */
    public void F_set_video(String getValue, Context mContext, String nowTime){

        try {

            JSONObject getJson = new JSONObject(getValue);

            int playingSecond = getMediaTimeTypeAndCalculate(getJson, nowTime);

            getJson.put("windowType", "web");
            getJson.put("mediaType", "video");
            getJson.put("time", playingSecond);

            Intent intent = new Intent(mContext, FloatingWindowService.class);
            intent.putExtra("action", "add");
            intent.putExtra("data", String.valueOf(getJson));

            mContext.startService(intent);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * 生成圖片廣播
     *
     * @param getValue 影片廣播參數-JSON格式
     *                 參數TYPE:
     *                 - url : 圖片url
     *                 - timeType: 播放時間類型 (designatedTime-指定時間 / durationTime-持續時間)
     *                 - designatedTime: 讀 designatedTime
     *                 - durationTime: 讀 time持續時間(s)
     *                 - mediaId : 廣播的ID
     *                 - width : 寬 (dp)
     *                 - height : 高 (dp)
     *                 - x : 向右偏移最左邊的值 (dp)
     *                 - y : 向下偏移最上方的值 (dp)
     *
     * @param mContext 上下文
     * @param nowTime 當前時間 yyyy-MM-dd HH:mm:ss
     *
     * @since 1.0.6
     * @author J Lee
     */
    public void F_set_image(String getValue, Context mContext, String nowTime){

        try {

            JSONObject getJson = new JSONObject(getValue);

            int playingSecond = getMediaTimeTypeAndCalculate(getJson, nowTime);

            getJson.put("windowType", "image");
            getJson.put("mediaType", "image");
            getJson.put("time", playingSecond);

            Intent intent = new Intent(mContext, FloatingWindowService.class);
            intent.putExtra("action", "add");
            intent.putExtra("data", String.valueOf(getJson));

            mContext.startService(intent);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * 關閉指定mediaId的FloatingWindow
     *
     * @param getValue 關閉的參數-JSON格式
     *                 參數TYPE:
     *                 - mediaId : 廣播的ID
     *
     * @param mContext 上下文
     *
     * @since 1.0.6
     */
    public void F_stop_floating_window(String getValue, Context mContext){

        Intent removeIntent = new Intent(mContext, FloatingWindowService.class);
        removeIntent.putExtra("action", "remove");
        removeIntent.putExtra("data", getValue);
        mContext.startService(removeIntent);

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

    /**
     * 計算media的播放時間
     *
     * @param dataJson 獲取dataJson中的 timeType/designatedTime/time
     *                 timeType分為designatedTime(指定時間) / durationTime(持續時間)
     *
     *                 designatedTime時拿designatedTime(HH:mm:ss) 計算出 持續執行時間(s)
     *                 durationTime時拿time,time就是 持續執行時間(s)
     *
     * @param nowTime 當前時間 yyyy-MM-dd HH:mm:ss
     *
     * @return 持續執行時間(s)
     */
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
