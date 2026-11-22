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
import br.com.enali.network.dto.ProdutoRequest
import br.com.enali.ui.components.CampoTextoCustomizado
import br.com.enali.ui.theme.CorBegeMedio
import br.com.enali.ui.theme.CorBranco
import br.com.enali.ui.theme.CorFundoClaro
import br.com.enali.ui.theme.CorMarromEscuro
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

internal fun formatarPrecoProduto(valor: Double): String {
    if (!valor.isFinite()) return "R$ 0,00"
    val centavos = (valor * 100).roundToLong()
    val reais = centavos / 100
    val resto = (centavos % 100).toString().padStart(2, '0')
    return "R$ $reais,$resto"
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

    var erroNome by remember { mutableStateOf<String?>(null) }
    var erroDescricao by remember { mutableStateOf<String?>(null) }
    var erroPreco by remember { mutableStateOf<String?>(null) }
    var erroDesconto by remember { mutableStateOf<String?>(null) }
    var erroApi by remember { mutableStateOf<String?>(null) }
    var mensagemSucesso by remember { mutableStateOf<String?>(null) }
    var carregando by remember { mutableStateOf(false) }

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
        erroNome = null
        erroDescricao = null
        erroPreco = null
        erroDesconto = null
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
                    apiClient.cadastrarProduto(tokenAdmin, request)
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
                                desconto = resp.desconto
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

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = gravarProduto,
                        enabled = !carregando,
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