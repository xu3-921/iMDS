package com.example.mdm_ycnt;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EmergencyAlertService extends Service {

    private WindowManager windowManager;
    private Map<Integer, View> floatingViews = new HashMap<>();

    boolean isEmergencyAlertModalShow = false;

    int emergencyAlertModalViewId = 55677;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

//        radiusVal = dpToPx(9);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (windowManager == null) {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        }

        String action = intent.getStringExtra("action");
//        int viewId = intent.getIntExtra("viewId", -1);
//        String windowType = intent.getStringExtra("windowType");

        if(action.equals("create")){
            createEmergencyAlertBtn(55688, intent);
        }
        else if(action.equals("delete")){

            for (View view : floatingViews.values()) {
                if (view != null) {

                    windowManager.removeView(view);
                }
            }

            floatingViews.clear();
            stopSelf();

        }

        //createEmergencyAlertModal(55677, intent);

//        if ("add".equals(action)) {
//
//            if ("web".equals(windowType)) {
//
//                addWebView(viewId, intent);
//
//            } else if ("silentBroadcast".equals(windowType)) {
//
//                addSilentBroadcastWindow(viewId, intent);
//
//            }else if ("image".equals(windowType)) {
//
//                addImageView(viewId, intent);
//
//            }
//
//        } else if ("remove".equals(action) && viewId != -1) {
//
//            removeView(viewId);
//        }

        return START_STICKY;
    }

    private void createEmergencyAlertBtn(int viewId, Intent intent) {

        int width = dpToPx(32);
        int height = dpToPx(30);
        int x = 0;
        int y = getResources().getDisplayMetrics().heightPixels - (dpToPx(30) * 4);


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
        // roundedFrameLayout.setCornerRadii(0, 0, dpToPx(10), dpToPx(10)); // 以像素為單位

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);

        int alpha = 180; // 半透明
        int red = 45;
        int green = 45;
        int blue = 45;

        int color = Color.argb(alpha, red, green, blue);
        drawable.setColor(color);

//        drawable.setColor(Color.GRAY); // 背景顏色
//        drawable.setCornerRadius(dpToPx(9)); // 圓角半徑，單位是像素

        // 設置每個角的半徑。數組中的每兩個值分別代表一個角的橫向和縱向半徑。
        // 例如: [topLeftX, topLeftY, topRightX, topRightY, bottomRightX, bottomRightY, bottomLeftX, bottomLeftY]

        int getRadiiNum = dpToPx(30);
        float[] radii = new float[]{
                0, 0,   // 左上角
                getRadiiNum, getRadiiNum,   // 右上角
                getRadiiNum, getRadiiNum, // 右下角
                0, 0    // 左下角
        };

        drawable.setCornerRadii(radii);

        roundedFrameLayout.setBackground(drawable);

        layout.addView(roundedFrameLayout);

        ImageView imageView = new ImageView(this);
        imageView.setId(viewId);
        imageView.setImageResource(R.drawable.ic_light_emergency);
        //imageView.setPadding(0, 0, dpToPx(2),1);

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(isEmergencyAlertModalShow){
                    removeView(emergencyAlertModalViewId);
                    isEmergencyAlertModalShow = false;
                }else{
                    createEmergencyAlertModal(emergencyAlertModalViewId, intent);
                    isEmergencyAlertModalShow = true;
                }

            }
        });


