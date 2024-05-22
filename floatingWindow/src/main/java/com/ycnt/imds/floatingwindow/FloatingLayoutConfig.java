package com.ycnt.imds.floatingwindow;

import android.content.Context;
import android.view.Gravity;
import android.view.WindowManager;

import com.ycnt.imds.floatingwindow.module.FloatingViewMovementModule;

public class FloatingLayoutConfig {

    private final Context context;
    private final int layoutRes;
    private final int movementModule;
    private final int gravity;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final boolean isShow;
    private final int flags;

    private FloatingLayoutConfig(Builder builder) {

        this.context = builder.context;
        this.layoutRes = builder.layoutRes;
        this.movementModule = builder.movementModule;
        this.gravity = builder.gravity;
        this.x = builder.x;
        this.y = builder.y;
        this.width = builder.width;
        this.height = builder.height;
        this.isShow = builder.isShow;
        this.flags = builder.flags;

    }

    public static class Builder {
        private Context context;
        private int layoutRes = -1;
        private int movementModule = FloatingViewMovementModule.MOVE_AXIS_XY;
        private int gravity = Gravity.START | Gravity.TOP;
        private int x = 0;
        private int y = 0;
        private int width = -2;
        private int height = -2;
        private boolean isShow = true;
        private int flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setLayoutRes(int layoutRes) {
            this.layoutRes = layoutRes;
            return this;
        }

        public Builder setMovementModule(int movementModule) {
            this.movementModule = movementModule;
            return this;
        }

        public Builder setGravity(int gravity) {
            this.gravity = gravity;
            return this;
        }

        public Builder setX(int x) {
            this.x = x;
            return this;
        }

        public Builder setY(int y) {
            this.y = y;
            return this;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setShow(boolean isShow) {
            this.isShow = isShow;
            return this;
        }

        public Builder setFlags(int flags) {
            this.flags = flags;
            return this;
        }

        public FloatingLayoutConfig build() {
            return new FloatingLayoutConfig(this);
        }
    }

    // Getters for all fields
    public Context getContext() {
        return context;
    }

    public int getLayoutRes() {
        return layoutRes;
    }

    public int getMovementModule() {
        return movementModule;
    }

    public int getGravity() {
        return gravity;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isShow() {
        return isShow;
    }

    public int getFlags() {
        return flags;
    }
}

