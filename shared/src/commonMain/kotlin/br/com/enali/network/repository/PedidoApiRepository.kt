package br.com.enali.network.services

import br.com.enali.network.EnaliConfig
import br.com.enali.network.dto.PedidoRequest
import br.com.enali.network.dto.PedidoResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post

class PedidoApiService(
    private val httpClient: HttpClient,
    private val baseUrl: String = EnaliConfig.BASE_URL
) {
    // Cliente logado
    suspend fun realizar(token: String, request: PedidoRequest): Result<PedidoResponse> =
        chamarApi { httpClient.post("$baseUrl/pedidos/clientes/me/pedidos") { autenticar(token); json(request) } }

    suspend fun listarMeus(token: String): Result<List<PedidoResponse>> =
        chamarApi { httpClient.get("$baseUrl/pedidos/clientes/me/pedidos") { autenticar(token) } }

    suspend fun cancelarMeu(token: String, id: Long): Result<PedidoResponse> =
        chamarApi { httpClient.patch("$baseUrl/pedidos/clientes/me/pedidos/$id/cancelar") { autenticar(token) } }

    // Administrador
    suspend fun listarTodos(token: String): Result<List<PedidoResponse>> =
        chamarApi { httpClient.get("$baseUrl/pedidos") { autenticar(token) } }

    suspend fun cancelar(token: String, id: Long): Result<PedidoResponse> =
        chamarApi { httpClient.patch("$baseUrl/pedidos/$id/cancelar") { autenticar(token) } }
}
