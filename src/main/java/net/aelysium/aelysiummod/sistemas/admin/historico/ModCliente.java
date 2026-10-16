package net.aelysium.aelysiummod.sistemas.admin.historico;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ModCliente(String id, String versao, String arquivo, String sha1) {

    public static final StreamCodec<RegistryFriendlyByteBuf, ModCliente> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ModCliente::id,
            ByteBufCodecs.STRING_UTF8, ModCliente::versao,
            ByteBufCodecs.STRING_UTF8, ModCliente::arquivo,
            ByteBufCodecs.STRING_UTF8, ModCliente::sha1,
            ModCliente::new
    );

    public ModCliente limpo() {
        return new ModCliente(cortar(id, 100).toLowerCase(), cortar(versao, 64), cortar(arquivo, 160), cortar(sha1, 64).toLowerCase());
    }

    private static String cortar(String texto, int max) {
        if (texto == null) return "";
        return texto.length() > max ? texto.substring(0, max) : texto;
    }
}
