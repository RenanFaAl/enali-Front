package br.com.enali.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import enalifront.shared.generated.resources.Res
import enalifront.shared.generated.resources.icone_enali
import enalifront.shared.generated.resources.logo_enali
import org.jetbrains.compose.resources.painterResource

@Composable
fun logoEnali(): Painter = painterResource(Res.drawable.logo_enali)

@Composable
fun iconeEnali(): Painter = painterResource(Res.drawable.icone_enali)
