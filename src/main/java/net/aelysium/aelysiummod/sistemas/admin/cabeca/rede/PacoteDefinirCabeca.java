package net.aelysium.aelysiummod.sistemas.admin.cabeca.rede;

import com.mojang.authlib.properties.PropertyMap;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.sistemas.admin.cabeca.ManipuladorCabecaJogador;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;
import java.util.regex.Pattern;

public record PacoteDefinirCabeca(BlockPos pos, String nick) implements CustomPacketPayload {

    private static final Pattern NICK_VALIDO = Pattern.compile("[A-Za-z0-9_]{1,16}");

    public static final Type<PacoteDefinirCabeca> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "cabeca_definir"));

    public static final StreamCodec<FriendlyByteBuf, PacoteDefinirCabeca> CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeBlockPos(p.pos);
                buf.writeUtf(p.nick, 16);
            },
            buf -> new PacoteDefinirCabeca(buf.readBlockPos(), buf.readUtf(16)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacoteDefinirCabeca pacote, IPayloadContext contexto) {
        contexto.enqueueWork(() -> {
            if (!(contexto.player() instanceof ServerPlayer jogador)) return;
            if (!jogador.hasPermissions(2)) return;
            if (jogador.distanceToSqr(pacote.pos().getCenter()) > 64.0) return;
            if (!(jogador.level() instanceof ServerLevel level)) return;

            SkullBlockEntity cabeca = ManipuladorCabecaJogador.cabecaDeJogador(level, pacote.pos());
            if (cabeca == null) return;

            String nick = pacote.nick().trim();
            if (nick.isEmpty()) {
                cabeca.setOwner(null);
                atualizar(level, pacote.pos());
                jogador.displayClientMessage(Component.translatable("mensagem.aelysiummod.cabeca.limpa"), true);
                return;
            }
            if (!NICK_VALIDO.matcher(nick).matches()) return;

            new ResolvableProfile(Optional.of(nick), Optional.empty(), new PropertyMap())
                    .resolve()
                    .thenAcceptAsync(resolvido -> {
                        SkullBlockEntity atual = ManipuladorCabecaJogador.cabecaDeJogador(level, pacote.pos());
                        if (atual == null) return;
                        if (resolvido.id().isEmpty()) {
                            jogador.displayClientMessage(Component.translatable("mensagem.aelysiummod.cabeca.nao_encontrado", nick), true);
                            return;
                        }
                        atual.setOwner(resolvido);
                        atualizar(level, pacote.pos());
                        jogador.displayClientMessage(Component.translatable("mensagem.aelysiummod.cabeca.aplicada", nick), true);
                    }, level.getServer());
        });
    }

    private static void atualizar(ServerLevel level, BlockPos pos) {
        BlockState estado = level.getBlockState(pos);
        level.sendBlockUpdated(pos, estado, estado, 3);
    }
}
