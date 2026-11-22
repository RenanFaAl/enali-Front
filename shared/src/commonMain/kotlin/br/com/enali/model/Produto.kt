package br.com.enali.model

data class Produto(
    val id: Long,
    val nome: String,
    val descricao: String,
    val preco: Double,
    val disponivel: Boolean = true,
    val desconto: Double = 0.0,
    val imagensUrls: List<String> = emptyList()
)