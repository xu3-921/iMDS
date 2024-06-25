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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.ycnt.imds.floatingwindow.FloatingLayout;
import com.ycnt.imds.floatingwindow.FloatingLayoutConfig;
import com.ycnt.imds.floatingwindow.callback.FloatingListener;
import com.ycnt.imds.floatingwindow.module.FloatingViewMovementModule;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EmergencyAlertService extends Service {

    private FloatingLayout emergencyAlertBtn = null;
    private FloatingLayout emergencyAlertModal = null;

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

        if(action.equals("create") && emergencyAlertBtn == null){

            createEmergencyAlertBtn();

        }
        else if(action.equals("delete")){

            stopSelf();

        }

        return START_STICKY;
    }

    private void createEmergencyAlertBtn(){

        FloatingLayoutConfig config =
                new FloatingLayoutConfig.Builder(this)
                        .setLayoutRes(-1)
                        .setMovementModule(FloatingViewMovementModule.MOVE_AXIS_Y)
                        .setGravity(Gravity.START | Gravity.TOP)
                        .setX(0)
                        .setY(getResources().getDisplayMetrics().heightPixels - (new UniversalFunction().dpToPx(30, this) * 4))
                        .setWidth(new UniversalFunction().dpToPx(32, this))
                        .setHeight(new UniversalFunction().dpToPx(30, this))
                        .setShow(true)
                        .build();

        emergencyAlertBtn = new FloatingLayout(config);

        FloatingListener floatingListener = new FloatingListener() {

            @Override
            public void onCreate(View view) {

                ViewGroup viewGroup = (ViewGroup) view;

                FrameLayout layout = new FrameLayout(EmergencyAlertService.this);
                layout.setLayoutParams(new FrameLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT));

                GradientDrawable drawable = new GradientDrawable();
                drawable.setShape(GradientDrawable.RECTANGLE);

                int alpha = 180; // 半透明
                int red = 45;
                int green = 45;
                int blue = 45;

                int color = Color.argb(alpha, red, green, blue);
                drawable.setColor(color);


                int getRadiiNum = new UniversalFunction().dpToPx(30,EmergencyAlertService.this);
                float[] radii = new float[]{
                        0, 0, // 左上角
                        getRadiiNum, getRadiiNum,  // 右上角
                        getRadiiNum, getRadiiNum, // 右下角
                        0, 0 // 左下角
                };

                drawable.setCornerRadii(radii);

                layout.setBackground(drawable);

                ImageView imageView = new ImageView(EmergencyAlertService.this);
                imageView.setImageResource(R.drawable.ic_light_emergency);


                FrameLayout.LayoutParams imageViewParams = new FrameLayout.LayoutParams(
                        new UniversalFunction().dpToPx(20,EmergencyAlertService.this),
                        new UniversalFunction().dpToPx(20, EmergencyAlertService.this));
                imageViewParams.setMargins(
                        0,
                        0,
                        new UniversalFunction().dpToPx(2,EmergencyAlertService.this),
                        0);
                imageViewParams.gravity = Gravity.CENTER;  // 設定為居中

                layout.addView(imageView, imageViewParams);


                viewGroup.addView(layout);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if(emergencyAlertModal == null){
                            createEmergencyAlertModal();
                        }

                    }
                });

            }

            @Override
            public void onClose() {

            }

            @Override
            public void willOpen(View view) {

            }

            @Override
            public void didOpen(View view) {

            }

            @Override
            public void willClose(View view) {

            }

            @Override
            public void didClose(View view) {

            }

        };

        emergencyAlertBtn.setFloatingListener(floatingListener);
        emergencyAlertBtn.create();

    }

    private void createEmergencyAlertModal(){

        FloatingLayoutConfig config =
                new FloatingLayoutConfig.Builder(this)
                        .setLayoutRes(-1)
                        .setMovementModule(FloatingViewMovementModule.MOVE_AXIS_XY)
                        .setGravity(Gravity.START | Gravity.TOP)
                        .setX(new UniversalFunction().dpToPx(45, this))
                        .setY(getResources().getDisplayMetrics().heightPixels - new UniversalFunction().dpToPx(250, this))
                        .setWidth(new UniversalFunction().dpToPx(230, this))
                        .setHeight(new UniversalFunction().dpToPx(240, this))
                        .setShow(true)
                        .build();

        emergencyAlertModal = new FloatingLayout(config);

        FloatingListener floatingListener = new FloatingListener() {

            @Override
            public void onCreate(View view) {


                ViewGroup viewGroup = (ViewGroup) view;

                RelativeLayout relativeLayout = new RelativeLayout(EmergencyAlertService.this);
                relativeLayout.setLayoutParams(new FrameLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                ));

                //

                CardView cardView = new CardView(EmergencyAlertService.this);
                CardView.LayoutParams cardViewParams = new CardView.LayoutParams(
                        CardView.LayoutParams.MATCH_PARENT,
                        CardView.LayoutParams.MATCH_PARENT
                );

                cardView.setRadius(new UniversalFunction().dpToPx(16, EmergencyAlertService.this)); // 圓角半徑
                cardView.setCardElevation(0); // 陰影大小

                cardView.setLayoutParams(cardViewParams);
                cardView.setCardBackgroundColor(Color.argb(150, 45, 45, 45));

                //

                relativeLayout.addView(cardView);
                viewGroup.addView(relativeLayout);

                // 建立GridLayout
                GridLayout gridLayout = new GridLayout(EmergencyAlertService.this);
                LayoutInflater inflater = LayoutInflater.from(EmergencyAlertService.this);
                View myLayout = inflater.inflate(R.layout.emergency_alert_modal_layout, gridLayout, false);

                // 为加载的布局设置 LayoutParams
                GridLayout.LayoutParams gridLayoutParams = new GridLayout.LayoutParams();
                gridLayoutParams.width = GridLayout.LayoutParams.MATCH_PARENT;
                gridLayoutParams.height = GridLayout.LayoutParams.MATCH_PARENT;
                gridLayoutParams.setMargins(
                        0,
                        new UniversalFunction().dpToPx(25, EmergencyAlertService.this),
                        0,
                        0);

                cardView.addView(myLayout, gridLayoutParams);

                //

                Button closeButton = getCloseButtonStyle(relativeLayout);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        removeEmergencyAlertModal();

                    }
                });

                setEventToBtn(cardView);

            }

            @Override
            public void onClose() {

            }

            @Override
            public void willOpen(View view) {

            }

            @Override
            public void didOpen(View view) {

            }

            @Override
            public void willClose(View view) {

            }

            @Override
            public void didClose(View view) {

            }

        };

        emergencyAlertModal.setFloatingListener(floatingListener);
        emergencyAlertModal.create();

    }

    private void setEventToBtn(ViewGroup viewGroup){

        LinearLayout btnEmergencyAlertDisaster = viewGroup.findViewById(R.id.btn_emergency_alert_disaster);
        LinearLayout btnEmergencyAlertInvasion = viewGroup.findViewById(R.id.btn_emergency_alert_invasion);
        LinearLayout btnEmergencyAlertRescue = viewGroup.findViewById(R.id.btn_emergency_alert_rescue);
        LinearLayout btnEmergencyAlertOther = viewGroup.findViewById(R.id.btn_emergency_alert_other);

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

                                removeEmergencyAlertModal();

                            }

                        }
                    }).start();

                }
            });

        }

    }

    private void removeEmergencyAlertModal(){

        if(emergencyAlertModal != null){

            emergencyAlertModal.destroy();
            emergencyAlertModal = null;

        }

    }

    private Button getCloseButtonStyle(RelativeLayout layout){

        int sizeInPx = new UniversalFunction().dpToPx(20, this);

        Button closeButton = new Button(this);

        // 取得svg
        Drawable vectorDrawable = ContextCompat.getDrawable(this, R.drawable.ic_x);

        GradientDrawable backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setShape(GradientDrawable.RECTANGLE);
        backgroundDrawable.setColor(Color.parseColor("#F44336")); // 背景顏色
        backgroundDrawable.setCornerRadius(new UniversalFunction().dpToPx(30, this)); // 設定圓角半徑

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


        RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(sizeInPx, sizeInPx);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);

        // 設置偏移量，將按鈕向左偏移
        buttonParams.setMargins(
                0,
                new UniversalFunction().dpToPx(5, this),
                new UniversalFunction().dpToPx(5, this),
                0);

        layout.addView(closeButton, buttonParams);

        return closeButton;

    }


    @Override
    public void onDestroy() {

        super.onDestroy();

        removeEmergencyAlertModal();

        if(emergencyAlertBtn != null){
            emergencyAlertBtn.destroy();
            emergencyAlertBtn = null;
        }

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