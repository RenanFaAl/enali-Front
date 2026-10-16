package br.com.enali.model

data class Material(
    val id: Long = 0,
    val nome: String = "",
    val descricao: String = "",
    val quantidade: Int = 0,
    val custo: Double = 0.0,
    val disponivel: Boolean = true
)
