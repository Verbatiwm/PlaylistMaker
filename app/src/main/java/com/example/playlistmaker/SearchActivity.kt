package com.example.playlistmaker

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
import android.widget.TextView
import android.util.Log
import android.view.inputmethod.EditorInfo
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

    private var searchText: String = ""

    private var lastSearchText = ""

    private lateinit var recyclerView: RecyclerView

    companion object {
        const val SEARCH_TEXT_KEY = "SEARCH_TEXT"
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

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        val input = findViewById<EditText>(R.id.search_input)
        val clearButton = findViewById<ImageView>(R.id.clear_button)
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


        input.doOnTextChanged { text, _, _, _ ->
            searchText = text.toString()

            clearButton.visibility =
                if (text.isNullOrEmpty()) View.GONE else View.VISIBLE

            if (text.isNullOrEmpty()) {
                recyclerView.visibility = View.GONE
                hidePlaceholders()
            }
        }



        clearButton.setOnClickListener {
            input.text.clear()
            hideKeyboard(input)
            clearButton.visibility = View.GONE
            recyclerView.visibility = View.GONE
            hidePlaceholders()
        }


        input.setOnEditorActionListener { _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_DONE) {

                hideKeyboard(input)

                searchTracks(input.text.toString())

                true
            } else {
                false
            }
        }






    }

    private fun searchTracks(text: String) {

        if (text.trim().isEmpty()) return

        lastSearchText = text

        RetrofitClient.api.search(text)
            .enqueue(object : Callback<SearchResponse> {

                override fun onResponse(
                    call: Call<SearchResponse>,
                    response: Response<SearchResponse>
                ) {

                    if (response.code() == 200) {

                        val tracks = response.body()?.results ?: emptyList()

                        if (tracks.isEmpty()) {

                            showEmptyPlaceholder()

                        } else {

                            hidePlaceholders()

                            val mappedTracks = tracks.map {

                                Track(
                                    trackName = it.trackName ?: "Unknown",
                                    artistName = it.artistName ?: "Unknown",
                                    trackTimeMillis = it.trackTimeMillis ?: 0L,
                                    artworkUrl100 = it.artworkUrl100 ?: ""
                                )
                            }

                            recyclerView.adapter = TrackAdapter(mappedTracks)
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

                    showErrorPlaceholder()

                    Log.e("API_ERROR", t.message ?: "Error")
                }
            })
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

}





