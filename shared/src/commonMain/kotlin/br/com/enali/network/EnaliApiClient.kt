package br.com.enali.network

import br.com.enali.network.EnaliConfig
import br.com.enali.network.dto.AdminAtualizarRequest
import br.com.enali.network.dto.AdminCadastroRequest
import br.com.enali.network.dto.AdminPerfilResponse
import br.com.enali.network.dto.AlterarSenhaRequest
import br.com.enali.network.dto.AuthLoginResponse
import br.com.enali.network.dto.ClienteAtualizarRequest
import br.com.enali.network.dto.ClienteCadastroRequest
import br.com.enali.network.dto.ClientePerfilResponse
import br.com.enali.network.repository.AdminApiRepository
import br.com.enali.network.repository.AuthApiRepository
import br.com.enali.network.repository.ClienteApiRepository
import br.com.enali.network.dto.ImagemUploadItem
import br.com.enali.network.dto.ProdutoImagemResponse
import br.com.enali.network.dto.ProdutoRequest
import br.com.enali.network.dto.ProdutoResponse
import br.com.enali.network.repository.ProdutoApiRepository
import br.com.enali.network.dto.EstoqueRequest
import br.com.enali.network.dto.EstoqueResponse
import br.com.enali.network.repository.EstoqueApiRepository
import io.ktor.client.HttpClient

class EnaliApiClient(
    private val baseUrl: String = EnaliConfig.BASE_URL,
    private val httpClient: HttpClient = EnaliHttpClientFactory.criar()
) {
    val auth: AuthApiRepository = AuthApiRepository(
        httpClient = httpClient,
        baseUrl = baseUrl
    )

    val admin: AdminApiRepository = AdminApiRepository(
        httpClient = httpClient,
        baseUrl = baseUrl
    )

    val cliente: ClienteApiRepository = ClienteApiRepository(
        httpClient = httpClient,
        baseUrl = baseUrl
    )
    
    val produto: ProdutoApiRepository = ProdutoApiRepository(
        httpClient = httpClient,
        baseUrl = baseUrl
    )
    
    val estoque: EstoqueApiRepository = EstoqueApiRepository(
        httpClient = httpClient,
        baseUrl = baseUrl
    )

    val endereco = br.com.enali.network.repository.EnderecoApiRepository(httpClient, baseUrl)
    val pedido = br.com.enali.network.repository.PedidoApiRepository(httpClient, baseUrl)
    val material = br.com.enali.network.repository.MaterialApiRepository(httpClient, baseUrl)
    val fornecedor = br.com.enali.network.repository.FornecedorApiRepository(httpClient, baseUrl)

    suspend fun loginCliente(email: String, senha: String): Result<AuthLoginResponse> =
        auth.loginCliente(email, senha)

    suspend fun loginAdmin(email: String, senha: String): Result<AuthLoginResponse> =
        auth.loginAdmin(email, senha)

    suspend fun cadastrarCliente(request: ClienteCadastroRequest): Result<ClientePerfilResponse> =
        auth.cadastrarCliente(request)

    suspend fun criarAdministrador(
        token: String,
        request: AdminCadastroRequest
    ): Result<AdminPerfilResponse> = admin.criarAdministrador(token, request)

    suspend fun buscarPerfilCliente(token: String): Result<ClientePerfilResponse> =
        cliente.buscarMeusDados(token)

    suspend fun atualizarPerfilCliente(
        token: String,
        request: ClienteAtualizarRequest
    ): Result<ClientePerfilResponse> = cliente.atualizarMeusDados(token, request)

    suspend fun alterarSenhaCliente(
        token: String,
        request: AlterarSenhaRequest
    ): Result<Unit> = cliente.alterarMinhaSenha(token, request)

    suspend fun buscarPerfilAdmin(token: String): Result<AdminPerfilResponse> =
        admin.buscarMeusDados(token)

    suspend fun atualizarPerfilAdmin(
        token: String,
        request: AdminAtualizarRequest
    ): Result<AdminPerfilResponse> = admin.atualizarMeusDados(token, request)

    suspend fun alterarSenhaAdmin(
        token: String,
        request: AlterarSenhaRequest
    ): Result<Unit> = admin.alterarMinhaSenha(token, request)

    fun fechar() {
        httpClient.close()
    }
    
    suspend fun listarProdutos(): Result<List<ProdutoResponse>> = produto.listarProdutos()

    suspend fun buscarProdutoPorId(id: Long): Result<ProdutoResponse> =
        produto.buscarProdutoPorId(id)

    suspend fun buscarProdutosPorNome(nome: String): Result<List<ProdutoResponse>> =
        produto.buscarProdutosPorNome(nome)

    suspend fun cadastrarProduto(
        token: String,
        request: ProdutoRequest,
        imagens: List<ImagemUploadItem> = emptyList()
    ): Result<ProdutoResponse> = produto.cadastrarProduto(token, request, imagens)

    suspend fun atualizarProduto(
        token: String,
        id: Long,
        request: ProdutoRequest
    ): Result<ProdutoResponse> = produto.atualizarProduto(token, id, request)

    suspend fun excluirProduto(token: String, id: Long): Result<Unit> =
        produto.excluirProduto(token, id)

    suspend fun listarImagensProduto(produtoId: Long): Result<List<ProdutoImagemResponse>> =
        produto.listarImagens(produtoId)

    suspend fun adicionarImagemProduto(
        token: String,
        produtoId: Long,
        imagem: ImagemUploadItem,
        ordem: Int? = null
    ): Result<ProdutoImagemResponse> = produto.adicionarImagem(token, produtoId, imagem, ordem)

    suspend fun buscarImagemProdutoBytes(
        produtoId: Long,
        imagemId: Long
    ): Result<ByteArray> = produto.buscarImagemBytes(produtoId, imagemId)
    
    suspend fun removerImagemProduto(
        token: String,
        produtoId: Long,
        imagemId: Long
    ): Result<Unit> = produto.removerImagem(token, produtoId, imagemId)

    suspend fun listarEstoques(token: String): Result<List<EstoqueResponse>> =
        estoque.listarEstoques(token)

    suspend fun buscarEstoquePorProdutoId(produtoId: Long): Result<EstoqueResponse> =
        estoque.buscarEstoquePorProdutoId(produtoId)

    suspend fun cadastrarEstoque(token: String, request: EstoqueRequest): Result<EstoqueResponse> =
        estoque.cadastrarEstoque(token, request)

    suspend fun atualizarEstoque(
        token: String,
        id: Long,
        request: EstoqueRequest
    ): Result<EstoqueResponse> = estoque.atualizarEstoque(token, id, request)

    suspend fun excluirEstoque(token: String, id: Long): Result<Unit> =
        estoque.excluirEstoque(token, id)
        
}