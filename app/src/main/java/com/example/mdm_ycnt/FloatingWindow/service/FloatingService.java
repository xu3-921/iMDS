package com.example.mdm_ycnt.FloatingWindow.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.mdm_ycnt.FloatingWindow.component.FloatingComponent;

/**
 * Author: Hamed Taherpour
 * *
 * Created: 7/30/2020
 * *
 * Address: https://github.com/HamedTaherpour
 */
public class FloatingService extends Service {

    public static final String EXTRA_LAYOUT_RESOURCE = "extra_layout_resource";
    public static final String EXTRA_RECEIVER = "extra_receiver";

    private FloatingComponent floatingComponent;
    //TODO: Memory leak
    @SuppressLint("StaticFieldLeak")
    public static View view;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int layoutRes = intent.getIntExtra(EXTRA_LAYOUT_RESOURCE, -1);
            ResultReceiver receiver = intent.getParcelableExtra(EXTRA_RECEIVER);

            floatingComponent = new FloatingComponent(layoutRes, this);
            if (receiver != null)
                floatingComponent.setReceiver(receiver);
            floatingComponent.setUp();

            view = floatingComponent.getFloatingWindowModule().getView();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (floatingComponent != null){
            floatingComponent.destroy();
            floatingComponent = null;
        }

        if (view != null){
            view = null;
        }

        super.onDestroy();
    }

}
