package com.paymentspeaker.app

import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale
import java.util.regex.Pattern

class PaymentNotificationListener : NotificationListenerService(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
        } else {
            Log.e("PaymentSpeaker", "TTS Failed")
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val packageName = sbn?.packageName ?: return
        val extras = sbn.notification?.extras ?: return

        val title = extras.getString("android.title") ?: ""
        val text = extras.getString("android.text") ?: ""
        val fullText = "$title $text"

        val targetPackages = listOf(
            "com.f1soft.esewa",
            "com.khalti",
            "com.phonepe.app",
            "net.one97.paytm",
            "com.google.android.apps.nbu.paisa.user"
        )

        val isPaymentApp = targetPackages.contains(packageName)
        val hasKeyword = fullText.contains("received", ignoreCase = true) ||
                fullText.contains("credited", ignoreCase = true) ||
                fullText.contains("प्राप्त", ignoreCase = true)

        if (isPaymentApp || hasKeyword) {
            val amount = extractAmount(fullText)
            if (amount != null) {
                speakAmount(amount)
            }
        }
    }

    private fun extractAmount(text: String): String? {
        val pattern = Pattern.compile("(?:Rs\\.?|NPR|₹|INR|रु\\.?)\\s*([0-9,]+(?:\\.[0-9]{1,2})?)", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)
        return if (matcher.find()) {
            matcher.group(1)
        } else {
            val numPattern = Pattern.compile("\\b([0-9,]+(?:\\.[0-9]{1,2})?)\\b")
            val numMatcher = numPattern.matcher(text)
            if (numMatcher.find()) numMatcher.group(1) else null
        }
    }

    private fun speakAmount(amount: String) {
        if (!isTtsReady) return

        val prefs = getSharedPreferences("PaymentSpeakerPrefs", Context.MODE_PRIVATE)
        val lang = prefs.getString("selected_lang", "hi") ?: "hi"

        val (locale, speechText) = when (lang) {
            "ne" -> Pair(Locale("ne", "NP"), "$amount रुपैया प्राप्त भयो")
            "en" -> Pair(Locale.ENGLISH, "Received $amount rupees")
            else -> Pair(Locale("hi", "IN"), "$amount रुपये प्राप्त हुए")
        }

        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts?.language = Locale.ENGLISH
            tts?.speak("Received $amount rupees", TextToSpeech.QUEUE_FLUSH, null, "PaymentTTS")
        } else {
            tts?.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, "PaymentTTS")
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
