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
import br.com.enali.network.services.AdminApiService
import br.com.enali.network.services.AuthApiService
import br.com.enali.network.services.ClienteApiService
import br.com.enali.network.dto.ProdutoRequest
import br.com.enali.network.dto.ProdutoResponse
import br.com.enali.network.services.ProdutoApiService
import br.com.enali.network.dto.EstoqueRequest
import br.com.enali.network.dto.EstoqueResponse
import br.com.enali.network.services.EstoqueApiService
import io.ktor.client.HttpClient

class EnaliApiClient(
    private val baseUrl: String = EnaliConfig.BASE_URL,
    private val httpClient: HttpClient = EnaliHttpClientFactory.criar()
) {
    val auth: AuthApiService = AuthApiService(
        httpClient = httpClient,
        baseUrl = baseUrl
    )

    val admin: AdminApiService = AdminApiService(
        httpClient = httpClient,
        baseUrl = baseUrl
    )

    val cliente: ClienteApiService = ClienteApiService(
        httpClient = httpClient,
        baseUrl = baseUrl
    )
    
    val produto: ProdutoApiService = ProdutoApiService(
        httpClient = httpClient,
        baseUrl = baseUrl
    )
    
    val estoque: EstoqueApiService = EstoqueApiService(
        httpClient = httpClient,
        baseUrl = baseUrl
    )

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

    suspend fun buscarProdutosPorNome(nome: String): Result<List<ProdutoResponse>> =
        produto.buscarProdutosPorNome(nome)

    suspend fun cadastrarProduto(token: String, request: ProdutoRequest): Result<ProdutoResponse> =
        produto.cadastrarProduto(token, request)

    suspend fun atualizarProduto(
        token: String,
        id: Long,
        request: ProdutoRequest
    ): Result<ProdutoResponse> = produto.atualizarProduto(token, id, request)

    suspend fun excluirProduto(token: String, id: Long): Result<Unit> =
        produto.excluirProduto(token, id)

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