// 設定 ImageView 的 LayoutParams 以使其在父布局中居中
        FrameLayout.LayoutParams imageViewParams = new FrameLayout.LayoutParams(
                dpToPx(20),
                dpToPx(20));
        imageViewParams.setMargins(0, 0, dpToPx(2),0);
        imageViewParams.gravity = Gravity.CENTER;  // 設定為居中

        roundedFrameLayout.addView(imageView, imageViewParams);

        //getCloseButtonStyle(viewId, layout);

        floatingViews.put(viewId, layout);
        windowManager.addView(layout, createLayoutParams(width, height, x, y, Gravity.START | Gravity.TOP));
        makeViewDraggable(layout, MOVE_AXIS_Y);

        // currentMediaList.add(viewId);

    }

    private void createEmergencyAlertModal(int viewId, Intent intent) {

        int width = dpToPx(230);
        int height = dpToPx(240);
        int x = dpToPx(45);
        int y = getResources().getDisplayMetrics().heightPixels - dpToPx(250);

        String mediaType = intent.getStringExtra("mediaType");
        // ArrayList<Integer> currentMediaList = checkMediaPlayingNum(mediaType);

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
        // roundedFrameLayout.setCornerRadii(0, 0, dpToPx(10), dpToPx(10)); // 以像素為單位

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);

        int alpha = 150; // 半透明
        int red = 45;
        int green = 45;
        int blue = 45;

        int color = Color.argb(alpha, red, green, blue);
        drawable.setColor(color);


        //drawable.setColor(Color.WHITE); // 背景顏色
        drawable.setCornerRadius(dpToPx(15)); // 圓角半徑，單位是像素


        roundedFrameLayout.setBackground(drawable);

        layout.addView(roundedFrameLayout);

        // 建立GridLayout
        GridLayout gridLayout = new GridLayout(this);

        LayoutInflater inflater = LayoutInflater.from(this);

        View myLayout = inflater.inflate(R.layout.emergency_alert_modal_layout, gridLayout, false);

        // 为加载的布局设置 LayoutParams
        GridLayout.LayoutParams gridLayoutParams = new GridLayout.LayoutParams();
        gridLayoutParams.width = GridLayout.LayoutParams.MATCH_PARENT;
        gridLayoutParams.height = GridLayout.LayoutParams.MATCH_PARENT;
        //gridLayoutParams.setGravity(Gravity.CENTER);
        gridLayoutParams.setMargins(0, dpToPx(25), 0, 0); // 根据需要设置边距



        // 将GridLayout添加到roundedFrameLayout
        roundedFrameLayout.addView(myLayout,gridLayoutParams);

        Button closeButton = getCloseButtonStyle(viewId, layout);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeView(viewId);
                isEmergencyAlertModalShow = false;
            }
        });

        floatingViews.put(viewId, layout);
        windowManager.addView(layout, createLayoutParams(width, height, x, y, Gravity.START | Gravity.TOP));
        makeViewDraggable(layout, MOVE_AXIS_XY);

        // currentMediaList.add(viewId);

        LinearLayout btnEmergencyAlertDisaster = layout.findViewById(R.id.btn_emergency_alert_disaster);
        LinearLayout btnEmergencyAlertInvasion = layout.findViewById(R.id.btn_emergency_alert_invasion);
        LinearLayout btnEmergencyAlertRescue = layout.findViewById(R.id.btn_emergency_alert_rescue);
        LinearLayout btnEmergencyAlertOther = layout.findViewById(R.id.btn_emergency_alert_other);

        Map<LinearLayout, String> btnEmergencyAlertMap = new HashMap<>();

        btnEmergencyAlertMap.put(btnEmergencyAlertDisaster,"Disaster");
        btnEmergencyAlertMap.put(btnEmergencyAlertInvasion,"Invasion");
        btnEmergencyAlertMap.put(btnEmergencyAlertRescue,"Rescue");
        btnEmergencyAlertMap.put(btnEmergencyAlertOther,"Other");

        for (Map.Entry<LinearLayout, String> entry : btnEmergencyAlertMap.entrySet()) {

            LinearLayout emergencyAlertBtn = entry.getKey();
            emergencyAlertBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            boolean isSuccess = postEmergencyMessage(entry.getValue());

                            if(isSuccess){

                                removeView(viewId);
                                isEmergencyAlertModalShow = false;

                            }

                        }
                    }).start();

                }
            });

        }

    }

    private Button getCloseButtonStyle(int viewId, RelativeLayout layout){

        int sizeInPx = dpToPx(20);

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

//        closeButton.setOnClickListener(v -> removeView(viewId));

        RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(sizeInPx, sizeInPx);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);

        // 設置偏移量，將按鈕向左偏移
        buttonParams.setMargins(0, dpToPx(5), dpToPx(5), 0);

        layout.addView(closeButton, buttonParams);

        return closeButton;

    }

    private void removeView(int viewId) {

        View view = floatingViews.get(viewId);

        if (view != null) {

            windowManager.removeView(view);

//            if (view instanceof ViewGroup) {
//                // 遞迴的遍歷viewGroup，銷毀所有WebView
//                destroyWebViewsInViewGroup((ViewGroup) view);
//            }

            floatingViews.remove(viewId);
        }

//        Thread thread = waitThread.get(viewId);
//        if (thread != null){
//            thread.interrupt();
//        }
//        waitThread.remove(viewId);

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

    private static final int MOVE_AXIS_X = 1;
    private static final int MOVE_AXIS_Y = 2;
    private static final int MOVE_AXIS_XY = 3;

    private void makeViewDraggable(View view, int moveAxis) {

        view.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private boolean isDragging;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                WindowManager.LayoutParams params = (WindowManager.LayoutParams) v.getLayoutParams();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        isDragging = false; // 初始化时，没有拖动
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        // Calculate the distance moved
                        final int dx = (int) (event.getRawX() - initialTouchX);
                        final int dy = (int) (event.getRawY() - initialTouchY);

                        // Update the position if there is significant movement
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            if (moveAxis == MOVE_AXIS_X || moveAxis == MOVE_AXIS_XY) {
                                params.x = initialX + dx;
                            }
                            if (moveAxis == MOVE_AXIS_Y || moveAxis == MOVE_AXIS_XY) {
                                params.y = initialY + dy;
                            }
                            windowManager.updateViewLayout(v, params);
                            isDragging = true; // 标记为拖动
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!isDragging) {
                            // 如果没有拖动，则触发点击事件
                            v.performClick();
                        }
                        return true;
                }
                return false;
            }
        });
    }

