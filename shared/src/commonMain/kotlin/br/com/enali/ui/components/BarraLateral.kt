
package br.com.enali.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.enali.ui.theme.CorBegeMedio
import br.com.enali.ui.theme.CorBranco
import br.com.enali.ui.theme.CorMarromEscuro

@Composable
fun ItemMenuLateral(
    titulo: String,
    icone: ImageVector,
    estaSelecionado: Boolean,
    aoClicar: () -> Unit,
    compacto: Boolean = false
) {
    val corItem = if (estaSelecionado) CorBegeMedio else CorBranco
    val corTexto = if (estaSelecionado) CorMarromEscuro else CorBranco

    if (compacto) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 3.dp)
                .background(
                    if (estaSelecionado) CorBegeMedio else CorMarromEscuro,
                    RoundedCornerShape(10.dp)
                )
                .clickable { aoClicar() }
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icone,
                contentDescription = titulo,
                tint = corTexto,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                titulo,
                color = corTexto,
                style = MaterialTheme.typography.labelSmall
            )
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 3.dp)
                .background(
                    if (estaSelecionado) CorBegeMedio else CorMarromEscuro,
                    RoundedCornerShape(10.dp)
                )
                .clickable { aoClicar() }
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                icone,
                contentDescription = titulo,
                tint = corItem,
                modifier = Modifier.size(21.dp)
            )
            Text(
                titulo,
                color = corTexto,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (estaSelecionado) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Normal
                }
            )
        }
    }
}

@Composable
fun BarraLateral(
    telaAtual: String,
    aoMudarTela: (String) -> Unit,
    compacto: Boolean = false
) {
    Column(
        modifier = Modifier
            .width(if (compacto) 86.dp else 194.dp)
            .fillMaxHeight()
            .background(CorMarromEscuro)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (compacto) "E" else "Enali",
            style = if (compacto) {
                MaterialTheme.typography.headlineMedium
            } else {
                MaterialTheme.typography.headlineSmall
            },
            fontWeight = FontWeight.Bold,
            color = CorBranco,
            modifier = Modifier.padding(top = 24.dp, bottom = 22.dp)
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 14.dp),
            color = CorBegeMedio.copy(alpha = 0.55f)
        )
        Spacer(Modifier.height(12.dp))

        ItemMenuLateral(
            "Admin",
            Icons.Default.Person,
            telaAtual == "ADMINISTRADOR",
            { aoMudarTela("ADMINISTRADOR") },
            compacto
        )
        ItemMenuLateral(
            "Endereço",
            Icons.Default.LocationOn,
            telaAtual == "ENDERECO",
            { aoMudarTela("ENDERECO") },
            compacto
        )
        ItemMenuLateral(
            "Produto",
            Icons.Default.ShoppingCart,
            telaAtual == "PRODUTO",
            { aoMudarTela("PRODUTO") },
            compacto
        )
        ItemMenuLateral(
            "Estoque",
            Icons.Default.List,
            telaAtual == "ESTOQUE",
            { aoMudarTela("ESTOQUE") },
            compacto
        )
        ItemMenuLateral(
            "Cliente",
            Icons.Default.Person,
            telaAtual == "CLIENTE",
            { aoMudarTela("CLIENTE") },
            compacto
        )
        ItemMenuLateral(
            "Pedido",
            Icons.Default.Receipt,
            telaAtual == "PEDIDO",
            { aoMudarTela("PEDIDO") },
            compacto
        )
        ItemMenuLateral(
            "Material",
            Icons.Default.Build,
            telaAtual == "MATERIAL",
            { aoMudarTela("MATERIAL") },
            compacto
        )
        ItemMenuLateral(
            "Fornecedor",
            Icons.Default.LocalShipping,
            telaAtual == "FORNECEDOR",
            { aoMudarTela("FORNECEDOR") },
            compacto
        )

        Spacer(Modifier.height(18.dp))
    }
}
