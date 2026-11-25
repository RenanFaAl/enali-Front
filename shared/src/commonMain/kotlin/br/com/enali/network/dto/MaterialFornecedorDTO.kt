package br.com.enali.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class MaterialRequest(
    val nome: String,
    val descricao: String? = null,
    val quantidade: Int,
    val custo: Double,
    val disponivel: Boolean = true
)

@Serializable
data class MaterialResponse(
    val id: Long,
    val nome: String,
    val descricao: String? = null,
    val quantidade: Int,
    val custo: Double,
    val disponivel: Boolean
)

@Serializable
data class FornecedorRequest(
    val nome: String,
    val cnpj: String,
    val email: String,
    val telefone: String
)

@Serializable
data class FornecedorResponse(
    val id: Long,
    val nome: String,
    val cnpj: String,
    val email: String,
    val telefone: String,
    val ativo: Boolean
)

@Serializable
data class DisponivelRequest(val disponivel: Boolean)

@Serializable
data class AtivoRequest(val ativo: Boolean)
