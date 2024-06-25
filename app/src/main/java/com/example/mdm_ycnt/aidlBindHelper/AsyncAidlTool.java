package com.example.mdm_ycnt.aidlBindHelper;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.CompletableFuture;

public class AsyncAidlTool {

    public class ServiceConnectionResult {
        private final Object service;
        private final ServiceBinderHelper<?> binderHelper;

        public ServiceConnectionResult(Object service, ServiceBinderHelper<?> binderHelper) {
            this.service = service;
            this.binderHelper = binderHelper;
        }

        public Object getService() {
            return service;
        }

        public ServiceBinderHelper<?> getBinderHelper() {
            return binderHelper;
        }
    }


    public CompletableFuture<Object> bindServiceAsyncWithResult(
            Context context, String serviceAction, String servicePackageName, String interfaceCanonicalName
    ) {

        CompletableFuture<Object> future = new CompletableFuture<>();

        ServiceBinderHelper<?> binderHelper = new ServiceBinderHelper<>(
                context, // Context
                serviceAction,
                servicePackageName,
                interfaceCanonicalName // AIDL介面的完整類別名
        );

        binderHelper.bindServiceAsync(new ServiceBinderHelper.ServiceBindListener<Object>() {
            @Override
            public void onServiceBound(Object service) {

                future.complete(new ServiceConnectionResult(service, binderHelper)); // 將服務和binder Helper一起返回

            }

            @Override
            public void onServiceError() {
                future.completeExceptionally(new Exception("Service binding failed")); // 服務綁定失敗，完成 Future 並拋出異常
            }
        });

        return future;
    }

}
