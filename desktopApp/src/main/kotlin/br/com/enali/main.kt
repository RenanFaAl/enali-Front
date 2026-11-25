package br.com.enali

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import br.com.enali.ui.components.iconeEnali

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Enali",
        icon = iconeEnali(),
    ) {
        App()
    }
}
