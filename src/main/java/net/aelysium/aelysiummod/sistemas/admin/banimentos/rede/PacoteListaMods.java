package net.aelysium.aelysiummod.sistemas.admin.banimentos.rede;

import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.admin.historico.ModCliente;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record PacoteListaMods(int parte, int totalPartes, String minecraft, String loader, String loaderVersao,
                              List<ModCliente> mods) implements CustomPacketPayload {

    public static final int MODS_POR_PARTE = 80;

    public static final Type<PacoteListaMods> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "lista_mods"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacoteListaMods> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, PacoteListaMods::parte,
                    ByteBufCodecs.VAR_INT, PacoteListaMods::totalPartes,
                    ByteBufCodecs.STRING_UTF8, PacoteListaMods::minecraft,
                    ByteBufCodecs.STRING_UTF8, PacoteListaMods::loader,
                    ByteBufCodecs.STRING_UTF8, PacoteListaMods::loaderVersao,
                    ModCliente.CODEC.apply(ByteBufCodecs.list(MODS_POR_PARTE)), PacoteListaMods::mods,
                    PacoteListaMods::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacoteListaMods packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                Class<?> h = Class.forName("net.aelysium.aelysiummod.sistemas.admin.banimentos.ManipuladorListaNegraModsServidor");
                h.getMethod("handle", PacoteListaMods.class, IPayloadContext.class)
                        .invoke(null, packet, context);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
