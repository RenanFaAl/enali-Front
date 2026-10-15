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
import br.com.enali.ui.components.CampoTextoCustomizado
import br.com.enali.ui.theme.CorBranco
import br.com.enali.ui.theme.CorMarromEscuro
import kotlin.random.Random

@Composable
fun TelaFormularioAdministrador(
    aoSalvar: (Administrador) -> Unit
) {
    var nomeAdministrador by remember { mutableStateOf("") }
    var emailAdministrador by remember { mutableStateOf("") }
    var senhaAdministrador by remember { mutableStateOf("") }
    var nivelAcessoAdministrador by remember { mutableStateOf("ADMIN_COMUM") }
    var estaAtivo by remember { mutableStateOf(true) }
    var dataCriacaoAdministrador by remember { mutableStateOf("") }

    var erroNome by remember { mutableStateOf<String?>(null) }
    var erroEmail by remember { mutableStateOf<String?>(null) }
    var erroSenha by remember { mutableStateOf<String?>(null) }
    var erroNivelAcesso by remember { mutableStateOf<String?>(null) }
    var erroDataCriacao by remember { mutableStateOf<String?>(null) }
    var mensagemSucesso by remember { mutableStateOf<String?>(null) }

    val limparCampos = {
        nomeAdministrador = ""
        emailAdministrador = ""
        senhaAdministrador = ""
        nivelAcessoAdministrador = "ADMIN_COMUM"
        estaAtivo = true
        dataCriacaoAdministrador = ""
        erroNome = null
        erroEmail = null
        erroSenha = null
        erroNivelAcesso = null
        erroDataCriacao = null
    }

    val gravarAdministrador = {
        erroNome = if (nomeAdministrador.isBlank()) "O nome é obrigatório" else null
        erroEmail = if (emailAdministrador.isBlank()) "O e-mail é obrigatório" else null
        erroSenha = if (senhaAdministrador.isBlank()) "A senha é obrigatória" else null
        erroNivelAcesso = if (nivelAcessoAdministrador.isBlank()) "O nível de acesso é obrigatório" else null
        erroDataCriacao = if (dataCriacaoAdministrador.isBlank()) "A data de criação é obrigatória" else null

        if (erroNome == null && erroEmail == null && erroSenha == null && erroNivelAcesso == null && erroDataCriacao == null) {
            val novoAdministrador = Administrador(
                id = Random.nextLong(1000L, 999999L),
                nome = nomeAdministrador,
                email = emailAdministrador,
                senha = senhaAdministrador,
                ativo = estaAtivo,
                nivelAcesso = nivelAcessoAdministrador,
                dataCriacao = dataCriacaoAdministrador
            )
            aoSalvar(novoAdministrador)
            mensagemSucesso = "Administrador ${novoAdministrador.nome} criado com sucesso!"
            limparCampos()
        } else {
            mensagemSucesso = null
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
                text = "Formulário de Cadastro de Administrador",
                style = MaterialTheme.typography.titleLarge,
                color = CorMarromEscuro
            )
            Spacer(modifier = Modifier.height(16.dp))

            mensagemSucesso?.let { sucesso ->
                Surface(
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
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
                onValueChange = { nomeAdministrador = it; erroNome = null },
                label = "Nome Completo",
                placeholder = "Nome completo",
                isError = erroNome != null
            )
            erroNome?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            CampoTextoCustomizado(
                value = emailAdministrador,
                onValueChange = { emailAdministrador = it; erroEmail = null },
                label = "E-mail",
                placeholder = "email@exemplo.com",
                isError = erroEmail != null
            )
            erroEmail?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            CampoTextoCustomizado(
                value = senhaAdministrador,
                onValueChange = { senhaAdministrador = it; erroSenha = null },
                label = "Senha",
                placeholder = "Digite a senha",
                visualTransformation = PasswordVisualTransformation(),
                isError = erroSenha != null
            )
            erroSenha?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            CampoTextoCustomizado(
                value = nivelAcessoAdministrador,
                onValueChange = { nivelAcessoAdministrador = it; erroNivelAcesso = null },
                label = "Nível de Acesso",
                placeholder = "ADMIN_COMUM ou ADMIN_MASTER"
            )
            erroNivelAcesso?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            CampoTextoCustomizado(
                value = dataCriacaoAdministrador,
                onValueChange = { dataCriacaoAdministrador = it; erroDataCriacao = null },
                label = "Data de Criação",
                placeholder = "AAAA-MM-DDTHH:MM",
                isError = erroDataCriacao != null
            )
            erroDataCriacao?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Ativo?")
                Switch(checked = estaAtivo, onCheckedChange = { estaAtivo = it })
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = gravarAdministrador,
                    colors = ButtonDefaults.buttonColors(containerColor = CorMarromEscuro),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Gravar", color = CorBranco)
                }
                OutlinedButton(
                    onClick = { limparCampos(); mensagemSucesso = null },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Limpar Campos")
                }
            }
            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}