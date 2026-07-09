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
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import com.example.playlistmaker.network.RetrofitClient
import com.example.playlistmaker.network.SearchResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MediaActivity : AppCompatActivity() {

    private lateinit var track: Track
    private lateinit var currentTimeText: TextView
    private lateinit var playButton: ImageButton

    private var isFavorite = false
    private var mediaPlayer: MediaPlayer? = null
    private var playWhenPrepared = false
    private var playerState = STATE_DEFAULT
    private var previewUrl: String? = null
    private var previewUrlCall: Call<SearchResponse>? = null
    private val dateFormat by lazy {
        SimpleDateFormat("mm:ss", Locale.getDefault())
    }

    companion object {
        private const val TIMER_UPDATE_DELAY = 300L

        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
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
        previewUrl = track.previewUrl

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

        resetCurrentTime()

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

            playbackControl()
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

        val url = previewUrl

        if (url.isNullOrEmpty()) {
            loadPreviewUrl()
            return
        }

        try {
            mediaPlayer?.release()
            mediaPlayer = null
            playerState = STATE_DEFAULT

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                setDataSource(url)

                setOnPreparedListener {

                    playerState = STATE_PREPARED
                    resetCurrentTime()
                    if (playWhenPrepared) {
                        startPlayer()
                    }
                }
                setOnCompletionListener {


                    this@MediaActivity.playWhenPrepared = false
                    this@MediaActivity.playerState = STATE_PREPARED
                    it.seekTo(0)
                    stopTimer()
                    resetCurrentTime()
                    updatePlayButton()
                }
                setOnErrorListener { _, _, _ ->


                    this@MediaActivity.playWhenPrepared = false
                    this@MediaActivity.playerState = STATE_DEFAULT
                    stopTimer()
                    resetCurrentTime()
                    updatePlayButton()
                    true
                }

                prepareAsync()
            }

        } catch (e: Exception) {

            resetCurrentTime()
        }
    }

    private fun playbackControl() {
        when {
            playerState == STATE_PLAYING || playWhenPrepared -> pausePlayer()
            playerState == STATE_PREPARED || playerState == STATE_PAUSED -> startPlayer()
            playerState == STATE_DEFAULT -> {
                playWhenPrepared = true
                updatePlayButton(isPendingStart = true)
                preparePlayer()
            }
        }
    }

    private fun startPlayer() {
        if (previewUrl.isNullOrEmpty()) {
            playWhenPrepared = true
            updatePlayButton(isPendingStart = true)
            loadPreviewUrl()
            return
        }

        if (playerState == STATE_DEFAULT) {
            playWhenPrepared = true
            updatePlayButton(isPendingStart = true)
            preparePlayer()
            return
        }

        mediaPlayer?.start()

        playWhenPrepared = false
        playerState = STATE_PLAYING
        updatePlayButton()
        startTimer()
    }

    private fun pausePlayer() {
        if (playerState == STATE_PLAYING) {
            mediaPlayer?.pause()
            playerState = STATE_PAUSED
        }

        playWhenPrepared = false
        stopTimer()
        updatePlayButton()
    }

    private fun loadPreviewUrl() {
        if (previewUrlCall != null) return

        previewUrlCall = RetrofitClient.api.lookup(track.trackId)
        previewUrlCall?.enqueue(object : Callback<SearchResponse> {
            override fun onResponse(
                call: Call<SearchResponse>,
                response: Response<SearchResponse>
            ) {
                previewUrlCall = null
                if (call.isCanceled) return

                previewUrl = response.body()
                    ?.results
                    ?.firstOrNull()
                    ?.previewUrl

                if (playWhenPrepared && !previewUrl.isNullOrEmpty()) {
                    preparePlayer()
                } else if (previewUrl.isNullOrEmpty()) {
                    playWhenPrepared = false
                    updatePlayButton()
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                previewUrlCall = null
                if (call.isCanceled) return

                playWhenPrepared = false
                updatePlayButton()
            }
        })
    }

    private fun startTimer() {
        handler.removeCallbacks(updateRunnable)
        handler.post(updateRunnable)
    }

    private fun stopTimer() {
        handler.removeCallbacks(updateRunnable)
    }

    private fun updatePlayButton(isPendingStart: Boolean = false) {
        val buttonBackground = if (mediaPlayer?.isPlaying == true  || isPendingStart) {
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

    private fun resetCurrentTime() {
        currentTimeText.text = formatTime(0L)
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
        previewUrlCall?.cancel()

        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onPause() {
        super.onPause()
        if (mediaPlayer?.isPlaying == true|| playWhenPrepared) {
            pausePlayer()
        }
    }
}
