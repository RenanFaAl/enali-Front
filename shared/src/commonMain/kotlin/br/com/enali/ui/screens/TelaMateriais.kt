package br.com.enali.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.enali.network.EnaliApiClient
import br.com.enali.network.dto.MaterialRequest
import br.com.enali.network.dto.MaterialResponse
import br.com.enali.ui.components.*
import br.com.enali.ui.theme.CorMarromEscuro
import kotlinx.coroutines.launch

@Composable
fun TelaMateriais(apiClient: EnaliApiClient, token: String) {
    val api = apiClient.material
    val escopo = rememberCoroutineScope()
    var materiais by remember { mutableStateOf<List<MaterialResponse>>(emptyList()) }
    var carregando by remember { mutableStateOf(true) }
    var erro by remember { mutableStateOf<String?>(null) }
    var sucesso by remember { mutableStateOf<String?>(null) }
    var busca by remember { mutableStateOf("") }
    var formularioAberto by remember { mutableStateOf(false) }
    var emEdicao by remember { mutableStateOf<MaterialResponse?>(null) }

    suspend fun recarregar() {
        carregando = true
        api.listar(token)
            .onSuccess { materiais = it.sortedBy { m -> m.nome.lowercase() }; erro = null }
            .onFailure { erro = it.message ?: "Não foi possível carregar os materiais." }
        carregando = false
    }

    LaunchedEffect(token) { recarregar() }

    if (formularioAberto) {
        FormularioMaterial(
            inicial = emEdicao,
            aoSalvar = { request ->
                val alvo = emEdicao
                (if (alvo == null) api.cadastrar(token, request) else api.atualizar(token, alvo.id, request))
                    .onSuccess {
                        sucesso = if (alvo == null) "Material cadastrado." else "Material atualizado."
                        recarregar() // antes de fechar: o formulário sai da tela e cancela o escopo dele
                        formularioAberto = false
                        emEdicao = null
                    }
            },
            aoCancelar = { formularioAberto = false; emEdicao = null }
        )
        return
    }

    val filtrados = materiais.filter { busca.isBlank() || it.nome.contains(busca.trim(), ignoreCase = true) }

    PaginaGestao(
        titulo = "Materiais",
        subtitulo = "Matéria-prima usada na produção.",
        acao = { BotaoPrincipal("Novo material") { sucesso = null; emEdicao = null; formularioAberto = true } }
    ) {
        OutlinedTextField(
            value = busca,
            onValueChange = { busca = it },
            label = { Text("Buscar material") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CorMarromEscuro,
                focusedLabelColor = CorMarromEscuro
            )
        )
        AvisoTela(erro, erro = true)
        AvisoTela(sucesso, erro = false)
        CarregandoOuVazio(
            carregando,
            filtrados.isEmpty(),
            if (materiais.isEmpty()) "Nenhum material cadastrado." else "Nenhum material encontrado."
        )

        filtrados.forEach { material ->
            CartaoItem {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(material.nome, fontWeight = FontWeight.SemiBold, color = CorMarromEscuro)
                    Etiqueta(if (material.disponivel) "Disponível" else "Indisponível", material.disponivel)
                }
                material.descricao?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = CorTextoSuave)
                }
                Text(
                    "Quantidade: ${material.quantidade}  |  Custo: ${formatarMoeda(material.custo)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CorTextoSuave
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    BotaoSecundario("Editar") { sucesso = null; emEdicao = material; formularioAberto = true }
                    BotaoSecundario(if (material.disponivel) "Marcar indisponível" else "Marcar disponível") {
                        escopo.launch {
                            api.alterarDisponivel(token, material.id, !material.disponivel)
                                .onSuccess { sucesso = null; recarregar() }
                                .onFailure { erro = it.message }
                        }
                    }
                    BotaoExcluir {
                        escopo.launch {
                            api.excluir(token, material.id)
                                .onSuccess { sucesso = "Material excluído."; erro = null; recarregar() }
                                .onFailure { erro = it.message ?: "Não foi possível excluir." }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormularioMaterial(
    inicial: MaterialResponse?,
    aoSalvar: suspend (MaterialRequest) -> Result<MaterialResponse>,
    aoCancelar: () -> Unit
) {
    val escopo = rememberCoroutineScope()
    var nome by remember { mutableStateOf(inicial?.nome ?: "") }
    var descricao by remember { mutableStateOf(inicial?.descricao ?: "") }
    var quantidade by remember { mutableStateOf(inicial?.quantidade?.toString() ?: "") }
    var custo by remember { mutableStateOf(inicial?.custo?.toString()?.replace('.', ',') ?: "") }
    var disponivel by remember { mutableStateOf(inicial?.disponivel ?: true) }
    var erros by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var erroApi by remember { mutableStateOf<String?>(null) }
    var salvando by remember { mutableStateOf(false) }

    PaginaGestao(
        titulo = if (inicial == null) "Novo material" else "Editar material",
        subtitulo = "Dados do material."
    ) {
        AvisoTela(erroApi, erro = true)
        CampoComErro(nome, { nome = it }, "Nome", erros["nome"])
        CampoComErro(descricao, { descricao = it }, "Descrição (opcional)", erros["descricao"])
        CampoComErro(quantidade, { quantidade = it }, "Quantidade", erros["quantidade"], "0")
        CampoComErro(custo, { custo = it }, "Custo (R$)", erros["custo"], "0,00")
        LinhaInterruptor("Disponível", disponivel) { disponivel = it }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            BotaoPrincipal("Salvar", habilitado = !salvando, modifier = Modifier.weight(1f)) {
                val qtd = quantidade.trim().toIntOrNull()
                val valor = custo.trim().replace(',', '.').toDoubleOrNull()
                erros = buildMap {
                    if (nome.trim().length !in 2..150) put("nome", "O nome deve ter entre 2 e 150 caracteres")
                    if (descricao.length > 500) put("descricao", "Máximo de 500 caracteres")
                    if (qtd == null || qtd < 0) put("quantidade", "Informe uma quantidade válida")
                    if (valor == null || valor < 0) put("custo", "Informe um custo válido")
                }
                if (erros.isEmpty()) {
                    escopo.launch {
                        salvando = true
                        erroApi = null
                        aoSalvar(MaterialRequest(nome.trim(), descricao.trim().ifBlank { null }, qtd!!, valor!!, disponivel))
                            .onFailure { erroApi = it.message ?: "Não foi possível salvar." }
                        salvando = false
                    }
                }
            }
            BotaoSecundario("Cancelar", habilitado = !salvando, modifier = Modifier.weight(1f), aoClicar = aoCancelar)
        }
    }
}
