package net.aelysium.aelysiummod.sistemas.rp.habilidade;

import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GerenciadorHabilidade {

    private static final ResourceLocation CONGELAR_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID, "habilidade_congelar");

    private static final Set<UUID> vooAtivo = new HashSet<>();
    private static final Set<UUID> deusAtivo = new HashSet<>();
    private static final Set<UUID> mudoAtivo = new HashSet<>();


    public static boolean toggleVoo(ServerPlayer player) {
        UUID id = player.getUUID();
        if (vooAtivo.contains(id)) {
            vooAtivo.remove(id);
            return false;
        } else {
            vooAtivo.add(id);
            return true;
        }
    }

    public static boolean temVoo(ServerPlayer player) {
        return vooAtivo.contains(player.getUUID());
    }

    public static boolean toggleDeus(ServerPlayer player) {
        UUID id = player.getUUID();
        if (deusAtivo.contains(id)) {
            deusAtivo.remove(id);
            return false;
        } else {
            deusAtivo.add(id);
            return true;
        }
    }

    public static boolean temDeus(ServerPlayer player) {
        return deusAtivo.contains(player.getUUID());
    }

    public static boolean toggleCongelar(ServerPlayer player) {
        if (estaCongelado(player)) {
            player.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(CONGELAR_MODIFIER_ID);
            player.getAttribute(Attributes.JUMP_STRENGTH).removeModifier(CONGELAR_MODIFIER_ID);
            player.getAttribute(Attributes.ATTACK_SPEED).removeModifier(CONGELAR_MODIFIER_ID);
            return false;
        } else {
            player.getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(
                    new AttributeModifier(CONGELAR_MODIFIER_ID, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            player.getAttribute(Attributes.JUMP_STRENGTH).addPermanentModifier(
                    new AttributeModifier(CONGELAR_MODIFIER_ID, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            player.getAttribute(Attributes.ATTACK_SPEED).addPermanentModifier(
                    new AttributeModifier(CONGELAR_MODIFIER_ID, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            return true;
        }
    }

    public static boolean estaCongelado(ServerPlayer player) {
        return player.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(CONGELAR_MODIFIER_ID) != null;
    }

    public static boolean toggleMudo(ServerPlayer player) {
        UUID id = player.getUUID();
        if (mudoAtivo.contains(id)) {
            mudoAtivo.remove(id);
            return false;
        } else {
            mudoAtivo.add(id);
            return true;
        }
    }

    public static boolean estaMudo(UUID uuid) {
        return mudoAtivo.contains(uuid);
    }

    public static boolean isManutencaoAtiva() {
        return net.aelysium.aelysiummod.sistemas.admin.historico.ConfiguracaoRegrasMods.manutencaoAtiva();
    }

    public static void setManutencao(boolean ativo) {
        net.aelysium.aelysiummod.sistemas.admin.historico.ConfiguracaoRegrasMods.definirManutencao(ativo);
    }

    public static boolean toggleManutencao() {
        boolean novo = !isManutencaoAtiva();
        setManutencao(novo);
        return novo;
    }

    public static void limpar(UUID uuid) {
        vooAtivo.remove(uuid);
        deusAtivo.remove(uuid);
        mudoAtivo.remove(uuid);
    }
}
