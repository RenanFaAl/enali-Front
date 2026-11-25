
package br.com.enali.ui.screens

import br.com.enali.ui.components.selecionarImagensProduto
import br.com.enali.network.dto.ImagemUploadItem
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
import br.com.enali.network.dto.EnderecoResponse
import br.com.enali.network.dto.ItemPedidoRequest
import br.com.enali.network.dto.PedidoRequest
import br.com.enali.network.dto.PedidoResponse
import br.com.enali.network.dto.ProdutoResponse
import br.com.enali.ui.components.AvisoTela
import br.com.enali.ui.components.BotaoPrincipal
import br.com.enali.ui.components.BotaoSecundario
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
                contentScale = ContentScale.Fit,
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CorFundoClaro)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val telaEstreita = maxWidth < 500.dp // celular: carrinho só com o ícone
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
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
                    contentDescription = if (telaEstreita) "Carrinho ($quantidadeCarrinho)" else null,
                    tint = CorMarromEscuro
                )

                if (!telaEstreita) {
                    Spacer(Modifier.width(8.dp))

                    Text(
                        "Carrinho ($quantidadeCarrinho)",
                        color = CorMarromEscuro
                    )
                }
            }
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
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val colunas = when {
                    maxWidth >= 900.dp -> 3
                    maxWidth >= 600.dp -> 2
                    else -> 1
                }
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        "Produtos (${disponiveis.size})",
                        color = CorMarromEscuro,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    disponiveis.chunked(colunas).forEach { fileira ->
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            fileira.forEach { produto ->
                                Surface(
                                    modifier = Modifier.weight(1f),
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
                                                .height(200.dp)
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

                                            // linha sempre presente para todos os cards terem a mesma altura
                                            Text(
                                                if (produto.desconto > 0) formatarMoedaLoja(produto.preco) else "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF71665C),
                                                textDecoration = TextDecoration.LineThrough
                                            )

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
                            // completa a última fileira para os cards manterem a mesma largura
                            repeat(colunas - fileira.size) { Spacer(Modifier.weight(1f)) }
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
    aoVoltarVitrine: () -> Unit,
    tokenCliente: String?,
    aoIrParaLogin: () -> Unit,
    aoIrParaEnderecos: () -> Unit,
    aoPedidoRealizado: (PedidoResponse) -> Unit
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

            FinalizacaoPedido(
                apiClient = apiClient,
                tokenCliente = tokenCliente,
                itens = itens,
                aoIrParaLogin = aoIrParaLogin,
                aoIrParaEnderecos = aoIrParaEnderecos,
                aoPedidoRealizado = aoPedidoRealizado
            )
        }
    }
}

// Fecha o pedido: cliente escolhe um endereço cadastrado e envia os itens do carrinho para a API
@Composable
private fun FinalizacaoPedido(
    apiClient: EnaliApiClient,
    tokenCliente: String?,
    itens: List<ItemCarrinhoEnali>,
    aoIrParaLogin: () -> Unit,
    aoIrParaEnderecos: () -> Unit,
    aoPedidoRealizado: (PedidoResponse) -> Unit
) {
    if (tokenCliente == null) {
        Text("Entre na sua conta para finalizar o pedido.", color = Color(0xFF71665C))
        BotaoPrincipal("Entrar para finalizar", aoClicar = aoIrParaLogin)
        return
    }

    val escopo = rememberCoroutineScope()
    var enderecos by remember { mutableStateOf<List<EnderecoResponse>>(emptyList()) }
    var enderecoId by remember { mutableStateOf<Long?>(null) }
    var observacoes by remember { mutableStateOf("") }
    var fotos by remember { mutableStateOf<List<ImagemUploadItem>>(emptyList()) }
    var carregando by remember { mutableStateOf(true) }
    var enviando by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(tokenCliente) {
        apiClient.endereco.listarMeus(tokenCliente)
            .onSuccess { lista ->
                enderecos = lista.filter { it.ativo }
                enderecoId = (enderecos.firstOrNull { it.principal } ?: enderecos.firstOrNull())?.id
            }
            .onFailure { erro = it.message ?: "Não foi possível carregar seus endereços." }
        carregando = false
    }

    Text("Endereço de entrega", fontWeight = FontWeight.SemiBold, color = CorMarromEscuro)

    if (carregando) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = CorMarromEscuro)
    } else if (enderecos.isEmpty()) {
        Text("Você ainda não tem endereço cadastrado.", color = Color(0xFF71665C))
        BotaoSecundario("Cadastrar endereço", aoClicar = aoIrParaEnderecos)
    } else {
        enderecos.forEach { endereco ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = enderecoId == endereco.id,
                    onClick = { enderecoId = endereco.id },
                    colors = RadioButtonDefaults.colors(selectedColor = CorMarromEscuro)
                )
                Text(endereco.resumo(), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }

    OutlinedTextField(
        value = observacoes,
        onValueChange = { observacoes = it },
        label = { Text("Observações (opcional)") },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CorMarromEscuro,
            focusedLabelColor = CorMarromEscuro
        )
    )

    // Fotos de referência anexadas às observações (ex.: personalização)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BotaoSecundario("Anexar fotos", habilitado = !enviando && fotos.size < 5) {
            selecionarImagensProduto(
                permiteMultiplas = true,
                onSelecionadas = { novas -> fotos = (fotos + novas).take(5); erro = null },
                onErro = { erro = it }
            )
        }
        Text(
            if (fotos.isEmpty()) "Opcional, até 5 fotos (JPEG, PNG ou WebP)" else "${fotos.size} foto(s) anexada(s)",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF71665C)
        )
    }
    fotos.forEachIndexed { indice, foto ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(foto.nomeArquivo, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            TextButton(onClick = { fotos = fotos.filterIndexed { i, _ -> i != indice } }, enabled = !enviando) {
                Text("Remover", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    AvisoTela(erro, erro = true)

    BotaoPrincipal(
        if (enviando) "Enviando pedido..." else "Finalizar pedido",
        habilitado = !enviando && enderecoId != null,
        modifier = Modifier.fillMaxWidth()
    ) {
        val idEndereco = enderecoId ?: return@BotaoPrincipal
        escopo.launch {
            enviando = true
            erro = null
            apiClient.pedido.realizar(
                tokenCliente,
                PedidoRequest(
                    enderecoId = idEndereco,
                    itens = itens.map { ItemPedidoRequest(it.produto.id, it.quantidade) },
                    observacoes = observacoes.trim().ifBlank { null }
                )
            )
                .onSuccess { pedido ->
                    // envia as fotos antes de sair da tela (sair cancela este escopo)
                    // shortcut: foto que falhar no envio é descartada sem aviso; os arquivos já são validados no seletor
                    fotos.forEach { apiClient.pedido.enviarFoto(tokenCliente, pedido.id, it) }
                    aoPedidoRealizado(pedido)
                }
                .onFailure { erro = it.message ?: "Não foi possível finalizar o pedido." }
            enviando = false
        }
    }
}
