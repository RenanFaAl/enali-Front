package br.com.enali.auth

import br.com.enali.network.dto.AuthLoginResponse

sealed interface SessaoUsuario {
    data object Visitante : SessaoUsuario

    data class ClienteLogado(
        val token: String,
        val id: Long,
        val nome: String,
        val email: String
    ) : SessaoUsuario

    data class AdminLogado(
        val token: String,
        val id: Long,
        val nome: String,
        val email: String,
        val nivelAcesso: String
    ) : SessaoUsuario {
        val isMaster: Boolean
            get() = nivelAcesso.equals("ADMIN_MASTER", ignoreCase = true)
    }

    companion object {
        fun fromLoginResponse(response: AuthLoginResponse): SessaoUsuario {
            return if (response.tipoUsuario.equals("ADMINISTRADOR", ignoreCase = true)) {
                AdminLogado(
                    token = response.token,
                    id = response.id,
                    nome = response.nome,
                    email = response.email,
                    nivelAcesso = response.nivelAcesso ?: "ADMIN_COMUM"
                )
            } else {
                ClienteLogado(
                    token = response.token,
                    id = response.id,
                    nome = response.nome,
                    email = response.email
                )
            }
        }
    }
}

enum class RotaEnali(val rotaInicial: String) {
    VISITANTE("VITRINE"),
    CLIENTE("VITRINE"),
    ADMIN_COMUM("PRODUTO"),
    ADMIN_MASTER("PRODUTO");

    companion object {
        fun daSessao(sessao: SessaoUsuario): RotaEnali = when (sessao) {
            is SessaoUsuario.Visitante -> VISITANTE
            is SessaoUsuario.ClienteLogado -> CLIENTE
            is SessaoUsuario.AdminLogado -> if (sessao.isMaster) ADMIN_MASTER else ADMIN_COMUM
        }

        fun rotasPermitidas(sessao: SessaoUsuario): Set<String> = when (daSessao(sessao)) {
            VISITANTE -> setOf(
                "VITRINE",
                "CARRINHO",
                "LOGIN_CLIENTE",
                "CADASTRO_CLIENTE",
                "LOGIN_ADMIN"
            )
            CLIENTE -> setOf(
                "VITRINE",
                "CARRINHO",
                "MEUS_PEDIDOS",
                "MEUS_ENDERECOS",
                "MEU_CADASTRO"
            )
            ADMIN_COMUM -> setOf(
                "PRODUTO",
                "ESTOQUE",
                "PEDIDO",
                "MATERIAL",
                "FORNECEDOR",
                "ENDERECO",
                "MEU_PERFIL_ADMIN"
            )
            ADMIN_MASTER -> setOf(
                "PRODUTO",
                "ESTOQUE",
                "PEDIDO",
                "MATERIAL",
                "FORNECEDOR",
                "ENDERECO",
                "ADMINISTRADOR",
                "MEU_PERFIL_ADMIN"
            )
        }
    }
}