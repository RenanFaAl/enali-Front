package br.com.enali.network.repository

import br.com.enali.network.EnaliConfig
import br.com.enali.network.dto.ImagemUploadItem
import br.com.enali.network.dto.PedidoImagemResponse
import br.com.enali.network.dto.PedidoRequest
import br.com.enali.network.dto.PedidoResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

class PedidoApiRepository(
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

    // Fotos das observações do pedido
    suspend fun enviarFoto(token: String, pedidoId: Long, foto: ImagemUploadItem): Result<PedidoImagemResponse> =
        chamarApi {
            httpClient.submitFormWithBinaryData(
                url = "$baseUrl/pedidos/clientes/me/pedidos/$pedidoId/imagens",
                formData = formData {
                    append("arquivo", foto.bytes, Headers.build {
                        append(HttpHeaders.ContentType, foto.tipoMime)
                        append(HttpHeaders.ContentDisposition, "filename=\"${foto.nomeArquivo}\"")
                    })
                }
            ) { autenticar(token) }
        }

    // admin = true usa as rotas administrativas; false, as do cliente logado
    suspend fun listarFotos(token: String, pedidoId: Long, admin: Boolean): Result<List<PedidoImagemResponse>> =
        chamarApi { httpClient.get("${rotaFotos(pedidoId, admin)}") { autenticar(token) } }

    suspend fun baixarFoto(token: String, pedidoId: Long, fotoId: Long, admin: Boolean): Result<ByteArray> =
        chamarApi { httpClient.get("${rotaFotos(pedidoId, admin)}/$fotoId") { autenticar(token) } }

    private fun rotaFotos(pedidoId: Long, admin: Boolean) =
        if (admin) "$baseUrl/pedidos/$pedidoId/imagens" else "$baseUrl/pedidos/clientes/me/pedidos/$pedidoId/imagens"
}
