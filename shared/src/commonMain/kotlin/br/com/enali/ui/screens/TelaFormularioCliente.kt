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
import br.com.enali.model.Cliente
import br.com.enali.network.EnaliApiClient
import br.com.enali.network.dto.ClienteCadastroRequest
import br.com.enali.ui.components.CampoTextoCustomizado
import br.com.enali.ui.theme.CorBranco
import br.com.enali.ui.theme.CorMarromEscuro
import kotlinx.coroutines.launch

@Composable
fun TelaFormularioCliente(
    apiClient: EnaliApiClient,
    aoSalvar: (Cliente) -> Unit = {},
    aoVoltarLogin: (() -> Unit)? = null
) {
    val escopo = rememberCoroutineScope()

    var nomeCliente by remember { mutableStateOf("") }
    var emailCliente by remember { mutableStateOf("") }
    var senhaCliente by remember { mutableStateOf("") }
    var cpfCliente by remember { mutableStateOf("") }
    var dataNascimentoCliente by remember { mutableStateOf("") }

    var erroNome by remember { mutableStateOf<String?>(null) }
    var erroEmail by remember { mutableStateOf<String?>(null) }
    var erroSenha by remember { mutableStateOf<String?>(null) }
    var erroCpf by remember { mutableStateOf<String?>(null) }
    var erroDataNascimento by remember { mutableStateOf<String?>(null) }
    var erroApi by remember { mutableStateOf<String?>(null) }
    var mensagemSucesso by remember { mutableStateOf<String?>(null) }
    var carregando by remember { mutableStateOf(false) }

    val regexEmail = remember { Regex("""^[\w.+-]+@[\w-]+(\.[\w-]+)+$""") }
    val regexData = remember { Regex("""^\d{4}-\d{2}-\d{2}$""") }

    val limparCampos = {
        nomeCliente = ""
        emailCliente = ""
        senhaCliente = ""
        cpfCliente = ""
        dataNascimentoCliente = ""
        erroNome = null
        erroEmail = null
        erroSenha = null
        erroCpf = null
        erroDataNascimento = null
        erroApi = null
    }

    val gravarCliente = {
        erroNome = when {
            nomeCliente.isBlank() -> "O nome é obrigatório"
            nomeCliente.trim().length < 2 || nomeCliente.trim().length > 100 ->
                "O nome deve ter entre 2 e 100 caracteres"
            else -> null
        }

        erroEmail = when {
            emailCliente.isBlank() -> "O e-mail é obrigatório"
            emailCliente.trim().length > 150 -> "O e-mail deve ter no máximo 150 caracteres"
            !regexEmail.matches(emailCliente.trim()) -> "Informe um e-mail válido"
            else -> null
        }

        erroSenha = when {
            senhaCliente.isBlank() -> "A senha é obrigatória"
            senhaCliente.length < 8 || senhaCliente.length > 100 ->
                "A senha deve ter entre 8 e 100 caracteres"
            else -> null
        }

        erroCpf = when {
            cpfCliente.isBlank() -> "O CPF é obrigatório"
            cpfCliente.length != 11 -> "O CPF deve conter 11 números, sem pontos ou traços"
            else -> null
        }

        erroDataNascimento = when {
            dataNascimentoCliente.isBlank() -> "A data de nascimento é obrigatória"
            !regexData.matches(dataNascimentoCliente.trim()) -> "Use a data no formato AAAA-MM-DD"
            else -> null
        }

        erroApi = null
        mensagemSucesso = null

        if (
            erroNome == null &&
            erroEmail == null &&
            erroSenha == null &&
            erroCpf == null &&
            erroDataNascimento == null &&
            !carregando
        ) {
            carregando = true
            val request = ClienteCadastroRequest(
                nome = nomeCliente.trim(),
                email = emailCliente.trim().lowercase(),
                senha = senhaCliente,
                cpf = cpfCliente,
                dataNascimento = dataNascimentoCliente.trim()
            )
            escopo.launch {
                val resultado = apiClient.cadastrarCliente(request)
                carregando = false
                resultado
                    .onSuccess { resposta ->
                        val clienteSalvo = Cliente(
                            id = resposta.id,
                            nome = resposta.nome,
                            email = resposta.email,
                            senha = "",
                            cpf = resposta.cpf,
                            dataNascimento = resposta.dataNascimento,
                            ativo = resposta.ativo
                        )
                        aoSalvar(clienteSalvo)
                        mensagemSucesso = "Cliente ${resposta.nome} cadastrado com sucesso!"
                        limparCampos()
                    }
                    .onFailure { erro ->
                        erroApi = erro.message ?: "Não foi possível cadastrar o cliente."
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
                text = "Cadastro de Cliente",
                style = MaterialTheme.typography.titleLarge,
                color = CorMarromEscuro
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                value = nomeCliente,
                onValueChange = { nomeCliente = it; erroNome = null; erroApi = null },
                label = "Nome do Cliente",
                placeholder = "Maria Silva",
                isError = erroNome != null
            )
            erroNome?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            CampoTextoCustomizado(
                value = emailCliente,
                onValueChange = { emailCliente = it; erroEmail = null; erroApi = null },
                label = "E-mail",
                placeholder = "maria@email.com",
                isError = erroEmail != null
            )
            erroEmail?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            CampoTextoCustomizado(
                value = senhaCliente,
                onValueChange = { senhaCliente = it; erroSenha = null; erroApi = null },
                label = "Senha",
                placeholder = "Mínimo de 8 caracteres",
                visualTransformation = PasswordVisualTransformation(),
                isError = erroSenha != null
            )
            erroSenha?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            CampoTextoCustomizado(
                value = cpfCliente,
                onValueChange = { cpfCliente = it.filter(Char::isDigit).take(11); erroCpf = null; erroApi = null },
                label = "CPF",
                placeholder = "Somente números (11 dígitos)",
                isError = erroCpf != null
            )
            erroCpf?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            CampoTextoCustomizado(
                value = dataNascimentoCliente,
                onValueChange = { dataNascimentoCliente = it; erroDataNascimento = null; erroApi = null },
                label = "Data de Nascimento",
                placeholder = "AAAA-MM-DD",
                isError = erroDataNascimento != null
            )
            erroDataNascimento?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = gravarCliente,
                    enabled = !carregando,
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
                        Text("Cadastrar", color = CorBranco)
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

            if (aoVoltarLogin != null) {
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = aoVoltarLogin,
                    enabled = !carregando,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Já tem conta? Entrar", color = CorMarromEscuro)
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}