package br.com.enali.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay

/** Telas do 2º nível de navegação de um CRUD. */
sealed interface TelaCrud<out T> {
    data object Listagem : TelaCrud<Nothing>
    data class Formulario<T>(val item: T?) : TelaCrud<T> // item null = cadastro novo
}

/**
 * Navigation3, 2º nível: alterna entre Listagem e Formulário de um CRUD
 * com uma pilha própria (Listagem embaixo, Formulário por cima).
 */
@Composable
fun <T> NavegacaoCrud(
    chave: Any,
    listagem: @Composable (abrirFormulario: (item: T?) -> Unit) -> Unit,
    formulario: @Composable (item: T?, voltarParaListagem: () -> Unit) -> Unit
) {
    val pilha = remember(chave) { mutableStateListOf<TelaCrud<T>>(TelaCrud.Listagem) }
    val voltar: () -> Unit = { if (pilha.size > 1) pilha.removeAt(pilha.lastIndex) }

    NavDisplay(
        backStack = pilha,
        onBack = voltar,
        entryProvider = { tela ->
            NavEntry(tela) {
                when (tela) {
                    is TelaCrud.Listagem -> listagem { item -> pilha.add(TelaCrud.Formulario(item)) }
                    is TelaCrud.Formulario -> formulario(tela.item, voltar)
                }
            }
        }
    )
}
