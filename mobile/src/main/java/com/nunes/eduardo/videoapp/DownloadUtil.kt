package com.nunes.eduardo.videoapp

import android.content.Context
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.ProgressiveDownloadAction
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import java.io.File

class DownloadUtil {
    companion object {
        @JvmStatic
        private var cache: Cache? = null

        @JvmStatic
        private var downloadManager: DownloadManager? = null

        @JvmStatic
        fun getCache(context: Context): Cache {
            synchronized(this) {
                if (cache == null) {
                    val cacheDirectory = File(context.getExternalFilesDir(null), "downloads")
                    cache = SimpleCache(cacheDirectory, NoOpCacheEvictor())
                }
                return cache!!
            }
        }

        @JvmStatic
        fun getDownloadManager(context: Context): DownloadManager {
            synchronized(this) {
                if (downloadManager == null) {
                    val actionFile = File(context.getExternalFilesDir(null), "actions")
                    downloadManager = DownloadManager(getCache(context),
                            DefaultDataSourceFactory(context,
                                    Util.getUserAgent(context, "exo-demo")),
                            actionFile, ProgressiveDownloadAction.DESERIALIZER)
                }
                return downloadManager!!
            }
        }
    }
}