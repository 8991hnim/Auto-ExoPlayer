package m.tech.demoexopool

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gg.gapo.video.hnim_exo.ExoController
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ui.PlayerView
import m.tech.demoexopool.hnim_exo.HnimExo

class VideoAdapter(
    private val context: Context,
    private val hnimExo: HnimExo,
    private val onMoveToNext: () -> Unit
) : ListAdapter<VideoItem, RecyclerView.ViewHolder>(ItemCallback()) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return currentList[position].hashCode().toLong()
    }

    private class ItemCallback : DiffUtil.ItemCallback<VideoItem>() {

        override fun areItemsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return VideoHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_video,
                parent,
                false
            )
        )
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val layoutManager = recyclerView.layoutManager
        (layoutManager as LinearLayoutManager)?.recycleChildrenOnDetach = true
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is VideoHolder -> {
                holder.bind(currentList[position])
            }
        }
    }

    var isMuted = false

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        Log.d("BindAdapter", "onViewRecycled: called")
    }

    inner class VideoHolder
    constructor(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val listener = object : ExoController.HnimExoPlayerListener {
            override fun onBuffering() {
                super.onBuffering()
                itemView.findViewById<PlayerView>(R.id.playerView).useController = false
            }

            override fun onReady() {
                super.onReady()
                itemView.findViewById<PlayerView>(R.id.playerView).useController = true
                itemView.findViewById<PlayerView>(R.id.playerView).hideController()
            }

            override fun onEnded() {
                super.onEnded()
                itemView.findViewById<PlayerView>(R.id.playerView).useController = false
                itemView.findViewById<PlayerView>(R.id.playerView).hideController()
            }

            override fun onPlayingChanged(isPlaying: Boolean) {
                super.onPlayingChanged(isPlaying)
            }

            override fun onError(exception: ExoPlaybackException) {
                super.onError(exception)
            }
        }

        fun getPlayerView(): PlayerView? = itemView.findViewById(R.id.playerView)

        fun bind(item: VideoItem) = with(itemView) {
            Log.d("BindAdapter", "bind: $adapterPosition")
            itemView.findViewById<TextView>(R.id.tvTest).text = item.source + " - " + adapterPosition

            itemView.findViewById<TextView>(R.id.btnMute).setOnClickListener {
                isMuted = !isMuted
                hnimExo.getExoController().setMuted(adapterPosition, isMuted)

                if (isMuted) {
                    itemView.findViewById<TextView>(R.id.btnMute).text = "Unmute"
                } else {
                    itemView.findViewById<TextView>(R.id.btnMute).text = "Mute"
                }

                notifyItemRangeChanged(0, itemCount, Videoinfo())
            }

            if (isMuted) {
                itemView.findViewById<TextView>(R.id.btnMute).text = "Unmute"
            } else {
                itemView.findViewById<TextView>(R.id.btnMute).text = "Mute"
            }

            hnimExo.getExoController().setupWith(
                position = adapterPosition,
                source = item.source,
                preloadSource = arrayOf(
                    try {
                        currentList[adapterPosition + 1].source
                    } catch (e: Exception) {
                        null
                    },
                    try {
                        currentList[adapterPosition + 2].source
                    } catch (e: Exception) {
                        null
                    }
                ),
                preloadImageSource = arrayOf(
                    try {
                        currentList[adapterPosition + 1].thumb
                    } catch (e: Exception) {
                        null
                    },
                    try {
                        currentList[adapterPosition + 2].thumb
                    } catch (e: Exception) {
                        null
                    },
                    try {
                        currentList[adapterPosition + 3].thumb
                    } catch (e: Exception) {
                        null
                    }
                ),
                thumbnail = itemView.findViewById(R.id.thumbnail),
                thumbSource = item.thumb,
                loadingView = null,
                listener = listener
            )

        }

    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            if (payloads.any { it is Videoinfo }) {
                when (holder) {
                    is VideoHolder -> {
                        holder.bind(currentList[position])
                    }
                }
            }
        }
    }

    class Videoinfo

}
