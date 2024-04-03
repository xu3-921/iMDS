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
                interfaceCanonicalName // AIDL接口的完整类名
        );

        binderHelper.bindServiceAsync(new ServiceBinderHelper.ServiceBindListener<Object>() {
            @Override
            public void onServiceBound(Object service) {

                future.complete(new ServiceConnectionResult(service, binderHelper)); // 将服务和binderHelper一起返回

            }

            @Override
            public void onServiceError() {
                future.completeExceptionally(new Exception("Service binding failed")); // 服务绑定失败，完成 Future 并抛出异常
            }
        });

        return future;
    }

}
