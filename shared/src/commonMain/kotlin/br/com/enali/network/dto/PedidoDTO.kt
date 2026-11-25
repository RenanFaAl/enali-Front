package br.com.enali.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class ItemPedidoRequest(
    val produtoId: Long,
    val quantidade: Int
)

@Serializable
data class PedidoRequest(
    val enderecoId: Long,
    val itens: List<ItemPedidoRequest>,
    val observacoes: String? = null
)

@Serializable
data class ItemPedidoResponse(
    val produtoId: Long,
    val nome: String,
    val precoUnitario: Double,
    val desconto: Double = 0.0,
    val quantidade: Int,
    val subtotal: Double
)

@Serializable
data class PedidoResponse(
    val id: Long,
    val numeroPedido: String,
    val valorTotal: Double,
    val dataPedido: String,
    val status: String,
    val observacoes: String? = null,
    val clienteNome: String = "",
    val clienteEmail: String = "",
    val enderecoEntrega: EnderecoResponse? = null,
    val itens: List<ItemPedidoResponse> = emptyList()
) {
    val cancelado: Boolean get() = status.equals("CANCELADO", ignoreCase = true)
}
