package br.com.enali.network.repository

import br.com.enali.network.EnaliHttpClientFactory
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

// Executa a requisição, transforma erro HTTP na mensagem da API e lê o corpo (Unit = resposta sem corpo)
internal suspend inline fun <reified T> chamarApi(
    crossinline requisicao: suspend () -> HttpResponse
): Result<T> = EnaliHttpClientFactory.executarRequisicao {
    val response = requisicao()
    EnaliHttpClientFactory.validarResposta(response)
    @Suppress("UNCHECKED_CAST")
    if (T::class == Unit::class) Unit as T else response.body<T>()
}

internal fun HttpRequestBuilder.autenticar(token: String) {
    header(HttpHeaders.Authorization, "Bearer $token")
}

internal inline fun <reified T> HttpRequestBuilder.json(corpo: T) {
    contentType(ContentType.Application.Json)
    setBody(corpo)
}
