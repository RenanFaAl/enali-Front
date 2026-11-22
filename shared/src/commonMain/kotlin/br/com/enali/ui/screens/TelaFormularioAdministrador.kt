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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import br.com.enali.model.Administrador
import br.com.enali.network.EnaliApiClient
import br.com.enali.network.dto.AdminCadastroRequest
import br.com.enali.ui.components.CampoTextoCustomizado
import br.com.enali.ui.theme.CorBranco
import br.com.enali.ui.theme.CorMarromEscuro
import kotlinx.coroutines.launch

@Composable
fun TelaFormularioAdministrador(
    apiClient: EnaliApiClient,
    tokenAdmin: String,
    isAdminMaster: Boolean,
    aoSalvar: (Administrador) -> Unit = {}
) {
    val escopo = rememberCoroutineScope()

    var nomeAdministrador by remember { mutableStateOf("") }
    var emailAdministrador by remember { mutableStateOf("") }
    var senhaAdministrador by remember { mutableStateOf("") }

    var erroNome by remember { mutableStateOf<String?>(null) }
    var erroEmail by remember { mutableStateOf<String?>(null) }
    var erroSenha by remember { mutableStateOf<String?>(null) }
    var erroApi by remember { mutableStateOf<String?>(null) }
    var mensagemSucesso by remember { mutableStateOf<String?>(null) }
    var carregando by remember { mutableStateOf(false) }

    val regexEmail = remember { Regex("""^[\w.+-]+@[\w-]+(\.[\w-]+)+$""") }
    val regexSenhaForte = remember {
        Regex("""^(?=\S*[A-Z])(?=\S*[a-z])(?=\S*\d)(?=\S*[^A-Za-z0-9\s])\S+$""")
    }

    val limparCampos = {
        nomeAdministrador = ""
        emailAdministrador = ""
        senhaAdministrador = ""
        erroNome = null
        erroEmail = null
        erroSenha = null
        erroApi = null
    }

    val gravarAdministrador = {
        if (!isAdminMaster) {
            erroApi = "Apenas um Admin Master pode cadastrar novos administradores."
        } else {
            erroNome = when {
                nomeAdministrador.isBlank() -> "O nome é obrigatório"
                nomeAdministrador.trim().length < 2 || nomeAdministrador.trim().length > 150 ->
                    "O nome deve ter entre 2 e 150 caracteres"
                else -> null
            }

            erroEmail = when {
                emailAdministrador.isBlank() -> "O e-mail é obrigatório"
                emailAdministrador.trim().length > 254 -> "O e-mail deve ter no máximo 254 caracteres"
                !regexEmail.matches(emailAdministrador.trim()) -> "Informe um e-mail válido"
                else -> null
            }

            erroSenha = when {
                senhaAdministrador.isBlank() -> "A senha é obrigatória"
                senhaAdministrador.length < 8 || senhaAdministrador.length > 254 ->
                    "A senha deve ter entre 8 e 254 caracteres"
                !regexSenhaForte.matches(senhaAdministrador) ->
                    "Use maiúscula, minúscula, número e caractere especial, sem espaços"
                else -> null
            }

            erroApi = null
            mensagemSucesso = null

            if (erroNome == null && erroEmail == null && erroSenha == null && !carregando) {
                carregando = true
                val request = AdminCadastroRequest(
                    nome = nomeAdministrador.trim(),
                    email = emailAdministrador.trim().lowercase(),
                    senha = senhaAdministrador
                )
                escopo.launch {
                    val resultado = apiClient.criarAdministrador(token = tokenAdmin, request = request)
                    carregando = false
                    resultado
                        .onSuccess { resposta ->
                            val adminSalvo = Administrador(
                                id = resposta.id,
                                nome = resposta.nome,
                                email = resposta.email,
                                senha = "",
                                ativo = resposta.ativo,
                                nivelAcesso = resposta.nivelAcesso,
                                dataCriacao = resposta.dataCriacao ?: ""
                            )
                            aoSalvar(adminSalvo)
                            mensagemSucesso = "Administrador ${resposta.nome} (${resposta.nivelAcesso}) cadastrado com sucesso!"
                            limparCampos()
                        }
                        .onFailure { erro ->
                            erroApi = erro.message ?: "Não foi possível cadastrar o administrador."
                        }
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(CorBranco)
            .padding(16.dp)
    ) {
        val larguraMaxima = if (maxWidth > 600.dp) 550.dp else maxWidth

        Column(
            modifier = Modifier
                .widthIn(max = larguraMaxima)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .align(Alignment.TopCenter)
        ) {
            Text(
                text = "Cadastro de Administrador",
                style = MaterialTheme.typography.titleLarge,
                color = CorMarromEscuro
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (!isAdminMaster) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Acesso restrito ao Admin Master.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            erroApi?.let { erro ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = erro,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            mensagemSucesso?.let { sucesso ->
                Surface(
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = sucesso,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            CampoTextoCustomizado(
                value = nomeAdministrador,
                onValueChange = { nomeAdministrador = it; erroNome = null; erroApi = null },
                label = "Nome Completo",
                placeholder = "Nome completo",
                isError = erroNome != null
            )
            erroNome?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            CampoTextoCustomizado(
                value = emailAdministrador,
                onValueChange = { emailAdministrador = it; erroEmail = null; erroApi = null },
                label = "E-mail",
                placeholder = "admin@exemplo.com",
                isError = erroEmail != null
            )
            erroEmail?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            CampoTextoCustomizado(
                value = senhaAdministrador,
                onValueChange = { senhaAdministrador = it; erroSenha = null; erroApi = null },
                label = "Senha",
                placeholder = "Maiúscula, minúscula, número e símbolo",
                visualTransformation = PasswordVisualTransformation(),
                isError = erroSenha != null
            )
            erroSenha?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = gravarAdministrador,
                    enabled = !carregando && isAdminMaster,
                    colors = ButtonDefaults.buttonColors(containerColor = CorMarromEscuro),
                    modifier = Modifier.weight(1f)
                ) {
                    if (carregando) {
                        CircularProgressIndicator(
                            color = CorBranco,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Text("Gravar", color = CorBranco)
                    }
                }

                OutlinedButton(
                    onClick = { limparCampos(); mensagemSucesso = null },
                    enabled = !carregando,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Limpar Campos")
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}