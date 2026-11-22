package br.com.enali.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthLoginRequest(
    val email: String,
    val senha: String
)

@Serializable
data class AuthLoginResponse(
    val token: String,
    val tipo: String = "Bearer",
    val id: Long,
    val nome: String,
    val email: String,
    val nivelAcesso: String? = null,
    val tipoUsuario: String
)

@Serializable
data class AlterarSenhaRequest(
    val senhaAtual: String,
    val senhaNova: String,
    val confirmarSenha: String
)