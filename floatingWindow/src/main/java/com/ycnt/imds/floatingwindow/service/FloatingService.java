package com.ycnt.imds.floatingwindow.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.Nullable;

import com.ycnt.imds.floatingwindow.FloatingLayoutConfig;
import com.ycnt.imds.floatingwindow.callback.FloatingListener;
import com.ycnt.imds.floatingwindow.component.FloatingComponent;

import java.util.HashMap;
import java.util.Map;


public class FloatingService extends Service {


    public static final String EXTRA_UNIQUE_ID = "extra_unique_id";


    private final Map<String, LocalBinder> binderMap = new HashMap<>();
    private final Map<String, FloatingComponent> floatingComponentHashMap = new HashMap<>();

    public class LocalBinder extends Binder {
        private final String uniqueId;

        public LocalBinder(String uniqueId) {
            this.uniqueId = uniqueId;
        }

        public void createFloatingWindow(FloatingLayoutConfig config){


            FloatingComponent floatingComponent =
                    new FloatingComponent(config);

            floatingComponent.setUp();

            floatingComponentHashMap.put(uniqueId, floatingComponent);
        }

        public FloatingService.LocalBinder getLocalBinder(String uniqueId){
            return binderMap.get(uniqueId);
        }

        public FloatingService getService() {
            return FloatingService.this;
        }

        public View getView() {

            FloatingComponent getFloatingComponent = floatingComponentHashMap.get(uniqueId);

            if(getFloatingComponent != null){
                return getFloatingComponent.getFloatingWindowModule().getView();
            }else{
                return null;
            }

        }

        public void unbindService(String uniqueId) {

//            floatingListener.onClose();
            FloatingService.this.unbindService(uniqueId);

        }
    }

    private void unbindService(String uniqueId) {

        LocalBinder binder = binderMap.get(uniqueId);
        FloatingComponent getFloatingComponent = floatingComponentHashMap.get(uniqueId);

        if (binder != null) {
            binderMap.remove(uniqueId);
        }

        if (getFloatingComponent != null) {

            getFloatingComponent.destroy();

            floatingComponentHashMap.remove(uniqueId);
        }

        if (binderMap.isEmpty() && floatingComponentHashMap.isEmpty()) {

            stopSelf();
        }

    }

    @Override
    public IBinder onBind(Intent intent) {

        String uniqueId = intent.getStringExtra(EXTRA_UNIQUE_ID);

        return binderMap.get(uniqueId);
    }

    @Override
    public boolean onUnbind(Intent intent) {

        if(binderMap.size() == 0){
            stopSelf();
        }

        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {

        super.onRebind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {

            String uniqueId = intent.getStringExtra(EXTRA_UNIQUE_ID);

            if (uniqueId != null && !binderMap.containsKey(uniqueId)) {

                binderMap.put(uniqueId, new LocalBinder(uniqueId));

            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        if(!binderMap.isEmpty()){
            binderMap.clear();
        }

        if(!floatingComponentHashMap.isEmpty()){

            floatingComponentHashMap.forEach((getId, floatingComponent) -> {
                floatingComponent.destroy();
            });

            floatingComponentHashMap.clear();
        }

        super.onDestroy();
    }

}
