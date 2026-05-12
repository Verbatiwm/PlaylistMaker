package com.example.playlistmaker

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class TrackViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context)
        .inflate(R.layout.item_track, parent, false)
) {
    private val trackName: TextView = itemView.findViewById(R.id.track_name)
    private val trackInfo: TextView = itemView.findViewById(R.id.track_info)
    private val artwork: ImageView = itemView.findViewById(R.id.artwork)

    fun bind(track: Track) {

        trackName.text = track.trackName
        val time = SimpleDateFormat(
            "mm:ss",
            Locale.getDefault()
        ).format(track.trackTimeMillis)

        trackInfo.text = "${track.artistName} • $time"


        Glide.with(itemView)
            .load(track.artworkUrl100)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_placeholder)
            .transform(CenterCrop(), RoundedCorners(8))
            .into(artwork)
    }
}