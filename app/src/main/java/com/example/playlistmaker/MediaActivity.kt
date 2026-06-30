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
import java.util.Date
import java.util.Locale
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper

class MediaActivity : AppCompatActivity() {

    private lateinit var track: Track
    private lateinit var currentTimeText: TextView
    private lateinit var playButton: ImageButton
    private var isPlaying = false
    private var isFavorite = false
    private var mediaPlayer: MediaPlayer? = null
    private var isPrepared = false
    private var playWhenPrepared = false
    private val dateFormat by lazy {
        SimpleDateFormat("mm:ss", Locale.getDefault())
    }

    companion object {
        private const val TIMER_UPDATE_DELAY = 300L
    }
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {

            mediaPlayer?.let {

                currentTimeText.text =
                    dateFormat.format(Date(it.currentPosition.toLong()))

                handler.postDelayed(this, TIMER_UPDATE_DELAY)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_media)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        playButton = findViewById(R.id.playButton)
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

            if (isPlaying || playWhenPrepared) {
                pausePlayer()
            } else {
                startPlayer()
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
                    if (playWhenPrepared) {
                        startPlayer()
                    }
                }
                setOnCompletionListener {

                    this@MediaActivity.isPlaying = false
                    this@MediaActivity.playWhenPrepared = false
                    it.seekTo(0)
                    stopTimer()
                    currentTimeText.text = "00:00"
                    updatePlayButton()
                }
                setOnErrorListener { _, _, _ ->

                    this@MediaActivity.isPlaying = false
                    this@MediaActivity.playWhenPrepared = false
                    this@MediaActivity.isPrepared = false
                    stopTimer()
                    currentTimeText.text = "00:00"
                    updatePlayButton()
                    true
                }
            }

        } catch (e: Exception) {

            currentTimeText.text = "00:00"
        }
    }

    private fun startPlayer() {
        if (track.previewUrl.isNullOrEmpty()) return

        if (!isPrepared) {
            playWhenPrepared = true
            updatePlayButton(isPendingStart = true)
            return
        }

        mediaPlayer?.start()
        isPlaying = true
        playWhenPrepared = false
        updatePlayButton()
        startTimer()
    }

    private fun pausePlayer() {
        if (isPrepared && isPlaying) {
            mediaPlayer?.pause()
        }
        isPlaying = false
        playWhenPrepared = false
        stopTimer()
        updatePlayButton()
    }

    private fun startTimer() {
        handler.removeCallbacks(updateRunnable)
        handler.post(updateRunnable)
    }

    private fun stopTimer() {
        handler.removeCallbacks(updateRunnable)
    }

    private fun updatePlayButton(isPendingStart: Boolean = false) {
        val buttonBackground = if (isPlaying || isPendingStart) {
            R.drawable.ic_pause_button
        } else {
            R.drawable.ic_play_button
        }
        playButton.setBackgroundResource(buttonBackground)
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

        stopTimer()

        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onPause() {
        super.onPause()
        if (isPlaying || playWhenPrepared) {
            pausePlayer()
        }
    }
}
