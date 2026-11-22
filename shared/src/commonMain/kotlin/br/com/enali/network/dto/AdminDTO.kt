package br.com.enali.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class AdminCadastroRequest(
    val nome: String,
    val email: String,
    val senha: String
)

@Serializable
data class AdminAtualizarRequest(
    val nome: String,
    val email: String
)

@Serializable
data class AdminPerfilResponse(
    val id: Long,
    val nome: String,
    val email: String,
    val ativo: Boolean,
    val nivelAcesso: String,
    val dataCriacao: String? = null
)