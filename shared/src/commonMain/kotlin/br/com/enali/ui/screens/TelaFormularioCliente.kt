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
import br.com.enali.ui.components.CampoTextoCustomizado
import br.com.enali.ui.theme.CorBranco
import br.com.enali.ui.theme.CorMarromEscuro
import kotlin.random.Random

@Composable
fun TelaFormularioCliente(
    aoSalvar: (Cliente) -> Unit
) {
    var nomeCliente by remember { mutableStateOf("") }
    var emailCliente by remember { mutableStateOf("") }
    var senhaCliente by remember { mutableStateOf("") }
    var cpfCliente by remember { mutableStateOf("") }
    var dataNascimentoCliente by remember { mutableStateOf("") }
    var estaAtivo by remember { mutableStateOf(true) }

    var erroNome by remember { mutableStateOf<String?>(null) }
    var erroEmail by remember { mutableStateOf<String?>(null) }
    var erroSenha by remember { mutableStateOf<String?>(null) }
    var erroCpf by remember { mutableStateOf<String?>(null) }
    var erroDataNascimento by remember { mutableStateOf<String?>(null) }
    var mensagemSucesso by remember { mutableStateOf<String?>(null) }

    val regexEmail = Regex("""^[\w.+-]+@[\w-]+(\.[\w-]+)+$""")
    val regexData = Regex("""^\d{4}-\d{2}-\d{2}$""")

    val limparCampos = {
        nomeCliente = ""
        emailCliente = ""
        senhaCliente = ""
        cpfCliente = ""
        dataNascimentoCliente = ""
        estaAtivo = true
        erroNome = null
        erroEmail = null
        erroSenha = null
        erroCpf = null
        erroDataNascimento = null
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
            emailCliente.length > 150 -> "O e-mail deve ter no máximo 150 caracteres"
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

        if (
            erroNome == null &&
            erroEmail == null &&
            erroSenha == null &&
            erroCpf == null &&
            erroDataNascimento == null
        ) {
            val novoCliente = Cliente(
                id = Random.nextLong(1000L, 999999L),
                nome = nomeCliente.trim(),
                email = emailCliente.trim().lowercase(),
                senha = senhaCliente,
                cpf = cpfCliente,
                dataNascimento = dataNascimentoCliente.trim(),
                ativo = estaAtivo
            )

            aoSalvar(novoCliente)
            mensagemSucesso = "Cliente cadastrado com sucesso!"
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
                text = "Formulário de Cadastro de Cliente",
                style = MaterialTheme.typography.titleLarge,
                color = CorMarromEscuro
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                onValueChange = {
                    nomeCliente = it
                    erroNome = null
                },
                label = "Nome do Cliente",
                placeholder = "Maria Silva",
                isError = erroNome != null
            )

            erroNome?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            CampoTextoCustomizado(
                value = emailCliente,
                onValueChange = {
                    emailCliente = it
                    erroEmail = null
                },
                label = "E-mail",
                placeholder = "maria@email.com",
                isError = erroEmail != null
            )

            erroEmail?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            CampoTextoCustomizado(
                value = senhaCliente,
                onValueChange = {
                    senhaCliente = it
                    erroSenha = null
                },
                label = "Senha",
                placeholder = "Mínimo de 8 caracteres",
                visualTransformation = PasswordVisualTransformation(),
                isError = erroSenha != null
            )

            erroSenha?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            CampoTextoCustomizado(
                value = cpfCliente,
                onValueChange = {
                    cpfCliente = it.filter(Char::isDigit).take(11)
                    erroCpf = null
                },
                label = "CPF",
                placeholder = "Somente números",
                isError = erroCpf != null
            )

            erroCpf?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            CampoTextoCustomizado(
                value = dataNascimentoCliente,
                onValueChange = {
                    dataNascimentoCliente = it
                    erroDataNascimento = null
                },
                label = "Data de Nascimento",
                placeholder = "AAAA-MM-DD",
                isError = erroDataNascimento != null
            )

            erroDataNascimento?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Cliente Ativo?")
                Switch(
                    checked = estaAtivo,
                    onCheckedChange = { estaAtivo = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = gravarCliente,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CorMarromEscuro
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Gravar", color = CorBranco)
                }

                OutlinedButton(
                    onClick = {
                        limparCampos()
                        mensagemSucesso = null
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Limpar Campos")
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
