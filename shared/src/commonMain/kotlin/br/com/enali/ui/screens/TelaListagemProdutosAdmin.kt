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
import br.com.enali.network.EnaliApiClient
import br.com.enali.ui.theme.CorBegeMedio
import br.com.enali.ui.theme.CorBranco
import br.com.enali.ui.theme.CorMarromEscuro
import kotlinx.coroutines.launch

@Composable
fun TelaListagemProdutosAdmin(
    apiClient: EnaliApiClient,
    tokenAdmin: String,
    listaProdutos: List<Produto>,
    aoAtualizarLista: (List<Produto>) -> Unit
) {
    val escopo = rememberCoroutineScope()
    var pesquisa by remember { mutableStateOf("") }
    var carregando by remember { mutableStateOf(false) }
    var erroApi by remember { mutableStateOf<String?>(null) }
    var mensagemSucesso by remember { mutableStateOf<String?>(null) }

    var produtoEmFormulario by remember { mutableStateOf<Produto?>(null) }
    var produtoParaExcluir by remember { mutableStateOf<Produto?>(null) }
    var excluindoProduto by remember { mutableStateOf(false) }

    fun carregarProdutos() {
        escopo.launch {
            carregando = true
            erroApi = null
            apiClient.listarProdutos()
                .onSuccess { lista ->
                    aoAtualizarLista(
                        lista.map {
                            Produto(
                                id = it.id,
                                nome = it.nome,
                                descricao = it.descricao,
                                preco = it.preco,
                                disponivel = it.disponivel,
                                desconto = it.desconto,
                                imagensUrls = it.imagensUrls
                            )
                        }
                    )
                }
                .onFailure { erro ->
                    erroApi = erro.message ?: "Não foi possível carregar os produtos."
                }
            carregando = false
        }
    }

    fun confirmarExclusaoProduto(produto: Produto) {
        if (excluindoProduto) return
        excluindoProduto = true
        erroApi = null
        mensagemSucesso = null

        escopo.launch {
            apiClient.excluirProduto(token = tokenAdmin, id = produto.id)
                .onSuccess {
                    aoAtualizarLista(listaProdutos.filterNot { it.id == produto.id })
                    mensagemSucesso = "Produto \"${produto.nome}\" excluído com sucesso."
                    produtoParaExcluir = null
                }
                .onFailure { erro ->
                    erroApi = erro.message ?: "Não foi possível excluir o produto."
                    produtoParaExcluir = null
                }
            excluindoProduto = false
        }
    }

    LaunchedEffect(Unit) {
        carregarProdutos()
    }

    if (produtoEmFormulario != null) {
        val alvoEdicao = produtoEmFormulario?.takeIf { it.id > 0L }
        TelaFormularioProduto(
            apiClient = apiClient,
            tokenAdmin = tokenAdmin,
            produtoEmEdicao = alvoEdicao,
            aoSalvar = { salvo ->
                val novaLista = if (alvoEdicao != null) {
                    listaProdutos.map { if (it.id == salvo.id) salvo else it }
                } else {
                    listaProdutos + salvo
                }
                aoAtualizarLista(novaLista)
                produtoEmFormulario = null
                carregarProdutos()
            },
            aoCancelar = {
                produtoEmFormulario = null
                carregarProdutos()
            }
        )
        return
    }

    produtoParaExcluir?.let { alvo ->
        AlertDialog(
            onDismissRequest = {
                if (!excluindoProduto) {
                    produtoParaExcluir = null
                }
            },
            containerColor = CorBranco,
            title = {
                Text(
                    text = "Confirmar exclusão do produto",
                    color = CorMarromEscuro,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tem certeza de que deseja excluir permanentemente este produto?",
                        color = CorMarromEscuro
                    )

                    Surface(
                        color = Color(0xFFFAF8F5),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CorBegeMedio.copy(alpha = 0.55f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "ID: ${alvo.id}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF71665C)
                            )
                            Text(
                                text = "Nome: ${alvo.nome}",
                                fontWeight = FontWeight.SemiBold,
                                color = CorMarromEscuro
                            )
                            Text(
                                text = "Descrição: ${alvo.descricao}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF71665C)
                            )
                            Text(
                                text = "Preço: ${formatarPrecoProduto(alvo.preco)}  |  Desconto: ${alvo.desconto}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF71665C)
                            )
                            Text(
                                text = "Status: ${if (alvo.disponivel) "Ativo" else "Inativo"}  |  Imagens: ${alvo.imagensUrls.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF71665C)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { confirmarExclusaoProduto(alvo) },
                    enabled = !excluindoProduto,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (excluindoProduto) "Excluindo..." else "Sim, excluir",
                        color = CorBranco
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { produtoParaExcluir = null },
                    enabled = !excluindoProduto,
                    border = BorderStroke(1.dp, CorMarromEscuro),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancelar", color = CorMarromEscuro)
                }
            }
        )
    }

    val produtosFiltrados = listaProdutos.filter {
        pesquisa.isBlank() ||
            it.nome.contains(pesquisa.trim(), ignoreCase = true) ||
            it.descricao.contains(pesquisa.trim(), ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CorBranco)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Produtos",
                    color = CorMarromEscuro,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Gerencie o catálogo, pesquise itens, imagens e atualize dados.",
                    color = Color(0xFF71665C),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = {
                    produtoEmFormulario = Produto(
                        id = 0L,
                        nome = "",
                        descricao = "",
                        preco = 0.0,
                        disponivel = true,
                        desconto = 0.0
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = CorMarromEscuro),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cadastrar Produto", color = CorBranco)
            }
        }

        OutlinedTextField(
            value = pesquisa,
            onValueChange = { pesquisa = it },
            label = { Text("Pesquisar produto") },
            placeholder = { Text("Digite o nome ou descrição...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CorMarromEscuro,
                focusedLabelColor = CorMarromEscuro
            )
        )

        mensagemSucesso?.let {
            Surface(
                color = Color(0xFFE8F5E9),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(it, color = Color(0xFF2E7D32), modifier = Modifier.padding(14.dp))
            }
        }

        erroApi?.let {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(it, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(14.dp))
            }
        }

        if (carregando) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = CorMarromEscuro)
        }

        Surface(
            color = CorBranco,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, CorBegeMedio.copy(alpha = 0.55f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (produtosFiltrados.isEmpty() && !carregando) {
                    Text(
                        text = if (listaProdutos.isEmpty()) {
                            "Nenhum produto cadastrado na API."
                        } else {
                            "Nenhum produto encontrado para \"$pesquisa\"."
                        },
                        color = Color(0xFF71665C),
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    produtosFiltrados.forEachIndexed { index, produto ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = produto.nome,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CorMarromEscuro
                                )
                                Text(
                                    text = "${formatarPrecoProduto(produto.preco)}  |  Desconto: ${produto.desconto}%  |  Imagens: ${produto.imagensUrls.size}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF71665C)
                                )
                                Surface(
                                    color = if (produto.disponivel) Color(0xFFE8F1E3) else Color(0xFFF5F3EF),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        text = if (produto.disponivel) "Ativo" else "Inativo",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (produto.disponivel) Color(0xFF37643A) else CorMarromEscuro
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { produtoEmFormulario = produto },
                                    border = BorderStroke(1.dp, CorMarromEscuro),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Atualizar", color = CorMarromEscuro)
                                }

                                OutlinedButton(
                                    onClick = { produtoParaExcluir = produto },
                                    enabled = !carregando && !excluindoProduto,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Excluir", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        if (index < produtosFiltrados.lastIndex) {
                            HorizontalDivider(color = CorBegeMedio.copy(alpha = 0.45f))
                        }
                    }
                }
            }
        }
    }
}