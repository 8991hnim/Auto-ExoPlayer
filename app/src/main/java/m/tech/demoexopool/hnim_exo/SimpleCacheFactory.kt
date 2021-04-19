package m.tech.demoexopool.hnim_exo

import android.content.Context
import android.net.Uri
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.*
import com.google.android.exoplayer2.upstream.cache.SimpleCache.delete
import com.google.android.exoplayer2.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.File

object SimpleCacheFactory {

    private var INSTANCE: SimpleCache? = null
    var CACHE_DATASOURCE_FACTORY: CacheDataSource.Factory? = null

    private const val MAX_CACHE_SIZE = 100L * 1024L * 1024L
    private const val MAX_PRE_CACHE_SIZE = 1L * 1024L * 1024L
    private const val CACHE_FOLDER_NAME = "media"

    private var cacheFile: File? = null

    fun init(context: Context){
        getInstance(context)
    }

    private fun getInstance(context: Context): SimpleCache {
        if (INSTANCE == null) {
            Log.d(TAG, "preload: INSTANCE NULL")
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
        if (CACHE_DATASOURCE_FACTORY == null) {
            CACHE_DATASOURCE_FACTORY = CacheDataSource.Factory()
                .setCache(getInstance(context))
                .setUpstreamDataSourceFactory(
                    DefaultDataSourceFactory(
                        context,
                        context.getUserAgent()
                    )
                )
        }
        return CACHE_DATASOURCE_FACTORY!!
    }

    fun clearCache(context: Context) {
        cacheFile?.let { delete(it, ExoDatabaseProvider(context)) }
    }

    fun preloadVideo(context: Context, source: String?) {
        if (source == null) return

        Log.d(
            TAG,
            "preloadVideo: CACHE LEFT ${INSTANCE?.cacheSpace} - ${(INSTANCE?.cacheSpace ?: 0) / 1024 / 1024}"
        )
        CoroutineScope(IO).launch {
            Log.d(TAG, "preloadVideo: $source")

            try {
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
            } catch (e: Exception) {
                //sẽ bắn exception lần đầu do instance simple cache chưa được khởi tạo
                Log.e(TAG, "preloadVideo: error ${e.message}")
            }
        }
    }

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

    fun Context.getUserAgent() = Util.getUserAgent(this, this.packageName ?: "Gapo")

    private const val TAG = "HnimExo::Cache"
}