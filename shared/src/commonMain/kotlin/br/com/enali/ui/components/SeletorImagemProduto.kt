package br.com.enali.ui.components

import br.com.enali.network.dto.ImagemUploadItem

expect fun selecionarImagensProduto(
    permiteMultiplas: Boolean = true,
    onSelecionadas: (List<ImagemUploadItem>) -> Unit,
    onErro: (String) -> Unit
)