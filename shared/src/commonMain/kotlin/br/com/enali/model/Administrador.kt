package br.com.enali.model

data class Administrador(
    val id: Long = 0,
    val nome: String = "",
    val email: String = "",
    val senha: String = "",
    val ativo: Boolean = true,
    val nivelAcesso: String = "ADMIN_COMUM",
    val dataCriacao: String = ""
)