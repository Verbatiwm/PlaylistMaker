package com.example.playlistmaker


import android.content.Intent
import android.os.Bundle
import com.google.android.material.button.MaterialButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar


class MainActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)

        val search = findViewById<MaterialButton>(R.id.search_button)
        val media = findViewById<MaterialButton>(R.id.media_button)
        val settings = findViewById<MaterialButton>(R.id.settings_button)




        search.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        media.setOnClickListener {
            val intent = Intent(this, MediaActivity::class.java)
            startActivity(intent)
        }
        settings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }


    }

}
