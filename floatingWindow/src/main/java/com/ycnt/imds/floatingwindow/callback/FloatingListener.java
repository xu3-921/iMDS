package com.ycnt.imds.floatingwindow.callback;

import android.view.View;
import android.widget.LinearLayout;


public interface FloatingListener {

    void onCreate(View view);

    void onClose();

    void willOpen(View view);

    void didOpen(View view);

    void willClose(View view);

    void didClose(View view);
}
