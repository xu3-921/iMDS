package com.example.mdm_ycnt.broadcastModal;

import static android.content.Context.WINDOW_SERVICE;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.mdm_ycnt.R;
import com.example.mdm_ycnt.UniversalFunction;
import com.ycnt.imds.floatingwindow.FloatingLayout;
import com.ycnt.imds.floatingwindow.FloatingLayoutConfig;
import com.ycnt.imds.floatingwindow.callback.FloatingListener;
import com.ycnt.imds.floatingwindow.module.FloatingViewMovementModule;

import org.json.JSONException;
import org.json.JSONObject;

public class MarqueeBroadcast extends FloatingLayout {

    private final Context mContext;
    private final JSONObject json;

    private OnDestroyListener onDestroyListener;

    public MarqueeBroadcast(Context mContext, JSONObject getJson, OnDestroyListener onDestroyListener) {

        super(createConfigFromJson(mContext, getJson));

        this.mContext = mContext;
        this.json = getJson;
        this.onDestroyListener = onDestroyListener;

        setFloatingListener(createFloatingListener());
        create();

    }

    public interface OnDestroyListener {
        void onDestroy();
    }

    private static FloatingLayoutConfig createConfigFromJson(Context mContext, JSONObject json) {

        try {

            int layoutRes = -1;
            int movementModule = FloatingViewMovementModule.MOVE_AXIS_XY;

            int x = 0;
            int y = 0;
            int width = WindowManager.LayoutParams.MATCH_PARENT;
            int height = WindowManager.LayoutParams.WRAP_CONTENT;
            boolean show = true;

            int gravity = Gravity.START | Gravity.TOP;

            if(json.getString("setPosition").equals("center")){
                gravity = Gravity.START | Gravity.CENTER;
            }else if(json.getString("setPosition").equals("bottom")){
                gravity = Gravity.START | Gravity.BOTTOM;
            }

            return new FloatingLayoutConfig.Builder(mContext)
                    .setLayoutRes(layoutRes)
                    .setMovementModule(movementModule)
                    .setGravity(gravity)
                    .setX(x)
                    .setY(y)
                    .setWidth(width)
                    .setHeight(height)
                    .setShow(show)
                    .build();

        } catch (JSONException e) {
            e.printStackTrace();

            return new FloatingLayoutConfig.Builder(mContext)
                    .setLayoutRes(-1)
                    .setMovementModule(FloatingViewMovementModule.MOVE_AXIS_XY)
                    .setGravity(Gravity.START | Gravity.TOP)
                    .setX(0)
                    .setY(0)
                    .setWidth(WindowManager.LayoutParams.MATCH_PARENT)
                    .setHeight(WindowManager.LayoutParams.WRAP_CONTENT)
                    .setShow(true)
                    .build();
        }
    }

    private FloatingListener createFloatingListener() {

        return new FloatingListener() {

            @Override
            public void onCreate(View view) {

                ViewGroup viewGroup = (ViewGroup) view;

                try {

                    createMarqueeBroadcast(viewGroup);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
    }

    private void createMarqueeBroadcast(ViewGroup viewGroup) throws JSONException {

        String textInfo = json.getString("text");
        String backgroundColor = json.optString("backgroundColor", "#000000");
        String textColor = json.optString("textColor", "#FFFFFF");

        int textSize = json.optInt("textSize", 50);
        int scrollSpeed = json.optInt("scrollSpeed", 200);
//        int time = json.optInt("time",180);


        // 生成樣式
        RelativeLayout relativeLayout = new RelativeLayout(mContext);

        FrameLayout backgroundLayout = new FrameLayout(mContext);
        backgroundLayout.setBackgroundColor(Color.parseColor(backgroundColor));

        FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );

        relativeLayout.addView(backgroundLayout, frameLayoutParams);

        TextView silentView = new TextView(mContext);
        silentView.setText(textInfo);
        silentView.setTextColor(Color.parseColor(textColor)); // 文字顏色
        silentView.setGravity(Gravity.END); // 文字靠右
        silentView.setTextSize(textSize); // 字體大小
        silentView.setPadding(
                0,
                new UniversalFunction().dpToPx(5, mContext),
                0,
                new UniversalFunction().dpToPx(5, mContext)
        );

        //螢幕寬度（px）
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        int windowWidth = displayMetrics.widthPixels;

        // 將文字藏到螢幕外
//        silentView.setTranslationX(windowWidth);

        // 計算文字的寬
        String dt = silentView.getText().toString();
        Rect bounds = new Rect();
        TextPaint paint = silentView.getPaint();
        paint.getTextBounds(dt, 0, dt.length(), bounds);
        int textWidth = (int) paint.measureText(textInfo);

        backgroundLayout.addView(silentView, new FrameLayout.LayoutParams(
                textWidth,
                FrameLayout.LayoutParams.WRAP_CONTENT)
        );

        Button closeBtn = getCloseButtonStyle();
        int sizeInPx = new UniversalFunction().dpToPx(30, mContext);
        RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(
                sizeInPx,
                sizeInPx);

        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);

        int marginVal = new UniversalFunction().dpToPx(5, mContext);

        // 設置偏移量，將按鈕向左偏移
        buttonParams.setMargins(
                0,
                marginVal,
                marginVal,
                0
        );

        relativeLayout.addView(closeBtn, buttonParams);

        closeBtn.setOnClickListener(v -> {

            if (onDestroyListener != null) {
                onDestroyListener.onDestroy();
            }

            this.destroy();
        });

        viewGroup.addView(relativeLayout);

        startAnimation(silentView, windowWidth, textWidth, scrollSpeed);

    }

    private void startAnimation(View silentView, int windowWidth, int textWidth, int scrollSpeed){

        // 創建跑馬燈效果
        int animationDuration = calculateMarqueeDuration(windowWidth, textWidth, scrollSpeed); // 計算出的動畫持續時間（毫秒）
        ObjectAnimator animator = ObjectAnimator.ofFloat(silentView, "translationX", windowWidth, -textWidth);
        animator.setDuration(animationDuration); // 設定動畫持續時間，根據需求調整
        animator.setRepeatCount(ValueAnimator.INFINITE); // 無限重複
        animator.setInterpolator(new LinearInterpolator()); // 直線插值器，確保匀速移動
        animator.start();

    }

    private Button getCloseButtonStyle(){

        int sizeInPx = new UniversalFunction().dpToPx(30, mContext);

        Button closeButton = new Button(mContext);

        // 取得svg
        Drawable vectorDrawable = ContextCompat.getDrawable(mContext, R.drawable.ic_x);

        GradientDrawable backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setShape(GradientDrawable.RECTANGLE);
        backgroundDrawable.setColor(Color.parseColor("#F44336")); // 背景顏色
        backgroundDrawable.setCornerRadius(sizeInPx); // 設定圓角半徑

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

        return closeButton;

    }


    /**
     * 計算動畫時間
     *
     * @param screenWidth 螢幕寬度 (px)
     * @param textWidth 文字寬度 (px)
     * @param scrollSpeed 每秒滾動的像素數
     *
     * @return 動畫時間(ms)
     */
    private int calculateMarqueeDuration(int screenWidth, int textWidth, int scrollSpeed) {

        // 滾動的總距離
        int totalDistance = screenWidth + textWidth;

        // 總時間 = 總距離 / 每秒滾動的像素數 * 1000 毫秒
        int duration = (int) ((double) totalDistance / scrollSpeed * 1000);


        return duration;
    }
}