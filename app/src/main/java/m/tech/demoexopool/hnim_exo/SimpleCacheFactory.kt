package m.tech.demoexopool.hnim_exo

import android.content.Context
import android.net.Uri
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.source.dash.offline.DashDownloader
import com.google.android.exoplayer2.source.hls.offline.HlsDownloader
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheWriter
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.upstream.cache.SimpleCache.delete
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.File

/**
 * @author: 89hnim
 * @since: 19/04/2021
 */
object SimpleCacheFactory {

    private var INSTANCE: SimpleCache? = null
    private var CACHE_FACTORY_INSTANCE: CacheDataSource.Factory? = null

    private const val MAX_CACHE_SIZE = 100L * 1024L * 1024L
    private const val MAX_PRE_CACHE_SIZE = 1L * 1024L * 1024L
    private const val CACHE_FOLDER_NAME = "media"

    private var cacheFile: File? = null

    fun init(context: Context) {
        getInstance(context)
    }

    private fun getInstance(context: Context): SimpleCache {
        if (INSTANCE == null) {
            cacheFile = File(context.cacheDir, CACHE_FOLDER_NAME)
            clearCache(context) //clear old cache if exists

            INSTANCE = SimpleCache(
                cacheFile!!,
                LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE),
                ExoDatabaseProvider(context)
            )
        }
        return INSTANCE!!
    }

    fun getCacheDataSource(context: Context): CacheDataSource.Factory {
        if (CACHE_FACTORY_INSTANCE == null) {
            CACHE_FACTORY_INSTANCE = CacheDataSource.Factory()
                .setCache(getInstance(context))
                .setUpstreamDataSourceFactory(
                    DefaultDataSourceFactory(
                        context,
                        context.getUserAgent()
                    )
                )
        }
        return CACHE_FACTORY_INSTANCE!!
    }

    /**
     * Preload images with glide
     */
    fun preloadImages(context: Context, source: Array<String?>?) {
        if (source != null) {
            for (image in source) {
                Glide.with(context)
                    .load(image)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .preload()
            }
        }
    }

    /**
     * Preload video
     * Not test Dash yet
     */
    fun preloadVideo(context: Context, source: String?) {
        if (source == null) return

        CoroutineScope(IO).launch {
            Log.d(TAG, "preloadVideo: $source")

            val type = Util.inferContentType(Uri.parse(source))

            try {
                when (type) {
                    C.TYPE_DASH -> cacheDash(context, source)
                    C.TYPE_HLS -> cacheHls(context, source)
                    else -> cacheDefault(context, source)
                }
            } catch (e: Exception) {
                //don't know why throw null exception when cancel download hls
                Log.e(TAG, "preloadVideo: error ${e.message}")
            }
        }
    }

    private fun cacheDash(context: Context, source: String) {
        val dashDownloader = DashDownloader(
            MediaItem.fromUri(Uri.parse(source)), getCacheDataSource(context)
        )

        dashDownloader.download { contentLength, bytesDownloaded, percentDownloaded ->
            if (bytesDownloaded >= MAX_PRE_CACHE_SIZE) {
                dashDownloader.cancel()
            }
        }
    }

    private fun cacheHls(context: Context, source: String) {
        val hlsDownloader = HlsDownloader(
            MediaItem.fromUri(Uri.parse(source)), getCacheDataSource(context)
        )

        hlsDownloader.download { contentLength, bytesDownloaded, percentDownloaded ->
            if (bytesDownloaded >= MAX_PRE_CACHE_SIZE) {
                hlsDownloader.cancel()
            }
        }
    }

    private fun cacheDefault(context: Context, source: String) {
        val dataSpec = DataSpec.Builder()
            .setUri(Uri.parse(source))
            .setLength(MAX_PRE_CACHE_SIZE)
            .build()

        CacheWriter(
            getCacheDataSource(context).createDataSource(),
            dataSpec,
            true,
            null,
            null
        ).cache()
    }

    fun clearCache(context: Context) {
        cacheFile?.let { delete(it, ExoDatabaseProvider(context)) }
        Glide.get(context).clearMemory() //should use this?
    }

    private fun Context.getUserAgent() = Util.getUserAgent(this, this.packageName ?: "Gapo")

    private const val TAG = "HnimExo::Cache"
}