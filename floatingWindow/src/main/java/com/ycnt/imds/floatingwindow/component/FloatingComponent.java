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


//    private int layoutRes;
//    private int move_axis;
//    private int gravity;
//    public final int x;
//    public final int y;
//    public final int width;
//    public final int height;
//    public final boolean isShow;

    private FloatingLayoutConfig config;

//    private Context context;
    private FloatingWindowModule floatingWindowModule;
    private FloatingViewMovementModule floatingViewMovementModule;

    public FloatingComponent(FloatingLayoutConfig config) {

        this.config = config;

//        int layoutRes, Context context, int move_axis, int gravity, int x, int y, int width, int height, boolean isShow

//        this.layoutRes = layoutRes;
//        this.context = context;
//        this.move_axis = move_axis;
//        this.gravity = gravity;
//
//        this.x = x;
//        this.y = y;
//        this.width = width;
//        this.height = height;
//        this.isShow = isShow;

    }

    public void setUp() {

        int ROOT_CONTAINER_ID = getViewRootId();

//        floatingWindowModule = new FloatingWindowModule(context, layoutRes, gravity, x, y, width, height, isShow);
        floatingWindowModule = new FloatingWindowModule(config);
        floatingWindowModule.create();

        View floatingView = floatingWindowModule.getView();
        View rootContainer = floatingView.findViewById(ROOT_CONTAINER_ID);

        floatingViewMovementModule =
                new FloatingViewMovementModule(
                        floatingWindowModule.getParams(),
                        floatingView,
                        floatingWindowModule.getWindowManager(),
                        floatingView
                );

        int move_axis = config.getMovementModule();

        if(move_axis != -1){
            floatingViewMovementModule.run(move_axis);
        }

    }


    public int getViewRootId(){
        Context context = config.getContext();
        return context.getResources().getIdentifier("root_container", "id", context.getPackageName());
    }

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
