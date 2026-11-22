package br.com.enali.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class EstoqueRequest(
    val produtoId: Long,
    val quantidadeDisponivel: Int,
    val quantidadeReservada: Int,
    val estoqueMinimo: Int,
    val localizacao: String,
    val ativo: Boolean
)

@Serializable
data class EstoqueResponse(
    val id: Long,
    val produtoId: Long,
    val produtoNome: String? = null,
    val quantidadeDisponivel: Int,
    val quantidadeReservada: Int,
    val estoqueMinimo: Int,
    val localizacao: String,
    val ativo: Boolean
)