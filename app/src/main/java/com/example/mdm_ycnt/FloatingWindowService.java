package com.example.mdm_ycnt;

import static com.example.mdm_ycnt.FloatingWindow.service.FloatingService.view;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FloatingWindowService extends Service {

    private WindowManager windowManager;
    private Map<Integer, View> floatingViews = new HashMap<>();
    private Map<Integer, Thread> waitThread = new HashMap<>();

    public Map<String, ArrayList<Integer>> playingMediaList = new HashMap<>();

    int radiusVal = 0;

    Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        radiusVal = dpToPx(9);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (windowManager == null) {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        }

        String action = intent.getStringExtra("action");
        int viewId = intent.getIntExtra("viewId", -1);
        String windowType = intent.getStringExtra("windowType");


        if ("add".equals(action)) {

            if ("web".equals(windowType)) {

                addWebView(viewId, intent);

            } else if ("silentBroadcast".equals(windowType)) {

                addSilentBroadcastWindow(viewId, intent);

            }else if ("image".equals(windowType)) {

                addImageView(viewId, intent);

            }

            Object data = Singleton.getInstance().getSingletonData();
            if (data instanceof ArrayList<?>) {
                ArrayList<Integer> list = (ArrayList<Integer>) data;
                list.add(viewId);
            }

        } else if ("remove".equals(action) && viewId != -1) {

            removeView(viewId);
        }

        return START_STICKY;
    }

    private void addWebView(int viewId, Intent intent) {

        int width = intent.getIntExtra("width", WindowManager.LayoutParams.WRAP_CONTENT);
        int height = intent.getIntExtra("height", WindowManager.LayoutParams.WRAP_CONTENT);
        int x = dpToPx(intent.getIntExtra("x", 0));
        int y = dpToPx(intent.getIntExtra("y", 0));
        int time = intent.getIntExtra("time",180);

        String mediaType = intent.getStringExtra("mediaType");
        ArrayList<Integer> currentMediaList = checkMediaPlayingNum(mediaType);


        width = width >= 0 ? dpToPx(width) : width;
        height = height >= 0 ? dpToPx(height) : height;

        String url = intent.getStringExtra("url");

        // 創建父布局
        RelativeLayout layout = new RelativeLayout(this);
        layout.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));


        RoundedFrameLayout roundedFrameLayout = new RoundedFrameLayout(this);
        roundedFrameLayout.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // 設定圓角半徑
        roundedFrameLayout.setCornerRadii(radiusVal, radiusVal, radiusVal, radiusVal); // 以像素為單位

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(Color.BLACK); // 背景顏色
        drawable.setCornerRadius(radiusVal); // 圓角半徑，單位是像素

        roundedFrameLayout.setBackground(drawable);

        layout.addView(roundedFrameLayout);

        // 創建WebView
        WebView webView = new WebView(this);
        webView.setBackgroundColor(Color.BLACK);
        webView.setId(viewId);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webView.getSettings().setDomStorageEnabled(true);

        WebView.setWebContentsDebuggingEnabled(true);

        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);//使用緩存的方式
