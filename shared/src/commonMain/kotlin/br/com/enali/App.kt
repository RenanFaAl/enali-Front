package br.com.enali

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.enali.auth.RotaEnali
import br.com.enali.auth.SessaoUsuario
import br.com.enali.model.Administrador
import br.com.enali.model.Cliente
import br.com.enali.model.Endereco
import br.com.enali.model.Estoque
import br.com.enali.model.Fornecedor
import br.com.enali.model.Material
import br.com.enali.model.Pedido
import br.com.enali.model.Produto
import br.com.enali.network.EnaliApiClient
import br.com.enali.ui.components.BarraLateral
import br.com.enali.ui.screens.*
import br.com.enali.ui.theme.CorBegeMedio
import br.com.enali.ui.theme.CorFundoClaro
import br.com.enali.ui.theme.CorMarromEscuro

@Composable
fun App() {
    val apiClient = remember { EnaliApiClient() }
    DisposableEffect(apiClient) {
        onDispose { apiClient.fechar() }
    }

    var sessao by remember { mutableStateOf<SessaoUsuario>(SessaoUsuario.Visitante) }
    var identificadorTelaAtual by remember { mutableStateOf(RotaEnali.VISITANTE.rotaInicial) }

    val rotasPermitidas = remember(sessao) { RotaEnali.rotasPermitidas(sessao) }

    LaunchedEffect(sessao, rotasPermitidas) {
        if (identificadorTelaAtual !in rotasPermitidas) {
            identificadorTelaAtual = RotaEnali.daSessao(sessao).rotaInicial
        }
    }

    val listaDeAdministradores = remember { mutableStateOf(listOf<Administrador>()) }
    val listaDeEnderecos = remember { mutableStateOf(listOf<Endereco>()) }
    val listaDeProdutos = remember { mutableStateOf(listOf<Produto>()) }
    val listaDeEstoques = remember { mutableStateOf(listOf<Estoque>()) }
    val listaDeClientes = remember { mutableStateOf(listOf<Cliente>()) }
    val listaDePedidos = remember { mutableStateOf(listOf<Pedido>()) }
    val listaDeMateriais = remember { mutableStateOf(listOf<Material>()) }
    val listaDeFornecedores = remember { mutableStateOf(listOf<Fornecedor>()) }

    val aoSair = {
        sessao = SessaoUsuario.Visitante
        identificadorTelaAtual = RotaEnali.VISITANTE.rotaInicial
    }

    val aoLogarComSucesso: (SessaoUsuario) -> Unit = { novaSessao ->
        sessao = novaSessao
        identificadorTelaAtual = RotaEnali.daSessao(novaSessao).rotaInicial
    }

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
                val statusSessao = when (val s = sessao) {
                    is SessaoUsuario.Visitante -> "Visitante"
                    is SessaoUsuario.ClienteLogado -> "Cliente: ${s.nome}"
                    is SessaoUsuario.AdminLogado -> "Admin (${s.nivelAcesso}): ${s.nome}"
                }
                Surface(
                    color = CorBegeMedio,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Sistema Enali",
                            color = CorMarromEscuro,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = statusSessao,
                            color = CorMarromEscuro,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        ) { espacamento ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(espacamento)
            ) {
                val compacto = maxWidth < 760.dp

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CorFundoClaro)
                ) {
                    BarraLateral(
                        sessao = sessao,
                        telaAtual = identificadorTelaAtual,
                        aoMudarTela = { novaTela ->
                            if (novaTela in rotasPermitidas) {
                                identificadorTelaAtual = novaTela
                            }
                        },
                        aoSair = aoSair,
                        aoAbrirLoginAdminDiscreto = {
                            if (sessao is SessaoUsuario.Visitante) {
                                identificadorTelaAtual = "LOGIN_ADMIN"
                            }
                        },
                        compacto = compacto
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        when (identificadorTelaAtual) {
                            "LOGIN_CLIENTE" -> TelaLoginCliente(
                                apiClient = apiClient,
                                aoLoginSucesso = { aoLogarComSucesso(it) },
                                aoIrParaCadastro = { identificadorTelaAtual = "CADASTRO_CLIENTE" },
                                aoIrParaLoginAdmin = { identificadorTelaAtual = "LOGIN_ADMIN" }
                            )

                            "LOGIN_ADMIN" -> TelaLoginAdmin(
                                apiClient = apiClient,
                                aoLoginSucesso = { aoLogarComSucesso(it) },
                                aoVoltarCliente = { identificadorTelaAtual = "LOGIN_CLIENTE" }
                            )

                            "CADASTRO_CLIENTE" -> TelaFormularioCliente(
                                apiClient = apiClient,
                                aoSalvar = { novoCliente ->
                                    listaDeClientes.value = listaDeClientes.value + novoCliente
                                },
                                aoVoltarLogin = { identificadorTelaAtual = "LOGIN_CLIENTE" }
                            )

                            "VITRINE" -> Text(
                                text = "Vitrine de Produtos",
                                color = CorMarromEscuro,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(24.dp)
                            )

                            "CARRINHO" -> Text(
                                text = "Carrinho de Compras",
                                color = CorMarromEscuro,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(24.dp)
                            )

                            "MEUS_PEDIDOS", "MEUS_ENDERECOS" -> Text(
                                text = "Área: $identificadorTelaAtual",
                                color = CorMarromEscuro,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(24.dp)
                            )

                            "MEU_CADASTRO" -> {
                                val s = sessao as? SessaoUsuario.ClienteLogado
                                if (s != null) {
                                    TelaPerfilCliente(
                                        apiClient = apiClient,
                                        token = s.token,
                                        nomeInicial = s.nome,
                                        emailInicial = s.email,
                                        aoAtualizarNomeSessao = { novoNome ->
                                            sessao = s.copy(nome = novoNome)
                                        }
                                    )
                                }
                            }

                            "MEU_PERFIL_ADMIN" -> {
                                val s = sessao as? SessaoUsuario.AdminLogado
                                if (s != null) {
                                    TelaPerfilAdmin(
                                        apiClient = apiClient,
                                        token = s.token,
                                        nomeInicial = s.nome,
                                        emailInicial = s.email,
                                        nivelAcessoInicial = s.nivelAcesso,
                                        aoAtualizarNomeSessao = { novoNome ->
                                            sessao = s.copy(nome = novoNome)
                                        }
                                    )
                                }
                            }

                            "ADMINISTRADOR" -> {
                                val sessaoAdmin = sessao as? SessaoUsuario.AdminLogado
                                if (sessaoAdmin != null && sessaoAdmin.isMaster) {
                                    TelaFormularioAdministrador(
                                        apiClient = apiClient,
                                        tokenAdmin = sessaoAdmin.token,
                                        isAdminMaster = sessaoAdmin.isMaster,
                                        aoSalvar = { novoAdmin ->
                                            listaDeAdministradores.value =
                                                listaDeAdministradores.value + novoAdmin
                                        }
                                    )
                                } else {
                                    Text(
                                        text = "Acesso restrito ao Admin Master.",
                                        color = CorMarromEscuro,
                                        modifier = Modifier.padding(24.dp)
                                    )
                                }
                            }

                            "ENDERECO" -> TelaFormularioEndereco(
                                aoSalvar = { novoEndereco ->
                                    listaDeEnderecos.value =
                                        listaDeEnderecos.value + novoEndereco
                                }
                            )

                            "PRODUTO" -> TelaFormularioProduto(
                                aoSalvar = { novoProduto ->
                                    listaDeProdutos.value =
                                        listaDeProdutos.value + novoProduto
                                },
                                listaProdutos = listaDeProdutos.value
                            )

                            "ESTOQUE" -> TelaFormularioEstoque(
                                listaProdutos = listaDeProdutos.value,
                                aoSalvar = { novoEstoque ->
                                    listaDeEstoques.value =
                                        listaDeEstoques.value + novoEstoque
                                },
                                listaEstoques = listaDeEstoques.value
                            )

                            "CLIENTE" -> TelaFormularioCliente(
                                apiClient = apiClient,
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
}