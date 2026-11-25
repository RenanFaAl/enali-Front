package br.com.enali.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import br.com.enali.network.dto.EnderecoResponse
import br.com.enali.network.dto.FornecedorRequest
import br.com.enali.network.dto.FornecedorResponse
import br.com.enali.ui.components.*
import br.com.enali.ui.theme.CorMarromEscuro
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TelaFornecedores(apiClient: EnaliApiClient, token: String) {
    val api = apiClient.fornecedor
    val escopo = rememberCoroutineScope()
    var fornecedores by remember { mutableStateOf<List<FornecedorResponse>>(emptyList()) }
    var carregando by remember { mutableStateOf(true) }
    var erro by remember { mutableStateOf<String?>(null) }
    var sucesso by remember { mutableStateOf<String?>(null) }
    var busca by remember { mutableStateOf("") }
    var formularioAberto by remember { mutableStateOf(false) }
    var emEdicao by remember { mutableStateOf<FornecedorResponse?>(null) }
    var enderecosDe by remember { mutableStateOf<FornecedorResponse?>(null) }
    var enderecosPorFornecedor by remember { mutableStateOf<Map<Long, List<EnderecoResponse>>>(emptyMap()) }

    suspend fun recarregar() {
        carregando = true
        api.listar(token)
            .onSuccess { lista ->
                fornecedores = lista.sortedBy { f -> f.nome.lowercase() }; erro = null
                // shortcut: uma chamada por fornecedor, trocar por endpoint único se a lista crescer
                enderecosPorFornecedor = lista.associate { f ->
                    f.id to apiClient.endereco.listarDoFornecedor(token, f.id).getOrDefault(emptyList())
                }
            }
            .onFailure { erro = it.message ?: "Não foi possível carregar os fornecedores." }
        carregando = false
    }

    LaunchedEffect(token) { recarregar() }

    enderecosDe?.let { fornecedor ->
        TelaEnderecos(
            titulo = "Endereços de ${fornecedor.nome}",
            subtitulo = "CNPJ ${fornecedor.cnpj}",
            chave = fornecedor.id,
            listar = { apiClient.endereco.listarDoFornecedor(token, fornecedor.id) },
            salvar = { id, request ->
                if (id == null) apiClient.endereco.cadastrarDoFornecedor(token, fornecedor.id, request)
                else apiClient.endereco.atualizarDoFornecedor(token, fornecedor.id, id, request)
            },
            excluir = { id -> apiClient.endereco.excluirDoFornecedor(token, fornecedor.id, id) },
            aoVoltar = { enderecosDe = null; escopo.launch { recarregar() } }
        )
        return
    }

    if (formularioAberto) {
        FormularioFornecedor(
            inicial = emEdicao,
            aoSalvar = { request ->
                val alvo = emEdicao
                (if (alvo == null) api.cadastrar(token, request) else api.atualizar(token, alvo.id, request))
                    .onSuccess { salvo ->
                        sucesso = if (alvo == null) "Fornecedor cadastrado. Agora cadastre o endereço." else "Fornecedor atualizado."
                        recarregar() // antes de fechar: o formulário sai da tela e cancela o escopo dele
                        formularioAberto = false
                        emEdicao = null
                        if (alvo == null) enderecosDe = salvo // novo fornecedor já segue para os endereços
                    }
            },
            aoCancelar = { formularioAberto = false; emEdicao = null }
        )
        return
    }

    val filtrados = fornecedores.filter {
        busca.isBlank() ||
            it.nome.contains(busca.trim(), ignoreCase = true) ||
            it.cnpj.contains(busca.trim())
    }

    PaginaGestao(
        titulo = "Fornecedores",
        subtitulo = "Cadastro de fornecedores e seus endereços.",
        acao = { BotaoPrincipal("Novo fornecedor") { sucesso = null; emEdicao = null; formularioAberto = true } }
    ) {
        OutlinedTextField(
            value = busca,
            onValueChange = { busca = it },
            label = { Text("Buscar por nome ou CNPJ") },
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
            if (fornecedores.isEmpty()) "Nenhum fornecedor cadastrado." else "Nenhum fornecedor encontrado."
        )

        filtrados.forEach { fornecedor ->
            CartaoItem {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(fornecedor.nome, fontWeight = FontWeight.SemiBold, color = CorMarromEscuro)
                    Etiqueta(if (fornecedor.ativo) "Ativo" else "Inativo", fornecedor.ativo)
                }
                Text(
                    "CNPJ ${fornecedor.cnpj}  |  ${fornecedor.email}  |  ${fornecedor.telefone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CorTextoSuave
                )
                val enderecos = enderecosPorFornecedor[fornecedor.id].orEmpty()
                if (enderecos.isEmpty()) {
                    Text("Nenhum endereço cadastrado.", style = MaterialTheme.typography.bodySmall, color = CorTextoSuave)
                } else {
                    enderecos.forEach {
                        Text(
                            "Endereço: ${it.resumo()}" + if (it.principal) " (principal)" else "",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                // FlowRow: no celular os botões descem para a linha de baixo em vez de sair da tela
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BotaoSecundario("Editar") { sucesso = null; emEdicao = fornecedor; formularioAberto = true }
                    BotaoSecundario("Endereços") { sucesso = null; enderecosDe = fornecedor }
                    BotaoSecundario(if (fornecedor.ativo) "Desativar" else "Ativar") {
                        escopo.launch {
                            api.alterarAtivo(token, fornecedor.id, !fornecedor.ativo)
                                .onSuccess { sucesso = null; recarregar() }
                                .onFailure { erro = it.message }
                        }
                    }
                    BotaoExcluir {
                        escopo.launch {
                            api.excluir(token, fornecedor.id)
                                .onSuccess { sucesso = "Fornecedor excluído."; erro = null; recarregar() }
                                .onFailure { erro = it.message ?: "Não foi possível excluir." }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormularioFornecedor(
    inicial: FornecedorResponse?,
    aoSalvar: suspend (FornecedorRequest) -> Result<FornecedorResponse>,
    aoCancelar: () -> Unit
) {
    val escopo = rememberCoroutineScope()
    var nome by remember { mutableStateOf(inicial?.nome ?: "") }
    var cnpj by remember { mutableStateOf(inicial?.cnpj ?: "") }
    var email by remember { mutableStateOf(inicial?.email ?: "") }
    var telefone by remember { mutableStateOf(inicial?.telefone ?: "") }
    var erros by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var erroApi by remember { mutableStateOf<String?>(null) }
    var salvando by remember { mutableStateOf(false) }

    PaginaGestao(
        titulo = if (inicial == null) "Novo fornecedor" else "Editar fornecedor",
        subtitulo = "Dados do fornecedor."
    ) {
        AvisoTela(erroApi, erro = true)
        CampoComErro(nome, { nome = it }, "Nome", erros["nome"])
        CampoComErro(cnpj, { cnpj = it }, "CNPJ", erros["cnpj"], "00.000.000/0000-00")
        CampoComErro(email, { email = it }, "E-mail", erros["email"])
        CampoComErro(telefone, { telefone = it }, "Telefone", erros["telefone"], "(11) 99999-9999")

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            BotaoPrincipal("Salvar", habilitado = !salvando, modifier = Modifier.weight(1f)) {
                // Mesmas regras do FornecedorRequestDTO do backend
                erros = buildMap {
                    if (nome.trim().length !in 2..150) put("nome", "O nome deve ter entre 2 e 150 caracteres")
                    if (!Regex("""^\d{2}\.?\d{3}\.?\d{3}/?\d{4}-?\d{2}$""").matches(cnpj.trim())) put("cnpj", "CNPJ inválido")
                    if (!Regex("""^[^@\s]+@[^@\s]+\.[^@\s]+$""").matches(email.trim())) put("email", "E-mail inválido")
                    if (!Regex("""^\(?\d{2}\)?\s?\d{4,5}-?\d{4}$""").matches(telefone.trim())) put("telefone", "Telefone inválido")
                }
                if (erros.isEmpty()) {
                    escopo.launch {
                        salvando = true
                        erroApi = null
                        aoSalvar(FornecedorRequest(nome.trim(), cnpj.trim(), email.trim(), telefone.trim()))
                            .onFailure { erroApi = it.message ?: "Não foi possível salvar." }
                        salvando = false
                    }
                }
            }
            BotaoSecundario("Cancelar", habilitado = !salvando, modifier = Modifier.weight(1f), aoClicar = aoCancelar)
        }
    }
}
