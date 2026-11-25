package br.com.enali.network.dto

import kotlinx.serialization.Serializable

// Mesmo corpo para endereço do cliente e do fornecedor (EnderecoRequestDTO no backend)
@Serializable
data class EnderecoRequest(
    val rua: String,
    val numero: String,
    val complemento: String? = null,
    val bairro: String,
    val cidade: String,
    val estado: String,
    val siglaEstado: String,
    val cep: String,
    val pais: String = "Brasil",
    val referencia: String? = null,
    val principal: Boolean = false
)

@Serializable
data class EnderecoResponse(
    val id: Long,
    val rua: String,
    val numero: String,
    val complemento: String? = null,
    val bairro: String,
    val cidade: String,
    val estado: String,
    val siglaEstado: String,
    val cep: String,
    val pais: String,
    val referencia: String? = null,
    val principal: Boolean = false,
    val ativo: Boolean = true,
    val dono: String? = null // "Cliente: Nome" ou "Fornecedor: Nome"
) {
    fun resumo(): String = buildString {
        append("$rua, $numero")
        if (!complemento.isNullOrBlank()) append(" - $complemento")
        append(" • $bairro, $cidade/$siglaEstado • CEP $cep")
    }
}
