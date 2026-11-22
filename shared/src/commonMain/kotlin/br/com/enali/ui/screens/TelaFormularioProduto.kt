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
import br.com.enali.network.dto.ImagemUploadItem
import br.com.enali.network.dto.ProdutoImagemResponse
import br.com.enali.network.dto.ProdutoRequest
import br.com.enali.ui.components.CampoTextoCustomizado
import br.com.enali.ui.components.selecionarImagensProduto
import br.com.enali.ui.theme.CorBegeMedio
import br.com.enali.ui.theme.CorBranco
import br.com.enali.ui.theme.CorMarromEscuro
import kotlinx.coroutines.launch
import kotlin.math.roundToLong
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import br.com.enali.ui.components.toComposeImageBitmap

internal fun formatarPrecoProduto(valor: Double): String {
    if (!valor.isFinite()) return "R$ 0,00"
    val centavos = (valor * 100).roundToLong()
    val reais = centavos / 100
    val resto = (centavos % 100).toString().padStart(2, '0')
    return "R$ $reais,$resto"
}

private fun formatarTamanhoKb(bytes: Int): String {
    val kb = (bytes / 1024.0 * 10).roundToLong() / 10.0
    return "$kb KB"
}

@Composable
fun TelaFormularioProduto(
    apiClient: EnaliApiClient,
    tokenAdmin: String,
    produtoEmEdicao: Produto? = null,
    aoSalvar: (Produto) -> Unit,
    aoCancelar: (() -> Unit)? = null
) {
    val escopo = rememberCoroutineScope()
    val estaEditando = produtoEmEdicao != null

    var nomeProduto by remember(produtoEmEdicao) { mutableStateOf(produtoEmEdicao?.nome ?: "") }
    var descricaoProduto by remember(produtoEmEdicao) { mutableStateOf(produtoEmEdicao?.descricao ?: "") }
    var precoProduto by remember(produtoEmEdicao) {
        mutableStateOf(produtoEmEdicao?.preco?.toString()?.replace('.', ',') ?: "")
    }
    var descontoProduto by remember(produtoEmEdicao) {
        mutableStateOf(produtoEmEdicao?.desconto?.toString()?.replace('.', ',') ?: "0")
    }
    var estaDisponivel by remember(produtoEmEdicao) { mutableStateOf(produtoEmEdicao?.disponivel ?: true) }

    val imagensNovoProduto = remember(produtoEmEdicao) { mutableStateListOf<ImagemUploadItem>() }

    val imagensExistentes = remember(produtoEmEdicao) { mutableStateListOf<ProdutoImagemResponse>() }
    var carregandoImagens by remember { mutableStateOf(false) }

    val novasImagensEdicao = remember(produtoEmEdicao) { mutableStateListOf<ImagemUploadItem>() }
    var ordemNovaImagemTexto by remember { mutableStateOf("") }
    var enviandoImagem by remember { mutableStateOf(false) }
    var removendoImagemId by remember { mutableStateOf<Long?>(null) }

    var erroNome by remember { mutableStateOf<String?>(null) }
    var erroDescricao by remember { mutableStateOf<String?>(null) }
    var erroPreco by remember { mutableStateOf<String?>(null) }
    var erroDesconto by remember { mutableStateOf<String?>(null) }
    var erroImagem by remember { mutableStateOf<String?>(null) }
    var erroApi by remember { mutableStateOf<String?>(null) }
    var mensagemSucesso by remember { mutableStateOf<String?>(null) }
    var carregando by remember { mutableStateOf(false) }
    
    val bitmapsExistentes = remember(produtoEmEdicao) { mutableStateMapOf<Long, ImageBitmap>() }

    LaunchedEffect(produtoEmEdicao?.id) {
        if (produtoEmEdicao != null && produtoEmEdicao.id > 0L) {
            carregandoImagens = true
            erroImagem = null
            bitmapsExistentes.clear()
            apiClient.listarImagensProduto(produtoEmEdicao.id)
                .onSuccess { lista ->
                    val ordenadas = lista.sortedWith(compareBy({ it.ordem }, { it.id }))
                    imagensExistentes.clear()
                    imagensExistentes.addAll(ordenadas)

                    ordenadas.forEach { img ->
                        apiClient.buscarImagemProdutoBytes(produtoEmEdicao.id, img.id)
                            .onSuccess { bytes ->
                                bytes.toComposeImageBitmap()?.let { bitmap ->
                                    bitmapsExistentes[img.id] = bitmap
                                }
                            }
                    }
                }
                .onFailure { erro ->
                    erroImagem = erro.message ?: "Não foi possível carregar as imagens do produto."
                }
            carregandoImagens = false
        } else {
            imagensExistentes.clear()
            bitmapsExistentes.clear()
        }
    }

    LaunchedEffect(produtoEmEdicao?.id) {
        if (produtoEmEdicao != null && produtoEmEdicao.id > 0L) {
            carregandoImagens = true
            erroImagem = null
            apiClient.listarImagensProduto(produtoEmEdicao.id)
                .onSuccess { lista ->
                    imagensExistentes.clear()
                    imagensExistentes.addAll(lista.sortedWith(compareBy({ it.ordem }, { it.id })))
                }
                .onFailure { erro ->
                    erroImagem = erro.message ?: "Não foi possível carregar as imagens do produto."
                }
            carregandoImagens = false
        } else {
            imagensExistentes.clear()
        }
    }

    val precoDigitado = precoProduto.replace(",", ".").toDoubleOrNull()
    val descontoDigitado = descontoProduto.replace(",", ".").toDoubleOrNull()

    val precoResumo = precoDigitado?.takeIf { it.isFinite() && it > 0 }
    val descontoResumo = descontoDigitado?.takeIf { it.isFinite() && it in 0.0..100.0 }
    val valorDesconto = if (precoResumo != null && descontoResumo != null) {
        precoResumo * descontoResumo / 100.0
    } else null
    val precoFinal = if (precoResumo != null && valorDesconto != null) {
        precoResumo - valorDesconto
    } else null

    val limparCampos = {
        nomeProduto = ""
        descricaoProduto = ""
        precoProduto = ""
        descontoProduto = "0"
        estaDisponivel = true
        imagensNovoProduto.clear()
        erroNome = null
        erroDescricao = null
        erroPreco = null
        erroDesconto = null
        erroImagem = null
        erroApi = null
    }

    val gravarProduto = {
        val precoValor = precoProduto.replace(",", ".").toDoubleOrNull()
        val descontoValor = descontoProduto.replace(",", ".").toDoubleOrNull()

        erroNome = when {
            nomeProduto.isBlank() -> "O nome é obrigatório"
            nomeProduto.trim().length > 120 -> "O nome deve ter no máximo 120 caracteres"
            else -> null
        }

        erroDescricao = when {
            descricaoProduto.isBlank() -> "A descrição é obrigatória"
            descricaoProduto.trim().length > 500 -> "A descrição deve ter no máximo 500 caracteres"
            else -> null
        }

        erroPreco = if (precoValor == null || !precoValor.isFinite() || precoValor <= 0) {
            "Informe um preço válido maior que zero"
        } else null

        erroDesconto = if (descontoValor == null || !descontoValor.isFinite() || descontoValor < 0 || descontoValor > 100) {
            "O desconto deve estar entre 0 e 100"
        } else null

        erroApi = null
        mensagemSucesso = null

        if (
            erroNome == null &&
            erroDescricao == null &&
            erroPreco == null &&
            erroDesconto == null &&
            precoValor != null &&
            descontoValor != null &&
            !carregando
        ) {
            carregando = true
            val request = ProdutoRequest(
                nome = nomeProduto.trim(),
                descricao = descricaoProduto.trim(),
                preco = precoValor,
                disponivel = estaDisponivel,
                desconto = descontoValor
            )
            escopo.launch {
                val resultado = if (produtoEmEdicao != null) {
                    apiClient.atualizarProduto(tokenAdmin, produtoEmEdicao.id, request)
                } else {
                    apiClient.cadastrarProduto(tokenAdmin, request, imagensNovoProduto.toList())
                }
                carregando = false
                resultado
                    .onSuccess { resp ->
                        aoSalvar(
                            Produto(
                                id = resp.id,
                                nome = resp.nome,
                                descricao = resp.descricao,
                                preco = resp.preco,
                                disponivel = resp.disponivel,
                                desconto = resp.desconto,
                                imagensUrls = resp.imagensUrls
                            )
                        )
                        mensagemSucesso = if (estaEditando) {
                            "Produto atualizado com sucesso!"
                        } else {
                            "Produto cadastrado com sucesso!"
                        }
                        if (!estaEditando) limparCampos()
                    }
                    .onFailure { erro ->
                        erroApi = erro.message ?: "Não foi possível salvar o produto."
                    }
            }
        }
    }

    fun enviarNovasImagensNaEdicao(produtoId: Long) {
        if (novasImagensEdicao.isEmpty() || enviandoImagem) return

        val ordemBase = ordemNovaImagemTexto.trim().toIntOrNull()
        if (ordemNovaImagemTexto.isNotBlank() && (ordemBase == null || ordemBase < 0)) {
            erroImagem = "A ordem informada deve ser um número inteiro maior ou igual a zero."
            return
        }

        erroImagem = null
        mensagemSucesso = null
        enviandoImagem = true

        escopo.launch {
            val fila = novasImagensEdicao.toList()
            fila.forEachIndexed { idx, img ->
                val ordemDefinida = ordemBase?.plus(idx)
                apiClient.adicionarImagemProduto(
                    token = tokenAdmin,
                    produtoId = produtoId,
                    imagem = img,
                    ordem = ordemDefinida
                ).onSuccess { criada ->
                    imagensExistentes.add(criada)
                    novasImagensEdicao.remove(img)
                }.onFailure { erro ->
                    erroImagem = erro.message ?: "Falha ao enviar a imagem \"${img.nomeArquivo}\"."
                }
            }
            imagensExistentes.sortWith(compareBy({ it.ordem }, { it.id }))
            enviandoImagem = false
            if (erroImagem == null) {
                ordemNovaImagemTexto = ""
                mensagemSucesso = "Imagem(ns) adicionada(s) ao produto!"
            }
        }
    }

    fun excluirImagemExistente(produtoId: Long, imagem: ProdutoImagemResponse) {
        if (removendoImagemId != null) return
        erroImagem = null
        mensagemSucesso = null
        removendoImagemId = imagem.id

        escopo.launch {
            apiClient.removerImagemProduto(
                token = tokenAdmin,
                produtoId = produtoId,
                imagemId = imagem.id
            ).onSuccess {
                imagensExistentes.removeAll { it.id == imagem.id }
                mensagemSucesso = "Imagem #${imagem.id} removida."
            }.onFailure { erro ->
                erroImagem = erro.message ?: "Não foi possível remover a imagem."
            }
            removendoImagemId = null
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
                text = if (estaEditando) "Atualizar Produto" else "Novo Produto",
                color = CorMarromEscuro,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            if (aoCancelar != null) {
                OutlinedButton(onClick = aoCancelar, enabled = !carregando && !enviandoImagem) {
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
                CampoTextoCustomizado(
                    value = nomeProduto,
                    onValueChange = { nomeProduto = it; erroNome = null },
                    label = "Nome do produto",
                    placeholder = "Caneca personalizada",
                    isError = erroNome != null
                )
                erroNome?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                OutlinedTextField(
                    value = descricaoProduto,
                    onValueChange = { descricaoProduto = it; erroDescricao = null },
                    label = { Text("Descrição") },
                    placeholder = { Text("Descrição do produto") },
                    minLines = 3,
                    maxLines = 5,
                    isError = erroDescricao != null,
                    modifier = Modifier.fillMaxWidth()
                )
                erroDescricao?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        CampoTextoCustomizado(
                            value = precoProduto,
                            onValueChange = { precoProduto = it; erroPreco = null },
                            label = "Preço (R$)",
                            placeholder = "35,90",
                            isError = erroPreco != null
                        )
                        erroPreco?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        CampoTextoCustomizado(
                            value = descontoProduto,
                            onValueChange = { descontoProduto = it; erroDesconto = null },
                            label = "Desconto (%)",
                            placeholder = "0",
                            isError = erroDesconto != null
                        )
                        erroDesconto?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Preço original", color = Color(0xFF71665C))
                    Text(precoResumo?.let(::formatarPrecoProduto) ?: "R$ 0,00")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Desconto${descontoResumo?.let { " (${it}%)" } ?: ""}", color = Color(0xFF71665C))
                    Text(valorDesconto?.let { "- ${formatarPrecoProduto(it)}" } ?: "R$ 0,00")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Preço final", fontWeight = FontWeight.SemiBold)
                    Text(
                        precoFinal?.let(::formatarPrecoProduto) ?: "R$ 0,00",
                        color = CorMarromEscuro,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Disponível para venda", fontWeight = FontWeight.Medium)
                    Switch(
                        checked = estaDisponivel,
                        onCheckedChange = { estaDisponivel = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = CorMarromEscuro)
                    )
                }

                HorizontalDivider(color = CorBegeMedio.copy(alpha = 0.55f))

                // --- SEÇÃO DE IMAGENS ---
                if (!estaEditando) {
                    Text(
                        text = "Imagens do produto (opcional — JPEG, PNG ou WebP até 5 MB)",
                        fontWeight = FontWeight.SemiBold,
                        color = CorMarromEscuro
                    )

                    OutlinedButton(
                        onClick = {
                            erroImagem = null
                            selecionarImagensProduto(
                                permiteMultiplas = true,
                                onSelecionadas = { selecionadas ->
                                    imagensNovoProduto.addAll(selecionadas)
                                },
                                onErro = { msg -> erroImagem = msg }
                            )
                        },
                        enabled = !carregando
                    ) {
                        Text("Selecionar imagens", color = CorMarromEscuro)
                    }

                    erroImagem?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    imagensNovoProduto.forEachIndexed { indice, img ->
                        Surface(
                            color = Color(0xFFFAF8F5),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, CorBegeMedio.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${indice + 1}. ${img.nomeArquivo}",
                                        fontWeight = FontWeight.Medium,
                                        color = CorMarromEscuro
                                    )
                                    Text(
                                        text = "${img.tipoMime} • ${formatarTamanhoKb(img.bytes.size)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF71665C)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    TextButton(
                                        onClick = {
                                            if (indice > 0) {
                                                val temp = imagensNovoProduto[indice - 1]
                                                imagensNovoProduto[indice - 1] = imagensNovoProduto[indice]
                                                imagensNovoProduto[indice] = temp
                                            }
                                        },
                                        enabled = indice > 0 && !carregando
                                    ) {
                                        Text("↑", color = CorMarromEscuro)
                                    }
                                    TextButton(
                                        onClick = {
                                            if (indice < imagensNovoProduto.lastIndex) {
                                                val temp = imagensNovoProduto[indice + 1]
                                                imagensNovoProduto[indice + 1] = imagensNovoProduto[indice]
                                                imagensNovoProduto[indice] = temp
                                            }
                                        },
                                        enabled = indice < imagensNovoProduto.lastIndex && !carregando
                                    ) {
                                        Text("↓", color = CorMarromEscuro)
                                    }
                                    TextButton(
                                        onClick = { imagensNovoProduto.removeAt(indice) },
                                        enabled = !carregando
                                    ) {
                                        Text("Remover", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                } else if (produtoEmEdicao != null) {
                    Text(
                        text = "Imagens cadastradas (${imagensExistentes.size})",
                        fontWeight = FontWeight.SemiBold,
                        color = CorMarromEscuro
                    )

                    Text(
                        text = "Dica: ao adicionar novas imagens, você pode informar a ordem inicial (opcional). A reordenação de imagens já salvas no banco requer endpoint específico no backend.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF71665C)
                    )

                    if (carregandoImagens) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = CorMarromEscuro)
                    }

                    imagensExistentes.forEachIndexed { indice, img ->
                        val bitmap = bitmapsExistentes[img.id]

                        Surface(
                            color = Color(0xFFFAF8F5),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, CorBegeMedio.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, CorBegeMedio.copy(alpha = 0.45f)),
                                    color = CorBranco,
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap,
                                            contentDescription = "Imagem #${img.id} do produto",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (carregandoImagens) "..." else "#${img.id}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF71665C)
                                            )
                                        }
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Imagem #${img.id} (Ordem na lista: $indice • API ordem: ${img.ordem})",
                                        fontWeight = FontWeight.Medium,
                                        color = CorMarromEscuro
                                    )
                                    Text(
                                        text = "${img.tipoMime} • ${formatarTamanhoKb(img.tamanhoBytes)} • ${img.url}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF71665C)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    TextButton(
                                        onClick = {
                                            if (indice > 0) {
                                                val temp = imagensExistentes[indice - 1]
                                                imagensExistentes[indice - 1] = imagensExistentes[indice]
                                                imagensExistentes[indice] = temp
                                            }
                                        },
                                        enabled = indice > 0 && !enviandoImagem && removendoImagemId == null
                                    ) {
                                        Text("↑", color = CorMarromEscuro)
                                    }
                                    TextButton(
                                        onClick = {
                                            if (indice < imagensExistentes.lastIndex) {
                                                val temp = imagensExistentes[indice + 1]
                                                imagensExistentes[indice + 1] = imagensExistentes[indice]
                                                imagensExistentes[indice] = temp
                                            }
                                        },
                                        enabled = indice < imagensExistentes.lastIndex && !enviandoImagem && removendoImagemId == null
                                    ) {
                                        Text("↓", color = CorMarromEscuro)
                                    }
                                    TextButton(
                                        onClick = {
                                            excluirImagemExistente(produtoEmEdicao.id, img)
                                            bitmapsExistentes.remove(img.id)
                                        },
                                        enabled = !enviandoImagem && removendoImagemId == null
                                    ) {
                                        Text(
                                            if (removendoImagemId == img.id) "Excluindo..." else "Excluir",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Adicionar novas imagens ao produto",
                        fontWeight = FontWeight.Medium,
                        color = CorMarromEscuro
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = {
                                erroImagem = null
                                selecionarImagensProduto(
                                    permiteMultiplas = true,
                                    onSelecionadas = { selecionadas ->
                                        novasImagensEdicao.addAll(selecionadas)
                                    },
                                    onErro = { msg -> erroImagem = msg }
                                )
                            },
                            enabled = !enviandoImagem && !carregando
                        ) {
                            Text("Escolher arquivos", color = CorMarromEscuro)
                        }

                        OutlinedTextField(
                            value = ordemNovaImagemTexto,
                            onValueChange = { ordemNovaImagemTexto = it; erroImagem = null },
                            label = { Text("Ordem (opcional)") },
                            placeholder = { Text("Ex.: 0") },
                            singleLine = true,
                            modifier = Modifier.width(150.dp)
                        )

                        Button(
                            onClick = { enviarNovasImagensNaEdicao(produtoEmEdicao.id) },
                            enabled = novasImagensEdicao.isNotEmpty() && !enviandoImagem && !carregando,
                            colors = ButtonDefaults.buttonColors(containerColor = CorMarromEscuro)
                        ) {
                            Text(
                                if (enviandoImagem) "Enviando..." else "Enviar imagens",
                                color = CorBranco
                            )
                        }
                    }

                    erroImagem?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    novasImagensEdicao.forEachIndexed { idx, img ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pendente: ${img.nomeArquivo} (${formatarTamanhoKb(img.bytes.size)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = CorMarromEscuro,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = { novasImagensEdicao.removeAt(idx) },
                                enabled = !enviandoImagem
                            ) {
                                Text("Remover", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = gravarProduto,
                        enabled = !carregando && !enviandoImagem,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CorMarromEscuro),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            when {
                                carregando -> "Salvando..."
                                estaEditando -> "Atualizar Produto"
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