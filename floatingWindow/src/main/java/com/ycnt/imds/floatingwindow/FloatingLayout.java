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
import java.util.concurrent.CompletableFuture;

public class FloatingLayout {

    private FloatingService.LocalBinder binder;


    private FloatingLayoutConfig config;

    private Context context;
    private String uniqueId;


    public boolean isShow;

    private FloatingListener floatingListener;

    private Intent intent;

    boolean isBound = false;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

//    private boolean isShow;

    public FloatingLayout(FloatingLayoutConfig config) {

        this.config = config;

        this.context = config.getContext();
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

            // 處理服務斷開連線的情況
            isBound = false;

            if(floatingListener != null){
                floatingListener.onClose();
            }

            clearHandler();

        }

    };

    public void create() {

        this.uniqueId = UUID.randomUUID().toString();

        intent = new Intent(context, FloatingService.class);

        intent.putExtra(FloatingService.EXTRA_UNIQUE_ID, uniqueId);

        context.startService(intent);
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        isBound = true;

    }

//    public void destroy() {
//
//        // 斷開連線
//        if(isBound){
//            binder.unbindService(uniqueId);
//            context.unbindService(serviceConnection);
//            isBound = false;
//        }
//
//    }

    public void destroy() {

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                // 斷開連線
                if (isBound) {

                    floatingListener.onClose();

                    context.unbindService(serviceConnection);
                    binder.unbindService(uniqueId);
                    isBound = false;
                }
                clearHandler(); // 清除所有的回調和訊息
            }
        });

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

    // 清除所有的回調和訊息
    private void clearHandler() {
        mainHandler.removeCallbacksAndMessages(null);
    }
}

