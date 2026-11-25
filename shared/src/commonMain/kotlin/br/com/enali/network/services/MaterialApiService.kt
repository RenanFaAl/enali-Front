package br.com.enali.network.services

import br.com.enali.network.EnaliConfig
import br.com.enali.network.dto.DisponivelRequest
import br.com.enali.network.dto.MaterialRequest
import br.com.enali.network.dto.MaterialResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put

class MaterialApiService(
    private val httpClient: HttpClient,
    private val baseUrl: String = EnaliConfig.BASE_URL
) {
    suspend fun listar(token: String): Result<List<MaterialResponse>> =
        chamarApi { httpClient.get("$baseUrl/materiais") { autenticar(token) } }

    suspend fun cadastrar(token: String, request: MaterialRequest): Result<MaterialResponse> =
        chamarApi { httpClient.post("$baseUrl/materiais") { autenticar(token); json(request) } }

    suspend fun atualizar(token: String, id: Long, request: MaterialRequest): Result<MaterialResponse> =
        chamarApi { httpClient.put("$baseUrl/materiais/$id") { autenticar(token); json(request) } }

    suspend fun alterarDisponivel(token: String, id: Long, disponivel: Boolean): Result<MaterialResponse> =
        chamarApi {
            httpClient.patch("$baseUrl/materiais/$id/disponivel") { autenticar(token); json(DisponivelRequest(disponivel)) }
        }

    suspend fun excluir(token: String, id: Long): Result<Unit> =
        chamarApi { httpClient.delete("$baseUrl/materiais/$id") { autenticar(token) } }
}
