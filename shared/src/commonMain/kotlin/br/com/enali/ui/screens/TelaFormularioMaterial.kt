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
import br.com.enali.model.Material
import br.com.enali.ui.components.CampoTextoCustomizado
import br.com.enali.ui.theme.CorBranco
import br.com.enali.ui.theme.CorMarromEscuro
import kotlin.random.Random

@Composable
fun TelaFormularioMaterial(
    aoSalvar: (Material) -> Unit
) {
    var nomeMaterial by remember { mutableStateOf("") }
    var descricaoMaterial by remember { mutableStateOf("") }
    var quantidadeMaterial by remember { mutableStateOf("0") }
    var custoMaterial by remember { mutableStateOf("") }
    var estaDisponivel by remember { mutableStateOf(true) }

    var erroNome by remember { mutableStateOf<String?>(null) }
    var erroDescricao by remember { mutableStateOf<String?>(null) }
    var erroQuantidade by remember { mutableStateOf<String?>(null) }
    var erroCusto by remember { mutableStateOf<String?>(null) }
    var mensagemSucesso by remember { mutableStateOf<String?>(null) }

    val regexCusto = Regex("""^\d{1,8}(\.\d{1,2})?$""")

    val limparCampos = {
        nomeMaterial = ""
        descricaoMaterial = ""
        quantidadeMaterial = "0"
        custoMaterial = ""
        estaDisponivel = true
        erroNome = null
        erroDescricao = null
        erroQuantidade = null
        erroCusto = null
    }

    val gravarMaterial = {
        val quantidadeValor = quantidadeMaterial.trim().toIntOrNull()
        val custoValor = custoMaterial.trim().replace(",", ".").toDoubleOrNull()

        erroNome = when {
            nomeMaterial.isBlank() -> "O nome é obrigatório"
            nomeMaterial.trim().length < 2 || nomeMaterial.trim().length > 150 ->
                "O nome deve ter entre 2 e 150 caracteres"
            else -> null
        }

        erroDescricao = if (descricaoMaterial.length > 500) {
            "A descrição deve ter no máximo 500 caracteres"
        } else null

        erroQuantidade = when {
            quantidadeMaterial.isBlank() -> "A quantidade é obrigatória"
            quantidadeValor == null -> "Informe uma quantidade válida"
            quantidadeValor < 0 -> "A quantidade não pode ser negativa"
            else -> null
        }

        erroCusto = when {
            custoMaterial.isBlank() -> "O custo é obrigatório"
            custoValor == null || !custoValor.isFinite() -> "Informe um custo válido"
            custoValor < 0.01 -> "O custo deve ser maior que zero"
            !regexCusto.matches(custoMaterial.trim().replace(",", ".")) ->
                "O custo deve ter no máximo 8 dígitos inteiros e 2 casas decimais"
            else -> null
        }

        if (
            erroNome == null &&
            erroDescricao == null &&
            erroQuantidade == null &&
            erroCusto == null &&
            quantidadeValor != null &&
            custoValor != null
        ) {
            val novoMaterial = Material(
                id = Random.nextLong(1000L, 999999L),
                nome = nomeMaterial.trim(),
                descricao = descricaoMaterial.trim(),
                quantidade = quantidadeValor,
                custo = custoValor,
                disponivel = estaDisponivel
            )

            aoSalvar(novoMaterial)
            mensagemSucesso = "Material cadastrado com sucesso!"
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
                text = "Formulário de Cadastro de Material",
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
                value = nomeMaterial,
                onValueChange = {
                    nomeMaterial = it
                    erroNome = null
                },
                label = "Nome do Material",
                placeholder = "Tecido de algodão",
                isError = erroNome != null
            )

            erroNome?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            CampoTextoCustomizado(
                value = descricaoMaterial,
                onValueChange = {
                    descricaoMaterial = it
                    erroDescricao = null
                },
                label = "Descrição (opcional)",
                placeholder = "Descrição do material",
                isError = erroDescricao != null
            )

            erroDescricao?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            CampoTextoCustomizado(
                value = quantidadeMaterial,
                onValueChange = {
                    quantidadeMaterial = it
                    erroQuantidade = null
                },
                label = "Quantidade",
                placeholder = "100",
                isError = erroQuantidade != null
            )

            erroQuantidade?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            CampoTextoCustomizado(
                value = custoMaterial,
                onValueChange = {
                    custoMaterial = it
                    erroCusto = null
                },
                label = "Custo",
                placeholder = "12,50",
                isError = erroCusto != null
            )

            erroCusto?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Disponível?")
                Switch(
                    checked = estaDisponivel,
                    onCheckedChange = { estaDisponivel = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = gravarMaterial,
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
