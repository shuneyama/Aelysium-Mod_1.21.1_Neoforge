package net.aelysium.aelysiummod.sistemas.mundo.porta.cliente;

import net.aelysium.aelysiummod.sistemas.mundo.porta.cliente.TelaPainelSenha;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.aelysium.aelysiummod.sistemas.mundo.porta.rede.PacoteAbrirPainelSenha;
import net.aelysium.aelysiummod.sistemas.mundo.porta.rede.PacoteResultadoSenha;

public class ManipuladoresPortaSenhaCliente {

    public static void handleAbrirPainel(PacoteAbrirPainelSenha packet, IPayloadContext context) {
        Minecraft.getInstance().setScreen(new TelaPainelSenha(packet.pos(), packet.definindo()));
    }

    public static void handleResultado(PacoteResultadoSenha packet, IPayloadContext context) {
        if (Minecraft.getInstance().screen instanceof TelaPainelSenha tela) {
            tela.receberResultado(packet.pos(), packet.sucesso());
        }
    }
}
