package br.com.enali.model

data class Cliente(
    val id: Long? = null,
    val nome: String,
    val email: String,
    val senha: String,
    val cpf: String,
    val dataNascimento: String,
    val ativo: Boolean = true
)