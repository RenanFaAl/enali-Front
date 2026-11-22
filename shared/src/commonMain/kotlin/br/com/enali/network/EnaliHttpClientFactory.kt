package br.com.enali.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object EnaliHttpClientFactory {
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun criar(): HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }
            )
        }
    }

    suspend fun validarResposta(response: HttpResponse) {
        if (!response.status.isSuccess()) {
            val corpoErro = response.bodyAsText()
            val mensagemApi = extrairMensagemDaApi(corpoErro)
                ?: corpoErro.ifBlank { "Erro HTTP ${response.status.value}" }
            error(mensagemApi)
        }
    }

    fun extrairMensagemDaApi(corpo: String): String? {
        if (corpo.isBlank()) return null
        return runCatching {
            val obj = jsonParser.parseToJsonElement(corpo).jsonObject
            listOf("mensagem", "message", "error", "erro", "detail")
                .firstNotNullOfOrNull { chave ->
                    obj[chave]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
                }
        }.getOrNull()
    }

    suspend fun <T> executarRequisicao(
        mensagem401: String? = null,
        bloco: suspend () -> T
    ): Result<T> = runCatching {
        bloco()
    }.recoverCatching { throwable ->
        if (throwable is IllegalStateException && !throwable.message.isNullOrBlank()) {
            throw throwable
        }
        val mensagem = when (throwable) {
            is ClientRequestException -> {
                if (throwable.response.status.value == 401 && mensagem401 != null) {
                    mensagem401
                } else {
                    throwable.response.bodyAsText()
                        .ifBlank { "Erro na requisição (${throwable.response.status.value})." }
                }
            }
            is ServerResponseException ->
                "Erro interno no servidor (${throwable.response.status.value})."
            else ->
                throwable.message ?: "Não foi possível conectar à API."
        }
        throw IllegalStateException(mensagem, throwable)
    }
}