package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.content.res.Configuration
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.os.Handler
import android.os.Looper
import com.example.playlistmaker.network.RetrofitClient
import com.example.playlistmaker.network.SearchResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar

class SearchActivity : AppCompatActivity() {

    private lateinit var placeholderContainer: LinearLayout
    private lateinit var placeholderImage: ImageView
    private lateinit var placeholderMessage: TextView

    private lateinit var placeholderDescription: TextView
    private lateinit var retryButton: Button
    private lateinit var progressBar: ProgressBar



    private var searchText: String = ""

    private var lastSearchText = ""

    private lateinit var recyclerView: RecyclerView

    private lateinit var history: SearchHistory

    private lateinit var historyTitle: TextView
    private lateinit var clearHistoryButton: Button

    private val handler = Handler(Looper.getMainLooper())
    private var searchCall: Call<SearchResponse>? = null
    private var isClickAllowed = true
    private val searchRunnable = Runnable {
        val query = searchText.trim()
        if (query.isNotEmpty()) {
            searchTracks(query)
        }
    }

    companion object {
        const val SEARCH_TEXT_KEY = "SEARCH_TEXT"
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)




        placeholderContainer = findViewById(R.id.placeholder_container)
        placeholderImage = findViewById(R.id.placeholder_image)
        placeholderMessage = findViewById(R.id.placeholder_message)
        placeholderDescription = findViewById(R.id.placeholder_description)
        retryButton = findViewById(R.id.retry_button)
        progressBar = findViewById(R.id.progress_bar)


        historyTitle = findViewById(R.id.historyTitle)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        val input = findViewById<EditText>(R.id.search_input)
        val clearButton = findViewById<ImageView>(R.id.clear_button)

        history = SearchHistory(
            getSharedPreferences("prefs", MODE_PRIVATE)
        )

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        toolbar.setNavigationOnClickListener { finish() }


        clearButton.visibility = View.GONE


        retryButton.setOnClickListener {

            searchTracks(lastSearchText)
        }





        savedInstanceState?.let {
            searchText = it.getString(SEARCH_TEXT_KEY, "")
            input.setText(searchText)
            input.setSelection(searchText.length)
        }


        input.requestFocus()
        input.post {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
        }

        if (searchText.isEmpty()) {
            showHistory()
        }


        input.doOnTextChanged { text, _, _, _ ->
            searchText = text.toString()

            clearButton.visibility =
                if (text.isNullOrEmpty()) View.GONE else View.VISIBLE

            if (text.isNullOrEmpty()) {
                handler.removeCallbacks(searchRunnable)
                searchCall?.cancel()
                hideLoading()
                recyclerView.visibility = View.GONE
                hidePlaceholders()
                showHistory()
            } else {
                historyTitle.visibility = View.GONE
                clearHistoryButton.visibility = View.GONE
                searchDebounce()


            }
        }



        clearButton.setOnClickListener {
            input.text.clear()
            hideKeyboard(input)
            clearButton.visibility = View.GONE
            recyclerView.visibility = View.GONE
            hideLoading()
            hidePlaceholders()
            showHistory()
        }


