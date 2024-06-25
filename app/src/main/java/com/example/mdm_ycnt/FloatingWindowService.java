package com.example.mdm_ycnt;

import android.app.Service;
import android.content.Intent;

import android.os.IBinder;

import com.example.mdm_ycnt.broadcastModal.ImageBroadcast;
import com.example.mdm_ycnt.broadcastModal.MarqueeBroadcast;
import com.example.mdm_ycnt.broadcastModal.TextBroadcast;
import com.example.mdm_ycnt.broadcastModal.WebViewBroadcast;
import com.ycnt.imds.floatingwindow.FloatingLayout;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FloatingWindowService extends Service {

    // 所以FloatingWindow清單
    private Map<String, FloatingLayout> floatingLayoutMap = new HashMap<>();

    // 等待時間到的線程
    private Map<String, Thread> waitThread = new HashMap<>();

    // 紀錄各類型廣播正在播放的數量和ID
    public Map<String, ArrayList<String>> playingMediaList = new HashMap<>();

    final int defaultExecutionTime = 180;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action = intent.getStringExtra("action");
        String data = intent.getStringExtra("data");

        try {
            JSONObject getJson = new JSONObject(data);

            // 移除
            if(action.equals("remove")){
                removeBroadcast(getJson);
            }
            // 新增
            else if(action.equals("add")){

                String windowType = getJson.getString("windowType");
                String mediaId = getJson.getString("mediaId");

                switch (windowType){

                    case "silentBroadcast":
                        addSilentBroadcastModal(getJson);
                        break;

                    case "web":
                        addWebViewModal(getJson);
                        break;

                    case "image":
                        addImageViewModal(getJson);
                        break;

                    case "text":
                        addTextViewModal(getJson);
                        break;
                }

                Object mediaIdList = Singleton.getInstance().getSingletonData();
                if (mediaIdList instanceof ArrayList<?>) {
                    ArrayList<String> list = (ArrayList<String>) mediaIdList;
                    list.add(mediaId);
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        for (String mediaId : floatingLayoutMap.keySet()){
            removeFloatingWindow(mediaId);
        }

        floatingLayoutMap.clear();
        waitThread.clear();
        playingMediaList.clear();

        Object mediaIdList = Singleton.getInstance().getSingletonData();
        if (mediaIdList instanceof ArrayList<?>) {
            ArrayList<String> list = (ArrayList<String>) mediaIdList;
            list.clear();
        }
    }

    private void addWebViewModal(JSONObject getJson){

        try {

            String mediaId = getJson.getString("mediaId");
            String mediaType = getJson.getString("mediaType");

            int time = getJson.optInt("time", defaultExecutionTime);

            // 確認是否有同mediaType的正在播放,如果超過數量限制,結束第一個
            ArrayList<String> currentMediaList = checkMediaPlayingNum(mediaType);

            WebViewBroadcast webViewBroadcast = new WebViewBroadcast(
                    this,
                    getJson,
                    new WebViewBroadcast.OnDestroyListener() {
                        @Override
                        public void onDestroy() {

                            removeFloatingWindow(mediaId);

                        }
                    }
            );

            currentMediaList.add(mediaId);
            playingMediaList.put(mediaType, currentMediaList);

            floatingLayoutMap.put(mediaId, webViewBroadcast);
            waitToCloseFloatingWindow(mediaId, time);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void addSilentBroadcastModal(JSONObject getJson) {

        try {
            String mediaId = getJson.getString("mediaId");
            String mediaType = getJson.getString("mediaType");

            int time = getJson.optInt("time", defaultExecutionTime);

            // 確認是否有同mediaType的正在播放,如果超過數量限制,結束第一個
            ArrayList<String> currentMediaList = checkMediaPlayingNum(mediaType);

            MarqueeBroadcast marqueeBroadcast = new MarqueeBroadcast(
                    this,
                    getJson,
                    new MarqueeBroadcast.OnDestroyListener(){
                        @Override
                        public void onDestroy() {

                            removeFloatingWindow(mediaId);

                        }
                    }
            );

            currentMediaList.add(mediaId);
            playingMediaList.put(mediaType, currentMediaList);

            floatingLayoutMap.put(mediaId, marqueeBroadcast);
            waitToCloseFloatingWindow(mediaId, time);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void addImageViewModal(JSONObject getJson) {

        try {
            String mediaId = getJson.getString("mediaId");
            String mediaType = getJson.getString("mediaType");

            int time = getJson.optInt("time", defaultExecutionTime);

            // 確認是否有同mediaType的正在播放,如果超過數量限制,結束第一個
            ArrayList<String> currentMediaList = checkMediaPlayingNum(mediaType);

            ImageBroadcast imageBroadcast = new ImageBroadcast(
                    this,
                    getJson,
                    new ImageBroadcast.OnDestroyListener(){
                        @Override
                        public void onDestroy() {

                            removeFloatingWindow(mediaId);

                        }

                }
            );

            currentMediaList.add(mediaId);
            playingMediaList.put(mediaType, currentMediaList);

            floatingLayoutMap.put(mediaId, imageBroadcast);
            waitToCloseFloatingWindow(mediaId, time);

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void addTextViewModal(JSONObject getJson) {

        try {

            String mediaId = getJson.getString("mediaId");
            String mediaType = getJson.getString("mediaType");

            int time = getJson.optInt("time", defaultExecutionTime);

            // 確認是否有同mediaType的正在播放,如果超過數量限制,結束第一個
            ArrayList<String> currentMediaList = checkMediaPlayingNum(mediaType);

            TextBroadcast textBroadcast = new TextBroadcast(
                    this,
                    getJson,
                    new TextBroadcast.OnDestroyListener() {
                        @Override
                        public void onDestroy() {

                            removeFloatingWindow(mediaId);

                        }
                    }
            );

            currentMediaList.add(mediaId);
            playingMediaList.put(mediaType, currentMediaList);

            floatingLayoutMap.put(mediaId, textBroadcast);
            waitToCloseFloatingWindow(mediaId, time);

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void removeBroadcast(JSONObject json){

        try {

            String mediaId = json.getString("mediaId");
            removeFloatingWindow(mediaId);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void waitToCloseFloatingWindow(String mediaId, int waitTime){

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(waitTime * 1000);
                    removeFloatingWindow(mediaId);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        });

        thread.start();
        waitThread.put(mediaId, thread);

    }

    private void removeFloatingWindow(String mediaId){

        // 結束FloatingLayout
        FloatingLayout floatingLayout = floatingLayoutMap.get(mediaId);
        if(floatingLayout != null)
            floatingLayout.destroy();

        // 清除 等待結束 線程
        Thread thread = waitThread.get(mediaId);
        if (thread != null){
            thread.interrupt();
        }
        waitThread.remove(mediaId);

        // 清除共享的mediaIdList中的指定mediaId
        Object mediaIdList = Singleton.getInstance().getSingletonData();
        if (mediaIdList instanceof ArrayList<?>) {
            ArrayList<String> list = (ArrayList<String>) mediaIdList;
            list.remove(mediaId);
        }

    }

    private ArrayList<String> checkMediaPlayingNum(String mediaType){

//        Map<String, Integer> mediaMaxNumMap = new HashMap<>();
//        mediaMaxNumMap.put("silentBroadcast", 1);
//        mediaMaxNumMap.put("audio", 1);
//        mediaMaxNumMap.put("video", 1);
//        mediaMaxNumMap.put("image", 1);
//
//        int maxFloatingWindowNum = mediaMaxNumMap.get(mediaType);

        final int maxFloatingWindowNum = 1;

        ArrayList<String> currentMediaList = playingMediaList.get(mediaType);

        if (currentMediaList != null) {

            // 只要列表的大小超過最大限制，就移除第一个元素
            while (currentMediaList.size() >= maxFloatingWindowNum) {

                String getMediaId = currentMediaList.get(0);

                removeFloatingWindow(getMediaId);

                currentMediaList.remove(0);
            }

            return currentMediaList;
        }
        else {
            return new ArrayList<String>();
        }

    }

}



