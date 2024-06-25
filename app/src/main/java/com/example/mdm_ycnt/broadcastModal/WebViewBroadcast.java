package com.example.mdm_ycnt.broadcastModal;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.mdm_ycnt.R;
import com.example.mdm_ycnt.UniversalFunction;
import com.ycnt.imds.floatingwindow.FloatingLayout;
import com.ycnt.imds.floatingwindow.FloatingLayoutConfig;
import com.ycnt.imds.floatingwindow.callback.FloatingListener;
import com.ycnt.imds.floatingwindow.module.FloatingViewMovementModule;

import org.json.JSONException;
import org.json.JSONObject;



public class WebViewBroadcast extends FloatingLayout {

    private final Context mContext;
    private final JSONObject json;
    private WebView webView = null;

    private OnDestroyListener onDestroyListener;

    public WebViewBroadcast(Context mContext, JSONObject getJson, OnDestroyListener onDestroyListener) {
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

            int layoutRes = -1;
            int movementModule = FloatingViewMovementModule.MOVE_AXIS_XY;

            int x = new UniversalFunction().dpToPx(json.optInt("x", 0), mContext);
            int y = new UniversalFunction().dpToPx(json.optInt("y", 0), mContext);


            int width = json.optInt("width", 0);
            width = width > 0 ? new UniversalFunction().dpToPx(width, mContext) : width;

            int height = json.optInt("height", 0);
            height = height > 0 ? new UniversalFunction().dpToPx(height, mContext) : height;

            boolean show = true;

            String setPosition = json.optString("setPosition", "top");
            int gravity = Gravity.START | Gravity.TOP;

            if(setPosition.equals("center")){
                gravity = Gravity.START | Gravity.CENTER;
            }else if(setPosition.equals("bottom")){
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

    }

    private FloatingListener createFloatingListener() {

        return new FloatingListener() {

            @Override
            public void onCreate(View view) {

                ViewGroup viewGroup = (ViewGroup) view;

                try {

                    createWebViewModal(viewGroup);

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

    private void createWebViewModal(ViewGroup viewGroup) throws JSONException {

        String url = json.getString("url");

        CardView cardView = new CardView(mContext);
        CardView.LayoutParams cardViewParams = new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,
                CardView.LayoutParams.MATCH_PARENT
        );

        cardView.setLayoutParams(cardViewParams);
        cardView.setRadius(9 * mContext.getResources().getDisplayMetrics().density);
        cardView.setCardElevation(0);
        cardView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.black));

        RelativeLayout relativeLayout = new RelativeLayout(mContext);
        cardView.addView(relativeLayout, new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        ));

        WebView webView = createWebView();
        webView.loadUrl(url);

        RelativeLayout.LayoutParams webViewParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        webViewParams.setMargins(
                0,
                new UniversalFunction().dpToPx(40, mContext),
                0,
                0);

        relativeLayout.addView(webView, webViewParams);

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

        closeBtn.setOnClickListener(v -> {

            if (onDestroyListener != null) {
                onDestroyListener.onDestroy();
            }

            this.destroy();
        });

        relativeLayout.addView(closeBtn, buttonParams);


        viewGroup.addView(cardView);
    }

    private WebView createWebView(){

        // 創建WebView
        webView = new WebView(mContext);
        webView.setBackgroundColor(Color.BLACK);
//        webView.setId(viewId);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webView.getSettings().setDomStorageEnabled(true);

        WebView.setWebContentsDebuggingEnabled(true);

        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);//使用緩存的方式
//        webView.getSettings().setAppCacheEnabled(false);

        webView.setWebViewClient(new WebViewClient());
//        webView.loadUrl(url);

        return webView;

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

}
