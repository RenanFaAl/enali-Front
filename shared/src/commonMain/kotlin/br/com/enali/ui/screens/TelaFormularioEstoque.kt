
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
import br.com.enali.model.Estoque
import br.com.enali.model.Produto
import br.com.enali.ui.components.CampoTextoCustomizado
import br.com.enali.ui.theme.CorBranco
import br.com.enali.ui.theme.CorMarromEscuro
import kotlin.random.Random

@Composable
fun TelaFormularioEstoque(
    listaProdutos: List<Produto>,
    aoSalvar: (Estoque) -> Unit
) {
    var produtoSelecionadoId by remember { mutableStateOf<Long?>(null) }
    var menuProdutosAberto by remember { mutableStateOf(false) }

    var quantidadeDisponivel by remember { mutableStateOf("") }
    var quantidadeReservada by remember { mutableStateOf("0") }
    var estoqueMinimo by remember { mutableStateOf("0") }
    var localizacao by remember { mutableStateOf("") }
    var estaAtivo by remember { mutableStateOf(true) }

    var erroProduto by remember { mutableStateOf<String?>(null) }
    var erroDisponivel by remember { mutableStateOf<String?>(null) }
    var erroReservada by remember { mutableStateOf<String?>(null) }
    var erroMinimo by remember { mutableStateOf<String?>(null) }
    var erroLocalizacao by remember { mutableStateOf<String?>(null) }
    var mensagemSucesso by remember { mutableStateOf<String?>(null) }

    val produtoSelecionado = listaProdutos.find {
        it.id == produtoSelecionadoId
    }

    val limparCampos = {
        produtoSelecionadoId = null
        quantidadeDisponivel = ""
        quantidadeReservada = "0"
        estoqueMinimo = "0"
        localizacao = ""
        estaAtivo = true

        erroProduto = null
        erroDisponivel = null
        erroReservada = null
        erroMinimo = null
        erroLocalizacao = null
    }

    val gravarEstoque = {
        val disponivelValor = quantidadeDisponivel.toIntOrNull()
        val reservadaValor = quantidadeReservada.toIntOrNull()
        val minimoValor = estoqueMinimo.toIntOrNull()

        erroProduto = if (produtoSelecionado == null) {
            "Selecione um produto"
        } else null

        erroDisponivel = if (
            disponivelValor == null || disponivelValor < 0
        ) "Informe uma quantidade válida" else null

        erroReservada = if (
            reservadaValor == null || reservadaValor < 0
        ) "A quantidade reservada não pode ser negativa" else null

        erroMinimo = if (
            minimoValor == null || minimoValor < 0
        ) "O estoque mínimo não pode ser negativo" else null

        erroLocalizacao = when {
            localizacao.isBlank() -> "A localização é obrigatória"
            localizacao.length > 100 -> "Máximo de 100 caracteres"
            else -> null
        }

        if (
            produtoSelecionado != null &&
            disponivelValor != null &&
            reservadaValor != null &&
            minimoValor != null &&
            erroDisponivel == null &&
            erroReservada == null &&
            erroMinimo == null &&
            erroLocalizacao == null
        ) {
            val novoEstoque = Estoque(
                id = Random.nextLong(1000L, 999999L),
                produtoId = produtoSelecionado.id,
                produtoNome = produtoSelecionado.nome,
                quantidadeDisponivel = disponivelValor,
                quantidadeReservada = reservadaValor,
                estoqueMinimo = minimoValor,
                localizacao = localizacao.trim(),
                ativo = estaAtivo
            )

            aoSalvar(novoEstoque)
            mensagemSucesso = "Estoque cadastrado com sucesso!"
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
                text = "Formulário de Cadastro de Estoque",
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

            Text(
                text = "Produto",
                style = MaterialTheme.typography.bodyMedium,
                color = CorMarromEscuro
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box {
                OutlinedButton(
                    onClick = { menuProdutosAberto = true },
                    enabled = listaProdutos.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = produtoSelecionado?.nome
                            ?: "Selecionar produto"
                    )
                }

                DropdownMenu(
                    expanded = menuProdutosAberto,
                    onDismissRequest = { menuProdutosAberto = false }
                ) {
                    listaProdutos.forEach { produto ->
                        DropdownMenuItem(
                            text = {
                                Text("${produto.nome} (ID: ${produto.id})")
                            },
                            onClick = {
                                produtoSelecionadoId = produto.id
                                erroProduto = null
                                menuProdutosAberto = false
                            }
                        )
                    }
                }
            }

            if (listaProdutos.isEmpty()) {
                Text(
                    text = "Cadastre um produto antes de cadastrar o estoque.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            erroProduto?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(8.dp))

            CampoTextoCustomizado(
                value = quantidadeDisponivel,
                onValueChange = {
                    quantidadeDisponivel = it
                    erroDisponivel = null
                },
                label = "Quantidade Disponível",
                placeholder = "50",
                isError = erroDisponivel != null
            )

            erroDisponivel?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            CampoTextoCustomizado(
                value = quantidadeReservada,
                onValueChange = {
                    quantidadeReservada = it
                    erroReservada = null
                },
                label = "Quantidade Reservada",
                placeholder = "5",
                isError = erroReservada != null
            )

            erroReservada?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            CampoTextoCustomizado(
                value = estoqueMinimo,
                onValueChange = {
                    estoqueMinimo = it
                    erroMinimo = null
                },
                label = "Estoque Mínimo",
                placeholder = "10",
                isError = erroMinimo != null
            )

            erroMinimo?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            CampoTextoCustomizado(
                value = localizacao,
                onValueChange = {
                    localizacao = it
                    erroLocalizacao = null
                },
                label = "Localização",
                placeholder = "Prateleira A3",
                isError = erroLocalizacao != null
            )

            erroLocalizacao?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Estoque Ativo?")
                Switch(
                    checked = estaAtivo,
                    onCheckedChange = { estaAtivo = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = gravarEstoque,
                    enabled = listaProdutos.isNotEmpty(),
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
