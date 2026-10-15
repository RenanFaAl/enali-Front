package br.com.enali.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.enali.ui.theme.CorBegeMedio
import br.com.enali.ui.theme.CorFundoClaro
import br.com.enali.ui.theme.CorMarromEscuro

@Composable
fun ItemMenuLateral(
    titulo: String,
    icone: ImageVector,
    estaSelecionado: Boolean,
    aoClicar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (estaSelecionado) CorFundoClaro else CorBegeMedio)
            .clickable { aoClicar() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icone,
            contentDescription = titulo,
            tint = CorMarromEscuro,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = titulo,
            color = CorMarromEscuro,
            fontWeight = if (estaSelecionado) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun BarraLateral(
    telaAtual: String,
    aoMudarTela: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .width(90.dp)
            .fillMaxHeight()
            .background(CorBegeMedio),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        ItemMenuLateral(
            titulo = "Admin",
            icone = Icons.Default.Person,
            estaSelecionado = (telaAtual == "ADMINISTRADOR"),
            aoClicar = { aoMudarTela("ADMINISTRADOR") }
        )

        ItemMenuLateral(
            titulo = "Endereço",
            icone = Icons.Default.LocationOn,
            estaSelecionado = (telaAtual == "ENDERECO"),
            aoClicar = { aoMudarTela("ENDERECO") }
        )


    }
}