//        webView.getSettings().setAppCacheEnabled(false);

        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(url);

        RelativeLayout.LayoutParams webViewParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        webViewParams.setMargins(0, dpToPx(40), 0, 0);

        roundedFrameLayout.addView(webView, webViewParams);


        getCloseButtonStyle(viewId, layout);

        floatingViews.put(viewId, layout);
        windowManager.addView(layout, createLayoutParams(width, height, x, y, Gravity.START | Gravity.TOP));
        makeViewDraggable(layout);

        currentMediaList.add(viewId);
        playingMediaList.put(mediaType,currentMediaList);

        waitToCloseFloatingWindow(viewId, time);
    }

    private void addSilentBroadcastWindow(int viewId, Intent intent) {

        //螢幕寬度（px）
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        String textInfo = intent.getStringExtra("textInfo");
        int time = intent.getIntExtra("time",180);

        String backgroundColor = intent.getStringExtra("backgroundColor") != null ?
                intent.getStringExtra("backgroundColor") : "#000000";

        String textColor = intent.getStringExtra("textColor") != null ?
                intent.getStringExtra("textColor") : "#FFFFFF";

        int textSize = intent.getIntExtra("textSize",50);

        String setPosition = intent.getStringExtra("setPosition") != null ?
                intent.getStringExtra("setPosition") : "top";

        int gravity = Gravity.START | Gravity.TOP;

        if(setPosition.equals("center")){
            gravity = Gravity.START | Gravity.CENTER;
        }else if(setPosition.equals("bottom")){
            gravity = Gravity.START | Gravity.BOTTOM;
        }

        String mediaType = intent.getStringExtra("mediaType");
        ArrayList<Integer> currentMediaList = checkMediaPlayingNum(mediaType);


        int width = displayMetrics.widthPixels;
        int height = -2;

        RelativeLayout layout = new RelativeLayout(this);
        layout.setId(viewId);

        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setBackgroundColor(Color.parseColor(backgroundColor));

        RelativeLayout.LayoutParams frameLayoutParams = new RelativeLayout.LayoutParams(
                width,
                height);
        layout.addView(frameLayout, frameLayoutParams);


        TextView silentView = new TextView(this);
        silentView.setText(textInfo);
        silentView.setTextColor(Color.parseColor(textColor)); // 文字顏色
        silentView.setGravity(Gravity.RIGHT); // 文字靠右
//        silentView.setTextSize(TypedValue.COMPLEX_UNIT_PX, dpToPx(textSize)); // 字體大小
        silentView.setTextSize(textSize); // 字體大小
        silentView.setPadding(0, dpToPx(5), 0, dpToPx(5));


        silentView.setTranslationX(width);

        //silentView寬度 (px)
        String dt = silentView.getText().toString();
        Rect bounds = new Rect();
        TextPaint paint = silentView.getPaint();
        paint.getTextBounds(dt, 0, dt.length(), bounds);
        int textWidth = (int)paint.measureText(textInfo);

        RelativeLayout.LayoutParams textViewParams = new RelativeLayout.LayoutParams(
                textWidth,
                height);
        frameLayout.addView(silentView, textViewParams);

        getCloseButtonStyle(viewId, layout);


        floatingViews.put(viewId, layout);
        windowManager.addView(layout, createLayoutParams(width, height, 0, 0, gravity));
        makeViewDraggable(layout);


        // 創建 Handler 和 Runnable 来更新文字位置
        final Handler handler = new Handler();
        final Runnable marquee = new Runnable() {

            private int offset = -width;
            private final int step = dpToPx(1.95); // 每次更新移動的像素
            private final int totalWidth  = textWidth;

            @Override
            public void run() {
                silentView.setTranslationX(-offset);

                offset += step;

                if (offset > totalWidth) {
                    offset = -width; // 重置，從頭開始
                }
                handler.postDelayed(this, 10); // X ms 後再次更新
            }
        };
        handler.post(marquee);


        currentMediaList.add(viewId);
        playingMediaList.put(mediaType,currentMediaList);

        waitToCloseFloatingWindow(viewId, time);

    }

    private void addImageView(int viewId, Intent intent) {

        int width = intent.getIntExtra("width", WindowManager.LayoutParams.WRAP_CONTENT);
        int height = intent.getIntExtra("height", WindowManager.LayoutParams.WRAP_CONTENT);
        int x = dpToPx(intent.getIntExtra("x", 0));
        int y = dpToPx(intent.getIntExtra("y", 0));

        int time = intent.getIntExtra("time",180);

        String mediaType = intent.getStringExtra("mediaType");
        ArrayList<Integer> currentMediaList = checkMediaPlayingNum(mediaType);


        width = width >= 0 ? dpToPx(width) : width;
        height = height >= 0 ? dpToPx(height) : height;

        String url = intent.getStringExtra("url");


        // 創建父布局
        RelativeLayout layout = new RelativeLayout(this);
        layout.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));


        RoundedFrameLayout roundedFrameLayout = new RoundedFrameLayout(this);
        roundedFrameLayout.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        // 設定圓角半徑
        roundedFrameLayout.setCornerRadii(radiusVal, radiusVal, radiusVal, radiusVal); // 以像素為單位

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(Color.BLACK); // 背景顏色
        drawable.setCornerRadius(radiusVal); // 圓角半徑，單位是像素

        roundedFrameLayout.setBackground(drawable);

        layout.addView(roundedFrameLayout);

        ImageView imageView = new ImageView(this);
        imageView.setId(viewId);


        // 使用 Glide 加載圖片
        Glide.with(this)
                .load(url)
                .into(imageView);

        RelativeLayout.LayoutParams imageViewParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        roundedFrameLayout.addView(imageView, imageViewParams);

        getCloseButtonStyle(viewId, layout);

        floatingViews.put(viewId, layout);
        windowManager.addView(layout, createLayoutParams(width, height, x, y,Gravity.START | Gravity.TOP));
        makeViewDraggable(layout);

        currentMediaList.add(viewId);
        playingMediaList.put(mediaType,currentMediaList);

        waitToCloseFloatingWindow(viewId, time);

    }


    private void getCloseButtonStyle(int viewId, RelativeLayout layout){

        int sizeInPx = dpToPx(30);

        Button closeButton = new Button(this);

        // 取得svg
        Drawable vectorDrawable = ContextCompat.getDrawable(this, R.drawable.ic_x);

        GradientDrawable backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setShape(GradientDrawable.RECTANGLE);
        backgroundDrawable.setColor(Color.parseColor("#F44336")); // 背景顏色
        backgroundDrawable.setCornerRadius(dpToPx(30)); // 設定圓角半徑

        // 生成LayerDrawable並設定圖形、背景顏色
        Drawable[] layers = {backgroundDrawable, vectorDrawable};
        LayerDrawable layerDrawable = new LayerDrawable(layers);

        // 設定圖形顯示的位置
        int inset = 0;
        layerDrawable.setLayerInset(1, inset, inset, inset, inset);

        // 把LayerDrawable設定為按鈕背景
        closeButton.setBackground(layerDrawable);

        closeButton.setWidth(sizeInPx);
        closeButton.setHeight(sizeInPx);
        closeButton.setGravity(Gravity.CENTER);

        closeButton.setOnClickListener(v -> removeView(viewId));

        RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(sizeInPx, sizeInPx);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);

        // 設置偏移量，將按鈕向左偏移
        buttonParams.setMargins(0, dpToPx(5), dpToPx(5), 0);

        layout.addView(closeButton, buttonParams);

    }

    private void destroyWebViewsInViewGroup(ViewGroup viewGroup) {

        for (int i = 0; i < viewGroup.getChildCount(); i++) {

            View child = viewGroup.getChildAt(i);
            // 檢查是否是WebView
//            if (child instanceof WebView) {
//                ((WebView) child).destroy();
//
//            }
            if (child instanceof WebView) {

                ((WebView) child).clearHistory();
                ((WebView) child).clearCache(true);
                ((WebView) child).stopLoading();

                ViewGroup parentView = (ViewGroup) child.getParent();

                if (parentView != null) {
                    parentView.removeView(child);
                }

                ((WebView) child).destroy();

                child = null;
            }
            else if (child instanceof ViewGroup) {
                // 如果child是另一個viewGroup，遞迴的調用此方法
                destroyWebViewsInViewGroup((ViewGroup) child);
            }

            // 提示系统进行垃圾回收
            System.gc();
        }

    }

    private void removeView(int viewId) {

        View view = floatingViews.get(viewId);

        if (view != null) {

            windowManager.removeView(view);

            if (view instanceof ViewGroup) {
                // 遞迴的遍歷viewGroup，銷毀所有WebView
                destroyWebViewsInViewGroup((ViewGroup) view);
            }

            floatingViews.remove(viewId);
        }

        Thread thread = waitThread.get(viewId);
        if (thread != null){
            thread.interrupt();
        }
        waitThread.remove(viewId);

        Object data = Singleton.getInstance().getSingletonData();
        if (data instanceof ArrayList<?>) {
            ArrayList<Integer> list = (ArrayList<Integer>) data;
            list.remove(Integer.valueOf(viewId));
        }

    }


    private WindowManager.LayoutParams createLayoutParams(int width, int height, int x, int y,int gravity) {

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                width,
                height,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = gravity;// Gravity.START | Gravity.TOP;
        params.x = x;
        params.y = y;

        return params;
    }

    private void makeViewDraggable(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                WindowManager.LayoutParams params = (WindowManager.LayoutParams) v.getLayoutParams();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(v, params);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        for (View view : floatingViews.values()) {
            if (view != null) {

                windowManager.removeView(view);

                if (view instanceof ViewGroup) {
                    // 遞迴的遍歷viewGroup，銷毀所有WebView
                    destroyWebViewsInViewGroup((ViewGroup) view);
                }

            }
        }

        floatingViews.clear();
        waitThread.clear();
    }

    public int dpToPx(double dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void waitToCloseFloatingWindow(int mediaId, int waitTime){


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Thread.sleep(waitTime * 1000);

                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            // 這部分的code會在主線程執行
                            removeView(mediaId);

                        }
                    });

                } catch (InterruptedException e) {

                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }

            }
        });

        thread.start();

        waitThread.put(mediaId, thread);


    }

    private ArrayList<Integer> checkMediaPlayingNum(String mediaType){

        int maxFloatingWindowNum = 1;

        ArrayList<Integer> currentMediaList = playingMediaList.get(mediaType);

        if (currentMediaList != null) {

            // 只要列表的大小超過最大限制，就移除第一个元素
            while (currentMediaList.size() >= maxFloatingWindowNum) {

                int getId = currentMediaList.get(0);

                View view = floatingViews.get(getId);

                if(view != null){
                    removeView(getId);
                }

                currentMediaList.remove(0);
            }

            return currentMediaList;
        }
        else {
            return new ArrayList<Integer>();
        }

    }

    public FloatingWindowService() {
        //this.someVariable = someVariable;
    }

    public Map<Integer, View> getSomeVariable() {
        return floatingViews;
    }

}



