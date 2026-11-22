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
import br.com.enali.ui.theme.CorFundoClaro
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

    var produtoEmFormulario by remember { mutableStateOf<Produto?>(null) }

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
                                desconto = it.desconto
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
            },
            aoCancelar = { produtoEmFormulario = null }
        )
        return
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
                    text = "Gerencie o catálogo, pesquise itens e atualize dados.",
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
                                    text = "${formatarPrecoProduto(produto.preco)}  |  Desconto: ${produto.desconto}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF71665C)
                                )
                                Surface(
                                    color = if (produto.disponivel) Color(0xFFE8F1E3) else CorFundoClaro,
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

                            OutlinedButton(
                                onClick = { produtoEmFormulario = produto },
                                border = BorderStroke(1.dp, CorMarromEscuro),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Atualizar", color = CorMarromEscuro)
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