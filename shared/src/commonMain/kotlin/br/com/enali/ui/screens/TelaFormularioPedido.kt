package br.com.enali.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.enali.model.Pedido
import br.com.enali.ui.components.CampoTextoCustomizado
import br.com.enali.ui.theme.CorBranco
import br.com.enali.ui.theme.CorMarromEscuro
import kotlin.random.Random

@Composable
fun TelaFormularioPedido(
    aoSalvar: (Pedido) -> Unit
) {
    var numeroPedido by remember { mutableStateOf("") }
    var valorTotalPedido by remember { mutableStateOf("") }
    var dataPedido by remember { mutableStateOf("") }
    var statusPedido by remember { mutableStateOf("PENDENTE") }
    var observacoesPedido by remember { mutableStateOf("") }

    var erroNumero by remember { mutableStateOf<String?>(null) }
    var erroValorTotal by remember { mutableStateOf<String?>(null) }
    var erroData by remember { mutableStateOf<String?>(null) }
    var erroStatus by remember { mutableStateOf<String?>(null) }
    var erroObservacoes by remember { mutableStateOf<String?>(null) }
    var mensagemSucesso by remember { mutableStateOf<String?>(null) }

    val regexDataHora = Regex("""^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}$""")

    val limparCampos = {
        numeroPedido = ""
        valorTotalPedido = ""
        dataPedido = ""
        statusPedido = "PENDENTE"
        observacoesPedido = ""
        erroNumero = null
        erroValorTotal = null
        erroData = null
        erroStatus = null
        erroObservacoes = null
    }

    val gravarPedido = {
        val valorTotal = valorTotalPedido.trim().replace(",", ".").toDoubleOrNull()

        erroNumero = if (numeroPedido.isBlank()) "O número do pedido é obrigatório" else null

        erroValorTotal = when {
            valorTotalPedido.isBlank() -> "O valor total é obrigatório"
            valorTotal == null || !valorTotal.isFinite() -> "Informe um valor total válido"
            valorTotal <= 0.0 -> "O valor total deve ser maior que zero"
            else -> null
        }

        erroData = when {
            dataPedido.isBlank() -> "A data do pedido é obrigatória"
            !regexDataHora.matches(dataPedido.trim()) -> "Use a data no formato AAAA-MM-DDTHH:mm:ss"
            else -> null
        }

        erroStatus = if (statusPedido.isBlank()) "O status é obrigatório" else null

        erroObservacoes = if (observacoesPedido.length > 500) {
            "As observações devem ter no máximo 500 caracteres"
        } else null

        if (
            erroNumero == null &&
            erroValorTotal == null &&
            erroData == null &&
            erroStatus == null &&
            erroObservacoes == null &&
            valorTotal != null
        ) {
            val novoPedido = Pedido(
                id = Random.nextLong(1000L, 999999L),
                numeroPedido = numeroPedido.trim(),
                valorTotal = valorTotal,
                dataPedido = dataPedido.trim(),
                status = statusPedido.trim().uppercase(),
                observacoes = observacoesPedido.trim().ifBlank { null }
            )

            aoSalvar(novoPedido)
            mensagemSucesso = "Pedido cadastrado com sucesso!"
            limparCampos()
        } else {
            mensagemSucesso = null
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(CorBranco)
            .padding(16.dp)
    ) {
        val larguraMaxima = if (maxWidth > 600.dp) 550.dp else maxWidth

        Column(
            modifier = Modifier
                .widthIn(max = larguraMaxima)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .align(Alignment.TopCenter)
        ) {
            Text(
                text = "Formulário de Cadastro de Pedido",
                style = MaterialTheme.typography.titleLarge,
                color = CorMarromEscuro
            )

            Spacer(modifier = Modifier.height(16.dp))

            mensagemSucesso?.let { sucesso ->
                Surface(
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = sucesso,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            CampoTextoCustomizado(
                value = numeroPedido,
                onValueChange = {
                    numeroPedido = it
                    erroNumero = null
                },
                label = "Número do Pedido",
                placeholder = "PED-0001",
                isError = erroNumero != null
            )

            erroNumero?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            CampoTextoCustomizado(
                value = valorTotalPedido,
                onValueChange = {
                    valorTotalPedido = it
                    erroValorTotal = null
                },
                label = "Valor Total",
                placeholder = "199,90",
                isError = erroValorTotal != null
            )

            erroValorTotal?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            CampoTextoCustomizado(
                value = dataPedido,
                onValueChange = {
                    dataPedido = it
                    erroData = null
                },
                label = "Data e Hora do Pedido",
                placeholder = "AAAA-MM-DDTHH:mm:ss",
                isError = erroData != null
            )

            erroData?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            CampoTextoCustomizado(
                value = statusPedido,
                onValueChange = {
                    statusPedido = it
                    erroStatus = null
                },
                label = "Status",
                placeholder = "PENDENTE",
                isError = erroStatus != null
            )

            erroStatus?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            CampoTextoCustomizado(
                value = observacoesPedido,
                onValueChange = {
                    observacoesPedido = it
                    erroObservacoes = null
                },
                label = "Observações (opcional)",
                placeholder = "Entregar no período da tarde",
                isError = erroObservacoes != null
            )

            erroObservacoes?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = gravarPedido,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CorMarromEscuro
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Gravar", color = CorBranco)
                }

                OutlinedButton(
                    onClick = {
                        limparCampos()
                        mensagemSucesso = null
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Limpar Campos")
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
