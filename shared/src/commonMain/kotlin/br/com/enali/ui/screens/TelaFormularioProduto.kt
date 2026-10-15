
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
import br.com.enali.model.Produto
import br.com.enali.ui.components.CampoTextoCustomizado
import br.com.enali.ui.theme.CorBranco
import br.com.enali.ui.theme.CorMarromEscuro
import kotlin.random.Random

@Composable
fun TelaFormularioProduto(
    aoSalvar: (Produto) -> Unit
) {
    var nomeProduto by remember { mutableStateOf("") }
    var descricaoProduto by remember { mutableStateOf("") }
    var precoProduto by remember { mutableStateOf("") }
    var descontoProduto by remember { mutableStateOf("0") }
    var estaDisponivel by remember { mutableStateOf(true) }

    var erroNome by remember { mutableStateOf<String?>(null) }
    var erroDescricao by remember { mutableStateOf<String?>(null) }
    var erroPreco by remember { mutableStateOf<String?>(null) }
    var erroDesconto by remember { mutableStateOf<String?>(null) }
    var mensagemSucesso by remember { mutableStateOf<String?>(null) }

    val limparCampos = {
        nomeProduto = ""
        descricaoProduto = ""
        precoProduto = ""
        descontoProduto = "0"
        estaDisponivel = true
        erroNome = null
        erroDescricao = null
        erroPreco = null
        erroDesconto = null
    }

    val gravarProduto = {
        val precoValor = precoProduto.replace(",", ".").toDoubleOrNull()
        val descontoValor = descontoProduto.replace(",", ".").toDoubleOrNull()

        erroNome = when {
            nomeProduto.isBlank() -> "O nome é obrigatório"
            nomeProduto.length > 120 -> "O nome deve ter no máximo 120 caracteres"
            else -> null
        }

        erroDescricao = when {
            descricaoProduto.isBlank() -> "A descrição é obrigatória"
            descricaoProduto.length > 500 -> "A descrição deve ter no máximo 500 caracteres"
            else -> null
        }

        erroPreco = if (
            precoValor == null || !precoValor.isFinite() || precoValor <= 0
        ) "Informe um preço válido maior que zero" else null

        erroDesconto = if (
            descontoValor == null || !descontoValor.isFinite() ||
            descontoValor < 0 || descontoValor > 100
        ) "O desconto deve estar entre 0 e 100" else null

        if (
            erroNome == null &&
            erroDescricao == null &&
            erroPreco == null &&
            erroDesconto == null &&
            precoValor != null &&
            descontoValor != null
        ) {
            val novoProduto = Produto(
                id = Random.nextLong(1000L, 999999L),
                nome = nomeProduto.trim(),
                descricao = descricaoProduto.trim(),
                preco = precoValor,
                disponivel = estaDisponivel,
                desconto = descontoValor
            )

            aoSalvar(novoProduto)
            mensagemSucesso = "Produto cadastrado com sucesso!"
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
                text = "Formulário de Cadastro de Produto",
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
                value = nomeProduto,
                onValueChange = {
                    nomeProduto = it
                    erroNome = null
                },
                label = "Nome do Produto",
                placeholder = "Caneca personalizada",
                isError = erroNome != null
            )

            erroNome?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            CampoTextoCustomizado(
                value = descricaoProduto,
                onValueChange = {
                    descricaoProduto = it
                    erroDescricao = null
                },
                label = "Descrição",
                placeholder = "Descrição do produto",
                isError = erroDescricao != null
            )

            erroDescricao?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            CampoTextoCustomizado(
                value = precoProduto,
                onValueChange = {
                    precoProduto = it
                    erroPreco = null
                },
                label = "Preço",
                placeholder = "35,90",
                isError = erroPreco != null
            )

            erroPreco?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            CampoTextoCustomizado(
                value = descontoProduto,
                onValueChange = {
                    descontoProduto = it
                    erroDesconto = null
                },
                label = "Desconto (%)",
                placeholder = "0",
                isError = erroDesconto != null
            )

            erroDesconto?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Disponível?")
                Switch(
                    checked = estaDisponivel,
                    onCheckedChange = { estaDisponivel = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = gravarProduto,
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
