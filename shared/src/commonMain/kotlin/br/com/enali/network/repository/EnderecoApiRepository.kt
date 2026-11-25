package br.com.enali.network.repository

import br.com.enali.network.EnaliConfig
import br.com.enali.network.dto.EnderecoRequest
import br.com.enali.network.dto.EnderecoResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put

class EnderecoApiRepository(
    private val httpClient: HttpClient,
    private val baseUrl: String = EnaliConfig.BASE_URL
) {
    // Cliente logado: /clientes/me/enderecos
    suspend fun listarMeus(token: String): Result<List<EnderecoResponse>> =
        chamarApi { httpClient.get("$baseUrl/clientes/me/enderecos") { autenticar(token) } }

    suspend fun cadastrarMeu(token: String, request: EnderecoRequest): Result<EnderecoResponse> =
        chamarApi { httpClient.post("$baseUrl/clientes/me/enderecos") { autenticar(token); json(request) } }

    suspend fun atualizarMeu(token: String, id: Long, request: EnderecoRequest): Result<EnderecoResponse> =
        chamarApi { httpClient.put("$baseUrl/clientes/me/enderecos/$id") { autenticar(token); json(request) } }

    suspend fun excluirMeu(token: String, id: Long): Result<Unit> =
        chamarApi { httpClient.delete("$baseUrl/clientes/me/enderecos/$id") { autenticar(token) } }

    // Administrador: consulta de todos os endereços
    suspend fun listarTodos(token: String): Result<List<EnderecoResponse>> =
        chamarApi { httpClient.get("$baseUrl/enderecos") { autenticar(token) } }

    // Administrador: /fornecedores/{id}/enderecos
    suspend fun listarDoFornecedor(token: String, fornecedorId: Long): Result<List<EnderecoResponse>> =
        chamarApi { httpClient.get("$baseUrl/fornecedores/$fornecedorId/enderecos") { autenticar(token) } }

    suspend fun cadastrarDoFornecedor(
        token: String,
        fornecedorId: Long,
        request: EnderecoRequest
    ): Result<EnderecoResponse> = chamarApi {
        httpClient.post("$baseUrl/fornecedores/$fornecedorId/enderecos") { autenticar(token); json(request) }
    }

    suspend fun atualizarDoFornecedor(
        token: String,
        fornecedorId: Long,
        id: Long,
        request: EnderecoRequest
    ): Result<EnderecoResponse> = chamarApi {
        httpClient.put("$baseUrl/fornecedores/$fornecedorId/enderecos/$id") { autenticar(token); json(request) }
    }

    suspend fun excluirDoFornecedor(token: String, fornecedorId: Long, id: Long): Result<Unit> =
        chamarApi { httpClient.delete("$baseUrl/fornecedores/$fornecedorId/enderecos/$id") { autenticar(token) } }
}
