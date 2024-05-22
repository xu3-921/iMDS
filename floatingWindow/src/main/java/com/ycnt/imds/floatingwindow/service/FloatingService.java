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
import com.ycnt.imds.floatingwindow.component.FloatingComponent;

import java.util.HashMap;
import java.util.Map;


public class FloatingService extends Service {


    public static final String EXTRA_UNIQUE_ID = "extra_unique_id";
//    public static final String EXTRA_LAYOUT_RESOURCE = "extra_layout_resource";
//    public static final String EXTRA_MOVE_AXIS = "extra_move_axis";
//    public static final String EXTRA_GRAVITY = "extra_gravity";
//    public static final String EXTRA_X = "extra_x";
//    public static final String EXTRA_Y = "extra_y";
//    public static final String EXTRA_WIDTH = "extra_width";
//    public static final String EXTRA_HEIGHT = "extra_height";
//    public static final String EXTRA_IS_SHOW = "extra_is_show";

    private final Map<String, LocalBinder> binderMap = new HashMap<>();
    private final Map<String, FloatingComponent> floatingComponentHashMap = new HashMap<>();


    public class LocalBinder extends Binder {
        private final String uniqueId;

        public LocalBinder(String uniqueId) {
            this.uniqueId = uniqueId;
        }

        public void createFloatingWindow(FloatingLayoutConfig config){

            Log.e("test03-1",uniqueId);
//            FloatingComponent floatingComponent =
//                    new FloatingComponent(layoutRes, this, move_axis, gravity, x, y, width, height , isShow);

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
            FloatingService.this.unbindService(uniqueId);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {

        String uniqueId = intent.getStringExtra(EXTRA_UNIQUE_ID);

        return binderMap.get(uniqueId);
    }


    private void unbindService(String uniqueId) {

//        Log.e("test01-3", "unbindService " + uniqueId);

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

//        Log.e("test01-3 binderMap", String.valueOf(binderMap.size()));
//        Log.e("test01-3 floatingComponentHashMap", String.valueOf(floatingComponentHashMap.size()));
    }

    @Override
    public boolean onUnbind(Intent intent) {

        if(binderMap.size() == 0){
            stopSelf();
        }

        return true;
    }

    @Override
    public void onRebind(Intent intent) {

        super.onRebind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e("test02", "onStartCommand");

        if (intent != null) {

            String uniqueId = intent.getStringExtra(EXTRA_UNIQUE_ID);

            if (uniqueId != null && !binderMap.containsKey(uniqueId)) {

                binderMap.put(uniqueId, new LocalBinder(uniqueId));

//                int layoutRes = intent.getIntExtra(EXTRA_LAYOUT_RESOURCE, -1);
//                int move_axis = intent.getIntExtra(EXTRA_MOVE_AXIS, -1);
//                int gravity = intent.getIntExtra(EXTRA_GRAVITY, Gravity.START | Gravity.TOP);
//                int x = intent.getIntExtra(EXTRA_X, 0);
//                int y = intent.getIntExtra(EXTRA_Y, 0);
//                int width = intent.getIntExtra(EXTRA_WIDTH, -2);
//                int height = intent.getIntExtra(EXTRA_HEIGHT, -2);
//                boolean isShow = intent.getBooleanExtra(EXTRA_IS_SHOW, true);


//                FloatingComponent floatingComponent =
//                        new FloatingComponent(layoutRes, this, move_axis, gravity, x, y, width, height , isShow);
//
//                floatingComponent.setUp();
//
//                floatingComponentHashMap.put(uniqueId, floatingComponent);
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

        Log.e("test01-5", "service onDestroy");

        super.onDestroy();
    }

}
