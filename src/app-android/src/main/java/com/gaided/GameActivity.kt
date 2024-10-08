package com.gaided

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.gaided.logger.Logger
import com.gaided.ui.MainView

internal class GameActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.enabled = true
        enableEdgeToEdge()
        setContent {
            MainView()
        }
    }
}