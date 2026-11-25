package br.com.enali.network.repository

import br.com.enali.network.EnaliConfig
import br.com.enali.network.EnaliHttpClientFactory
import br.com.enali.network.dto.AuthLoginRequest
import br.com.enali.network.dto.AuthLoginResponse
import br.com.enali.network.dto.ClienteCadastroRequest
import br.com.enali.network.dto.ClientePerfilResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthApiRepository(
    private val httpClient: HttpClient,
    private val baseUrl: String = EnaliConfig.BASE_URL
) {
    suspend fun loginCliente(email: String, senha: String): Result<AuthLoginResponse> =
        postLogin("$baseUrl/auth/cliente/signin", email, senha)

    suspend fun loginAdmin(email: String, senha: String): Result<AuthLoginResponse> =
        postLogin("$baseUrl/auth/admin/signin", email, senha)

    suspend fun cadastrarCliente(request: ClienteCadastroRequest): Result<ClientePerfilResponse> =
        EnaliHttpClientFactory.executarRequisicao {
            val response = httpClient.post("$baseUrl/auth/cliente/signup") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            EnaliHttpClientFactory.validarResposta(response)
            response.body<ClientePerfilResponse>()
        }

    private suspend fun postLogin(
        endpoint: String,
        email: String,
        senha: String
    ): Result<AuthLoginResponse> = EnaliHttpClientFactory.executarRequisicao {
        val response = httpClient.post(endpoint) {
            contentType(ContentType.Application.Json)
            setBody(AuthLoginRequest(email = email.trim(), senha = senha))
        }
        EnaliHttpClientFactory.validarResposta(response)
        response.body<AuthLoginResponse>()
    }
}