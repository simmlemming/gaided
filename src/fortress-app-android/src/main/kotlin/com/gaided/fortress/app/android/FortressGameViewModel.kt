package com.gaided.fortress.app.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.gaided.fortress.game.FortressGame
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlin.random.Random
import kotlin.reflect.KClass

internal class FortressGameViewModel(private val game: FortressGame) : ViewModel() {

    val text = flow {
        val random = Random(System.currentTimeMillis())

        while (currentCoroutineContext().isActive) {
            emit(random.nextInt(100).toString())
            delay(1000)
        }
    }


    internal class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            val game = FortressGame()
            return FortressGameViewModel(game) as T
        }
    }
}