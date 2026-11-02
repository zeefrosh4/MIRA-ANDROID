package com.mira.assistant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var miraText: TextView
    private lateinit var tts: TextToSpeech

    private val microphoneRequest = 101
    private val speechRequest = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        createInterface()
    }

    private fun createInterface() {

        val layout = LinearLayout(this)

        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setPadding(30, 40, 30, 40)

        layout.setBackgroundColor(
            android.graphics.Color.rgb(12, 18, 35)
        )

        val title = TextView(this)

        title.text = "MIRA AI"
        title.textSize = 36f
        title.gravity = Gravity.CENTER
        title.setTextColor(android.graphics.Color.CYAN)

        miraText = TextView(this)

        miraText.text = "Hello! I'm MIRA.\nYour personal assistant."
        miraText.textSize = 20f
        miraText.gravity = Gravity.CENTER
        miraText.setTextColor(android.graphics.Color.WHITE)

        val micButton = Button(this)

        micButton.text = "🎤 TALK TO MIRA"

        micButton.setOnClickListener {
            listenToUser()
        }

        val welcome = Button(this)

        welcome.text = "INTRODUCE YOURSELF"

        welcome.setOnClickListener {
            speak("Hello! I'm MIRA. What can I do for you?")
        }

        layout.addView(title)
        layout.addView(miraText)

        layout.addView(micButton)
        layout.addView(welcome)

        setContentView(layout)
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.UK
        }
    }

    private fun speak(message: String) {

        miraText.text = message

        tts.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "MIRA_RESPONSE"
        )
    }

    private fun listenToUser() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            speak("Voice recognition is not available on this phone.")
            return
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                microphoneRequest
            )

            return
        }

        val intent = Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault()
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            "Speak to MIRA"
        )

        startActivityForResult(intent, speechRequest)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == speechRequest &&
            resultCode == Activity.RESULT_OK
        ) {

            val results = data?.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS
            )

            val command = results?.firstOrNull()

            if (!command.isNullOrBlank()) {
                processCommand(command)
            }
        }
    }

    private fun processCommand(command: String) {

        miraText.text = "You: $command"

        val text = command.lowercase(Locale.ROOT)

        when {

            text.contains("hello") ||
            text.contains("hi mira") -> {
                speak("Hello! What can I do for you?")
            }

            text.contains("open whatsapp business") -> {
                openApp("com.whatsapp.w4b", "WhatsApp Business")
            }

            text.contains("open whatsapp") -> {
                openApp("com.whatsapp", "WhatsApp")
            }

            text.contains("open instagram") -> {
                openApp("com.instagram.android", "Instagram")
            }

            text.contains("open facebook") -> {
                openApp("com.facebook.katana", "Facebook")
            }

            text.contains("what time") ||
            text.contains("tell me the time") -> {

                val time = java.text.SimpleDateFormat(
                    "hh:mm a",
                    Locale.getDefault()
                ).format(java.util.Date())

                speak("The time is $time")
            }

            text.startsWith("search for ") ||
            text.startsWith("search the internet for ") -> {

                val query = text
                    .removePrefix("search the internet for ")
                    .removePrefix("search for ")
                    .trim()

                if (query.isNotEmpty()) {
                    speak("Searching the internet for $query")
                    searchInternet(query)
                }
            }

            else -> {
                speak("I'm still learning that command. Please try another one.")
            }
        }
    }

    private fun openApp(
        packageName: String,
        appName: String
    ) {

        val launchIntent =
            packageManager.getLaunchIntentForPackage(packageName)

        if (launchIntent != null) {

            speak("Opening $appName")

            startActivity(launchIntent)

        } else {

            speak("$appName is not installed on your phone.")
        }
    }

    private fun searchInternet(query: String) {

        val url = "https://www.google.com/search?q=" +
                Uri.encode(query)

        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(url)
        )

        startActivity(intent)
    }

    override fun onDestroy() {

        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }

        super.onDestroy()
    }
}
