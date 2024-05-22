package com.ycnt.imds.floatingwindow;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import androidx.annotation.LayoutRes;

import com.ycnt.imds.floatingwindow.callback.FloatingListener;
import com.ycnt.imds.floatingwindow.service.FloatingService;

import java.util.UUID;

public class FloatingLayout {

    private FloatingService.LocalBinder binder;


    private FloatingLayoutConfig config;

    private Context context;
    private String uniqueId;


//    private int layoutRes;
//    private int move_axis;
//    public int gravity;
//    public int x;
//    public int y;
//    public int width;
//    public int height;
    public boolean isShow;

    private FloatingListener floatingListener;

    private Intent intent;

    boolean isBound = false;
//    private boolean isShow;

    public FloatingLayout(FloatingLayoutConfig config) {

        this.config = config;

        this.context = config.getContext();
//        this.layoutRes = config.getLayoutRes();
//        this.move_axis = config.getMovementModule();
//        this.gravity = config.getGravity();
//        this.x = config.getX();
//        this.y = config.getY();
//        this.width = config.getWidth();
//        this.height = config.getHeight();
        this.isShow = config.isShow();

    }

    public void setFloatingListener(FloatingListener floatingListener) {
        this.floatingListener = floatingListener;
    }


    private final ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            // 建立連線
            FloatingService.LocalBinder getBinder = (FloatingService.LocalBinder) service;

            // 拿到正確的binder
            binder = getBinder.getLocalBinder(uniqueId);
            // 生成floatingWindow
            binder.createFloatingWindow(config);

            View view = binder.getView();

            if(floatingListener != null && view != null){
                floatingListener.onCreate(view);
            }

            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            Log.e("test01-4", "onServiceDisconnected");

            // 處理服務斷開連線的情況
            isBound = false;

            if(floatingListener != null){
                floatingListener.onClose();
            }

        }

    };

    public void create() {

        this.uniqueId = UUID.randomUUID().toString();
        Log.e("test02-3", uniqueId);
//        isShow = true;

        intent = new Intent(context, FloatingService.class);

        intent.putExtra(FloatingService.EXTRA_UNIQUE_ID, uniqueId);

//        intent.putExtra(FloatingService.EXTRA_LAYOUT_RESOURCE, layoutRes);
//        intent.putExtra(FloatingService.EXTRA_MOVE_AXIS, move_axis);
//        intent.putExtra(FloatingService.EXTRA_GRAVITY, gravity);
//        intent.putExtra(FloatingService.EXTRA_X, x);
//        intent.putExtra(FloatingService.EXTRA_Y, y);
//        intent.putExtra(FloatingService.EXTRA_WIDTH, width);
//        intent.putExtra(FloatingService.EXTRA_HEIGHT, height);
//        intent.putExtra(FloatingService.EXTRA_IS_SHOW, isShow);



        context.startService(intent);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        isBound = true;

    }

    public void destroy() {

        // 斷開連線
        if(isBound){
            binder.unbindService(uniqueId);
            context.unbindService(serviceConnection);
            isBound = false;
        }

    }


    public void show(){

        View view = binder.getView();

        if(view != null && !isShow){

            floatingListener.willOpen(view);

            view.setVisibility(View.VISIBLE);
            isShow = true;

            floatingListener.didOpen(view);


        }


    }

    public void hide(){

        View view = binder.getView();

        if(view != null && isShow){

            floatingListener.willClose(view);

            view.setVisibility(View.GONE);
            isShow = false;

            floatingListener.didClose(view);
        }

    }

    public boolean isShow() {
        return isShow;
    }

//    private Intent getIntent() {
//        if (intent == null)
//            intent = new Intent(context, FloatingService.class);
//        return intent;
//    }
}

