import androidx.compose.material.Text
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import com.gaided.game.ui.MainView

fun main() {
    singleWindowApplication(
        state = WindowState(),
        title = "Gaided"
    ) {
        MainView()
    }
}