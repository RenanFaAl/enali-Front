package br.com.enali.model

data class Endereco(
    val id: Long = 0,
    val rua: String = "",
    val numero: String = "",
    val complemento: String = "",
    val bairro: String = "",
    val cidade: String = "",
    val estado: String = "",
    val siglaEstado: String = "",
    val cep: String = "",
    val pais: String = "Brasil",
    val referencia: String = "",
    val principal: Boolean = false,
    val ativo: Boolean = true,
    val dataCadastro: String = ""
)