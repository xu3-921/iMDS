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
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import com.example.mdm_ycnt.R;
import com.example.mdm_ycnt.UniversalFunction;
import com.ycnt.imds.floatingwindow.FloatingLayout;
import com.ycnt.imds.floatingwindow.FloatingLayoutConfig;
import com.ycnt.imds.floatingwindow.callback.FloatingListener;
import com.ycnt.imds.floatingwindow.module.FloatingViewMovementModule;

import org.json.JSONException;
import org.json.JSONObject;

public class TextBroadcast extends FloatingLayout {

    private final Context mContext;
    private final JSONObject json;

    private WebView webView = null;

    private final OnDestroyListener onDestroyListener;

    public TextBroadcast(Context mContext, JSONObject getJson, OnDestroyListener onDestroyListener) {

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

        int width = json.optInt("width", WindowManager.LayoutParams.MATCH_PARENT);
        width = width > 0 ? new UniversalFunction().dpToPx(width, mContext) : width;

        int height = json.optInt("height", WindowManager.LayoutParams.MATCH_PARENT);
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

                    createTextBroadcast(viewGroup);

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

    private void createTextBroadcast(ViewGroup viewGroup) throws JSONException {

        String textInfo = json.getString("text");
        String backgroundColor = json.optString("backgroundColor", "#000000");
        String textColor = json.optString("textColor", "#FFFFFF");

        int textSize = json.optInt("textSize", 50);

        // 設定布局

        CardView cardView = new CardView(mContext);
        CardView.LayoutParams cardViewParams = new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,
                CardView.LayoutParams.MATCH_PARENT
        );

        cardView.setLayoutParams(cardViewParams);
        cardView.setRadius(9 * mContext.getResources().getDisplayMetrics().density);
        cardView.setCardElevation(0);
        cardView.setCardBackgroundColor(Color.parseColor("#FF6200EE"));

        RelativeLayout relativeLayout = new RelativeLayout(mContext);
        cardView.addView(relativeLayout, new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        ));

//        ScrollView scrollView = new ScrollView(mContext);
//        scrollView.addView(relativeLayout, new FrameLayout.LayoutParams(
//                FrameLayout.LayoutParams.MATCH_PARENT,
//                FrameLayout.LayoutParams.MATCH_PARENT
//        ));
//
//        relativeLayout.addView(scrollView);

        WebView webView = createWebView();
        webView.loadData(textInfo, "text/html", "UTF-8");

        RelativeLayout.LayoutParams webViewParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );

        relativeLayout.addView(webView, webViewParams);

