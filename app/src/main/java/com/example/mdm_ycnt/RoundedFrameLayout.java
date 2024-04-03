package com.example.mdm_ycnt;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.widget.FrameLayout;

public class RoundedFrameLayout extends FrameLayout {

    private float[] cornerRadii = new float[8];
    private Path path = new Path();
    private RectF rect = new RectF();

    public RoundedFrameLayout(Context context) {
        super(context);
    }

    public void setCornerRadii(float topLeft, float topRight, float bottomRight, float bottomLeft) {
        // 设置每个角的圆角半径
        cornerRadii[0] = topLeft; // 左上角
        cornerRadii[1] = topLeft;

        cornerRadii[2] = topRight; // 右上角
        cornerRadii[3] = topRight;

        cornerRadii[4] = bottomRight; // 右下角
        cornerRadii[5] = bottomRight;

        cornerRadii[6] = bottomLeft; // 左下角
        cornerRadii[7] = bottomLeft;

        updatePath();
    }

    private void updatePath() {
        if (rect.right > 0 && rect.bottom > 0) {
            path.reset();
            path.addRoundRect(rect, cornerRadii, Path.Direction.CW);
            invalidate(); // 重繪視圖
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        rect.set(0, 0, w, h);
        updatePath();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int save = canvas.save();
        canvas.clipPath(path);
        super.dispatchDraw(canvas);
        canvas.restoreToCount(save);
    }
}

