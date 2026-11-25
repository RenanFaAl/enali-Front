package br.com.enali.network.services

import br.com.enali.network.EnaliConfig
import br.com.enali.network.EnaliHttpClientFactory
import br.com.enali.network.dto.AdminAtualizarRequest
import br.com.enali.network.dto.AdminCadastroRequest
import br.com.enali.network.dto.AdminPerfilResponse
import br.com.enali.network.dto.AlterarSenhaRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class AdminApiService(
    private val httpClient: HttpClient,
    private val baseUrl: String = EnaliConfig.BASE_URL
) {
    suspend fun buscarMeusDados(token: String): Result<AdminPerfilResponse> =
        EnaliHttpClientFactory.executarRequisicao {
            val response = httpClient.get("$baseUrl/admin/me") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            EnaliHttpClientFactory.validarResposta(response)
            response.body<AdminPerfilResponse>()
        }

    suspend fun atualizarMeusDados(
        token: String,
        request: AdminAtualizarRequest
    ): Result<AdminPerfilResponse> =
        EnaliHttpClientFactory.executarRequisicao {
            val response = httpClient.put("$baseUrl/admin/me") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            EnaliHttpClientFactory.validarResposta(response)
            response.body<AdminPerfilResponse>()
        }

    suspend fun alterarMinhaSenha(
        token: String,
        request: AlterarSenhaRequest
    ): Result<Unit> =
        EnaliHttpClientFactory.executarRequisicao {
            val response = httpClient.patch("$baseUrl/admin/me/senha") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            EnaliHttpClientFactory.validarResposta(response)
        }

    suspend fun criarAdministrador(
        token: String,
        request: AdminCadastroRequest
    ): Result<AdminPerfilResponse> =
        EnaliHttpClientFactory.executarRequisicao {
            val response = httpClient.post("$baseUrl/admin") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            EnaliHttpClientFactory.validarResposta(response)
            response.body<AdminPerfilResponse>()
        }
}