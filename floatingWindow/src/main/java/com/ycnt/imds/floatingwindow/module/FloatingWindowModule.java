package com.ycnt.imds.floatingwindow.module;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Build;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;

import com.ycnt.imds.floatingwindow.FloatingLayoutConfig;
import com.ycnt.imds.floatingwindow.R;


public class FloatingWindowModule {

    private FloatingLayoutConfig config;

    private WindowManager.LayoutParams params;
    private View view;
    private WindowManager windowManager;

//    public FloatingWindowModule(Context context, @LayoutRes int layoutRes, int gravity, int x, int y, int width, int height, boolean isShow) {
     public FloatingWindowModule(FloatingLayoutConfig config) {

         this.config = config;

//        this.context = context;
//        this.layoutRes = layoutRes;
//        this.gravity = gravity;
//        this.x = x;
//        this.y = y;
//        this.width = width;
//        this.height = height;
//        this.isShow = isShow;
    }

    public void create() {

         Context context = config.getContext();

         windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

         windowManager.addView(
                 getView(),
                 getParams()
         );

//        view.post(new Runnable() {
//            @Override
//            public void run() {
//                Animation slideIn = AnimationUtils.loadAnimation(context, R.anim.slide_in_from_right);
//                view.startAnimation(slideIn);
//            }
//        });
    }

    public WindowManager.LayoutParams getParams() {

        if (params == null){

            params = new WindowManager.LayoutParams(
                    config.getWidth(),
                    config.getHeight(),
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    config.getFlags(),
                    PixelFormat.TRANSLUCENT
            );
        }


//        WindowManager.LayoutParams.MATCH_PARENT
//        WindowManager.LayoutParams.WRAP_CONTENT

//        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE

//        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH

//        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,

        int gravity = config.getGravity();

        // 使用預設值以防止無效值
        if ((gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == 0) {
            gravity |= Gravity.START;
        }
        if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
            gravity |= Gravity.TOP;
        }

        params.gravity = gravity;

//        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = config.getX();
        params.y = config.getY();

//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
//        int screenWidth = displayMetrics.widthPixels;

//        params.gravity = Gravity.LEFT | Gravity.TOP;
//        params.x = -200;  // 初始位置在屏幕外
//        params.y = 100;

//        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
//                width,
//                height,
//                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
//                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//
//        params.gravity = gravity;// Gravity.START | Gravity.TOP;
//        params.x = x;
//        params.y = y;

        return params;
    }

    public View getView() {

        if (view == null) {

            Context context = config.getContext();

            view = createDefaultView(context);

            @LayoutRes int layoutRes = config.getLayoutRes();

            if (layoutRes != -1) {

                view = View.inflate(context, layoutRes, null);

            }
        }


        if(!config.isShow()){
            view.setVisibility(View.GONE);
        }


        return view;
    }

    private View createDefaultView(Context context) {

        LinearLayout container = new LinearLayout(context);
        container.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        container.setOrientation(LinearLayout.VERTICAL);

        // 返回创建的 TextView
        return container;
    }

//    public int getWindowType() {
//        // Set to TYPE_SYSTEM_ALERT so that the Service can display it
//        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
//    }

    public WindowManager getWindowManager() {
        return windowManager;
    }

    public void destroy() {

        try {
            if (windowManager != null){

                if (view != null){

                    windowManager.removeViewImmediate(view);

                    if (view instanceof ViewGroup) {
                        // 遞迴的遍歷viewGroup，銷毀所有WebView
                        destroyWebViewsInViewGroup((ViewGroup) view);
                    }
                }

            }

        } catch (IllegalArgumentException e) {

            e.printStackTrace();

        } finally {
            params = null;
            view = null;
            windowManager = null;
        }
    }

    private void destroyWebViewsInViewGroup(ViewGroup viewGroup) {

        for (int i = 0; i < viewGroup.getChildCount(); i++) {

            View child = viewGroup.getChildAt(i);

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
}
