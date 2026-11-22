package br.com.enali.network.auth

import br.com.enali.network.EnaliHttpClientFactory
import br.com.enali.network.dto.AuthLoginRequest
import br.com.enali.network.dto.AuthLoginResponse
import br.com.enali.network.dto.ClienteCadastroRequest
import br.com.enali.network.dto.ClientePerfilResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AuthApiService(
    private val httpClient: HttpClient,
    private val baseUrl: String = "http://localhost:8080"
) {
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

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
            if (!response.status.isSuccess()) {
                val corpoErro = response.bodyAsText()
                val mensagemApi = extrairMensagemDaApi(corpoErro)
                    ?: corpoErro.ifBlank { "Erro HTTP ${response.status.value}" }
                error(mensagemApi)
            }
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

        if (!response.status.isSuccess()) {
            val corpoErro = response.bodyAsText()
            val mensagemApi = extrairMensagemDaApi(corpoErro)
                ?: corpoErro.ifBlank { "Erro HTTP ${response.status.value}" }
            error(mensagemApi)
        }

        response.body<AuthLoginResponse>()
    }

    private fun extrairMensagemDaApi(corpo: String): String? {
        if (corpo.isBlank()) return null
        return runCatching {
            val obj = jsonParser.parseToJsonElement(corpo).jsonObject
            listOf("mensagem", "message", "error", "erro", "detail")
                .firstNotNullOfOrNull { chave ->
                    obj[chave]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
                }
        }.getOrNull()
    }
}