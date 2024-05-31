import androidx.lifecycle.viewmodel.CreationExtras
import com.gaided.game.GameViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    println("Gaided desktop.")

    val viewModel = GameViewModel.Factory().create(GameViewModel::class, CreationExtras.Empty)

    val mainLoop = MainScope().launch {
        viewModel.board.collectLatest {
            println("board = $it")
        }
    }

    runBlocking {
        mainLoop.join()
    }

//    singleWindowApplication(
//        state = WindowState(
//            width = 800.dp, height = 1024.dp,
//            position = WindowPosition(Alignment.CenterEnd),
//        ),
//        title = "Gaided"
//    ) {
//        MainView()
//    }
}