        input.setOnEditorActionListener { _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard(input)
                true
            } else {
                false
            }
        }

        clearHistoryButton.setOnClickListener {
            history.clearHistory()
            showHistory()
        }

    }

    override fun onResume() {
        super.onResume()
        if (searchText.isEmpty()) {
            showHistory()
        }
    }

    private fun searchTracks(text: String) {

        if (text.trim().isEmpty()) return

        handler.removeCallbacks(searchRunnable)
        searchCall?.cancel()
        showLoading()
        recyclerView.visibility = View.GONE

        lastSearchText = text

        historyTitle.visibility = View.GONE
        clearHistoryButton.visibility = View.GONE

        searchCall = RetrofitClient.api.search(text)
        searchCall?.enqueue(object : Callback<SearchResponse> {

                override fun onResponse(
                    call: Call<SearchResponse>,
                    response: Response<SearchResponse>
                ) {
                    if (call.isCanceled) return

                    hideLoading()

                    if (response.code() == 200) {

                        val tracks = response.body()?.results ?: emptyList()

                        if (tracks.isEmpty()) {

                            showEmptyPlaceholder()

                        } else {

                            hidePlaceholders()

                            val mappedTracks = tracks.map {

                                Track(
                                    trackId = it.trackId ?: 0,
                                    trackName = it.trackName ?: "Unknown",
                                    artistName = it.artistName ?: "Unknown",
                                    trackTimeMillis = it.trackTimeMillis ?: 0L,
                                    artworkUrl100 = it.artworkUrl100 ?: "",

                                    collectionName = it.collectionName,
                                    releaseDate = it.releaseDate,
                                    primaryGenreName = it.primaryGenreName ?: "Unknown",
                                    country = it.country ?: "Unknown",
                                    previewUrl = it.previewUrl
                                )

                            }


                            val adapter = TrackAdapter(mappedTracks) { track ->

                                openTrack(track)
                            }

                            recyclerView.adapter = adapter
                            recyclerView.visibility = View.VISIBLE
                        }

                    } else {

                        showErrorPlaceholder()
                    }
                }

                override fun onFailure(
                    call: Call<SearchResponse>,
                    t: Throwable
                ) {
                    if (call.isCanceled) return

                    hideLoading()

                    showErrorPlaceholder()

                    Log.e("API_ERROR", t.message ?: "Error")
                }
            })
    }


    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    private fun openTrack(track: Track) {
        if (!clickDebounce()) return

        history.addTrack(track)

        val intent = Intent(this, MediaActivity::class.java)
        intent.putExtra("track", track)
        startActivity(intent)
    }



    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT_KEY, searchText)
    }


    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showEmptyPlaceholder() {



        recyclerView.visibility = View.GONE
        hideLoading()
        placeholderContainer.visibility = View.VISIBLE
        val isDarkTheme =
            resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK ==
                    Configuration.UI_MODE_NIGHT_YES

        if (isDarkTheme) {
            placeholderImage.setImageResource(R.drawable.ic_not_found_dm)
        } else {
            placeholderImage.setImageResource(R.drawable.ic_not_found_lm)
        }

        placeholderMessage.text = "Ничего не нашлось"

        placeholderDescription.visibility = View.GONE

        retryButton.visibility = View.GONE
    }



    private fun showErrorPlaceholder() {

        recyclerView.visibility = View.GONE
        hideLoading()

        placeholderContainer.visibility = View.VISIBLE

        val isDarkTheme =
            resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK ==
                    Configuration.UI_MODE_NIGHT_YES

        if (isDarkTheme) {
            placeholderImage.setImageResource(R.drawable.ic_connection_error_dm)
        } else {
            placeholderImage.setImageResource(R.drawable.ic_connection_error_lm)
        }

        placeholderMessage.text = "Проблемы со связью"

        placeholderDescription.visibility = View.VISIBLE

        retryButton.visibility = View.VISIBLE
    }

    private fun hidePlaceholders() {

        placeholderContainer.visibility = View.GONE
    }

    private fun showLoading() {
        hidePlaceholders()
        recyclerView.visibility = View.GONE
        historyTitle.visibility = View.GONE
        clearHistoryButton.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        progressBar.visibility = View.GONE
    }

    private fun showHistory() {
        val historyList = history.getHistory()

        if (historyList.isEmpty()) {
            historyTitle.visibility = View.GONE
            clearHistoryButton.visibility = View.GONE
            recyclerView.visibility = View.GONE
            return
        }

        hidePlaceholders()
        hideLoading()

        historyTitle.visibility = View.VISIBLE
        clearHistoryButton.visibility = View.VISIBLE

        recyclerView.adapter = TrackAdapter(historyList) { track ->
            openTrack(track)
        }

        recyclerView.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        searchCall?.cancel()
    }

}
