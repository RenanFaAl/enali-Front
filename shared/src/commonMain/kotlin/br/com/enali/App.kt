
package br.com.enali

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.enali.model.Administrador
import br.com.enali.model.Endereco
import br.com.enali.model.Produto
import br.com.enali.model.Estoque
import br.com.enali.ui.components.BarraLateral
import br.com.enali.ui.screens.TelaFormularioAdministrador
import br.com.enali.ui.screens.TelaFormularioEndereco
import br.com.enali.ui.screens.TelaFormularioProduto
import br.com.enali.ui.screens.TelaFormularioEstoque
import br.com.enali.model.Cliente
import br.com.enali.model.Pedido
import br.com.enali.model.Material
import br.com.enali.model.Fornecedor
import br.com.enali.ui.screens.TelaFormularioMaterial
import br.com.enali.ui.screens.TelaFormularioFornecedor
import br.com.enali.ui.components.BarraLateral
import br.com.enali.ui.screens.TelaFormularioAdministrador
import br.com.enali.ui.screens.TelaFormularioEndereco
import br.com.enali.ui.screens.TelaFormularioCliente
import br.com.enali.ui.screens.TelaFormularioPedido
import br.com.enali.ui.theme.CorBegeMedio
import br.com.enali.ui.theme.CorFundoClaro
import br.com.enali.ui.theme.CorMarromEscuro

@Composable
fun App() {

    var identificadorTelaAtual by remember { mutableStateOf("ADMINISTRADOR") }

    val listaDeAdministradores = remember { mutableStateOf(listOf<Administrador>()) }
    val listaDeEnderecos = remember { mutableStateOf(listOf<Endereco>()) }
    val listaDeProdutos = remember { mutableStateOf(listOf<Produto>()) }
    val listaDeEstoques = remember { mutableStateOf(listOf<Estoque>()) }
    val listaDeClientes = remember { mutableStateOf(listOf<Cliente>()) }
    val listaDePedidos = remember { mutableStateOf(listOf<Pedido>()) }
    val listaDeMateriais = remember { mutableStateOf(listOf<Material>()) }
    val listaDeFornecedores = remember { mutableStateOf(listOf<Fornecedor>()) }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = CorMarromEscuro,
            secondary = CorBegeMedio,
            background = CorFundoClaro,
            surface = CorFundoClaro
        )
    ) {
        Scaffold(
            bottomBar = {
                Surface(
                    color = CorBegeMedio,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Sistema Enali",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = CorMarromEscuro,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CorFundoClaro)
            ) {
                BarraLateral(
                    telaAtual = identificadorTelaAtual,
                    aoMudarTela = { novaTela -> identificadorTelaAtual = novaTela }
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    when (identificadorTelaAtual) {
                        "ADMINISTRADOR" -> TelaFormularioAdministrador(
                            aoSalvar = { novoAdmin ->
                                listaDeAdministradores.value = listaDeAdministradores.value + novoAdmin
                            }
                        )

                        "ENDERECO" -> TelaFormularioEndereco(
                            aoSalvar = { novoEndereco ->
                                listaDeEnderecos.value = listaDeEnderecos.value + novoEndereco
                            }
                        )

                        "PRODUTO" -> TelaFormularioProduto(
                            aoSalvar = { novoProduto ->
                                listaDeProdutos.value = listaDeProdutos.value + novoProduto
                            }
                        )

                        "ESTOQUE" -> TelaFormularioEstoque(
                            listaProdutos = listaDeProdutos.value,
                            aoSalvar = { novoEstoque ->
                                listaDeEstoques.value = listaDeEstoques.value + novoEstoque
                            }
                        )
                        
                        "CLIENTE" -> TelaFormularioCliente(
                            aoSalvar = { novoCliente ->
                                listaDeClientes.value =
                                    listaDeClientes.value + novoCliente
                            }
                        )

                        "PEDIDO" -> TelaFormularioPedido(
                            aoSalvar = { novoPedido ->
                                listaDePedidos.value =
                                    listaDePedidos.value + novoPedido
                            }
                        )

                        "MATERIAL" -> TelaFormularioMaterial(
                            aoSalvar = { novoMaterial ->
                                listaDeMateriais.value =
                                    listaDeMateriais.value + novoMaterial
                            }
                        )

                        "FORNECEDOR" -> TelaFormularioFornecedor(
                            aoSalvar = { novoFornecedor ->
                                listaDeFornecedores.value =
                                    listaDeFornecedores.value + novoFornecedor
                            }
                        )
                    }
                }
            }
        }
    }
}
