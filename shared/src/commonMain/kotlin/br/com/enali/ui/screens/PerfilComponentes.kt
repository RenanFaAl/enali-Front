package br.com.enali.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import br.com.enali.network.dto.AlterarSenhaRequest
import br.com.enali.ui.components.CampoTextoCustomizado
import br.com.enali.ui.theme.CorBranco
import br.com.enali.ui.theme.CorMarromEscuro
import kotlinx.coroutines.launch

@Composable
internal fun LinhaInfoPerfil(rotulo: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$rotulo:",
            fontWeight = FontWeight.SemiBold,
            color = CorMarromEscuro
        )
        Text(
            text = valor,
            color = CorMarromEscuro
        )
    }
}

@Composable
internal fun DialogAlterarSenha(
    aoFechar: () -> Unit,
    aoConfirmar: suspend (AlterarSenhaRequest) -> Result<Unit>
) {
    val escopo = rememberCoroutineScope()
    var senhaAtual by remember { mutableStateOf("") }
    var senhaNova by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var erroForm by remember { mutableStateOf<String?>(null) }
    var salvando by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!salvando) aoFechar() },
        title = { Text("Alterar Senha", color = CorMarromEscuro) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    value = senhaAtual,
                    onValueChange = { senhaAtual = it; erroForm = null },
                    label = "Senha Atual",
                    placeholder = "Digite a senha atual",
                    visualTransformation = PasswordVisualTransformation()
                )
                CampoTextoCustomizado(
                    value = senhaNova,
                    onValueChange = { senhaNova = it; erroForm = null },
                    label = "Nova Senha",
                    placeholder = "Digite a nova senha",
                    visualTransformation = PasswordVisualTransformation()
                )
                CampoTextoCustomizado(
                    value = confirmarSenha,
                    onValueChange = { confirmarSenha = it; erroForm = null },
                    label = "Confirmar Nova Senha",
                    placeholder = "Repita a nova senha",
                    visualTransformation = PasswordVisualTransformation()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = !salvando,
                colors = ButtonDefaults.buttonColors(containerColor = CorMarromEscuro),
                onClick = {
                    if (senhaAtual.isBlank() || senhaNova.isBlank() || confirmarSenha.isBlank()) {
                        erroForm = "Preencha todos os campos de senha."
                        return@Button
                    }
                    if (senhaNova != confirmarSenha) {
                        erroForm = "A nova senha e a confirmação não coincidem."
                        return@Button
                    }
                    salvando = true
                    escopo.launch {
                        val req = AlterarSenhaRequest(
                            senhaAtual = senhaAtual,
                            senhaNova = senhaNova,
                            confirmarSenha = confirmarSenha
                        )
                        aoConfirmar(req)
                            .onSuccess { aoFechar() }
                            .onFailure { erro ->
                                erroForm = erro.message ?: "Erro ao alterar senha."
                            }
                        salvando = false
                    }
                }
            ) {
                Text(if (salvando) "Salvando..." else "Alterar", color = CorBranco)
            }
        },
        dismissButton = {
            TextButton(
                enabled = !salvando,
                onClick = aoFechar
            ) {
                Text("Cancelar")
            }
        }
    )
}