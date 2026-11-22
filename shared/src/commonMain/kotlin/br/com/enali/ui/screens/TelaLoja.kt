
package br.com.enali.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.enali.network.EnaliApiClient
import br.com.enali.network.dto.ProdutoResponse
import br.com.enali.ui.components.toComposeImageBitmap
import br.com.enali.ui.theme.CorBegeMedio
import br.com.enali.ui.theme.CorBranco
import br.com.enali.ui.theme.CorFundoClaro
import br.com.enali.ui.theme.CorMarromEscuro
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

data class ItemCarrinhoEnali(
    val produto: ProdutoResponse,
    val quantidade: Int
)

private fun valorFinalLoja(produto: ProdutoResponse): Double {
    return produto.preco * (1 - produto.desconto.coerceIn(0.0, 100.0) / 100)
}

private fun formatarMoedaLoja(valor: Double): String {
    if (!valor.isFinite() || valor < 0) return "R$ 0,00"
    val centavos = (valor * 100).roundToLong()
    return "R$ ${centavos / 100},${(centavos % 100).toString().padStart(2, '0')}"
}

@Composable
private fun ImagemDaLoja(
    apiClient: EnaliApiClient,
    produto: ProdutoResponse,
    modifier: Modifier = Modifier
) {
    var imagem by remember(produto.id) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(produto.id) {
        apiClient.listarImagensProduto(produto.id).onSuccess { imagens ->
            val primeira = imagens.minByOrNull { it.ordem }
            if (primeira != null) {
                apiClient.buscarImagemProdutoBytes(produto.id, primeira.id)
                    .onSuccess { bytes ->
                        imagem = bytes.toComposeImageBitmap()
                    }
            }
        }
    }

    Box(
        modifier = modifier.background(CorFundoClaro),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = imagem

        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Imagem de ${produto.nome}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                Icons.Default.ShoppingBag,
                contentDescription = null,
                tint = CorBegeMedio,
                modifier = Modifier.size(52.dp)
            )
        }
    }
}

