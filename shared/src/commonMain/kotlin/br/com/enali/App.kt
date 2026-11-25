
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
import br.com.enali.model.Estoque
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

    var sessao by remember {
        mutableStateOf<SessaoUsuario>(SessaoUsuario.Visitante)
    }

    var identificadorTelaAtual by remember {
        mutableStateOf(RotaEnali.VISITANTE.rotaInicial)
    }

    val rotasPermitidas = remember(sessao) {
        RotaEnali.rotasPermitidas(sessao)
    }

    LaunchedEffect(sessao, rotasPermitidas) {
        if (identificadorTelaAtual !in rotasPermitidas) {
            identificadorTelaAtual = RotaEnali.daSessao(sessao).rotaInicial
        }
    }

    val listaDeAdministradores = remember {
        mutableStateOf(listOf<Administrador>())
    }
    val listaDeProdutos = remember {
        mutableStateOf(listOf<Produto>())
    }
    val listaDeEstoques = remember {
        mutableStateOf(listOf<Estoque>())
    }
    val listaDeClientes = remember {
        mutableStateOf(listOf<Cliente>())
    }

    var itensCarrinho by remember {
        mutableStateOf(listOf<ItemCarrinhoEnali>())
    }

    val aoSair = {
        itensCarrinho = emptyList()
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
                    is SessaoUsuario.AdminLogado ->
                        "Admin (${s.nivelAcesso}): ${s.nome}"
                }

                Surface(
                    color = CorBegeMedio,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            ),
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
                                aoLoginSucesso = {
                                    aoLogarComSucesso(it)
                                },
                                aoIrParaCadastro = {
                                    identificadorTelaAtual = "CADASTRO_CLIENTE"
                                },
                                aoIrParaLoginAdmin = {
                                    identificadorTelaAtual = "LOGIN_ADMIN"
                                }
                            )

                            "LOGIN_ADMIN" -> TelaLoginAdmin(
                                apiClient = apiClient,
                                aoLoginSucesso = {
                                    aoLogarComSucesso(it)
                                },
                                aoVoltarCliente = {
                                    identificadorTelaAtual = "LOGIN_CLIENTE"
                                }
                            )

                            "CADASTRO_CLIENTE" -> TelaFormularioCliente(
                                apiClient = apiClient,
                                aoSalvar = { novoCliente ->
                                    listaDeClientes.value =
                                        listaDeClientes.value + novoCliente
                                },
                                aoVoltarLogin = {
                                    identificadorTelaAtual = "LOGIN_CLIENTE"
                                }
                            )

                            "VITRINE" -> TelaVitrineEnali(
                                apiClient = apiClient,
                                quantidadeCarrinho = itensCarrinho.sumOf {
                                    it.quantidade
                                },
                                aoAdicionarAoCarrinho = { produto ->
                                    val anterior = itensCarrinho.find {
                                        it.produto.id == produto.id
                                    }

                                    itensCarrinho = if (anterior == null) {
                                        itensCarrinho +
                                            ItemCarrinhoEnali(produto, 1)
                                    } else {
                                        itensCarrinho.map {
                                            if (it.produto.id == produto.id) {
                                                it.copy(
                                                    quantidade = it.quantidade + 1
                                                )
                                            } else {
                                                it
                                            }
                                        }
                                    }
                                },
                                aoAbrirCarrinho = {
                                    identificadorTelaAtual = "CARRINHO"
                                }
                            )

                            "CARRINHO" -> TelaCarrinhoEnali(
                                apiClient = apiClient,
                                itens = itensCarrinho,
                                aoAlterarQuantidade = { id, quantidade ->
                                    itensCarrinho = if (quantidade <= 0) {
                                        itensCarrinho.filterNot {
                                            it.produto.id == id
                                        }
                                    } else {
                                        itensCarrinho.map {
                                            if (it.produto.id == id) {
                                                it.copy(
                                                    quantidade = quantidade
                                                )
                                            } else {
                                                it
                                            }
                                        }
                                    }
                                },
                                aoRemover = { id ->
                                    itensCarrinho = itensCarrinho.filterNot {
                                        it.produto.id == id
                                    }
                                },
                                aoVoltarVitrine = {
                                    identificadorTelaAtual = "VITRINE"
                                },
                                tokenCliente = (sessao as? SessaoUsuario.ClienteLogado)?.token,
                                aoIrParaLogin = { identificadorTelaAtual = "LOGIN_CLIENTE" },
                                aoIrParaEnderecos = { identificadorTelaAtual = "MEUS_ENDERECOS" },
                                aoPedidoRealizado = {
                                    itensCarrinho = emptyList()
                                    identificadorTelaAtual = "MEUS_PEDIDOS"
                                }
                            )

                            "MEUS_PEDIDOS" -> (sessao as? SessaoUsuario.ClienteLogado)?.let { s ->
                                TelaPedidos(
                                    titulo = "Meus pedidos",
                                    subtitulo = "Acompanhe e cancele seus pedidos.",
                                    chave = s.token,
                                    mostrarCliente = false,
                                    listar = { apiClient.pedido.listarMeus(s.token) },
                                    cancelar = { id -> apiClient.pedido.cancelarMeu(s.token, id) }
                                )
                            }

                            "MEUS_ENDERECOS" -> (sessao as? SessaoUsuario.ClienteLogado)?.let { s ->
                                TelaEnderecos(
                                    titulo = "Meus endereços",
                                    subtitulo = "Endereços de entrega dos seus pedidos.",
                                    chave = s.token,
                                    listar = { apiClient.endereco.listarMeus(s.token) },
                                    salvar = { id, request ->
                                        if (id == null) apiClient.endereco.cadastrarMeu(s.token, request)
                                        else apiClient.endereco.atualizarMeu(s.token, id, request)
                                    },
                                    excluir = { id -> apiClient.endereco.excluirMeu(s.token, id) }
                                )
                            }

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
                                val sessaoAdmin =
                                    sessao as? SessaoUsuario.AdminLogado

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

                            "ENDERECO" -> (sessao as? SessaoUsuario.AdminLogado)?.let { s ->
                                TelaEnderecos(
                                    titulo = "Endereços",
                                    subtitulo = "Todos os endereços cadastrados (consulta).",
                                    chave = s.token,
                                    listar = { apiClient.endereco.listarTodos(s.token) },
                                    mostrarDono = true
                                )
                            }

                            "PRODUTO" -> {
                                val sessaoAdmin =
                                    sessao as? SessaoUsuario.AdminLogado

                                if (sessaoAdmin != null) {
                                    TelaListagemProdutosAdmin(
                                        apiClient = apiClient,
                                        tokenAdmin = sessaoAdmin.token,
                                        listaProdutos = listaDeProdutos.value,
                                        aoAtualizarLista = { novaLista ->
                                            listaDeProdutos.value = novaLista
                                        }
                                    )
                                } else {
                                    Text(
                                        text = "Acesso restrito a administradores.",
                                        color = CorMarromEscuro,
                                        modifier = Modifier.padding(24.dp)
                                    )
                                }
                            }

                            "ESTOQUE" -> {
                                val sessaoAdmin =
                                    sessao as? SessaoUsuario.AdminLogado

                                if (sessaoAdmin != null) {
                                    TelaListagemEstoqueAdmin(
                                        apiClient = apiClient,
                                        tokenAdmin = sessaoAdmin.token,
                                        listaProdutos = listaDeProdutos.value,
                                        aoAtualizarProdutos = {
                                            listaDeProdutos.value = it
                                        },
                                        listaEstoques = listaDeEstoques.value,
                                        aoAtualizarLista = {
                                            listaDeEstoques.value = it
                                        }
                                    )
                                } else {
                                    Text(
                                        text = "Acesso restrito a administradores.",
                                        color = CorMarromEscuro,
                                        modifier = Modifier.padding(24.dp)
                                    )
                                }
                            }

                            "CLIENTE" -> TelaFormularioCliente(
                                apiClient = apiClient,
                                aoSalvar = { novoCliente ->
                                    listaDeClientes.value =
                                        listaDeClientes.value + novoCliente
                                }
                            )

                            "PEDIDO" -> (sessao as? SessaoUsuario.AdminLogado)?.let { s ->
                                TelaPedidos(
                                    titulo = "Pedidos",
                                    subtitulo = "Todos os pedidos dos clientes.",
                                    chave = s.token,
                                    mostrarCliente = true,
                                    listar = { apiClient.pedido.listarTodos(s.token) },
                                    cancelar = { id -> apiClient.pedido.cancelar(s.token, id) }
                                )
                            }

                            "MATERIAL" -> (sessao as? SessaoUsuario.AdminLogado)?.let { s ->
                                TelaMateriais(apiClient = apiClient, token = s.token)
                            }

                            "FORNECEDOR" -> (sessao as? SessaoUsuario.AdminLogado)?.let { s ->
                                TelaFornecedores(apiClient = apiClient, token = s.token)
                            }
                        }
                    }
                }
            }
        }
    }
}
