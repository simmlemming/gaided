import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import com.gaided.game.ui.MainView

fun main() {
    singleWindowApplication(
        state = WindowState(
            width = 800.dp, height = 1024.dp,
            position = WindowPosition(Alignment.CenterEnd),
        ),
        title = "Gaided"
    ) {
        MainView()
    }
}
