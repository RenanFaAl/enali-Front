package br.com.enali.model

data class Fornecedor(
    val id: Long = 0,
    val nome: String = "",
    val cnpj: String = "",
    val email: String = "",
    val telefone: String = "",
    val ativo: Boolean = true
)
