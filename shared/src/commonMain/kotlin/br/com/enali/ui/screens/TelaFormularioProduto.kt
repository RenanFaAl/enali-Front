
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
import br.com.enali.model.Produto
import br.com.enali.ui.components.CampoTextoCustomizado
import br.com.enali.ui.theme.CorBegeMedio
import br.com.enali.ui.theme.CorBranco
import br.com.enali.ui.theme.CorFundoClaro
import br.com.enali.ui.theme.CorMarromEscuro
import kotlin.math.roundToLong
import kotlin.random.Random

private fun formatarPrecoProduto(valor: Double): String {
    if (!valor.isFinite()) return "R$ 0,00"

    val centavos = (valor * 100).roundToLong()
    val reais = centavos / 100
    val resto = (centavos % 100).toString().padStart(2, '0')

    return "R$ $reais,$resto"
}

@Composable
fun TelaFormularioProduto(
    aoSalvar: (Produto) -> Unit,
    listaProdutos: List<Produto> = emptyList()
) {
    var nomeProduto by remember { mutableStateOf("") }
    var descricaoProduto by remember { mutableStateOf("") }
    var precoProduto by remember { mutableStateOf("") }
    var descontoProduto by remember { mutableStateOf("0") }
    var estaDisponivel by remember { mutableStateOf(true) }
    var pesquisa by remember { mutableStateOf("") }

    var erroNome by remember { mutableStateOf<String?>(null) }
    var erroDescricao by remember { mutableStateOf<String?>(null) }
    var erroPreco by remember { mutableStateOf<String?>(null) }
    var erroDesconto by remember { mutableStateOf<String?>(null) }
    var mensagemSucesso by remember { mutableStateOf<String?>(null) }

    val precoDigitado = precoProduto.replace(",", ".").toDoubleOrNull()
    val descontoDigitado = descontoProduto.replace(",", ".").toDoubleOrNull()

    val precoResumo = if (
        precoDigitado != null &&
        precoDigitado.isFinite() &&
        precoDigitado > 0
    ) {
        precoDigitado
    } else null

    val descontoResumo = if (
        descontoDigitado != null &&
        descontoDigitado.isFinite() &&
        descontoDigitado in 0.0..100.0
    ) {
        descontoDigitado
    } else null

    val valorDesconto = if (
        precoResumo != null &&
        descontoResumo != null
    ) {
        precoResumo * descontoResumo / 100.0
    } else null

    val precoFinal = if (
        precoResumo != null &&
        valorDesconto != null
    ) {
        precoResumo - valorDesconto
    } else null

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
            nomeProduto.length > 120 ->
                "O nome deve ter no máximo 120 caracteres"
            else -> null
        }

        erroDescricao = when {
            descricaoProduto.isBlank() -> "A descrição é obrigatória"
            descricaoProduto.length > 500 ->
                "A descrição deve ter no máximo 500 caracteres"
            else -> null
        }

        erroPreco = if (
            precoValor == null ||
            !precoValor.isFinite() ||
            precoValor <= 0
        ) {
            "Informe um preço válido maior que zero"
        } else null

        erroDesconto = if (
            descontoValor == null ||
            !descontoValor.isFinite() ||
            descontoValor < 0 ||
            descontoValor > 100
        ) {
            "O desconto deve estar entre 0 e 100"
        } else null

        if (
            erroNome == null &&
            erroDescricao == null &&
            erroPreco == null &&
            erroDesconto == null &&
            precoValor != null &&
            descontoValor != null
        ) {
            aoSalvar(
                Produto(
                    id = Random.nextLong(1000L, 999999L),
                    nome = nomeProduto.trim(),
                    descricao = descricaoProduto.trim(),
                    preco = precoValor,
                    disponivel = estaDisponivel,
                    desconto = descontoValor
                )
            )

            mensagemSucesso = "Produto cadastrado com sucesso!"
            limparCampos()
        } else {
            mensagemSucesso = null
        }
    }

    val produtosVisiveis = listaProdutos.filter {
        it.nome.contains(pesquisa, ignoreCase = true)
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
                text = "Produtos",
                color = CorMarromEscuro,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Cadastre e consulte os produtos do sistema.",
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
                        text = it,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            val formulario: @Composable () -> Unit = {
                PainelProduto(
                    titulo = "Novo produto",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Preencha as informações para cadastrar um produto.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF71665C)
                    )

                    CampoTextoCustomizado(
                        value = nomeProduto,
                        onValueChange = {
                            nomeProduto = it
                            erroNome = null
                        },
                        label = "Nome do produto",
                        placeholder = "Caneca personalizada",
                        isError = erroNome != null
                    )

                    erroNome?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }

                    OutlinedTextField(
                        value = descricaoProduto,
                        onValueChange = {
                            descricaoProduto = it
                            erroDescricao = null
                        },
                        label = { Text("Descrição") },
                        placeholder = { Text("Descrição do produto") },
                        minLines = 3,
                        maxLines = 5,
                        isError = erroDescricao != null,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CorMarromEscuro,
                            focusedLabelColor = CorMarromEscuro,
                            cursorColor = CorMarromEscuro
                        )
                    )

                    erroDescricao?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            CampoTextoCustomizado(
                                value = precoProduto,
                                onValueChange = {
                                    precoProduto = it
                                    erroPreco = null
                                },
                                label = "Preço (R$)",
                                placeholder = "35,90",
                                isError = erroPreco != null
                            )

                            erroPreco?.let {
                                Text(
                                    it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
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
                                Text(
                                    it,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        verticalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Preço original",
                                color = Color(0xFF71665C),
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                precoResumo?.let {
                                    formatarPrecoProduto(it)
                                } ?: "R$ 0,00",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Desconto${descontoResumo?.let { " (${it}%)" } ?: ""}",
                                color = Color(0xFF71665C),
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                valorDesconto?.let {
                                    "- ${formatarPrecoProduto(it)}"
                                } ?: "R$ 0,00",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Preço final",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Text(
                                precoFinal?.let {
                                    formatarPrecoProduto(it)
                                } ?: "R$ 0,00",
                                color = CorMarromEscuro,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                                    "Disponível para venda",
                                    fontWeight = FontWeight.Medium
                                )

                                Text(
                                    if (estaDisponivel) {
                                        "Produto ativo"
                                    } else {
                                        "Produto indisponível"
                                    },
                                    color = Color(0xFF71665C),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Switch(
                                checked = estaDisponivel,
                                onCheckedChange = {
                                    estaDisponivel = it
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
                            onClick = gravarProduto,
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
                PainelProduto(
                    titulo = "Produtos cadastrados (${listaProdutos.size})",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = pesquisa,
                        onValueChange = {
                            pesquisa = it
                        },
                        label = {
                            Text("Buscar produto")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CorMarromEscuro,
                            focusedLabelColor = CorMarromEscuro
                        )
                    )

                    if (produtosVisiveis.isEmpty()) {
                        Text(
                            text = if (listaProdutos.isEmpty()) {
                                "Nenhum produto cadastrado ainda."
                            } else {
                                "Nenhum produto encontrado."
                            },
                            color = Color(0xFF71665C),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .heightIn(max = 480.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            produtosVisiveis.forEach { produto ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalArrangement =
                                        Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            produto.nome,
                                            fontWeight = FontWeight.SemiBold
                                        )

                                        Text(
                                            text = "R$ ${produto.preco.toString().replace('.', ',')}  |  Desconto: ${produto.desconto}%",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF71665C)
                                        )
                                    }

                                    Surface(
                                        color = if (produto.disponivel) {
                                            Color(0xFFE8F1E3)
                                        } else {
                                            CorFundoClaro
                                        },
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Text(
                                            text = if (produto.disponivel) {
                                                "Ativo"
                                            } else {
                                                "Inativo"
                                            },
                                            modifier = Modifier.padding(
                                                horizontal = 10.dp,
                                                vertical = 5.dp
                                            ),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (produto.disponivel) {
                                                Color(0xFF37643A)
                                            } else {
                                                CorMarromEscuro
                                            }
                                        )
                                    }
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
private fun PainelProduto(
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
