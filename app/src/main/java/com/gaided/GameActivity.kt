package com.gaided

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.gaided.view.chessboard.ChessBoardView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class GameActivity : AppCompatActivity() {

    private val viewModel by viewModels<GameViewModel>(factoryProducer = { GameViewModel.Factory() })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val boardView = findViewById<ChessBoardView>(R.id.board)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.boardState.collect {
                    boardView.update(it)
                }
            }
        }

        if (savedInstanceState == null) {
            viewModel.start()
        }

        lifecycleScope.launch {
            delay(3000)
            viewModel.move("e2", "e4")
        }

//        lifecycleScope.launch {
//            repeatOnLifecycle(Lifecycle.State.RESUMED) {
//                while (currentCoroutineContext().isActive) {
//                    viewModel.move("", "")
//                    delay(1000)
//                }
//            }
//        }

//        val api = StockfishApi("http://10.0.2.2:8080")
//        val engine = Engine(api)
//
//        lifecycleScope.launch {
//            try {
//                val response = engine.getFenPosition()
//                Log.i("Gaided", response)
//            } catch (e: IOException) {
//                Log.e("Gaided", "", e)
//            }
//        }
    }
}