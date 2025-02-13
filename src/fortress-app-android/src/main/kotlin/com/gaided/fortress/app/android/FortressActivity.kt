package com.gaided.fortress.app.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel


class FortressActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FortressApp()
        }
    }
}

@Composable
private fun FortressApp() {
    val viewModel: FortressGameViewModel = viewModel(
        FortressGameViewModel::class,
        factory = FortressGameViewModel.Factory()
    )

    val textState by viewModel.text.collectAsStateWithLifecycle("")

    GameScreen(textState)
}

@Composable
private fun GameScreen(text: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray)
    ) {
        Text(
            text = text,
            fontSize = 32.sp,
        )
    }
}

@Preview
@Composable
fun AppPreview() {
    GameScreen("32")
}