package com.example.playlistmaker

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.google.android.material.appbar.MaterialToolbar

class SearchActivity : AppCompatActivity() {

    private var searchText: String = ""

    companion object {
        const val SEARCH_TEXT_KEY = "SEARCH_TEXT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        val input = findViewById<EditText>(R.id.search_input)
        val clearButton = findViewById<ImageView>(R.id.clear_button)

        toolbar.setNavigationOnClickListener { finish() }


        clearButton.visibility = View.GONE


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
        }


        clearButton.setOnClickListener {
            input.text.clear()
            hideKeyboard(input)
            clearButton.visibility = View.GONE
        }


        input.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                hideKeyboard(input)
                true
            } else false
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT_KEY, searchText)
    }


    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}