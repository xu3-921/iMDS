package com.example.mdm_ycnt.aidlBindHelper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.Method;

public class ServiceBinderHelper<T> {

    private Context context;
    private T myService;
    private ServiceBindListener<T> serviceBindListener;
    private String serviceAction;
    private String servicePackageName;
    private String interfaceCanonicalName;

    public interface ServiceBindListener<T> {
        void onServiceBound(T service);
        void onServiceError();
    }

    public ServiceBinderHelper(Context context, String serviceAction, String servicePackageName, String interfaceCanonicalName) {
        this.context = context.getApplicationContext();
        this.serviceAction = serviceAction;
        this.servicePackageName = servicePackageName;
        this.interfaceCanonicalName = interfaceCanonicalName;
    }

    public void bindServiceAsync(ServiceBindListener<Object> listener) {
        this.serviceBindListener = (ServiceBindListener<T>) listener;
        Intent intent = new Intent(serviceAction);
        intent.setPackage(servicePackageName);
        boolean bound = context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        if (!bound && serviceBindListener != null) {
            serviceBindListener.onServiceError();
        }
    }

    public void unbindService() {

        if (myService != null) {
            context.unbindService(serviceConnection);
            myService = null;
        }
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                // 获取AIDL Stub类
                Class<?> stub = Class.forName(interfaceCanonicalName + "$Stub");
                // 调用asInterface方法
                Method asInterfaceMethod = stub.getDeclaredMethod("asInterface", IBinder.class);
                myService = (T) asInterfaceMethod.invoke(null, service);
                if (serviceBindListener != null) {
                    serviceBindListener.onServiceBound(myService);
                }
            } catch (Exception e) {

                if (serviceBindListener != null) {
                    serviceBindListener.onServiceError();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myService = null;
        }

        @Override
        public void onBindingDied(ComponentName name) {
            if (serviceBindListener != null) {
                serviceBindListener.onServiceError();
            }
        }

        @Override
        public void onNullBinding(ComponentName name) {
            if (serviceBindListener != null) {
                serviceBindListener.onServiceError();
            }
        }
    };
}