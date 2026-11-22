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
import br.com.enali.network.EnaliApiClient
import br.com.enali.ui.theme.CorBegeMedio
import br.com.enali.ui.theme.CorBranco
import br.com.enali.ui.theme.CorMarromEscuro
import kotlinx.coroutines.launch

@Composable
fun TelaListagemEstoqueAdmin(
    apiClient: EnaliApiClient,
    tokenAdmin: String,
    listaProdutos: List<Produto>,
    aoAtualizarProdutos: (List<Produto>) -> Unit,
    listaEstoques: List<Estoque>,
    aoAtualizarLista: (List<Estoque>) -> Unit
) {
    val escopo = rememberCoroutineScope()
    var pesquisa by remember { mutableStateOf("") }
    var carregando by remember { mutableStateOf(false) }
    var erroApi by remember { mutableStateOf<String?>(null) }

    var estoqueEmFormulario by remember { mutableStateOf<Estoque?>(null) }

    fun carregarDados() {
        escopo.launch {
            carregando = true
            erroApi = null

            val produtosAtuais = if (listaProdutos.isEmpty()) {
                apiClient.listarProdutos().getOrNull()?.map {
                    Produto(
                        id = it.id,
                        nome = it.nome,
                        descricao = it.descricao,
                        preco = it.preco,
                        disponivel = it.disponivel,
                        desconto = it.desconto
                    )
                }?.also(aoAtualizarProdutos) ?: listaProdutos
            } else {
                listaProdutos
            }

            apiClient.listarEstoques(tokenAdmin)
                .onSuccess { lista ->
                    aoAtualizarLista(
                        lista.map { resp ->
                            val nomeProduto = resp.produtoNome
                                ?: produtosAtuais.find { it.id == resp.produtoId }?.nome
                                ?: "Produto #${resp.produtoId}"
                            Estoque(
                                id = resp.id,
                                produtoId = resp.produtoId,
                                produtoNome = nomeProduto,
                                quantidadeDisponivel = resp.quantidadeDisponivel,
                                quantidadeReservada = resp.quantidadeReservada,
                                estoqueMinimo = resp.estoqueMinimo,
                                localizacao = resp.localizacao,
                                ativo = resp.ativo
                            )
                        }
                    )
                }
                .onFailure { erro ->
                    erroApi = erro.message ?: "Não foi possível carregar o estoque."
                }
            carregando = false
        }
    }

    LaunchedEffect(tokenAdmin) {
        if (tokenAdmin.isNotBlank()) {
            carregarDados()
        }
    }

    if (estoqueEmFormulario != null) {
        val alvoEdicao = estoqueEmFormulario?.takeIf { it.id > 0L }
        TelaFormularioEstoque(
            apiClient = apiClient,
            tokenAdmin = tokenAdmin,
            listaProdutos = listaProdutos,
            listaEstoques = listaEstoques,
            estoqueEmEdicao = alvoEdicao,
            aoSalvar = { salvo ->
                val novaLista = if (alvoEdicao != null) {
                    listaEstoques.map { if (it.id == salvo.id) salvo else it }
                } else {
                    listaEstoques + salvo
                }
                aoAtualizarLista(novaLista)
                estoqueEmFormulario = null
            },
            aoCancelar = { estoqueEmFormulario = null }
        )
        return
    }

    val estoquesFiltrados = listaEstoques.filter {
        pesquisa.isBlank() ||
            it.produtoNome.contains(pesquisa.trim(), ignoreCase = true) ||
            it.localizacao.contains(pesquisa.trim(), ignoreCase = true)
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
                    text = "Estoque",
                    color = CorMarromEscuro,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Controle quantidades, alertas de mínimo e localização.",
                    color = Color(0xFF71665C),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = {
                    estoqueEmFormulario = Estoque(
                        id = 0L,
                        produtoId = 0L,
                        produtoNome = "",
                        quantidadeDisponivel = 0,
                        quantidadeReservada = 0,
                        estoqueMinimo = 0,
                        localizacao = "",
                        ativo = true
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = CorMarromEscuro),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cadastrar Estoque", color = CorBranco)
            }
        }

        OutlinedTextField(
            value = pesquisa,
            onValueChange = { pesquisa = it },
            label = { Text("Buscar produto ou localização") },
            placeholder = { Text("Ex.: Caneca ou Prateleira A3...") },
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
                if (estoquesFiltrados.isEmpty() && !carregando) {
                    Text(
                        text = if (listaEstoques.isEmpty()) {
                            "Nenhum estoque cadastrado na API."
                        } else {
                            "Nenhum registro encontrado para \"$pesquisa\"."
                        },
                        color = Color(0xFF71665C),
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    estoquesFiltrados.forEachIndexed { index, estoque ->
                        val abaixoDoMinimo = estoque.quantidadeDisponivel < estoque.estoqueMinimo

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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = estoque.produtoNome,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CorMarromEscuro
                                    )
                                    Surface(
                                        color = if (abaixoDoMinimo) Color(0xFFFFE8CC) else Color(0xFFE8F1E3),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Text(
                                            text = if (abaixoDoMinimo) "Estoque baixo" else "Em estoque",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CorMarromEscuro
                                        )
                                    }
                                }
                                Text(
                                    text = "Disponível: ${estoque.quantidadeDisponivel}  |  Reservada: ${estoque.quantidadeReservada}  |  Mínimo: ${estoque.estoqueMinimo}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF71665C)
                                )
                                Text(
                                    text = "${estoque.localizacao}  •  ${if (estoque.ativo) "Ativo" else "Inativo"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF71665C)
                                )
                            }

                            OutlinedButton(
                                onClick = { estoqueEmFormulario = estoque },
                                border = BorderStroke(1.dp, CorMarromEscuro),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Atualizar", color = CorMarromEscuro)
                            }
                        }

                        if (index < estoquesFiltrados.lastIndex) {
                            HorizontalDivider(color = CorBegeMedio.copy(alpha = 0.45f))
                        }
                    }
                }
            }
        }
    }
}