package com.gaided

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.gaided.game.GameViewModel
import com.gaided.game.ui.MainView

internal class GameActivity2 : AppCompatActivity(){

    private val viewModel by viewModels<GameViewModel>(factoryProducer = { GameViewModel.Factory() })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainView(viewModel)
        }
    }
}