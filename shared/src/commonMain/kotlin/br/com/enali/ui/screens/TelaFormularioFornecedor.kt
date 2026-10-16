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
import androidx.compose.ui.unit.dp
import br.com.enali.model.Fornecedor
import br.com.enali.ui.components.CampoTextoCustomizado
import br.com.enali.ui.theme.CorBranco
import br.com.enali.ui.theme.CorMarromEscuro
import kotlin.random.Random

@Composable
fun TelaFormularioFornecedor(
    aoSalvar: (Fornecedor) -> Unit
) {
    var nomeFornecedor by remember { mutableStateOf("") }
    var cnpjFornecedor by remember { mutableStateOf("") }
    var emailFornecedor by remember { mutableStateOf("") }
    var telefoneFornecedor by remember { mutableStateOf("") }
    var estaAtivo by remember { mutableStateOf(true) }

    var erroNome by remember { mutableStateOf<String?>(null) }
    var erroCnpj by remember { mutableStateOf<String?>(null) }
    var erroEmail by remember { mutableStateOf<String?>(null) }
    var erroTelefone by remember { mutableStateOf<String?>(null) }
    var mensagemSucesso by remember { mutableStateOf<String?>(null) }

    val regexCnpj = Regex("""^\d{2}\.?\d{3}\.?\d{3}/?\d{4}-?\d{2}$""")
    val regexTelefone = Regex("""^\(?\d{2}\)?\s?\d{4,5}-?\d{4}$""")
    val regexEmail = Regex("""^[\w.+-]+@[\w-]+(\.[\w-]+)+$""")

    val limparCampos = {
        nomeFornecedor = ""
        cnpjFornecedor = ""
        emailFornecedor = ""
        telefoneFornecedor = ""
        estaAtivo = true
        erroNome = null
        erroCnpj = null
        erroEmail = null
        erroTelefone = null
    }

    val gravarFornecedor = {
        erroNome = when {
            nomeFornecedor.isBlank() -> "O nome é obrigatório"
            nomeFornecedor.trim().length < 2 || nomeFornecedor.trim().length > 150 ->
                "O nome deve ter entre 2 e 150 caracteres"
            else -> null
        }

        erroCnpj = when {
            cnpjFornecedor.isBlank() -> "O CNPJ é obrigatório"
            !regexCnpj.matches(cnpjFornecedor.trim()) ->
                "Informe um CNPJ válido, como 12.345.678/0001-90"
            else -> null
        }

        erroEmail = when {
            emailFornecedor.isBlank() -> "O e-mail é obrigatório"
            emailFornecedor.length > 254 -> "O e-mail deve ter no máximo 254 caracteres"
            !regexEmail.matches(emailFornecedor.trim()) -> "Informe um e-mail válido"
            else -> null
        }

        erroTelefone = when {
            telefoneFornecedor.isBlank() -> "O telefone é obrigatório"
            !regexTelefone.matches(telefoneFornecedor.trim()) ->
                "Informe um telefone válido, como (11) 91234-5678"
            else -> null
        }

        if (
            erroNome == null &&
            erroCnpj == null &&
            erroEmail == null &&
            erroTelefone == null
        ) {
            val novoFornecedor = Fornecedor(
                id = Random.nextLong(1000L, 999999L),
                nome = nomeFornecedor.trim(),
                cnpj = cnpjFornecedor.filter { it.isDigit() },
                email = emailFornecedor.trim().lowercase(),
                telefone = telefoneFornecedor.filter { it.isDigit() },
                ativo = estaAtivo
            )

            aoSalvar(novoFornecedor)
            mensagemSucesso = "Fornecedor cadastrado com sucesso!"
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
                text = "Formulário de Cadastro de Fornecedor",
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
                value = nomeFornecedor,
                onValueChange = {
                    nomeFornecedor = it
                    erroNome = null
                },
                label = "Nome do Fornecedor",
                placeholder = "Tecidos Brasil Ltda",
                isError = erroNome != null
            )

            erroNome?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            CampoTextoCustomizado(
                value = cnpjFornecedor,
                onValueChange = {
                    cnpjFornecedor = it
                    erroCnpj = null
                },
                label = "CNPJ",
                placeholder = "12.345.678/0001-90",
                isError = erroCnpj != null
            )

            erroCnpj?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            CampoTextoCustomizado(
                value = emailFornecedor,
                onValueChange = {
                    emailFornecedor = it
                    erroEmail = null
                },
                label = "E-mail",
                placeholder = "contato@fornecedor.com",
                isError = erroEmail != null
            )

            erroEmail?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            CampoTextoCustomizado(
                value = telefoneFornecedor,
                onValueChange = {
                    telefoneFornecedor = it
                    erroTelefone = null
                },
                label = "Telefone",
                placeholder = "(11) 91234-5678",
                isError = erroTelefone != null
            )

            erroTelefone?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Fornecedor Ativo?")
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
                    onClick = gravarFornecedor,
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
