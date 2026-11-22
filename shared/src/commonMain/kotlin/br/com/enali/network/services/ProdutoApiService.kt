package br.com.enali.network.services

import br.com.enali.network.EnaliConfig
import br.com.enali.network.EnaliHttpClientFactory
import br.com.enali.network.dto.ProdutoRequest
import br.com.enali.network.dto.ProdutoResponse
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

class ProdutoApiService(
    private val httpClient: HttpClient,
    private val baseUrl: String = EnaliConfig.BASE_URL
) {
    suspend fun listarProdutos(): Result<List<ProdutoResponse>> =
        EnaliHttpClientFactory.executarRequisicao {
            val response = httpClient.get("$baseUrl/produtos")
            EnaliHttpClientFactory.validarResposta(response)
            response.body<List<ProdutoResponse>>()
        }

    suspend fun buscarProdutoPorId(id: Long): Result<ProdutoResponse> =
        EnaliHttpClientFactory.executarRequisicao {
            val response = httpClient.get("$baseUrl/produtos/$id")
            EnaliHttpClientFactory.validarResposta(response)
            response.body<ProdutoResponse>()
        }

    suspend fun buscarProdutosPorNome(nome: String): Result<List<ProdutoResponse>> =
        EnaliHttpClientFactory.executarRequisicao {
            val response = httpClient.get("$baseUrl/produtos/buscar") {
                parameter("nome", nome)
            }
            EnaliHttpClientFactory.validarResposta(response)
            response.body<List<ProdutoResponse>>()
        }

    suspend fun cadastrarProduto(
        token: String,
        request: ProdutoRequest
    ): Result<ProdutoResponse> =
        EnaliHttpClientFactory.executarRequisicao {
            val response = httpClient.post("$baseUrl/produtos") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            EnaliHttpClientFactory.validarResposta(response)
            response.body<ProdutoResponse>()
        }

    suspend fun atualizarProduto(
        token: String,
        id: Long,
        request: ProdutoRequest
    ): Result<ProdutoResponse> =
        EnaliHttpClientFactory.executarRequisicao {
            val response = httpClient.put("$baseUrl/produtos/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            EnaliHttpClientFactory.validarResposta(response)
            response.body<ProdutoResponse>()
        }

    suspend fun excluirProduto(
        token: String,
        id: Long
    ): Result<Unit> =
        EnaliHttpClientFactory.executarRequisicao {
            val response = httpClient.delete("$baseUrl/produtos/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            EnaliHttpClientFactory.validarResposta(response)
        }
}