package br.com.enali.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.enali.auth.SessaoUsuario
import br.com.enali.ui.theme.CorBegeMedio
import br.com.enali.ui.theme.CorBranco
import br.com.enali.ui.theme.CorMarromEscuro

private data class ItemMenuConfig(
    val rota: String,
    val titulo: String,
    val icone: ImageVector
)

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
                fontWeight = if (estaSelecionado) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun BarraLateral(
    sessao: SessaoUsuario,
    telaAtual: String,
    aoMudarTela: (String) -> Unit,
    aoSair: () -> Unit,
    aoAbrirLoginAdminDiscreto: () -> Unit = {},
    compacto: Boolean = false
) {
    var toquesLogo by remember { mutableIntStateOf(0) }

    val itens = when (sessao) {
        is SessaoUsuario.Visitante -> listOf(
            ItemMenuConfig("VITRINE", "Vitrine", Icons.Default.Storefront),
            ItemMenuConfig("CARRINHO", "Carrinho", Icons.Default.ShoppingCart),
            ItemMenuConfig("LOGIN_CLIENTE", "Entrar", Icons.AutoMirrored.Filled.Login)
        )

        is SessaoUsuario.ClienteLogado -> listOf(
            ItemMenuConfig("VITRINE", "Vitrine", Icons.Default.Storefront),
            ItemMenuConfig("CARRINHO", "Carrinho", Icons.Default.ShoppingCart),
            ItemMenuConfig("MEUS_PEDIDOS", "Pedidos", Icons.Default.ShoppingBag),
            ItemMenuConfig("MEUS_ENDERECOS", "Endereços", Icons.Default.LocationOn),
            ItemMenuConfig("MEU_CADASTRO", "Meu Perfil", Icons.Default.Person)
        )

        is SessaoUsuario.AdminLogado -> buildList {
            add(ItemMenuConfig("PRODUTO", "Produto", Icons.Default.ShoppingCart))
            add(ItemMenuConfig("ESTOQUE", "Estoque", Icons.Default.List))
            add(ItemMenuConfig("PEDIDO", "Pedidos", Icons.Default.Receipt))
            add(ItemMenuConfig("MATERIAL", "Material", Icons.Default.Build))
            add(ItemMenuConfig("FORNECEDOR", "Fornecedor", Icons.Default.LocalShipping))
            add(ItemMenuConfig("ENDERECO", "Endereços", Icons.Default.LocationOn))
            if (sessao.isMaster) {
                add(ItemMenuConfig("ADMINISTRADOR", "Admins", Icons.Default.AdminPanelSettings))
            }
            add(ItemMenuConfig("MEU_PERFIL_ADMIN", "Meu Perfil", Icons.Default.Person))
        }
    }

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
            modifier = Modifier
                .padding(top = 24.dp, bottom = 22.dp)
                .clickable(enabled = sessao is SessaoUsuario.Visitante) {
                    toquesLogo += 1
                    if (toquesLogo >= 5) {
                        toquesLogo = 0
                        aoAbrirLoginAdminDiscreto()
                    }
                }
        )

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 14.dp),
            color = CorBegeMedio.copy(alpha = 0.55f)
        )
        Spacer(Modifier.height(12.dp))

        itens.forEach { item ->
            ItemMenuLateral(
                titulo = item.titulo,
                icone = item.icone,
                estaSelecionado = telaAtual == item.rota,
                aoClicar = { aoMudarTela(item.rota) },
                compacto = compacto
            )
        }

        if (sessao !is SessaoUsuario.Visitante) {
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 14.dp),
                color = CorBegeMedio.copy(alpha = 0.35f)
            )
            Spacer(Modifier.height(8.dp))
            ItemMenuLateral(
                titulo = "Sair",
                icone = Icons.AutoMirrored.Filled.ExitToApp,
                estaSelecionado = false,
                aoClicar = aoSair,
                compacto = compacto
            )
        }

        Spacer(Modifier.height(18.dp))
    }
}