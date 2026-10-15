
package br.com.enali.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.enali.model.Pedido

@Composable
fun TelaFormularioPedido(
    aoSalvar: (Pedido) -> Unit
) {
    var numeroPedido by remember { mutableStateOf("") }
    var valorTotal by remember { mutableStateOf("") }
    var dataPedido by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("PENDENTE") }
    var observacoes by remember { mutableStateOf("") }
    var mensagem by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Cadastro de Pedido",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = numeroPedido,
            onValueChange = { numeroPedido = it },
            label = { Text("Número do pedido") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = valorTotal,
            onValueChange = { valorTotal = it },
            label = { Text("Valor total") },
            placeholder = { Text("Ex.: 199.90") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = dataPedido,
            onValueChange = { dataPedido = it },
            label = { Text("Data e hora do pedido") },
            placeholder = { Text("AAAA-MM-DDTHH:mm:ss") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = status,
            onValueChange = { status = it },
            label = { Text("Status") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = observacoes,
            onValueChange = { observacoes = it },
            label = { Text("Observações") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    val valor = valorTotal
                        .trim()
                        .replace(',', '.')
                        .toDoubleOrNull()

                    mensagem = when {
                        numeroPedido.isBlank() ->
                            "Informe o número do pedido."

                        valor == null || !valor.isFinite() || valor <= 0.0 ->
                            "Informe um valor total válido e maior que zero."

                        !Regex(
                            """\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}"""
                        ).matches(dataPedido) ->
                            "Informe a data no formato AAAA-MM-DDTHH:mm:ss."

                        status.isBlank() ->
                            "Informe o status do pedido."

                        observacoes.length > 500 ->
                            "As observações devem ter até 500 caracteres."

                        else -> {
                            aoSalvar(
                                Pedido(
                                    numeroPedido = numeroPedido.trim(),
                                    valorTotal = valor,
                                    dataPedido = dataPedido,
                                    status = status.trim(),
                                    observacoes = observacoes
                                        .trim()
                                        .ifBlank { null }
                                )
                            )
                            "Pedido adicionado à lista."
                        }
                    }
                }
            ) {
                Text("Gravar")
            }

            OutlinedButton(
                onClick = {
                    numeroPedido = ""
                    valorTotal = ""
                    dataPedido = ""
                    status = "PENDENTE"
                    observacoes = ""
                    mensagem = ""
                }
            ) {
                Text("Limpar Campos")
            }
        }

        if (mensagem.isNotBlank()) {
            Text(mensagem)
        }
    }
}
