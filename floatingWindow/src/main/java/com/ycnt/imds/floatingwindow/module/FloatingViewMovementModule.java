package com.ycnt.imds.floatingwindow.module;

import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;


public class FloatingViewMovementModule {

    public static final int MOVE_AXIS_X = 1;
    public static final int MOVE_AXIS_Y = 2;
    public static final int MOVE_AXIS_XY = 3;

    private WindowManager.LayoutParams params;
    private View rootContainer;
    private WindowManager windowManager;
    private View baseView;

    public FloatingViewMovementModule(WindowManager.LayoutParams params, View rootContainer, WindowManager windowManager, View baseView) {
        this.params = params;
        this.rootContainer = rootContainer;
        this.windowManager = windowManager;
        this.baseView = baseView;
    }

    public void run(int moveAxis) {

        if (rootContainer != null && moveAxis != -1) { // get position for moving

//            rootContainer.setOnTouchListener(new View.OnTouchListener() {
//                private int initialX;
//                private int initialY;
//                private float initialTouchX;
//                private float initialTouchY;
//                private boolean isDragging;
//
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    WindowManager.LayoutParams params = (WindowManager.LayoutParams) v.getLayoutParams();
//                    switch (event.getAction()) {
//                        case MotionEvent.ACTION_DOWN:
//                            initialX = params.x;
//                            initialY = params.y;
//                            initialTouchX = event.getRawX();
//                            initialTouchY = event.getRawY();
//                            isDragging = false; // 初始化时，没有拖动
//                            return true;
//                        case MotionEvent.ACTION_MOVE:
//                            // Calculate the distance moved
//                            final int dx = (int) (event.getRawX() - initialTouchX);
//                            final int dy = (int) (event.getRawY() - initialTouchY);
//
//                            // Update the position if there is significant movement
//                            if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
//                                if ((params.gravity & Gravity.START) == Gravity.START || (params.gravity & Gravity.END) == Gravity.END || (params.gravity & Gravity.CENTER_HORIZONTAL) == Gravity.CENTER_HORIZONTAL) {
//                                    if (moveAxis == MOVE_AXIS_X || moveAxis == MOVE_AXIS_XY) {
//                                        params.x = initialX + (params.gravity & Gravity.END) == Gravity.END ? -dx : dx;
//                                    }
//                                }
//
//                                if ((params.gravity & Gravity.TOP) == Gravity.TOP || (params.gravity & Gravity.BOTTOM) == Gravity.BOTTOM || (params.gravity & Gravity.CENTER_VERTICAL) == Gravity.CENTER_VERTICAL) {
//                                    if (moveAxis == MOVE_AXIS_Y || moveAxis == MOVE_AXIS_XY) {
//                                        params.y = initialY + (params.gravity & Gravity.BOTTOM) == Gravity.BOTTOM ? -dy : dy;
//                                    }
//                                }
//
//                                windowManager.updateViewLayout(v, params);
//                                isDragging = true; // 标记为拖动
//                            }
//                            return true;
//                        case MotionEvent.ACTION_UP:
//                            if (!isDragging) {
//                                // 如果没有拖动，则触发点击事件
//                                v.performClick();
//                            }
//                            return true;
//                    }
//                    return false;
//                }
//            });

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

//            rootContainer.setOnTouchListener(new View.OnTouchListener() {
//                private int initialX = 0;
//                private int initialY = 0;
//                private float initialTouchX = 0f;
//                private float initialTouchY = 0f;
//
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    switch (event.getAction()) {
//                        case MotionEvent.ACTION_DOWN:
//                            //remember the initial position.
//                            initialX = params.x;
//                            initialY = params.y;
//                            //get the touch location
//                            initialTouchX = event.getRawX();
//                            initialTouchY = event.getRawY();
//                            return true;
//                        case MotionEvent.ACTION_MOVE:
//                            //Calculate the X and Y coordinates of the view.
//                            params.x = (int) (initialX + (event.getRawX() - initialTouchX));
//                            params.y = (int) (initialY + (event.getRawY() - initialTouchY));
//                            //Update the layout with new X & Y coordinate
//                            windowManager.updateViewLayout(baseView, params);
//                            return true;
//                    }
//                    return false;
//                }
//            });
        }
    }

    public void destroy() {
        try {
            if (windowManager != null)
                if (baseView != null)
                    windowManager.removeViewImmediate(baseView);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
            params = null;
            baseView = null;
            windowManager = null;
        }
    }

}
