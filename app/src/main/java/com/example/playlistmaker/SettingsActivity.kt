package com.example.playlistmaker

import android.annotation.SuppressLint
import android.content.Intent

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.core.net.toUri


class SettingsActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)



        toolbar.setNavigationOnClickListener {
            finish()
        }
        val share = findViewById<View>(R.id.share)

        share.setOnClickListener {


            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message))
            }
            val chooser = Intent.createChooser(intent, getString(R.string.chooser_email))
            startActivity(chooser)

        }

        val support = findViewById<View>(R.id.support)

        support.setOnClickListener {

            val email = getString(R.string.support_email)
            val subject = getString(R.string.support_subject)
            val message = getString(R.string.support_message)

            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:".toUri()
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, message)
            }

            startActivity(Intent.createChooser(intent, getString(R.string.chooser_email)))
        }

        val agreement = findViewById<View>(R.id.agreement)

        agreement.setOnClickListener {

            val url = getString(R.string.agreement_url)

            val intent = Intent(Intent.ACTION_VIEW, url.toUri())


            startActivity(Intent.createChooser(intent, getString(R.string.chooser_browser)))        }

        val switch = findViewById<SwitchMaterial>(R.id.theme_switch)

        val currentNightMode = resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK

        switch.isChecked = currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
        switch.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }


    }
}