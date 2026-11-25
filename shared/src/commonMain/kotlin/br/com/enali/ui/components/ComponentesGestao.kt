package br.com.enali.ui.components

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
import br.com.enali.ui.theme.CorBegeMedio
import br.com.enali.ui.theme.CorBranco
import br.com.enali.ui.theme.CorMarromEscuro
import kotlin.math.roundToLong

// Peças visuais comuns das telas de endereço, pedido, material e fornecedor

val CorTextoSuave = Color(0xFF71665C)

fun formatarMoeda(valor: Double): String {
    if (!valor.isFinite() || valor < 0) return "R$ 0,00"
    val centavos = (valor * 100).roundToLong()
    return "R$ ${centavos / 100},${(centavos % 100).toString().padStart(2, '0')}"
}

// "2026-11-24T16:32:43.123" -> "24/11/2026 16:32"
fun formatarDataHora(iso: String): String {
    val data = iso.substringBefore('T').split('-')
    if (data.size != 3) return iso
    val hora = iso.substringAfter('T', "").take(5)
    return "${data[2]}/${data[1]}/${data[0]}" + if (hora.isNotEmpty()) " $hora" else ""
}

@Composable
fun PaginaGestao(
    titulo: String,
    subtitulo: String,
    acao: (@Composable () -> Unit)? = null,
    conteudo: @Composable ColumnScope.() -> Unit
) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    titulo,
                    color = CorMarromEscuro,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(subtitulo, color = CorTextoSuave, style = MaterialTheme.typography.bodyMedium)
            }
            acao?.invoke()
        }
        conteudo()
    }
}

@Composable
fun AvisoTela(mensagem: String?, erro: Boolean) {
    if (mensagem == null) return
    Surface(
        color = if (erro) MaterialTheme.colorScheme.errorContainer else Color(0xFFE8F5E9),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            mensagem,
            color = if (erro) MaterialTheme.colorScheme.onErrorContainer else Color(0xFF2E7D32),
            modifier = Modifier.padding(14.dp)
        )
    }
}

@Composable
fun CarregandoOuVazio(carregando: Boolean, vazio: Boolean, textoVazio: String) {
    if (carregando) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = CorMarromEscuro)
    } else if (vazio) {
        Text(textoVazio, color = CorTextoSuave)
    }
}

@Composable
fun CartaoItem(conteudo: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = CorBranco,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CorBegeMedio.copy(alpha = 0.65f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            content = conteudo
        )
    }
}

@Composable
fun Etiqueta(texto: String, positiva: Boolean) {
    Surface(
        color = if (positiva) Color(0xFFE8F1E3) else Color(0xFFF3E1DC),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            texto,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = CorMarromEscuro
        )
    }
}

@Composable
fun BotaoPrincipal(texto: String, habilitado: Boolean = true, modifier: Modifier = Modifier, aoClicar: () -> Unit) {
    Button(
        onClick = aoClicar,
        enabled = habilitado,
        colors = ButtonDefaults.buttonColors(containerColor = CorMarromEscuro),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(texto, color = CorBranco)
    }
}

@Composable
fun BotaoSecundario(texto: String, habilitado: Boolean = true, modifier: Modifier = Modifier, aoClicar: () -> Unit) {
    OutlinedButton(
        onClick = aoClicar,
        enabled = habilitado,
        border = BorderStroke(1.dp, CorMarromEscuro),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(texto, color = CorMarromEscuro)
    }
}

// Exclusão em dois cliques: o primeiro pede confirmação
@Composable
fun BotaoExcluir(
    habilitado: Boolean = true,
    texto: String = "Excluir",
    textoConfirmar: String = "Confirmar exclusão",
    aoConfirmar: () -> Unit
) {
    var confirmando by remember { mutableStateOf(false) }
    TextButton(
        onClick = { if (confirmando) { confirmando = false; aoConfirmar() } else confirmando = true },
        enabled = habilitado
    ) {
        Text(
            if (confirmando) textoConfirmar else texto,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun CampoComErro(
    valor: String,
    aoMudar: (String) -> Unit,
    rotulo: String,
    erro: String? = null,
    exemplo: String = ""
) {
    CampoTextoCustomizado(
        value = valor,
        onValueChange = aoMudar,
        label = rotulo,
        placeholder = exemplo,
        isError = erro != null
    )
    erro?.let {
        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun LinhaInterruptor(texto: String, marcado: Boolean, aoMudar: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(texto)
        Switch(checked = marcado, onCheckedChange = aoMudar)
    }
}
