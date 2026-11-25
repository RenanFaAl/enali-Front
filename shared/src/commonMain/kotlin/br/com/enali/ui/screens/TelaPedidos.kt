package br.com.enali.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.enali.network.dto.PedidoResponse
import br.com.enali.ui.components.*
import br.com.enali.ui.theme.CorMarromEscuro
import kotlinx.coroutines.launch

/** Lista de pedidos com cancelamento. Serve para "Meus pedidos" (cliente) e "Pedidos" (admin). */
@Composable
fun TelaPedidos(
    titulo: String,
    subtitulo: String,
    chave: Any,
    mostrarCliente: Boolean,
    listar: suspend () -> Result<List<PedidoResponse>>,
    cancelar: suspend (id: Long) -> Result<PedidoResponse>
) {
    val escopo = rememberCoroutineScope()
    var pedidos by remember(chave) { mutableStateOf<List<PedidoResponse>>(emptyList()) }
    var carregando by remember(chave) { mutableStateOf(true) }
    var erro by remember(chave) { mutableStateOf<String?>(null) }
    var sucesso by remember(chave) { mutableStateOf<String?>(null) }
    var busca by remember(chave) { mutableStateOf("") }

    suspend fun recarregar() {
        carregando = true
        listar()
            .onSuccess { lista -> pedidos = lista.sortedByDescending { it.dataPedido }; erro = null }
            .onFailure { erro = it.message ?: "Não foi possível carregar os pedidos." }
        carregando = false
    }

    LaunchedEffect(chave) { recarregar() }

    val filtrados = pedidos.filter {
        busca.isBlank() ||
            it.numeroPedido.contains(busca.trim(), ignoreCase = true) ||
            it.clienteNome.contains(busca.trim(), ignoreCase = true)
    }

    PaginaGestao(
        titulo = titulo,
        subtitulo = subtitulo,
        acao = { BotaoSecundario("Atualizar", habilitado = !carregando) { escopo.launch { recarregar() } } }
    ) {
        OutlinedTextField(
            value = busca,
            onValueChange = { busca = it },
            label = { Text(if (mostrarCliente) "Buscar por número ou cliente" else "Buscar por número") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CorMarromEscuro,
                focusedLabelColor = CorMarromEscuro
            )
        )
        AvisoTela(erro, erro = true)
        AvisoTela(sucesso, erro = false)
        CarregandoOuVazio(
            carregando,
            filtrados.isEmpty(),
            if (pedidos.isEmpty()) "Nenhum pedido realizado." else "Nenhum pedido encontrado."
        )

        filtrados.forEach { pedido ->
            CartaoItem {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Pedido ${pedido.numeroPedido}", fontWeight = FontWeight.SemiBold, color = CorMarromEscuro)
                        Etiqueta(pedido.status, positiva = !pedido.cancelado)
                    }
                    Text(
                        formatarMoeda(pedido.valorTotal),
                        fontWeight = FontWeight.Bold,
                        color = CorMarromEscuro,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(formatarDataHora(pedido.dataPedido), style = MaterialTheme.typography.bodySmall, color = CorTextoSuave)
                if (mostrarCliente) {
                    Text(
                        "Cliente: ${pedido.clienteNome} (${pedido.clienteEmail})",
                        style = MaterialTheme.typography.bodySmall,
                        color = CorTextoSuave
                    )
                }
                pedido.itens.forEach { item ->
                    Text(
                        "${item.quantidade}x ${item.nome} — ${formatarMoeda(item.subtotal)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                pedido.enderecoEntrega?.let {
                    Text("Entrega: ${it.resumo()}", style = MaterialTheme.typography.bodySmall, color = CorTextoSuave)
                }
                pedido.observacoes?.takeIf { it.isNotBlank() }?.let {
                    Text("Observações: $it", style = MaterialTheme.typography.bodySmall, color = CorTextoSuave)
                }
                if (!pedido.cancelado) {
                    BotaoExcluir(texto = "Cancelar pedido", textoConfirmar = "Confirmar cancelamento") {
                        escopo.launch {
                            cancelar(pedido.id)
                                .onSuccess { sucesso = "Pedido ${pedido.numeroPedido} cancelado."; erro = null; recarregar() }
                                .onFailure { erro = it.message ?: "Não foi possível cancelar o pedido." }
                        }
                    }
                }
            }
        }
    }
}
