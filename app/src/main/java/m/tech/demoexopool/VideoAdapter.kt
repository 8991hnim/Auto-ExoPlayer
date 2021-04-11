package m.tech.demoexopool

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ui.PlayerView
import m.tech.demoexopool.exo.ExoController
import m.tech.demoexopool.exo.ExoProvider
import m.tech.demoexopool.exo.HnimExo

class VideoAdapter(
    private val hnimExo: HnimExo,
    private val onMoveToNext: () -> Unit
) : ListAdapter<VideoItem, RecyclerView.ViewHolder>(ItemCallback()) {

    private class ItemCallback : DiffUtil.ItemCallback<VideoItem>() {

        override fun areItemsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
            return false
        }

        override fun areContentsTheSame(oldItem: VideoItem, newItem: VideoItem): Boolean {
            return false
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is VideoHolder -> {
                holder.bind(currentList[position])
            }
        }
    }

    var isMuted = false

    inner class VideoHolder
    constructor(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: VideoItem) = with(itemView) {
            itemView.findViewById<TextView>(R.id.tvTest).text = item.name + " - " + adapterPosition

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
                thumbnail =  itemView.findViewById(R.id.thumbnail),
                thumbSource = item.thumb,
                useController = true,
                playerView = itemView.findViewById(R.id.playerView),
                listener = object: ExoController.HnimExoPlayerListener{
                    override fun onEnded() {
                        super.onEnded()
                        onMoveToNext()
                    }
                }
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
