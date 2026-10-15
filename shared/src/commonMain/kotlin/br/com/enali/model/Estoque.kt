
package br.com.enali.model

data class Estoque(
    val id: Long = 0,
    val produtoId: Long = 0,
    val produtoNome: String = "",
    val quantidadeDisponivel: Int = 0,
    val quantidadeReservada: Int = 0,
    val estoqueMinimo: Int = 0,
    val localizacao: String = "",
    val ativo: Boolean = true,
    val dataAtualizacao: String = ""
)
