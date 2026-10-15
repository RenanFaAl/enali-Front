
package br.com.enali.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import br.com.enali.model.Cliente

@Composable
fun TelaFormularioCliente(
    aoSalvar: (Cliente) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var dataNascimento by remember { mutableStateOf("") }
    var ativo by remember { mutableStateOf(true) }
    var mensagem by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Cadastro de Cliente",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = cpf,
            onValueChange = {
                cpf = it.filter(Char::isDigit).take(11)
            },
            label = { Text("CPF") },
            placeholder = { Text("Somente números") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = dataNascimento,
            onValueChange = { dataNascimento = it },
            label = { Text("Data de nascimento") },
            placeholder = { Text("AAAA-MM-DD") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(
                checked = ativo,
                onCheckedChange = { ativo = it }
            )
            Text("Cliente ativo")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    mensagem = when {
                        nome.isBlank() ->
                            "Informe o nome."

                        !email.contains("@") ->
                            "Informe um e-mail válido."

                        senha.length < 8 ->
                            "A senha deve ter pelo menos 8 caracteres."

                        cpf.length != 11 ->
                            "O CPF deve conter 11 dígitos."

                        !Regex("""\d{4}-\d{2}-\d{2}""")
                            .matches(dataNascimento) ->
                            "Use a data no formato AAAA-MM-DD."

                        else -> {
                            aoSalvar(
                                Cliente(
                                    nome = nome.trim(),
                                    email = email.trim(),
                                    senha = senha,
                                    cpf = cpf,
                                    dataNascimento = dataNascimento,
                                    ativo = ativo
                                )
                            )
                            "Cliente adicionado à lista."
                        }
                    }
                }
            ) {
                Text("Gravar")
            }

            OutlinedButton(
                onClick = {
                    nome = ""
                    email = ""
                    senha = ""
                    cpf = ""
                    dataNascimento = ""
                    ativo = true
                    mensagem = ""
                }
            ) {
                Text("Limpar Campos")
            }
        }

        if (mensagem.isNotBlank()) {
            Text(mensagem)
        }
    }
}
