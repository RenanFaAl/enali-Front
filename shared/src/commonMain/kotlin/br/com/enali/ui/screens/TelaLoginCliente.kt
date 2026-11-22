package br.com.enali.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import br.com.enali.auth.SessaoUsuario
import br.com.enali.network.EnaliApiClient
import br.com.enali.ui.components.CampoTextoCustomizado
import br.com.enali.ui.theme.CorBranco
import br.com.enali.ui.theme.CorMarromEscuro
import kotlinx.coroutines.launch

@Composable
fun TelaLoginCliente(
    apiClient: EnaliApiClient,
    aoLoginSucesso: (SessaoUsuario.ClienteLogado) -> Unit,
    aoIrParaCadastro: () -> Unit,
    aoIrParaLoginAdmin: () -> Unit
) {
    val escopo = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var erroEmail by remember { mutableStateOf<String?>(null) }
    var erroSenha by remember { mutableStateOf<String?>(null) }
    var erroGeral by remember { mutableStateOf<String?>(null) }
    var carregando by remember { mutableStateOf(false) }

    val submeterLogin = {
        erroEmail = if (email.isBlank()) "Informe o e-mail" else null
        erroSenha = if (senha.isBlank()) "Informe a senha" else null
        erroGeral = null

        if (erroEmail == null && erroSenha == null && !carregando) {
            carregando = true
            escopo.launch {
                val resultado = apiClient.loginCliente(email, senha)
                carregando = false
                resultado
                    .onSuccess { resposta ->
                        val sessao = SessaoUsuario.fromLoginResponse(resposta)
                        if (sessao is SessaoUsuario.ClienteLogado) {
                            aoLoginSucesso(sessao)
                        } else {
                            erroGeral = "Perfil retornado não corresponde a Cliente."
                        }
                    }
                    .onFailure { erro ->
                        erroGeral = erro.message ?: "Falha ao realizar login."
                    }
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(CorBranco)
            .padding(24.dp)
    ) {
        val larguraMaxima = if (maxWidth > 520.dp) 440.dp else maxWidth

        Column(
            modifier = Modifier
                .widthIn(max = larguraMaxima)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Entrar na Conta",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = CorMarromEscuro
            )

            if (erroGeral != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = erroGeral ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            CampoTextoCustomizado(
                value = email,
                onValueChange = { email = it; erroEmail = null; erroGeral = null },
                label = "E-mail",
                placeholder = "cliente@exemplo.com",
                isError = erroEmail != null
            )
            erroEmail?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            CampoTextoCustomizado(
                value = senha,
                onValueChange = { senha = it; erroSenha = null; erroGeral = null },
                label = "Senha",
                placeholder = "Digite sua senha",
                isError = erroSenha != null,
                visualTransformation = PasswordVisualTransformation()
            )
            erroSenha?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = submeterLogin,
                enabled = !carregando,
                colors = ButtonDefaults.buttonColors(containerColor = CorMarromEscuro),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (carregando) {
                    CircularProgressIndicator(
                        color = CorBranco,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text("Entrar", color = CorBranco)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ainda não tem conta?", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = aoIrParaCadastro, enabled = !carregando) {
                    Text("Cadastrar", color = CorMarromEscuro, fontWeight = FontWeight.SemiBold)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            TextButton(
                onClick = aoIrParaLoginAdmin,
                enabled = !carregando,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "Acesso administrativo",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}