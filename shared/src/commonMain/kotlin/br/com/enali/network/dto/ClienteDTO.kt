package br.com.enali.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class ClienteCadastroRequest(
    val nome: String,
    val email: String,
    val senha: String,
    val cpf: String,
    val dataNascimento: String
)

@Serializable
data class ClienteAtualizarRequest(
    val nome: String,
    val email: String,
    val cpf: String,
    val dataNascimento: String
)

@Serializable
data class ClientePerfilResponse(
    val id: Long,
    val nome: String,
    val email: String,
    val cpf: String,
    val dataNascimento: String,
    val ativo: Boolean
)