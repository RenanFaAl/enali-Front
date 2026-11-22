package br.com.enali.network.admin

import br.com.enali.network.EnaliHttpClientFactory
import br.com.enali.network.dto.AdminCadastroRequest
import br.com.enali.network.dto.AdminPerfilResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AdminApiService(
    private val httpClient: HttpClient,
    private val baseUrl: String = "http://localhost:8080"
) {
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun criarAdministrador(
        token: String,
        request: AdminCadastroRequest
    ): Result<AdminPerfilResponse> = EnaliHttpClientFactory.executarRequisicao {
        val response = httpClient.post("$baseUrl/admin") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess()) {
            val corpoErro = response.bodyAsText()
            val mensagemApi = extrairMensagemDaApi(corpoErro)
                ?: corpoErro.ifBlank { "Erro HTTP ${response.status.value}" }
            error(mensagemApi)
        }
        response.body<AdminPerfilResponse>()
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