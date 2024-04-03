package com.example.mdm_ycnt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class UploadMessageReceiver extends BroadcastReceiver {

    private final UniversalFunction universalFunction_instance = new UniversalFunction();

    @Override
    public void onReceive(Context context, Intent intent) {
        //iMdsGetToUploadMessage

        Bundle bundle = intent.getExtras();
        String action = bundle.getString("action");
        String path = bundle.getString("path");


        if(action.equals("upload")){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    uploadScreenShot(path, context);
                }
            }).start();
        }

    }

    public void uploadScreenShot(String imagePath, Context mContext){

        String base64Image = universalFunction_instance.convertToBase64String(imagePath);

        if(base64Image != null){

            JSONObject dataJson = new JSONObject();

            try {
                dataJson.put("deviceId",UniversalFunction.F_getDeviceId(mContext));
                dataJson.put("base64",base64Image);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String url_domain = UniversalFunction.GetServerIP(mContext);
            String phpUrl = url_domain + "php/app_php/mdm_upload_screen_shot.php";

            JSONObject getJson = universalFunction_instance.httpPostData(dataJson, phpUrl);

            //remove jpg
            File file = new File(imagePath);
            if (file.exists()) {
                boolean deleted = file.delete();
//                if (deleted) {
//                    // 文件删除成功
//                } else {
//                    // 文件删除失败
//                }
            }

        }



    }
}