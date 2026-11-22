// shared/src/commonMain/kotlin/br/com/enali/ui/screens/TelaLoginAdmin.kt
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
fun TelaLoginAdmin(
    apiClient: EnaliApiClient,
    aoLoginSucesso: (SessaoUsuario.AdminLogado) -> Unit,
    aoVoltarCliente: () -> Unit
) {
    val escopo = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var erroEmail by remember { mutableStateOf<String?>(null) }
    var erroSenha by remember { mutableStateOf<String?>(null) }
    var erroGeral by remember { mutableStateOf<String?>(null) }
    var carregando by remember { mutableStateOf(false) }

    val submeterLogin = {
        erroEmail = if (email.isBlank()) "Informe o e-mail administrativo" else null
        erroSenha = if (senha.isBlank()) "Informe a senha" else null
        erroGeral = null

        if (erroEmail == null && erroSenha == null && !carregando) {
            carregando = true
            escopo.launch {
                val resultado = apiClient.loginAdmin(email, senha)
                carregando = false
                resultado
                    .onSuccess { resposta ->
                        val sessao = SessaoUsuario.fromLoginResponse(resposta)
                        if (sessao is SessaoUsuario.AdminLogado) {
                            aoLoginSucesso(sessao)
                        } else {
                            erroGeral = "Credenciais não pertencem a um administrador."
                        }
                    }
                    .onFailure { erro ->
                        erroGeral = erro.message ?: "Falha ao autenticar administrador."
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
                text = "Portal Administrativo",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = CorMarromEscuro
            )
            Text(
                text = "Acesso restrito a administradores do sistema.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                label = "E-mail corporativo",
                placeholder = "admin@enali.com.br",
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
                    Text("Acessar painel", color = CorBranco)
                }
            }

            TextButton(
                onClick = aoVoltarCliente,
                enabled = !carregando,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Voltar para login de cliente", color = CorMarromEscuro)
            }
        }
    }
}