package br.com.enali.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.enali.network.dto.EnderecoRequest
import br.com.enali.network.dto.EnderecoResponse
import br.com.enali.ui.components.*
import br.com.enali.ui.theme.CorMarromEscuro
import kotlinx.coroutines.launch

private val SIGLAS_UF = setOf(
    "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG", "PA",
    "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"
)

/**
 * Lista e cadastro de endereços. A mesma tela atende:
 * - cliente logado (Meus endereços)
 * - endereços de um fornecedor (admin)
 * - consulta geral do admin (sem salvar/excluir)
 */
@Composable
fun TelaEnderecos(
    titulo: String,
    subtitulo: String,
    chave: Any,
    listar: suspend () -> Result<List<EnderecoResponse>>,
    salvar: (suspend (id: Long?, request: EnderecoRequest) -> Result<EnderecoResponse>)? = null,
    excluir: (suspend (id: Long) -> Result<Unit>)? = null,
    aoVoltar: (() -> Unit)? = null,
    mostrarDono: Boolean = false
) {
    val escopo = rememberCoroutineScope()
    var enderecos by remember(chave) { mutableStateOf<List<EnderecoResponse>>(emptyList()) }
    var carregando by remember(chave) { mutableStateOf(true) }
    var erro by remember(chave) { mutableStateOf<String?>(null) }
    var sucesso by remember(chave) { mutableStateOf<String?>(null) }
    // null = lista; id 0 = novo; id > 0 = edição
    var emEdicao by remember(chave) { mutableStateOf<EnderecoResponse?>(null) }
    var formularioAberto by remember(chave) { mutableStateOf(false) }

    suspend fun recarregar() {
        carregando = true
        listar()
            .onSuccess { enderecos = it; erro = null }
            .onFailure { erro = it.message ?: "Não foi possível carregar os endereços." }
        carregando = false
    }

    LaunchedEffect(chave) { recarregar() }

    if (formularioAberto && salvar != null) {
        FormularioEndereco(
            inicial = emEdicao,
            aoSalvar = { request ->
                salvar(emEdicao?.id, request).onSuccess {
                    sucesso = if (emEdicao != null) "Endereço atualizado." else "Endereço cadastrado."
                    recarregar() // antes de fechar: o formulário sai da tela e cancela o escopo dele
                    formularioAberto = false
                    emEdicao = null
                }
            },
            aoCancelar = { formularioAberto = false; emEdicao = null }
        )
        return
    }

    PaginaGestao(
        titulo = titulo,
        subtitulo = subtitulo,
        acao = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                aoVoltar?.let { BotaoSecundario("Voltar", aoClicar = it) }
                if (salvar != null) {
                    BotaoPrincipal("Novo endereço") { sucesso = null; emEdicao = null; formularioAberto = true }
                }
            }
        }
    ) {
        AvisoTela(erro, erro = true)
        AvisoTela(sucesso, erro = false)
        CarregandoOuVazio(carregando, enderecos.isEmpty(), "Nenhum endereço cadastrado.")

        enderecos.forEach { endereco ->
            CartaoItem {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "${endereco.rua}, ${endereco.numero}",
                        fontWeight = FontWeight.SemiBold,
                        color = CorMarromEscuro
                    )
                    if (endereco.principal) Etiqueta("Principal", positiva = true)
                    if (!endereco.ativo) Etiqueta("Inativo", positiva = false)
                }
                if (mostrarDono) endereco.dono?.let { Text(it, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium) }
                Text(endereco.resumo(), style = MaterialTheme.typography.bodySmall, color = CorTextoSuave)
                endereco.referencia?.takeIf { it.isNotBlank() }?.let {
                    Text("Referência: $it", style = MaterialTheme.typography.bodySmall, color = CorTextoSuave)
                }
                if (salvar != null || excluir != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (salvar != null) {
                            BotaoSecundario("Editar") { sucesso = null; emEdicao = endereco; formularioAberto = true }
                        }
                        if (excluir != null) {
                            BotaoExcluir {
                                escopo.launch {
                                    excluir(endereco.id)
                                        .onSuccess { sucesso = "Endereço excluído."; erro = null; recarregar() }
                                        .onFailure { erro = it.message ?: "Não foi possível excluir." }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatarCep(valor: String): String {
    val numeros = valor.filter { it.isDigit() }.take(8)

    return if (numeros.length > 5) {
        "${numeros.substring(0, 5)}-${numeros.substring(5)}"
    } else {
        numeros
    }
}

@Composable
fun FormularioEndereco(
    inicial: EnderecoResponse?,
    aoSalvar: suspend (EnderecoRequest) -> Result<EnderecoResponse>,
    aoCancelar: () -> Unit
) {
    val escopo = rememberCoroutineScope()
    var cep by remember {mutableStateOf(formatarCep(inicial?.cep ?: ""))}
    var rua by remember { mutableStateOf(inicial?.rua ?: "") }
    var numero by remember { mutableStateOf(inicial?.numero ?: "") }
    var complemento by remember { mutableStateOf(inicial?.complemento ?: "") }
    var bairro by remember { mutableStateOf(inicial?.bairro ?: "") }
    var cidade by remember { mutableStateOf(inicial?.cidade ?: "") }
    var estado by remember { mutableStateOf(inicial?.estado ?: "") }
    var sigla by remember { mutableStateOf(inicial?.siglaEstado ?: "") }
    var pais by remember { mutableStateOf(inicial?.pais ?: "Brasil") }
    var referencia by remember { mutableStateOf(inicial?.referencia ?: "") }
    var principal by remember { mutableStateOf(inicial?.principal ?: false) }
    var erros by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var erroApi by remember { mutableStateOf<String?>(null) }
    var salvando by remember { mutableStateOf(false) }

    fun validar(): Map<String, String> = buildMap {
        if (!Regex("""^\d{5}-?\d{3}$""").matches(cep.trim())) put("cep", "CEP inválido (00000-000)")
        if (rua.isBlank()) put("rua", "A rua é obrigatória")
        if (numero.isBlank()) put("numero", "O número é obrigatório")
        if (bairro.isBlank()) put("bairro", "O bairro é obrigatório")
        if (cidade.isBlank()) put("cidade", "A cidade é obrigatória")
        if (estado.isBlank()) put("estado", "O estado é obrigatório")
        if (sigla.trim().uppercase() !in SIGLAS_UF) put("sigla", "Sigla inválida (ex.: SP)")
        if (pais.isBlank()) put("pais", "O país é obrigatório")
    }

    PaginaGestao(
        titulo = if (inicial == null) "Novo endereço" else "Editar endereço",
        subtitulo = "Preencha os dados de entrega."
    ) {
        AvisoTela(erroApi, erro = true)
        CampoComErro(cep, { cep = it }, "CEP", erros["cep"], "00000-000")
        CampoComErro(rua, { rua = it }, "Rua", erros["rua"])
        CampoComErro(numero, { numero = it }, "Número", erros["numero"])
        CampoComErro(complemento, { complemento = it }, "Complemento (opcional)")
        CampoComErro(bairro, { bairro = it }, "Bairro", erros["bairro"])
        CampoComErro(cidade, { cidade = it }, "Cidade", erros["cidade"])
        CampoComErro(estado, { estado = it }, "Estado", erros["estado"], "São Paulo")
        CampoComErro(sigla, { sigla = it.take(2).uppercase() }, "Sigla do estado", erros["sigla"], "SP")
        CampoComErro(pais, { pais = it }, "País", erros["pais"])
        CampoComErro(referencia, { referencia = it }, "Referência (opcional)")
        LinhaInterruptor("Endereço principal", principal) { principal = it }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            BotaoPrincipal("Salvar", habilitado = !salvando, modifier = Modifier.weight(1f)) {
                erros = validar()
                if (erros.isEmpty()) {
                    escopo.launch {
                        salvando = true
                        erroApi = null
                        aoSalvar(
                            EnderecoRequest(
                                rua = rua.trim(),
                                numero = numero.trim(),
                                complemento = complemento.trim().ifBlank { null },
                                bairro = bairro.trim(),
                                cidade = cidade.trim(),
                                estado = estado.trim(),
                                siglaEstado = sigla.trim().uppercase(),
                                cep = cep.trim(),
                                pais = pais.trim(),
                                referencia = referencia.trim().ifBlank { null },
                                principal = principal
                            )
                        ).onFailure { erroApi = it.message ?: "Não foi possível salvar o endereço." }
                        salvando = false
                    }
                }
            }
            BotaoSecundario("Cancelar", habilitado = !salvando, modifier = Modifier.weight(1f), aoClicar = aoCancelar)
        }
    }
}
