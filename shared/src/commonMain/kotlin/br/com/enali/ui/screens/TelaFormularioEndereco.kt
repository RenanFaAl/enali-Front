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
import br.com.enali.model.Endereco
import br.com.enali.ui.components.CampoTextoCustomizado
import br.com.enali.ui.theme.CorBranco
import br.com.enali.ui.theme.CorMarromEscuro
import kotlin.random.Random

@Composable
fun TelaFormularioEndereco(
    aoSalvar: (Endereco) -> Unit
) {
    var cepEndereco by remember { mutableStateOf("") }
    var paisEndereco by remember { mutableStateOf("Brasil") }
    var estadoEndereco by remember { mutableStateOf("") }
    var siglaEstadoEndereco by remember { mutableStateOf("") }
    var cidadeEndereco by remember { mutableStateOf("") }
    var bairroEndereco by remember { mutableStateOf("") }
    var ruaEndereco by remember { mutableStateOf("") }
    var numeroEndereco by remember { mutableStateOf("") }
    var complementoEndereco by remember { mutableStateOf("") }
    var referenciaEndereco by remember { mutableStateOf("") }
    var dataCadastroEndereco by remember { mutableStateOf("") }
    var eEnderecoPrincipal by remember { mutableStateOf(false) }
    var estaAtivo by remember { mutableStateOf(true) }

    var erroCep by remember { mutableStateOf<String?>(null) }
    var erroPais by remember { mutableStateOf<String?>(null) }
    var erroEstado by remember { mutableStateOf<String?>(null) }
    var erroSiglaEstado by remember { mutableStateOf<String?>(null) }
    var erroCidade by remember { mutableStateOf<String?>(null) }
    var erroBairro by remember { mutableStateOf<String?>(null) }
    var erroRua by remember { mutableStateOf<String?>(null) }
    var erroNumero by remember { mutableStateOf<String?>(null) }
    var erroDataCadastro by remember { mutableStateOf<String?>(null) }
    var mensagemSucesso by remember { mutableStateOf<String?>(null) }

    val limparCampos = {
        cepEndereco = ""
        paisEndereco = "Brasil"
        estadoEndereco = ""
        siglaEstadoEndereco = ""
        cidadeEndereco = ""
        bairroEndereco = ""
        ruaEndereco = ""
        numeroEndereco = ""
        complementoEndereco = ""
        referenciaEndereco = ""
        dataCadastroEndereco = ""
        estaAtivo = true
        eEnderecoPrincipal = false
        erroCep = null
        erroPais = null
        erroEstado = null
        erroSiglaEstado = null
        erroCidade = null
        erroBairro = null
        erroRua = null
        erroNumero = null
        erroDataCadastro = null
    }

    val gravarEndereco = {
        erroCep = if (cepEndereco.isBlank()) "O CEP é obrigatório" else null
        erroPais = if (paisEndereco.isBlank()) "O país é obrigatório" else null
        erroEstado = if (estadoEndereco.isBlank()) "O estado é obrigatório" else null
        erroSiglaEstado = if (siglaEstadoEndereco.isBlank()) "A sigla do estado é obrigatória" else null
        erroCidade = if (cidadeEndereco.isBlank()) "A cidade é obrigatória" else null
        erroBairro = if (bairroEndereco.isBlank()) "O bairro é obrigatório" else null
        erroRua = if (ruaEndereco.isBlank()) "A rua é obrigatória" else null
        erroNumero = if (numeroEndereco.isBlank()) "O número é obrigatório" else null
        erroDataCadastro = if (dataCadastroEndereco.isBlank()) "A data de cadastro é obrigatória" else null

        if (erroCep == null && erroPais == null && erroEstado == null &&
            erroSiglaEstado == null && erroCidade == null && erroBairro == null &&
            erroRua == null && erroNumero == null && erroDataCadastro == null
        ) {

            val novoEndereco = Endereco(
                id = Random.nextLong(1000L, 999999L),
                rua = ruaEndereco,
                numero = numeroEndereco,
                complemento = complementoEndereco,
                bairro = bairroEndereco,
                cidade = cidadeEndereco,
                estado = estadoEndereco,
                siglaEstado = siglaEstadoEndereco,
                cep = cepEndereco,
                pais = paisEndereco,
                referencia = referenciaEndereco,
                principal = eEnderecoPrincipal,
                ativo = estaAtivo,
                dataCadastro = dataCadastroEndereco
            )
            aoSalvar(novoEndereco)
            mensagemSucesso = "Endereço cadastrado com sucesso!"
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
                text = "Formulário de Cadastro de Endereço",
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
                value = paisEndereco,
                onValueChange = { paisEndereco = it; erroPais = null },
                label = "País",
                placeholder = "Brasil",
                isError = erroPais != null
            )
            erroPais?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            CampoTextoCustomizado(
                value = cepEndereco,
                onValueChange = { cepEndereco = it; erroCep = null },
                label = "CEP",
                placeholder = "00000-000",
                isError = erroCep != null
            )
            erroCep?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            CampoTextoCustomizado(
                value = estadoEndereco,
                onValueChange = { estadoEndereco = it; erroEstado = null },
                label = "Estado",
                placeholder = "São Paulo",
                isError = erroEstado != null
            )
            erroEstado?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            CampoTextoCustomizado(
                value = siglaEstadoEndereco,
                onValueChange = { siglaEstadoEndereco = it; erroSiglaEstado = null },
                label = "Sigla do Estado",
                placeholder = "SP",
                isError = erroSiglaEstado != null
            )
            erroSiglaEstado?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            CampoTextoCustomizado(
                value = cidadeEndereco,
                onValueChange = { cidadeEndereco = it; erroCidade = null },
                label = "Cidade",
                placeholder = "Nome da cidade",
                isError = erroCidade != null
            )
            erroCidade?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            CampoTextoCustomizado(
                value = bairroEndereco,
                onValueChange = { bairroEndereco = it; erroBairro = null },
                label = "Bairro",
                placeholder = "Nome do bairro",
                isError = erroBairro != null
            )
            erroBairro?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            CampoTextoCustomizado(
                value = ruaEndereco,
                onValueChange = { ruaEndereco = it; erroRua = null },
                label = "Rua",
                placeholder = "Nome da rua",
                isError = erroRua != null
            )
            erroRua?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            CampoTextoCustomizado(
                value = numeroEndereco,
                onValueChange = { numeroEndereco = it; erroNumero = null },
                label = "Número",
                placeholder = "123",
                isError = erroNumero != null
            )
            erroNumero?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            CampoTextoCustomizado(
                value = complementoEndereco,
                onValueChange = { complementoEndereco = it },
                label = "Complemento",
                placeholder = "Apto 42"
            )

            CampoTextoCustomizado(
                value = referenciaEndereco,
                onValueChange = { referenciaEndereco = it },
                label = "Referência",
                placeholder = "Próximo à praça"
            )

            CampoTextoCustomizado(
                value = dataCadastroEndereco,
                onValueChange = { dataCadastroEndereco = it; erroDataCadastro = null },
                label = "Data de Cadastro",
                placeholder = "AAAA-MM-DDTHH:MM",
                isError = erroDataCadastro != null
            )
            erroDataCadastro?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Endereço Principal?")
                Switch(checked = eEnderecoPrincipal, onCheckedChange = { eEnderecoPrincipal = it })
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
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
                    onClick = gravarEndereco,
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