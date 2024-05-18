import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.gaided.game.GameViewModel
import com.gaided.game.ui.MainView

fun main() {
    singleWindowApplication(
        state = WindowState(),
        title = "Gaided"
    ) {
        MainView(getGameViewModel())
    }
}

private val viewModelStore = ViewModelStore()

private val viewModelProvider = ViewModelProvider.create(
    viewModelStore,
    GameViewModel.Factory()
)

private fun getGameViewModel(): GameViewModel {
    return viewModelProvider[GameViewModel::class]
}