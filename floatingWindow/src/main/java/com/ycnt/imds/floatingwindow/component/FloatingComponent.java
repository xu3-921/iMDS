package com.ycnt.imds.floatingwindow.component;

import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;

import com.ycnt.imds.floatingwindow.FloatingLayoutConfig;
import com.ycnt.imds.floatingwindow.module.FloatingViewMovementModule;
import com.ycnt.imds.floatingwindow.module.FloatingWindowModule;


public class FloatingComponent {

    private FloatingLayoutConfig config;

    private FloatingWindowModule floatingWindowModule;
    private FloatingViewMovementModule floatingViewMovementModule;

    public FloatingComponent(FloatingLayoutConfig config) {

        this.config = config;

    }

    public void setUp() {

//        int ROOT_CONTAINER_ID = getViewRootId();

        floatingWindowModule = new FloatingWindowModule(config);
        floatingWindowModule.create();

        View floatingView = floatingWindowModule.getView();
//        View rootContainer = floatingView.findViewById(ROOT_CONTAINER_ID);

        floatingViewMovementModule =
                new FloatingViewMovementModule(
                        floatingView,
                        floatingWindowModule.getWindowManager()
                );

        int move_axis = config.getMovementModule();

        if(move_axis != -1){
            floatingViewMovementModule.run(move_axis);
        }

    }

//    public int getViewRootId(){
//        Context context = config.getContext();
//        return context.getResources().getIdentifier("root_container", "id", context.getPackageName());
//    }

    public FloatingWindowModule getFloatingWindowModule() {
        return floatingWindowModule;
    }


    public void destroy() {

        if (floatingWindowModule != null) {
            floatingWindowModule.destroy();
            floatingWindowModule = null;
        }
        if (floatingViewMovementModule != null) {
            floatingViewMovementModule.destroy();
            floatingViewMovementModule = null;
        }
    }
}
