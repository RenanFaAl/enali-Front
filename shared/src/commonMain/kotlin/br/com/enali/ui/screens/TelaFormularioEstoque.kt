
package br.com.enali.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.enali.model.Estoque
import br.com.enali.model.Produto
import br.com.enali.ui.components.CampoTextoCustomizado
import br.com.enali.ui.theme.CorBegeMedio
import br.com.enali.ui.theme.CorBranco
import br.com.enali.ui.theme.CorFundoClaro
import br.com.enali.ui.theme.CorMarromEscuro
import kotlin.random.Random

@Composable
fun TelaFormularioEstoque(
    listaProdutos: List<Produto>,
    aoSalvar: (Estoque) -> Unit,
    listaEstoques: List<Estoque> = emptyList()
) {
    var produtoSelecionadoId by remember {
        mutableStateOf<Long?>(null)
    }
    var menuProdutosAberto by remember {
        mutableStateOf(false)
    }
    var quantidadeDisponivel by remember {
        mutableStateOf("")
    }
    var quantidadeReservada by remember {
        mutableStateOf("0")
    }
    var estoqueMinimo by remember {
        mutableStateOf("0")
    }
    var localizacao by remember {
        mutableStateOf("")
    }
    var estaAtivo by remember {
        mutableStateOf(true)
    }
    var pesquisa by remember {
        mutableStateOf("")
    }

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
        menuProdutosAberto = false
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
        ) {
            "Informe uma quantidade válida"
        } else null

        erroReservada = if (
            reservadaValor == null || reservadaValor < 0
        ) {
            "A quantidade reservada não pode ser negativa"
        } else null

        erroMinimo = if (
            minimoValor == null || minimoValor < 0
        ) {
            "O estoque mínimo não pode ser negativo"
        } else null

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
            aoSalvar(
                Estoque(
                    id = Random.nextLong(1000L, 999999L),
                    produtoId = produtoSelecionado.id,
                    produtoNome = produtoSelecionado.nome,
                    quantidadeDisponivel = disponivelValor,
                    quantidadeReservada = reservadaValor,
                    estoqueMinimo = minimoValor,
                    localizacao = localizacao.trim(),
                    ativo = estaAtivo
                )
            )

            mensagemSucesso = "Estoque cadastrado com sucesso!"
            limparCampos()
        } else {
            mensagemSucesso = null
        }
    }

    val estoquesVisiveis = listaEstoques.filter {
        it.produtoNome.contains(pesquisa, ignoreCase = true) ||
            it.localizacao.contains(pesquisa, ignoreCase = true)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(CorFundoClaro)
    ) {
        val telaLarga = maxWidth >= 880.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                "Estoque",
                color = CorMarromEscuro,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Controle as quantidades e a localização dos produtos.",
                color = Color(0xFF71665C),
                style = MaterialTheme.typography.bodyMedium
            )

            mensagemSucesso?.let {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        it,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            val formulario: @Composable () -> Unit = {
                PainelEstoque(
                    titulo = "Cadastro de estoque",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Selecione um produto e informe os dados do estoque.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF71665C)
                    )

                    Text(
                        "Produto",
                        color = CorMarromEscuro,
                        style = MaterialTheme.typography.labelLarge
                    )

                    Box {
                        OutlinedButton(
                            onClick = {
                                menuProdutosAberto = true
                            },
                            enabled = listaProdutos.isNotEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            border = BorderStroke(1.dp, CorBegeMedio),
                            shape = RoundedCornerShape(9.dp)
                        ) {
                            Text(
                                produtoSelecionado?.nome
                                    ?: "Selecionar produto",
                                color = CorMarromEscuro
                            )
                        }

                        DropdownMenu(
                            expanded = menuProdutosAberto,
                            onDismissRequest = {
                                menuProdutosAberto = false
                            }
                        ) {
                            listaProdutos.forEach { produto ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "${produto.nome} (ID: ${produto.id})"
                                        )
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
                            "Cadastre um produto antes de cadastrar o estoque.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    erroProduto?.let {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            CampoTextoCustomizado(
                                value = quantidadeDisponivel,
                                onValueChange = {
                                    quantidadeDisponivel = it
                                    erroDisponivel = null
                                },
                                label = "Qtd. disponível",
                                placeholder = "50",
                                isError = erroDisponivel != null
                            )

                            erroDisponivel?.let {
                                Text(
                                    it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            CampoTextoCustomizado(
                                value = quantidadeReservada,
                                onValueChange = {
                                    quantidadeReservada = it
                                    erroReservada = null
                                },
                                label = "Qtd. reservada",
                                placeholder = "5",
                                isError = erroReservada != null
                            )

                            erroReservada?.let {
                                Text(
                                    it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    CampoTextoCustomizado(
                        value = estoqueMinimo,
                        onValueChange = {
                            estoqueMinimo = it
                            erroMinimo = null
                        },
                        label = "Estoque mínimo",
                        placeholder = "10",
                        isError = erroMinimo != null
                    )

                    erroMinimo?.let {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error
                        )
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
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Surface(
                        color = CorFundoClaro,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Estoque ativo",
                                    fontWeight = FontWeight.Medium
                                )

                                Text(
                                    if (estaAtivo) {
                                        "Registro ativo"
                                    } else {
                                        "Registro inativo"
                                    },
                                    color = Color(0xFF71665C),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Switch(
                                checked = estaAtivo,
                                onCheckedChange = {
                                    estaAtivo = it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = CorMarromEscuro
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = gravarEstoque,
                            enabled = listaProdutos.isNotEmpty(),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CorMarromEscuro
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Gravar", color = CorBranco)
                        }

                        OutlinedButton(
                            onClick = {
                                limparCampos()
                                mensagemSucesso = null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            border = BorderStroke(
                                1.dp,
                                CorMarromEscuro
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                "Limpar campos",
                                color = CorMarromEscuro
                            )
                        }
                    }
                }
            }

            val listagem: @Composable () -> Unit = {
                PainelEstoque(
                    titulo = "Registros de estoque (${listaEstoques.size})",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = pesquisa,
                        onValueChange = {
                            pesquisa = it
                        },
                        label = {
                            Text("Buscar produto ou localização")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CorMarromEscuro,
                            focusedLabelColor = CorMarromEscuro
                        )
                    )

                    if (estoquesVisiveis.isEmpty()) {
                        Text(
                            text = if (listaEstoques.isEmpty()) {
                                "Nenhum estoque cadastrado ainda."
                            } else {
                                "Nenhum registro encontrado."
                            },
                            color = Color(0xFF71665C),
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .heightIn(max = 480.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            estoquesVisiveis.forEach { estoque ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    verticalArrangement =
                                        Arrangement.spacedBy(5.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement =
                                            Arrangement.SpaceBetween,
                                        verticalAlignment =
                                            Alignment.CenterVertically
                                    ) {
                                        Text(
                                            estoque.produtoNome,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.weight(1f)
                                        )

                                        val abaixoDoMinimo =
                                            estoque.quantidadeDisponivel <
                                                estoque.estoqueMinimo

                                        Surface(
                                            color = if (abaixoDoMinimo) {
                                                Color(0xFFFFE8CC)
                                            } else {
                                                Color(0xFFE8F1E3)
                                            },
                                            shape = RoundedCornerShape(20.dp)
                                        ) {
                                            Text(
                                                if (abaixoDoMinimo) {
                                                    "Estoque baixo"
                                                } else {
                                                    "Em estoque"
                                                },
                                                modifier = Modifier.padding(
                                                    horizontal = 10.dp,
                                                    vertical = 5.dp
                                                ),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = CorMarromEscuro
                                            )
                                        }
                                    }

                                    Text(
                                        text = "Disponível: ${estoque.quantidadeDisponivel}  |  Reservada: ${estoque.quantidadeReservada}  |  Mínimo: ${estoque.estoqueMinimo}",
                                        color = Color(0xFF71665C),
                                        style = MaterialTheme.typography.bodySmall
                                    )

                                    Text(
                                        estoque.localizacao,
                                        color = Color(0xFF71665C),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                HorizontalDivider(
                                    color = CorBegeMedio.copy(alpha = 0.45f)
                                )
                            }
                        }
                    }
                }
            }

            if (telaLarga) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(Modifier.weight(1f)) {
                        formulario()
                    }

                    Box(Modifier.weight(1f)) {
                        listagem()
                    }
                }
            } else {
                formulario()
                listagem()
            }
        }
    }
}

@Composable
private fun PainelEstoque(
    titulo: String,
    modifier: Modifier = Modifier,
    conteudo: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        color = CorBranco,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            CorBegeMedio.copy(alpha = 0.55f)
        ),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                titulo,
                color = CorMarromEscuro,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium
            )

            HorizontalDivider(
                color = CorBegeMedio.copy(alpha = 0.55f)
            )

            conteudo()
        }
    }
}
