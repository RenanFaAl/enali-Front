package br.com.enali.network

import br.com.enali.network.admin.AdminApiService
import br.com.enali.network.auth.AuthApiService
import br.com.enali.network.dto.AdminCadastroRequest
import br.com.enali.network.dto.AdminPerfilResponse
import br.com.enali.network.dto.AuthLoginResponse
import br.com.enali.network.dto.ClienteCadastroRequest
import br.com.enali.network.dto.ClientePerfilResponse
import io.ktor.client.HttpClient

class EnaliApiClient(
    private val baseUrl: String = "http://localhost:8080",
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

    fun fechar() {
        httpClient.close()
    }
}