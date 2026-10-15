
package br.com.enali.model

data class Produto(
    val id: Long = 0,
    val nome: String = "",
    val descricao: String = "",
    val preco: Double = 0.0,
    val disponivel: Boolean = true,
    val desconto: Double = 0.0
)