//    private void makeViewDraggable(View view) {
//        view.setOnTouchListener(new View.OnTouchListener() {
//            private int initialX;
//            private int initialY;
//            private float initialTouchX;
//            private float initialTouchY;
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                WindowManager.LayoutParams params = (WindowManager.LayoutParams) v.getLayoutParams();
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        initialX = params.x;
//                        initialY = params.y;
//                        initialTouchX = event.getRawX();
//                        initialTouchY = event.getRawY();
//                        return true;
//                    case MotionEvent.ACTION_MOVE:
//                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
//                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
//                        windowManager.updateViewLayout(v, params);
//                        return true;
//                }
//                return false;
//            }
//        });
//    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        for (View view : floatingViews.values()) {
            if (view != null) {

                windowManager.removeView(view);

//                if (view instanceof ViewGroup) {
//                    // 遞迴的遍歷viewGroup，銷毀所有WebView
//                    destroyWebViewsInViewGroup((ViewGroup) view);
//                }

            }
        }

        floatingViews.clear();
//        waitThread.clear();
    }

    public int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private LinearLayout createImageTextButton(Context context, int imageResId, String text) {
        // 创建一个垂直的 LinearLayout 作为按钮容器
        LinearLayout buttonLayout = new LinearLayout(context);
        buttonLayout.setOrientation(LinearLayout.VERTICAL);
        buttonLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        buttonLayout.setPadding(10, 10, 10, 10); // 设置适当的内边距

        // 创建 ImageView 并设置图片资源
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(imageResId);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        imageView.setLayoutParams(imageParams);

        // 创建 TextView 并设置文字
        TextView textView = new TextView(context);
        textView.setText(text);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textView.setLayoutParams(textParams);

        // 将 ImageView 和 TextView 添加到 LinearLayout
        buttonLayout.addView(imageView);
        buttonLayout.addView(textView);

        return buttonLayout;
    }

    private boolean postEmergencyMessage(String emergencyMessage){

        try {
            String deviceId = UniversalFunction.F_getDeviceId(EmergencyAlertService.this);

            String phpUrl = "php/app_php/mdm_emergency_message_to_line.php";

            JSONObject jsonObjectData = new JSONObject();


            jsonObjectData.put("deviceId",deviceId);
            jsonObjectData.put("emergencyMessage",emergencyMessage);

            JSONObject getData =  UniversalFunction.HttpPostData(phpUrl ,jsonObjectData, EmergencyAlertService.this);

            String returnStates = getData.getString("returnStates");

            if(returnStates.equals("success")){
                return  true;
            }else{
                return  false;
            }

        }
        catch (JSONException e){
            e.printStackTrace();
            return  false;
        }

    }

}