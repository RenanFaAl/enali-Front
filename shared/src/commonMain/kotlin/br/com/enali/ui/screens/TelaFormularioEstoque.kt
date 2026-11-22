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
import br.com.enali.network.dto.EstoqueRequest
import br.com.enali.ui.components.CampoTextoCustomizado
import br.com.enali.ui.theme.CorBegeMedio
import br.com.enali.ui.theme.CorBranco
import br.com.enali.ui.theme.CorMarromEscuro
import kotlinx.coroutines.launch

@Composable
fun TelaFormularioEstoque(
    apiClient: EnaliApiClient,
    tokenAdmin: String,
    listaProdutos: List<Produto>,
    listaEstoques: List<Estoque> = emptyList(),
    estoqueEmEdicao: Estoque? = null,
    aoSalvar: (Estoque) -> Unit,
    aoCancelar: (() -> Unit)? = null
) {
    val escopo = rememberCoroutineScope()
    val estaEditando = estoqueEmEdicao != null

    var produtoSelecionadoId by remember(estoqueEmEdicao) {
        mutableStateOf<Long?>(estoqueEmEdicao?.produtoId)
    }
    var menuProdutosAberto by remember { mutableStateOf(false) }
    var quantidadeDisponivel by remember(estoqueEmEdicao) {
        mutableStateOf(estoqueEmEdicao?.quantidadeDisponivel?.toString() ?: "")
    }
    var quantidadeReservada by remember(estoqueEmEdicao) {
        mutableStateOf(estoqueEmEdicao?.quantidadeReservada?.toString() ?: "0")
    }
    var estoqueMinimo by remember(estoqueEmEdicao) {
        mutableStateOf(estoqueEmEdicao?.estoqueMinimo?.toString() ?: "0")
    }
    var localizacao by remember(estoqueEmEdicao) {
        mutableStateOf(estoqueEmEdicao?.localizacao ?: "")
    }
    var estaAtivo by remember(estoqueEmEdicao) {
        mutableStateOf(estoqueEmEdicao?.ativo ?: true)
    }

    var erroProduto by remember { mutableStateOf<String?>(null) }
    var erroDisponivel by remember { mutableStateOf<String?>(null) }
    var erroReservada by remember { mutableStateOf<String?>(null) }
    var erroMinimo by remember { mutableStateOf<String?>(null) }
    var erroLocalizacao by remember { mutableStateOf<String?>(null) }
    var erroApi by remember { mutableStateOf<String?>(null) }
    var mensagemSucesso by remember { mutableStateOf<String?>(null) }
    var carregando by remember { mutableStateOf(false) }

    val idsProdutosComEstoque = remember(listaEstoques, estoqueEmEdicao) {
        listaEstoques
            .map { it.produtoId }
            .filter { id -> id != estoqueEmEdicao?.produtoId }
            .toSet()
    }

    val produtosDisponiveisParaEstoque = remember(listaProdutos, idsProdutosComEstoque) {
        listaProdutos.filter { it.id !in idsProdutosComEstoque }
    }

    val produtoSelecionado = listaProdutos.find { it.id == produtoSelecionadoId }

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
        erroApi = null
    }

    val gravarEstoque = {
        val disponivelValor = quantidadeDisponivel.trim().toIntOrNull()
        val reservadaValor = quantidadeReservada.trim().toIntOrNull()
        val minimoValor = estoqueMinimo.trim().toIntOrNull()

        erroProduto = if (produtoSelecionadoId == null) "Selecione um produto" else null
        erroDisponivel = if (disponivelValor == null || disponivelValor < 0) {
            "Informe uma quantidade válida"
        } else null
        erroReservada = if (reservadaValor == null || reservadaValor < 0) {
            "A quantidade reservada não pode ser negativa"
        } else null
        erroMinimo = if (minimoValor == null || minimoValor < 0) {
            "O estoque mínimo não pode ser negativo"
        } else null
        erroLocalizacao = when {
            localizacao.isBlank() -> "A localização é obrigatória"
            localizacao.trim().length > 100 -> "Máximo de 100 caracteres"
            else -> null
        }
        erroApi = null
        mensagemSucesso = null

        val prodId = produtoSelecionadoId
        if (
            prodId != null &&
            disponivelValor != null &&
            reservadaValor != null &&
            minimoValor != null &&
            erroDisponivel == null &&
            erroReservada == null &&
            erroMinimo == null &&
            erroLocalizacao == null &&
            !carregando
        ) {
            carregando = true
            val request = EstoqueRequest(
                produtoId = prodId,
                quantidadeDisponivel = disponivelValor,
                quantidadeReservada = reservadaValor,
                estoqueMinimo = minimoValor,
                localizacao = localizacao.trim(),
                ativo = estaAtivo
            )
            escopo.launch {
                val resultado = if (estoqueEmEdicao != null) {
                    apiClient.atualizarEstoque(tokenAdmin, estoqueEmEdicao.id, request)
                } else {
                    apiClient.cadastrarEstoque(tokenAdmin, request)
                }
                carregando = false
                resultado
                    .onSuccess { resp ->
                        val nomeProduto = resp.produtoNome
                            ?: listaProdutos.find { it.id == resp.produtoId }?.nome
                            ?: estoqueEmEdicao?.produtoNome
                            ?: "Produto #${resp.produtoId}"

                        aoSalvar(
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
                        )
                        mensagemSucesso = if (estaEditando) {
                            "Estoque atualizado com sucesso!"
                        } else {
                            "Estoque cadastrado com sucesso!"
                        }
                        if (!estaEditando) limparCampos()
                    }
                    .onFailure { erro ->
                        erroApi = erro.message ?: "Não foi possível salvar o estoque."
                    }
            }
        }
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
            Text(
                text = if (estaEditando) "Atualizar Estoque" else "Cadastro de Estoque",
                color = CorMarromEscuro,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            if (aoCancelar != null) {
                OutlinedButton(onClick = aoCancelar, enabled = !carregando) {
                    Text("Voltar para lista", color = CorMarromEscuro)
                }
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

        mensagemSucesso?.let {
            Surface(
                color = Color(0xFFE8F5E9),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(it, color = Color(0xFF2E7D32), modifier = Modifier.padding(14.dp))
            }
        }

        Surface(
            color = CorBranco,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, CorBegeMedio.copy(alpha = 0.55f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Produto", color = CorMarromEscuro, style = MaterialTheme.typography.labelLarge)

                Box {
                    OutlinedButton(
                        onClick = { menuProdutosAberto = true },
                        enabled = !carregando && (produtosDisponiveisParaEstoque.isNotEmpty() || produtoSelecionadoId != null),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        border = BorderStroke(1.dp, CorBegeMedio),
                        shape = RoundedCornerShape(9.dp)
                    ) {
                        Text(
                            text = produtoSelecionado?.let { "${it.nome} (ID: ${it.id})" }
                                ?: produtoSelecionadoId?.let { "Produto #${it}" }
                                ?: "Selecionar produto",
                            color = CorMarromEscuro
                        )
                    }

                    DropdownMenu(
                        expanded = menuProdutosAberto,
                        onDismissRequest = { menuProdutosAberto = false }
                    ) {
                        produtosDisponiveisParaEstoque.forEach { produto ->
                            DropdownMenuItem(
                                text = { Text("${produto.nome} (ID: ${produto.id})") },
                                onClick = {
                                    produtoSelecionadoId = produto.id
                                    erroProduto = null
                                    menuProdutosAberto = false
                                }
                            )
                        }
                    }
                }

                if (produtosDisponiveisParaEstoque.isEmpty() && produtoSelecionadoId == null) {
                    Text(
                        text = if (listaProdutos.isEmpty()) {
                            "Cadastre um produto antes de cadastrar o estoque."
                        } else {
                            "Todos os produtos cadastrados já possuem estoque vinculado."
                        },
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                erroProduto?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        CampoTextoCustomizado(
                            value = quantidadeDisponivel,
                            onValueChange = { quantidadeDisponivel = it; erroDisponivel = null },
                            label = "Qtd. disponível",
                            placeholder = "50",
                            isError = erroDisponivel != null
                        )
                        erroDisponivel?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        CampoTextoCustomizado(
                            value = quantidadeReservada,
                            onValueChange = { quantidadeReservada = it; erroReservada = null },
                            label = "Qtd. reservada",
                            placeholder = "5",
                            isError = erroReservada != null
                        )
                        erroReservada?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                }

                CampoTextoCustomizado(
                    value = estoqueMinimo,
                    onValueChange = { estoqueMinimo = it; erroMinimo = null },
                    label = "Estoque mínimo",
                    placeholder = "10",
                    isError = erroMinimo != null
                )
                erroMinimo?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                CampoTextoCustomizado(
                    value = localizacao,
                    onValueChange = { localizacao = it; erroLocalizacao = null },
                    label = "Localização",
                    placeholder = "Prateleira A3",
                    isError = erroLocalizacao != null
                )
                erroLocalizacao?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Estoque ativo", fontWeight = FontWeight.Medium)
                    Switch(
                        checked = estaAtivo,
                        onCheckedChange = { estaAtivo = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = CorMarromEscuro)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = gravarEstoque,
                        enabled = !carregando && (produtosDisponiveisParaEstoque.isNotEmpty() || produtoSelecionadoId != null),
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CorMarromEscuro),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            when {
                                carregando -> "Salvando..."
                                estaEditando -> "Atualizar Estoque"
                                else -> "Gravar"
                            },
                            color = CorBranco
                        )
                    }

                    if (!estaEditando) {
                        OutlinedButton(
                            onClick = { limparCampos(); mensagemSucesso = null },
                            enabled = !carregando,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Limpar campos", color = CorMarromEscuro)
                        }
                    }
                }
            }
        }
    }
}