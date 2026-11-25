
package br.com.enali

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
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

    // Navigation3, 1º nível: pilha de telas (CRUDs); o topo da pilha é a tela exibida
    val pilhaTelas = remember { mutableStateListOf(RotaEnali.VISITANTE.rotaInicial) }
    val identificadorTelaAtual = pilhaTelas.last()

    // Menu lateral, login e logout: a tela escolhida vira a raiz da pilha
    fun trocarRaiz(tela: String) {
        pilhaTelas.clear()
        pilhaTelas.add(tela)
    }

    // Navegação dentro de um fluxo (ex.: vitrine -> carrinho): empilha para o "voltar" funcionar
    fun abrir(tela: String) {
        val indice = pilhaTelas.indexOf(tela)
        if (indice >= 0) {
            while (pilhaTelas.size > indice + 1) pilhaTelas.removeAt(pilhaTelas.lastIndex)
        } else {
            pilhaTelas.add(tela)
        }
    }

    val rotasPermitidas = remember(sessao) {
        RotaEnali.rotasPermitidas(sessao)
    }

    LaunchedEffect(sessao, rotasPermitidas) {
        if (identificadorTelaAtual !in rotasPermitidas) {
            trocarRaiz(RotaEnali.daSessao(sessao).rotaInicial)
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
        trocarRaiz(RotaEnali.VISITANTE.rotaInicial)
    }

    val aoLogarComSucesso: (SessaoUsuario) -> Unit = { novaSessao ->
        sessao = novaSessao
        trocarRaiz(RotaEnali.daSessao(novaSessao).rotaInicial)
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
                                trocarRaiz(novaTela)
                            }
                        },
                        aoSair = aoSair,
                        aoAbrirLoginAdminDiscreto = {
                            if (sessao is SessaoUsuario.Visitante) {
                                abrir("LOGIN_ADMIN")
                            }
                        },
                        compacto = compacto
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        NavDisplay(
                            backStack = pilhaTelas,
                            onBack = { if (pilhaTelas.size > 1) pilhaTelas.removeAt(pilhaTelas.lastIndex) },
                            entryProvider = { tela ->
                                NavEntry(tela) {
                        when (tela) {
                            "LOGIN_CLIENTE" -> TelaLoginCliente(
                                apiClient = apiClient,
                                aoLoginSucesso = {
                                    aoLogarComSucesso(it)
                                },
                                aoIrParaCadastro = {
                                    abrir("CADASTRO_CLIENTE")
                                },
                                aoIrParaLoginAdmin = {
                                    abrir("LOGIN_ADMIN")
                                }
                            )

                            "LOGIN_ADMIN" -> TelaLoginAdmin(
                                apiClient = apiClient,
                                aoLoginSucesso = {
                                    aoLogarComSucesso(it)
                                },
                                aoVoltarCliente = {
                                    abrir("LOGIN_CLIENTE")
                                }
                            )

                            "CADASTRO_CLIENTE" -> TelaFormularioCliente(
                                apiClient = apiClient,
                                aoSalvar = { novoCliente ->
                                    listaDeClientes.value =
                                        listaDeClientes.value + novoCliente
                                },
                                aoVoltarLogin = {
                                    abrir("LOGIN_CLIENTE")
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
                                    abrir("CARRINHO")
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
                                    abrir("VITRINE")
                                },
                                tokenCliente = (sessao as? SessaoUsuario.ClienteLogado)?.token,
                                aoIrParaLogin = { abrir("LOGIN_CLIENTE") },
                                aoIrParaEnderecos = { abrir("MEUS_ENDERECOS") },
                                aoPedidoRealizado = {
                                    itensCarrinho = emptyList()
                                    abrir("MEUS_PEDIDOS")
                                }
                            )

                            "MEUS_PEDIDOS" -> (sessao as? SessaoUsuario.ClienteLogado)?.let { s ->
                                TelaPedidos(
                                    titulo = "Meus pedidos",
                                    subtitulo = "Acompanhe e cancele seus pedidos.",
                                    chave = s.token,
                                    mostrarCliente = false,
                                    listar = { apiClient.pedido.listarMeus(s.token) },
                                    cancelar = { id -> apiClient.pedido.cancelarMeu(s.token, id) },
                                    carregarFotos = { id -> carregarFotosPedido(apiClient, s.token, id, admin = false) }
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
                                    cancelar = { id -> apiClient.pedido.cancelar(s.token, id) },
                                    carregarFotos = { id -> carregarFotosPedido(apiClient, s.token, id, admin = true) }
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
                        )
                    }
                }
            }
        }
    }
}

// Busca a lista de fotos do pedido e baixa cada uma; falhas viram lista vazia
private suspend fun carregarFotosPedido(api: EnaliApiClient, token: String, pedidoId: Long, admin: Boolean): List<ByteArray> =
    api.pedido.listarFotos(token, pedidoId, admin).getOrDefault(emptyList())
        .mapNotNull { api.pedido.baixarFoto(token, pedidoId, it.id, admin).getOrNull() }
