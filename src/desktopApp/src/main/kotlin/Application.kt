import androidx.compose.material.Text
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication

fun main() {
    singleWindowApplication(
        state = WindowState(),
        title = "Gaided"
    ) {
        Text(
            text = "Hello, World!",
        )
    }
}