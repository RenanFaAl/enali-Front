package br.com.enali.ui.components

import br.com.enali.network.dto.ImagemUploadItem
import java.io.File
import java.nio.file.Files
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

private const val MAX_TAMANHO_BYTES = 5 * 1024 * 1024

private fun detectarTipoMime(arquivo: File): String? {
    val mimePorExtensao = when (arquivo.extension.lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "webp" -> "image/webp"
        else -> null
    }
    if (mimePorExtensao != null) return mimePorExtensao

    val probed = runCatching { Files.probeContentType(arquivo.toPath())?.lowercase() }.getOrNull()
    return when (probed) {
        "image/jpeg", "image/png", "image/webp" -> probed
        else -> null
    }
}

actual fun selecionarImagensProduto(
    permiteMultiplas: Boolean,
    onSelecionadas: (List<ImagemUploadItem>) -> Unit,
    onErro: (String) -> Unit
) {
    val chooser = JFileChooser().apply {
        dialogTitle = if (permiteMultiplas) {
            "Selecionar imagens do produto (JPEG, PNG ou WebP - até 5 MB)"
        } else {
            "Selecionar imagem do produto (JPEG, PNG ou WebP - até 5 MB)"
        }
        isMultiSelectionEnabled = permiteMultiplas
        fileSelectionMode = JFileChooser.FILES_ONLY
        isAcceptAllFileFilterUsed = false
        addChoosableFileFilter(
            FileNameExtensionFilter("Imagens (JPG, JPEG, PNG, WebP)", "jpg", "jpeg", "png", "webp")
        )
    }

    if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return

    val arquivos = if (permiteMultiplas) {
        chooser.selectedFiles?.toList().orEmpty()
    } else {
        listOfNotNull(chooser.selectedFile)
    }

    if (arquivos.isEmpty()) return

    val itens = mutableListOf<ImagemUploadItem>()
    for (arquivo in arquivos) {
        if (!arquivo.exists() || arquivo.length() == 0L) {
            onErro("O arquivo \"${arquivo.name}\" está vazio ou não existe.")
            return
        }
        if (arquivo.length() > MAX_TAMANHO_BYTES) {
            onErro("A imagem \"${arquivo.name}\" excede o limite máximo de 5 MB.")
            return
        }
        val tipoMime = detectarTipoMime(arquivo)
        if (tipoMime == null) {
            onErro("Formato não suportado em \"${arquivo.name}\". Use JPEG, PNG ou WebP.")
            return
        }
        val bytes = runCatching { arquivo.readBytes() }.getOrElse {
            onErro("Não foi possível ler o arquivo \"${arquivo.name}\".")
            return
        }
        itens.add(
            ImagemUploadItem(
                nomeArquivo = arquivo.name,
                tipoMime = tipoMime,
                bytes = bytes
            )
        )
    }

    onSelecionadas(itens)
}