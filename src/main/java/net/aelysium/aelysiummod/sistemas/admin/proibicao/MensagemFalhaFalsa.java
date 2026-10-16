package net.aelysium.aelysiummod.sistemas.admin.proibicao;

import net.minecraft.network.chat.Component;

public class MensagemFalhaFalsa {

    public static Component gerar() {
        return Component.literal(
                "§fInternal Client Exception\n" +
                "§f---------------------------\n\n" +
                "§fjava.lang.IllegalStateException: Failed to initialize client connection\n" +
                "§f\tat TRANSFORMER/minecraft@1.21.1/net.minecraft.client.multiplayer.ClientPacketListener.handleLogin(ClientPacketListener.java:412) ~[client-1.21.1-20240808.144430-srg.jar%23142!/:?] {re:mixin,pl:accesstransformer:B,re:classloading,pl:accesstransformer:B,pl:mixin:APP:create.mixins.json:NetworkMixin from mod create,pl:mixin:A}\n" +
                "§f\tat TRANSFORMER/minecraft@1.21.1/net.minecraft.network.Connection.channelRead0(Connection.java:294) ~[client-1.21.1-20240808.144430-srg.jar%23142!/:?] {re:mixin,pl:accesstransformer:B}\n" +
                "§f\tat net.minecraft.network.Connection.channelActive(Connection.java:112) [client-1.21.1-20240808.144430-srg.jar%23142!/:?] {re:mixin}\n" +
                "§f\tat io.netty.channel.AbstractChannelHandlerContext.invokeChannelActive(AbstractChannelHandlerContext.java:252) [netty-transport-4.1.97.Final.jar%2377!/:4.1.97.Final]\n" +
                "§f\tat java.base/java.lang.Thread.run(Thread.java:1583) [?:?] {re:mixin}\n" +
                "§fCaused by: java.lang.NullPointerException: Cannot invoke \"net.minecraft.world.entity.player.Player.getGameProfile()\" because \"this.player\" is null\n" +
                "§f\tat TRANSFORMER/minecraft@1.21.1/net.minecraft.client.multiplayer.ClientPacketListener.handleLogin(ClientPacketListener.java:398) ~[client-1.21.1-20240808.144430-srg.jar%23142!/:?] {re:mixin}\n" +
                "§f\t... 4 more"
        );
    }

    private MensagemFalhaFalsa() {
    }
}
