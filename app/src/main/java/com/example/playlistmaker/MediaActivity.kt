package com.example.playlistmaker

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.*
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper

class MediaActivity : AppCompatActivity() {

    private lateinit var track: Track

    private lateinit var currentTimeText: TextView
    private var isPlaying = false
    private var isFavorite = false
    private var mediaPlayer: MediaPlayer? = null
    private var isPrepared = false

    private val handler = Handler(Looper.getMainLooper())

    private val updateRunnable = object : Runnable {
        override fun run() {

            mediaPlayer?.let {

                currentTimeText.text =
                    SimpleDateFormat(
                        "mm:ss",
                        Locale.getDefault()
                    ).format(it.currentPosition)

                handler.postDelayed(this, 300)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media)


        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        val playButton = findViewById<ImageButton>(R.id.playButton)
        val favButton = findViewById<ImageButton>(R.id.favButton)

        val trackExtra = intent.getSerializableExtra("track")

        if (trackExtra == null) {
            finish()
            return
        }


        track = trackExtra as Track







        val trackName = findViewById<TextView>(R.id.trackName)
        val artistName = findViewById<TextView>(R.id.artistName)
        val cover = findViewById<ImageView>(R.id.cover)

        currentTimeText = findViewById(R.id.currentTime)
        preparePlayer()
        val durationValue = findViewById<TextView>(R.id.durationValue)

        val albumLabel = findViewById<TextView>(R.id.albumLabel)
        val albumValue = findViewById<TextView>(R.id.albumValue)

        val yearLabel = findViewById<TextView>(R.id.yearLabel)
        val yearValue = findViewById<TextView>(R.id.yearValue)

        val genreLabel = findViewById<TextView>(R.id.genreLabel)
        val genreValue = findViewById<TextView>(R.id.genreValue)

        val countryLabel = findViewById<TextView>(R.id.countryLabel)
        val countryValue = findViewById<TextView>(R.id.countryValue)



        trackName.text = track.trackName
        artistName.text = track.artistName


        val highResCover = track.artworkUrl100
            .replaceAfterLast('/', "512x512bb.jpg")

        Glide.with(this)
            .load(highResCover)
            .placeholder(R.drawable.ic_placeholder)
            .error(R.drawable.ic_placeholder)
            .fallback(R.drawable.ic_placeholder)
            .into(cover)


        currentTimeText.text = "00:00"


        durationValue.text = formatTime(track.trackTimeMillis)


        if (!track.collectionName.isNullOrEmpty()) {
            albumValue.text = track.collectionName
        } else {
            albumLabel.visibility = View.GONE
            albumValue.visibility = View.GONE
        }


        if (!track.releaseDate.isNullOrEmpty()) {
            yearValue.text = extractYear(track.releaseDate!!)
        } else {
            yearLabel.visibility = View.GONE
            yearValue.visibility = View.GONE
        }


        if (!track.primaryGenreName.isNullOrEmpty()) {
            genreValue.text = track.primaryGenreName
        } else {
            genreLabel.visibility = View.GONE
            genreValue.visibility = View.GONE
        }


        if (!track.country.isNullOrEmpty()) {
            countryValue.text = track.country
        } else {
            countryLabel.visibility = View.GONE
            countryValue.visibility = View.GONE
        }

        playButton.setOnClickListener {

            isPlaying = !isPlaying

            if (isPlaying) {
                playButton.setBackgroundResource(R.drawable.ic_pause_button)
            } else {
                playButton.setBackgroundResource(R.drawable.ic_play_button)
            }
        }

        favButton.setOnClickListener {

            isFavorite = !isFavorite

            if (isFavorite) {
                favButton.setBackgroundResource(
                    R.drawable.ic_fav_button_selected
                )
            } else {
                favButton.setBackgroundResource(
                    R.drawable.ic_fav_button
                )
            }
        }



    }


    private fun preparePlayer() {

        if (track.previewUrl.isNullOrEmpty()) return

        try {

            mediaPlayer = MediaPlayer()

            mediaPlayer?.apply {

                setDataSource(track.previewUrl)

                prepareAsync()

                setOnPreparedListener {

                    isPrepared = true
                    currentTimeText.text = "00:00"
                }

                setOnErrorListener { _, _, _ ->

                    currentTimeText.text = "ERROR"
                    true
                }
            }

        } catch (e: Exception) {

            currentTimeText.text = "ERROR"
        }
    }


    private fun formatTime(millis: Long): String {
        val sdf = SimpleDateFormat("mm:ss", Locale.getDefault())
        return sdf.format(Date(millis))
    }


    private fun extractYear(date: String): String {
        return try {
            date.take(4)
        } catch (e: Exception) {
            ""
        }
    }
    override fun onDestroy() {
        super.onDestroy()

        handler.removeCallbacks(updateRunnable)

        mediaPlayer?.release()
        mediaPlayer = null
    }
}
