package br.com.enali.ui.components

import br.com.enali.network.dto.ImagemUploadItem

// Na web o upload ainda não é suportado; as imagens já cadastradas continuam aparecendo.
actual fun selecionarImagensProduto(
    permiteMultiplas: Boolean,
    onSelecionadas: (List<ImagemUploadItem>) -> Unit,
    onErro: (String) -> Unit
) {
    onErro("Envio de imagem disponível apenas no desktop e Android.")
}
