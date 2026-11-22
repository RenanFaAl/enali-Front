package br.com.enali.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class ClienteCadastroRequest(
    val nome: String,
    val email: String,
    val senha: String,
    val cpf: String,
    val dataNascimento: String // Formato ISO: "AAAA-MM-DD"
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