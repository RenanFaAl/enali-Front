package br.com.enali.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProdutoRequest(
    val nome: String,
    val descricao: String,
    val preco: Double,
    val disponivel: Boolean,
    val desconto: Double
)

@Serializable
data class ProdutoImagemResponse(
    val id: Long,
    val produtoId: Long,
    val url: String,
    val tipoMime: String,
    val ordem: Int,
    val tamanhoBytes: Int
)

@Serializable
data class ProdutoResponse(
    val id: Long,
    val nome: String,
    val descricao: String,
    val preco: Double,
    val disponivel: Boolean,
    val desconto: Double,
    val imagensUrls: List<String> = emptyList()
)

data class ImagemUploadItem(
    val nomeArquivo: String,
    val tipoMime: String, // "image/jpeg", "image/png" ou "image/webp"
    val bytes: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ImagemUploadItem
        return nomeArquivo == other.nomeArquivo &&
            tipoMime == other.tipoMime &&
            bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = nomeArquivo.hashCode()
        result = 31 * result + tipoMime.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }
}