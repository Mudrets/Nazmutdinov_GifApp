package com.example.gifapp

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.util.CoilUtils
import okhttp3.OkHttpClient
import android.os.Build.VERSION.SDK_INT
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.CachePolicy

class MyApp : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        //Для Coil
        return ImageLoader.Builder(applicationContext)
            .crossfade(true)
            .diskCachePolicy(CachePolicy.ENABLED)
            .okHttpClient {
                OkHttpClient.Builder()
                    .cache(CoilUtils.createDefaultCache(applicationContext))
                    .build()
            }.componentRegistry {
                if (SDK_INT >= 28) {
                    add(ImageDecoderDecoder(applicationContext))
                } else {
                    add(GifDecoder())
                }
            }
            .build()
    }
}