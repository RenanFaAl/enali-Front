package br.com.enali.network.repository

import br.com.enali.network.EnaliConfig
import br.com.enali.network.EnaliHttpClientFactory
import br.com.enali.network.dto.EstoqueRequest
import br.com.enali.network.dto.EstoqueResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

class EstoqueApiRepository(
    private val httpClient: HttpClient,
    private val baseUrl: String = EnaliConfig.BASE_URL
) {
    suspend fun listarEstoques(token: String): Result<List<EstoqueResponse>> =
        EnaliHttpClientFactory.executarRequisicao {
            val response = httpClient.get("$baseUrl/estoques") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            EnaliHttpClientFactory.validarResposta(response)
            response.body<List<EstoqueResponse>>()
        }

    suspend fun buscarEstoquePorId(token: String, id: Long): Result<EstoqueResponse> =
        EnaliHttpClientFactory.executarRequisicao {
            val response = httpClient.get("$baseUrl/estoques/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            EnaliHttpClientFactory.validarResposta(response)
            response.body<EstoqueResponse>()
        }

    suspend fun buscarEstoquePorProdutoId(produtoId: Long): Result<EstoqueResponse> =
        EnaliHttpClientFactory.executarRequisicao {
            val response = httpClient.get("$baseUrl/estoques/produto/$produtoId")
            EnaliHttpClientFactory.validarResposta(response)
            response.body<EstoqueResponse>()
        }

    suspend fun buscarEstoquesPorLocalizacao(
        token: String,
        localizacao: String
    ): Result<List<EstoqueResponse>> =
        EnaliHttpClientFactory.executarRequisicao {
            val response = httpClient.get("$baseUrl/estoques/buscar") {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("localizacao", localizacao)
            }
            EnaliHttpClientFactory.validarResposta(response)
            response.body<List<EstoqueResponse>>()
        }

    suspend fun cadastrarEstoque(
        token: String,
        request: EstoqueRequest
    ): Result<EstoqueResponse> =
        EnaliHttpClientFactory.executarRequisicao {
            val response = httpClient.post("$baseUrl/estoques") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            EnaliHttpClientFactory.validarResposta(response)
            response.body<EstoqueResponse>()
        }

    suspend fun atualizarEstoque(
        token: String,
        id: Long,
        request: EstoqueRequest
    ): Result<EstoqueResponse> =
        EnaliHttpClientFactory.executarRequisicao {
            val response = httpClient.put("$baseUrl/estoques/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            EnaliHttpClientFactory.validarResposta(response)
            response.body<EstoqueResponse>()
        }

    suspend fun excluirEstoque(
        token: String,
        id: Long
    ): Result<Unit> =
        EnaliHttpClientFactory.executarRequisicao {
            val response = httpClient.delete("$baseUrl/estoques/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            EnaliHttpClientFactory.validarResposta(response)
        }
}