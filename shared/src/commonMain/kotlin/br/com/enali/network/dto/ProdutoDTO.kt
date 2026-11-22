package br.com.enali.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProdutoRequest(
    val nome: String,
    val descricao: String,
    val preco: Double,
    val disponivel: Boolean,
    val desconto: Double
)

@Serializable
data class ProdutoResponse(
    val id: Long,
    val nome: String,
    val descricao: String,
    val preco: Double,
    val disponivel: Boolean,
    val desconto: Double
)