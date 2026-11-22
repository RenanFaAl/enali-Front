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
}