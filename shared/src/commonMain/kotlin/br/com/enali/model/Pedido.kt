package br.com.enali.model

data class Pedido(
    val id: Long? = null,
    val numeroPedido: String,
    val valorTotal: Double,
    val dataPedido: String,
    val status: String,
    val observacoes: String? = null
)