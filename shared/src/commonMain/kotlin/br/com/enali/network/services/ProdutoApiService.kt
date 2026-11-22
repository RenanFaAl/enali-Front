package br.com.enali.network.services

import br.com.enali.network.EnaliConfig
import br.com.enali.network.EnaliHttpClientFactory
import br.com.enali.network.dto.ImagemUploadItem
import br.com.enali.network.dto.ProdutoImagemResponse
import br.com.enali.network.dto.ProdutoRequest
import br.com.enali.network.dto.ProdutoResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ProdutoApiService(
    private val httpClient: HttpClient,
    private val baseUrl: String = EnaliConfig.BASE_URL
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

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
        request: ProdutoRequest,
        imagens: List<ImagemUploadItem> = emptyList()
    ): Result<ProdutoResponse> =
        EnaliHttpClientFactory.executarRequisicao {
            val response = if (imagens.isEmpty()) {
                httpClient.post("$baseUrl/produtos") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            } else {
                val produtoJson = json.encodeToString(request)
                httpClient.submitFormWithBinaryData(
                    url = "$baseUrl/produtos",
                    formData = formData {
                        append(
                            key = "produto",
                            value = produtoJson,
                            headers = Headers.build {
                                append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                            }
                        )
                        imagens.forEach { img ->
                            append(
                                key = "imagens",
                                value = img.bytes,
                                headers = Headers.build {
                                    append(HttpHeaders.ContentType, img.tipoMime)
                                    append(
                                        HttpHeaders.ContentDisposition,
                                        "filename=\"${img.nomeArquivo}\""
                                    )
                                }
                            )
                        }
                    }
                ) {
                    method = HttpMethod.Post
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
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

    suspend fun listarImagens(produtoId: Long): Result<List<ProdutoImagemResponse>> =
        EnaliHttpClientFactory.executarRequisicao {
            val response = httpClient.get("$baseUrl/produtos/$produtoId/imagens")
            EnaliHttpClientFactory.validarResposta(response)
            response.body<List<ProdutoImagemResponse>>()
        }

    suspend fun adicionarImagem(
        token: String,
        produtoId: Long,
        imagem: ImagemUploadItem,
        ordem: Int? = null
    ): Result<ProdutoImagemResponse> =
        EnaliHttpClientFactory.executarRequisicao {
            val response = httpClient.submitFormWithBinaryData(
                url = "$baseUrl/produtos/$produtoId/imagens",
                formData = formData {
                    append(
                        key = "arquivo",
                        value = imagem.bytes,
                        headers = Headers.build {
                            append(HttpHeaders.ContentType, imagem.tipoMime)
                            append(
                                HttpHeaders.ContentDisposition,
                                "filename=\"${imagem.nomeArquivo}\""
                            )
                        }
                    )
                }
            ) {
                method = HttpMethod.Post
                header(HttpHeaders.Authorization, "Bearer $token")
                if (ordem != null) {
                    parameter("ordem", ordem)
                }
            }
            EnaliHttpClientFactory.validarResposta(response)
            response.body<ProdutoImagemResponse>()
        }
        
    suspend fun buscarImagemBytes(
        produtoId: Long,
        imagemId: Long
    ): Result<ByteArray> =
        EnaliHttpClientFactory.executarRequisicao {
            val response = httpClient.get("$baseUrl/produtos/$produtoId/imagens/$imagemId")
            EnaliHttpClientFactory.validarResposta(response)
            response.body<ByteArray>()
        }

    suspend fun removerImagem(
        token: String,
        produtoId: Long,
        imagemId: Long
    ): Result<Unit> =
        EnaliHttpClientFactory.executarRequisicao {
            val response = httpClient.delete("$baseUrl/produtos/$produtoId/imagens/$imagemId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            EnaliHttpClientFactory.validarResposta(response)
        }
}