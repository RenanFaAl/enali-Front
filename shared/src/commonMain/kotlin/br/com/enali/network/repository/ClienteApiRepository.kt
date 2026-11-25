package br.com.enali.network.services

import br.com.enali.network.EnaliConfig
import br.com.enali.network.EnaliHttpClientFactory
import br.com.enali.network.dto.AlterarSenhaRequest
import br.com.enali.network.dto.ClienteAtualizarRequest
import br.com.enali.network.dto.ClientePerfilResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class ClienteApiService(
    private val httpClient: HttpClient,
    private val baseUrl: String = EnaliConfig.BASE_URL
) {
    suspend fun buscarMeusDados(token: String): Result<ClientePerfilResponse> =
        EnaliHttpClientFactory.executarRequisicao {
            val response = httpClient.get("$baseUrl/clientes/me") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            EnaliHttpClientFactory.validarResposta(response)
            response.body<ClientePerfilResponse>()
        }

    suspend fun atualizarMeusDados(
        token: String,
        request: ClienteAtualizarRequest
    ): Result<ClientePerfilResponse> =
        EnaliHttpClientFactory.executarRequisicao {
            val response = httpClient.put("$baseUrl/clientes/me") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            EnaliHttpClientFactory.validarResposta(response)
            response.body<ClientePerfilResponse>()
        }

    suspend fun alterarMinhaSenha(
        token: String,
        request: AlterarSenhaRequest
    ): Result<Unit> =
        EnaliHttpClientFactory.executarRequisicao {
            val response = httpClient.patch("$baseUrl/clientes/me/senha") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            EnaliHttpClientFactory.validarResposta(response)
        }
}