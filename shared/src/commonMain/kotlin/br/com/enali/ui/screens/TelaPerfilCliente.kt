package br.com.enali.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.enali.network.EnaliApiClient
import br.com.enali.network.dto.ClienteAtualizarRequest
import br.com.enali.network.dto.ClientePerfilResponse
import br.com.enali.ui.components.CampoTextoCustomizado
import br.com.enali.ui.theme.CorBegeMedio
import br.com.enali.ui.theme.CorBranco
import br.com.enali.ui.theme.CorMarromEscuro
import kotlinx.coroutines.launch

@Composable
fun TelaPerfilCliente(
    apiClient: EnaliApiClient,
    token: String,
    nomeInicial: String,
    emailInicial: String,
    aoAtualizarNomeSessao: (String) -> Unit = {}
) {
    val escopo = rememberCoroutineScope()

    var perfil by remember {
        mutableStateOf(
            ClientePerfilResponse(
                id = 0L,
                nome = nomeInicial,
                email = emailInicial,
                cpf = "",
                dataNascimento = "",
                ativo = true
            )
        )
    }
    var carregandoPerfil by remember { mutableStateOf(false) }
    var erroTela by remember { mutableStateOf<String?>(null) }
    var mensagemSucesso by remember { mutableStateOf<String?>(null) }

    var abrirDialogDados by remember { mutableStateOf(false) }
    var abrirDialogSenha by remember { mutableStateOf(false) }

    LaunchedEffect(token) {
        if (token.isBlank()) return@LaunchedEffect
        carregandoPerfil = true
        erroTela = null
        apiClient.buscarPerfilCliente(token)
            .onSuccess { dados ->
                perfil = dados
                aoAtualizarNomeSessao(dados.nome)
            }
            .onFailure { erro ->
                erroTela = erro.message ?: "Não foi possível carregar os dados do perfil."
            }
        carregandoPerfil = false
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(CorBranco)
            .padding(16.dp)
    ) {
        val larguraMaxima = if (maxWidth > 600.dp) 600.dp else maxWidth

        Column(
            modifier = Modifier
                .widthIn(max = larguraMaxima)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Meu Perfil",
                style = MaterialTheme.typography.headlineSmall,
                color = CorMarromEscuro,
                fontWeight = FontWeight.Bold
            )

            erroTela?.let { erro ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = erro,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            mensagemSucesso?.let { msg ->
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (carregandoPerfil) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = CorMarromEscuro
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = CorBegeMedio.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinhaInfoPerfil("Nome", perfil.nome)
                    LinhaInfoPerfil("E-mail", perfil.email)
                    LinhaInfoPerfil("CPF", perfil.cpf.ifBlank { "—" })
                    LinhaInfoPerfil("Data de Nascimento", perfil.dataNascimento.ifBlank { "—" })
                    LinhaInfoPerfil("Status", if (perfil.ativo) "Ativo" else "Inativo")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { mensagemSucesso = null; abrirDialogDados = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CorMarromEscuro),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Atualizar Dados", color = CorBranco)
                }

                OutlinedButton(
                    onClick = { mensagemSucesso = null; abrirDialogSenha = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Alterar Senha", color = CorMarromEscuro)
                }
            }
        }
    }

    if (abrirDialogDados) {
        var nome by remember { mutableStateOf(perfil.nome) }
        var email by remember { mutableStateOf(perfil.email) }
        var cpf by remember { mutableStateOf(perfil.cpf) }
        var dataNascimento by remember { mutableStateOf(perfil.dataNascimento) }
        var erroForm by remember { mutableStateOf<String?>(null) }
        var salvando by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!salvando) abrirDialogDados = false },
            title = { Text("Atualizar Dados", color = CorMarromEscuro) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    erroForm?.let {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                    CampoTextoCustomizado(
                        value = nome,
                        onValueChange = { nome = it; erroForm = null },
                        label = "Nome",
                        placeholder = "Seu nome"
                    )
                    CampoTextoCustomizado(
                        value = email,
                        onValueChange = { email = it; erroForm = null },
                        label = "E-mail",
                        placeholder = "email@exemplo.com"
                    )
                    CampoTextoCustomizado(
                        value = cpf,
                        onValueChange = { cpf = it.filter(Char::isDigit).take(11); erroForm = null },
                        label = "CPF",
                        placeholder = "Somente números"
                    )
                    CampoTextoCustomizado(
                        value = dataNascimento,
                        onValueChange = { dataNascimento = it; erroForm = null },
                        label = "Data de Nascimento",
                        placeholder = "AAAA-MM-DD"
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = !salvando,
                    colors = ButtonDefaults.buttonColors(containerColor = CorMarromEscuro),
                    onClick = {
                        if (nome.isBlank() || email.isBlank() || cpf.length != 11 || dataNascimento.isBlank()) {
                            erroForm = "Preencha nome, e-mail, CPF (11 dígitos) e data (AAAA-MM-DD)."
                            return@Button
                        }
                        salvando = true
                        escopo.launch {
                            val req = ClienteAtualizarRequest(
                                nome = nome.trim(),
                                email = email.trim(),
                                cpf = cpf.trim(),
                                dataNascimento = dataNascimento.trim()
                            )
                            apiClient.atualizarPerfilCliente(token, req)
                                .onSuccess { atualizado ->
                                    perfil = atualizado
                                    aoAtualizarNomeSessao(atualizado.nome)
                                    mensagemSucesso = "Dados atualizados com sucesso!"
                                    abrirDialogDados = false
                                }
                                .onFailure { erro ->
                                    erroForm = erro.message ?: "Erro ao atualizar dados."
                                }
                            salvando = false
                        }
                    }
                ) {
                    Text(if (salvando) "Salvando..." else "Salvar", color = CorBranco)
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !salvando,
                    onClick = { abrirDialogDados = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (abrirDialogSenha) {
        DialogAlterarSenha(
            aoFechar = { abrirDialogSenha = false },
            aoConfirmar = { req ->
                apiClient.alterarSenhaCliente(token, req).map {
                    mensagemSucesso = "Senha alterada com sucesso!"
                }
            }
        )
    }
}