@Composable
fun TelaVitrineEnali(
    apiClient: EnaliApiClient,
    quantidadeCarrinho: Int,
    aoAdicionarAoCarrinho: (ProdutoResponse) -> Unit,
    aoAbrirCarrinho: () -> Unit
) {
    var produtos by remember { mutableStateOf<List<ProdutoResponse>>(emptyList()) }
    var busca by remember { mutableStateOf("") }
    var carregando by remember { mutableStateOf(true) }
    var erro by remember { mutableStateOf<String?>(null) }
    var aviso by remember { mutableStateOf<String?>(null) }
    var atualizacao by remember { mutableIntStateOf(0) }

    LaunchedEffect(atualizacao) {
        carregando = true
        erro = null

        apiClient.listarProdutos()
            .onSuccess { produtos = it }
            .onFailure {
                erro = it.message ?: "Não foi possível carregar os produtos."
            }

        carregando = false
    }

    val disponiveis = produtos.filter { produto ->
        produto.disponivel && (
            busca.isBlank() ||
                produto.nome.contains(busca.trim(), ignoreCase = true) ||
                produto.descricao.contains(busca.trim(), ignoreCase = true)
        )
    }

    val lista = rememberLazyListState()
    val escopo = rememberCoroutineScope()

    LaunchedEffect(busca, disponiveis.size) {
        lista.scrollToItem(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CorFundoClaro)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Loja Enali",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = CorMarromEscuro
                )

                Text(
                    "Confira os produtos disponíveis",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF71665C)
                )
            }

            OutlinedButton(
                onClick = aoAbrirCarrinho,
                border = BorderStroke(1.dp, CorMarromEscuro),
                shape = RoundedCornerShape(9.dp)
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = CorMarromEscuro
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    "Carrinho ($quantidadeCarrinho)",
                    color = CorMarromEscuro
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = busca,
                onValueChange = { busca = it },
                placeholder = { Text("Buscar produto") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CorMarromEscuro,
                    focusedLabelColor = CorMarromEscuro
                )
            )

            OutlinedButton(
                onClick = { atualizacao++ },
                enabled = !carregando,
                border = BorderStroke(1.dp, CorBegeMedio),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Atualizar", color = CorMarromEscuro)
            }
        }

        if (carregando) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = CorMarromEscuro
            )
        }

        erro?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error
            )
        }

        aviso?.let {
            Text(it, color = CorMarromEscuro)
        }

        if (!carregando && erro == null && disponiveis.isEmpty()) {
            Text(
                if (busca.isBlank()) {
                    "Nenhum produto disponível no momento."
                } else {
                    "Nenhum produto encontrado."
                },
                color = Color(0xFF71665C)
            )
        }

        if (disponiveis.isNotEmpty()) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth()
            ) {
                val quantidadeVisivel = when {
                    maxWidth >= 1080.dp -> 3
                    maxWidth >= 690.dp -> 2
                    else -> 1
                }

                val larguraLista = maxWidth - 88.dp
                val larguraCard = (
                    larguraLista - 14.dp * (quantidadeVisivel - 1)
                ) / quantidadeVisivel

                val ultimoIndice =
                    (disponiveis.size - quantidadeVisivel).coerceAtLeast(0)

                val indiceAtual =
                    lista.firstVisibleItemIndex.coerceAtMost(ultimoIndice)

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Produtos (${disponiveis.size})",
                            color = CorMarromEscuro,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            "${indiceAtual + 1} de ${ultimoIndice + 1}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF71665C)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                escopo.launch {
                                    lista.animateScrollToItem(
                                        (indiceAtual - 1).coerceAtLeast(0)
                                    )
                                }
                            },
                            enabled = indiceAtual > 0
                        ) {
                            Icon(
                                Icons.Default.ChevronLeft,
                                contentDescription = "Produto anterior"
                            )
                        }

                        LazyRow(
                            state = lista,
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(
                                disponiveis,
                                key = { it.id }
                            ) { produto ->
                                Surface(
                                    modifier = Modifier.width(larguraCard),
                                    color = CorBranco,
                                    shape = RoundedCornerShape(11.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        CorBegeMedio.copy(alpha = 0.65f)
                                    )
                                ) {
                                    Column(
                                        verticalArrangement =
                                            Arrangement.spacedBy(10.dp)
                                    ) {
                                        ImagemDaLoja(
                                            apiClient = apiClient,
                                            produto = produto,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(155.dp)
                                        )

                                        Column(
                                            modifier = Modifier.padding(
                                                start = 14.dp,
                                                end = 14.dp,
                                                bottom = 14.dp
                                            ),
                                            verticalArrangement =
                                                Arrangement.spacedBy(9.dp)
                                        ) {
                                            Text(
                                                produto.nome,
                                                fontWeight = FontWeight.SemiBold,
                                                style = MaterialTheme.typography.titleMedium,
                                                maxLines = 2,
                                                minLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Text(
                                                produto.descricao,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF71665C),
                                                maxLines = 2,
                                                minLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            if (produto.desconto > 0) {
                                                Text(
                                                    formatarMoedaLoja(produto.preco),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFF71665C),
                                                    textDecoration = TextDecoration.LineThrough
                                                )
                                            }

                                            Text(
                                                formatarMoedaLoja(
                                                    valorFinalLoja(produto)
                                                ),
                                                color = CorMarromEscuro,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleLarge
                                            )

                                            Button(
                                                onClick = {
                                                    aoAdicionarAoCarrinho(produto)
                                                    aviso =
                                                        "${produto.nome} adicionado ao carrinho."
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = CorMarromEscuro
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    "Adicionar ao carrinho",
                                                    color = CorBranco
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        IconButton(
                            onClick = {
                                escopo.launch {
                                    lista.animateScrollToItem(
                                        (indiceAtual + 1).coerceAtMost(ultimoIndice)
                                    )
                                }
                            },
                            enabled = indiceAtual < ultimoIndice
                        ) {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "Próximo produto"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TelaCarrinhoEnali(
    apiClient: EnaliApiClient,
    itens: List<ItemCarrinhoEnali>,
    aoAlterarQuantidade: (Long, Int) -> Unit,
    aoRemover: (Long) -> Unit,
    aoVoltarVitrine: () -> Unit
) {
    val total = itens.sumOf {
        valorFinalLoja(it.produto) * it.quantidade
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CorFundoClaro)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Carrinho de compras",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = CorMarromEscuro
        )

        OutlinedButton(
            onClick = aoVoltarVitrine,
            border = BorderStroke(1.dp, CorMarromEscuro)
        ) {
            Text(
                "Voltar para a loja",
                color = CorMarromEscuro
            )
        }

        if (itens.isEmpty()) {
            Text(
                "Seu carrinho está vazio.",
                color = Color(0xFF71665C)
            )
        } else {
            itens.forEach { item ->
                Surface(
                    color = CorBranco,
                    border = BorderStroke(
                        1.dp,
                        CorBegeMedio.copy(alpha = 0.65f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        ImagemDaLoja(
                            apiClient = apiClient,
                            produto = item.produto,
                            modifier = Modifier.size(90.dp)
                        )

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                item.produto.nome,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                formatarMoedaLoja(
                                    valorFinalLoja(item.produto)
                                ),
                                color = CorMarromEscuro
                            )

                            TextButton(
                                onClick = {
                                    aoRemover(item.produto.id)
                                }
                            ) {
                                Text(
                                    "Remover",
                                    color = CorMarromEscuro
                                )
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        aoAlterarQuantidade(
                                            item.produto.id,
                                            item.quantidade - 1
                                        )
                                    }
                                ) {
                                    Text("−", color = CorMarromEscuro)
                                }

                                Text(item.quantidade.toString())

                                TextButton(
                                    onClick = {
                                        aoAlterarQuantidade(
                                            item.produto.id,
                                            item.quantidade + 1
                                        )
                                    }
                                ) {
                                    Text("+", color = CorMarromEscuro)
                                }
                            }

                            Text(
                                formatarMoedaLoja(
                                    valorFinalLoja(item.produto) *
                                        item.quantidade
                                ),
                                fontWeight = FontWeight.SemiBold,
                                color = CorMarromEscuro
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = CorBegeMedio)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Total",
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    formatarMoedaLoja(total),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = CorMarromEscuro
                )
            }

            Text(
                "A finalização dos pedidos será conectada à API depois.",
                color = Color(0xFF71665C),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
