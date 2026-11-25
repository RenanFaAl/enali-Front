package br.com.enali.network.repository

import br.com.enali.network.EnaliConfig
import br.com.enali.network.dto.AtivoRequest
import br.com.enali.network.dto.FornecedorRequest
import br.com.enali.network.dto.FornecedorResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put

class FornecedorApiRepository(
    private val httpClient: HttpClient,
    private val baseUrl: String = EnaliConfig.BASE_URL
) {
    suspend fun listar(token: String): Result<List<FornecedorResponse>> =
        chamarApi { httpClient.get("$baseUrl/fornecedores") { autenticar(token) } }

    suspend fun cadastrar(token: String, request: FornecedorRequest): Result<FornecedorResponse> =
        chamarApi { httpClient.post("$baseUrl/fornecedores") { autenticar(token); json(request) } }

    suspend fun atualizar(token: String, id: Long, request: FornecedorRequest): Result<FornecedorResponse> =
        chamarApi { httpClient.put("$baseUrl/fornecedores/$id") { autenticar(token); json(request) } }

    suspend fun alterarAtivo(token: String, id: Long, ativo: Boolean): Result<FornecedorResponse> =
        chamarApi { httpClient.patch("$baseUrl/fornecedores/$id/ativo") { autenticar(token); json(AtivoRequest(ativo)) } }

    suspend fun excluir(token: String, id: Long): Result<Unit> =
        chamarApi { httpClient.delete("$baseUrl/fornecedores/$id") { autenticar(token) } }
}
