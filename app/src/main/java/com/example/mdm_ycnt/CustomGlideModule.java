package com.example.mdm_ycnt;

import android.content.Context;
import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;

@GlideModule
public final class CustomGlideModule extends AppGlideModule {

    @Override
    public void registerComponents(Context context, Glide glide, com.bumptech.glide.Registry registry) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(30, TimeUnit.SECONDS);
        builder.connectTimeout(30, TimeUnit.SECONDS);

        OkHttpClient okHttpClient = builder.build();
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory((Call.Factory) okHttpClient));
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}
