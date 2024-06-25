package com.ycnt.imds.floatingwindow.module;

import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;


public class FloatingViewMovementModule {

    public static final int MOVE_AXIS_X = 1;
    public static final int MOVE_AXIS_Y = 2;
    public static final int MOVE_AXIS_XY = 3;

//    private WindowManager.LayoutParams params;
    private View rootContainer;
    private WindowManager windowManager;

    public FloatingViewMovementModule(View rootContainer, WindowManager windowManager) {

//        this.params = params;
        this.rootContainer = rootContainer;
        this.windowManager = windowManager;

    }

    public void run(int moveAxis) {

        if (rootContainer != null && moveAxis != -1) { // get position for moving

            rootContainer.setOnTouchListener(new View.OnTouchListener() {
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;
                private boolean isDragging;

                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    WindowManager.LayoutParams params = (WindowManager.LayoutParams) v.getLayoutParams();

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = params.x;
                            initialY = params.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            isDragging = false; // 初始化時，沒有拖動
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            // Calculate the distance moved
                            final int dx = (int) (event.getRawX() - initialTouchX);
                            final int dy = (int) (event.getRawY() - initialTouchY);

                            // Update the position if there is significant movement
                            if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                                if (moveAxis == MOVE_AXIS_X || moveAxis == MOVE_AXIS_XY) {
                                    params.x = initialX + dx;
                                }
                                if (moveAxis == MOVE_AXIS_Y || moveAxis == MOVE_AXIS_XY) {
                                    params.y = initialY + dy;
                                }
                                windowManager.updateViewLayout(v, params);
                                isDragging = true; // 标记为拖动
                            }
                            return true;
                        case MotionEvent.ACTION_UP:
                            if (!isDragging) {
                                // 如果没有拖动，则触发点击事件
                                v.performClick();
                            }
                            return true;
                    }
                    return false;
                }
            });

        }
    }

    public void destroy() {


        if (rootContainer instanceof ViewGroup) {

            ViewGroup viewGroup = (ViewGroup) rootContainer;
            viewGroup.removeAllViews();

        }

        rootContainer = null;
        windowManager = null;

//        try {
//            if (windowManager != null)
//                if (baseView != null)
//                    windowManager.removeViewImmediate(baseView);
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//        } finally {
//            params = null;
//            baseView = null;
//            windowManager = null;
//        }

//        params = null;
//        baseView = null;

    }

}
