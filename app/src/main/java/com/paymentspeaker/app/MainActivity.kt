package com.paymentspeaker.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate()
        setContentView(R.layout.activity_main)

        val btnPermission: Button = findViewById(R.id.btnPermission)
        val radioGroupLang: RadioGroup = findViewById(R.id.radioGroupLang)
        val rbHindi: RadioButton = findViewById(R.id.rbHindi)
        val rbNepali: RadioButton = findViewById(R.id.rbNepali)
        val rbEnglish: RadioButton = findViewById(R.id.rbEnglish)

        val prefs = getSharedPreferences("PaymentSpeakerPrefs", Context.MODE_PRIVATE)

        when (prefs.getString("selected_lang", "hi")) {
            "ne" -> rbNepali.isChecked = true
            "en" -> rbEnglish.isChecked = true
            else -> rbHindi.isChecked = true
        }

        radioGroupLang.setOnCheckedChangeListener { _, checkedId ->
            val langCode = when (checkedId) {
                R.id.rbNepali -> "ne"
                R.id.rbEnglish -> "en"
                else -> "hi"
            }
            prefs.edit().putString("selected_lang", langCode).apply()
            Toast.makeText(this, "Language Saved!", Toast.LENGTH_SHORT).show()
        }

        btnPermission.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }
}