//        int setPaddingNum = new UniversalFunction().dpToPx(15, mContext);
//        ConstraintLayout constraintLayout = new ConstraintLayout(mContext);
//        constraintLayout.setPadding(
//                setPaddingNum/2, setPaddingNum, setPaddingNum/2, setPaddingNum
//        );
//        constraintLayout.setLayoutParams(new ConstraintLayout.LayoutParams(
//                ConstraintLayout.LayoutParams.MATCH_PARENT,
//                ConstraintLayout.LayoutParams.MATCH_PARENT
//        ));
//        relativeLayout.addView(constraintLayout);
//
//        ScrollView scrollView = new ScrollView(mContext);
//        scrollView.setId(View.generateViewId());
//        scrollView.setLayoutParams(new ScrollView.LayoutParams(
//                ScrollView.LayoutParams.MATCH_PARENT,
//                ScrollView.LayoutParams.WRAP_CONTENT
//        ));
//        constraintLayout.addView(scrollView);
//
//        // 設定約束
//        ConstraintSet constraintSet = new ConstraintSet();
//        constraintSet.clone(constraintLayout);
//
//        // 設定 ScrollView 左右填滿
//        constraintSet.connect(scrollView.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 0);
//        constraintSet.connect(scrollView.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 0);
//
//        // 設定 ScrollView 上下置中
//        constraintSet.connect(scrollView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
//        constraintSet.connect(scrollView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
//
//        constraintSet.applyTo(constraintLayout);
//
//
//        TextView silentView = new TextView(mContext);
//        silentView.setText(textInfo);
////        silentView.setText(Html.fromHtml(textInfo, Html.FROM_HTML_MODE_COMPACT));
//        silentView.setTextColor(Color.parseColor(textColor)); // 文字顏色
//        silentView.setGravity(Gravity.CENTER);
//        silentView.setTextSize(textSize); // 字體大小
//
//        silentView.setPadding(
//                setPaddingNum/2, 0, setPaddingNum/2, 0
//        );
//
//        LinearLayout.LayoutParams textViewLayoutParams = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT);
//
//        scrollView.addView(silentView, textViewLayoutParams);


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

        viewGroup.addView(cardView);

    }

    private WebView createWebView(){

        // 創建WebView
        webView = new WebView(mContext);
        webView.setBackgroundColor(Color.parseColor("#000000"));

        WebSettings webSettings = webView.getSettings();
//        webView.getSettings().setJavaScriptEnabled(true);
//        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
//        webView.getSettings().setDomStorageEnabled(true);

        webSettings.setJavaScriptEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(false);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setBlockNetworkImage(false);
        webSettings.setBlockNetworkLoads(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setDisplayZoomControls(false);

        WebView.setWebContentsDebuggingEnabled(true);

        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);//使用緩存的方式
//        webView.getSettings().setAppCacheEnabled(false);
        webView.addJavascriptInterface(new WebAppInterface(mContext), "AndroidInterface");

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

//        webView.setWebViewClient(new WebViewClient());

        // 設定 WebViewClient 並在頁面載入完成後注入 JavaScript
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {

                webView.loadUrl(
                        "javascript:(" +
                                "function(){" +
                                "var body = document.body," +
                                "html = document.documentElement;" +

                                "var height = Math.max( body.scrollHeight, body.offsetHeight, html.clientHeight, html.scrollHeight, html.offsetHeight );" +

                                "console.log(height);" +

                                "AndroidInterface.getContentHeight(height)" +
                                "})()"
                );
            }

//                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
//                        RelativeLayout.LayoutParams.MATCH_PARENT,
//                        RelativeLayout.LayoutParams.WRAP_CONTENT
//                );
//                webView.setLayoutParams(params);
//
//
//                int webViewHeight = webView.getHeight();
//                Log.e("WebView", "WebView Height: " + webViewHeight);
//
//                // 注入檢測滾動條的函數和呼叫接口
////                view.loadUrl(
////                        "javascript:(function() {" +
////                            "function hasScrollBar(element) {" +
////                                "return element.scrollHeight > element.clientHeight;" +
////                            "}" +
////                            "var targetElement = document.body.children[0];" +
////                            "AndroidInterface.sendScrollBarStatus(hasScrollBar(targetElement));" +
////                        "})()");
//
//                view.loadUrl(
//                        "javascript:(function() {" +
//                            "function hasScrollBar(element) {" +
//                                "return element.scrollHeight > element.clientHeight;" +
//                            "}" +
//
//                            "var targetElement = document.body.children[0];" +
//
//                            "if (hasScrollBar(targetElement)) {" +
////                                "AndroidInterface.sendScrollBarStatus(true);" +
//                                "startScrolling(targetElement);" +
//                            "} " +
////                             "else {" +
////                                "AndroidInterface.sendScrollBarStatus(false);" +
////                            "}" +
//
//                            "function startScrolling(element) {" +
//                                "var scrollStep = 0.5;" +
//                                "var scrollInterval = 10;" +
//                                "setInterval(function() {" +
//                                    "if (element.scrollTop + element.clientHeight >= element.scrollHeight) {" +
//                                        "element.scrollTop = 0;" +
//                                    "} else {" +
//                                        "element.scrollTop += scrollStep;" +
//                                    "}" +
//                                "}, scrollInterval);" +
//                            "}" +
//                        "})()");
//            }
        });

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

    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void sendScrollBarStatus(boolean hasScrollBar) {
            // 處理滾動條狀態
            if (hasScrollBar) {
                Log.e("test03-4", "网页有滚动条");
            } else {
                Log.e("test03-4", "网页没有滚动条");
            }
        }

        @JavascriptInterface
        public void getContentHeight(String height) {
            final int contentHeight = Integer.parseInt(height);

            Log.e("test03-4", String.valueOf(contentHeight));

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    // 设置 WebView 的高度
                    ViewGroup.LayoutParams params = webView.getLayoutParams();
                    params.height = contentHeight;
                    webView.setLayoutParams(params);
                }
            });
        }
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
//    private int calculateMarqueeDuration(int screenWidth, int textWidth, int scrollSpeed) {
//
//        // 滾動的總距離
//        int totalDistance = screenWidth + textWidth;
//
//        // 總時間 = 總距離 / 每秒滾動的像素數 * 1000 毫秒
//        int duration = (int) ((double) totalDistance / scrollSpeed * 1000);
//
//
//        return duration;
//    }
}
