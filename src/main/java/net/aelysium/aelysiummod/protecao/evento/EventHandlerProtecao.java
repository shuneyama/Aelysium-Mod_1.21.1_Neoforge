package net.aelysium.aelysiummod.protecao.evento;

import net.aelysium.aelysiummod.item.custom.VarinhaProtecao;
import net.aelysium.aelysiummod.protecao.regiao.FlagRegiao;
import net.aelysium.aelysiummod.protecao.regiao.GerenciadorRegioes;
import net.aelysium.aelysiummod.protecao.regiao.Regiao;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EventHandlerProtecao {

    private static final Map<UUID, String> ultimaRegiaoJogador = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> cooldownMensagem = new ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 2000;

    private static boolean podeMostrarMensagem(UUID uuid) {
        long agora = System.currentTimeMillis();
        Long ultimo = cooldownMensagem.get(uuid);
        if (ultimo == null || agora - ultimo >= COOLDOWN_MS) {
            cooldownMensagem.put(uuid, agora);
            return true;
        }
        return false;
    }

    private static boolean usandoVarinhaProtecao(ServerPlayer jogador) {
        return jogador.getMainHandItem().getItem() instanceof VarinhaProtecao && jogador.hasPermissions(2);
    }

    @SubscribeEvent
    public void aoClicarBlocoEsquerdo(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer jogador)) return;
        if (!usandoVarinhaProtecao(jogador)) return;

        event.setCanceled(true);

        BlockPos pos = event.getPos();

        if (jogador.isShiftKeyDown()) {
            net.aelysium.aelysiummod.protecao.regiao.SelecaoManager.setPosicao2(jogador.getUUID(), pos);
            jogador.sendSystemMessage(Component.literal(
                    "§ePosição 2 §7definida em §f" + pos.getX() + "§7, §f" + pos.getY() + "§7, §f" + pos.getZ()
            ));
        } else {
            net.aelysium.aelysiummod.protecao.regiao.SelecaoManager.setPosicao1(jogador.getUUID(), pos);
            jogador.sendSystemMessage(Component.literal(
                    "§ePosição 1 §7definida em §f" + pos.getX() + "§7, §f" + pos.getY() + "§7, §f" + pos.getZ()
            ));
        }
    }

    @SubscribeEvent
    public void aoQuebrarBloco(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer jogador)) return;
        if (usandoVarinhaProtecao(jogador)) return;

        BlockPos pos = event.getPos();
        Regiao regiao = GerenciadorRegioes.getInstance().getRegiaoMaisEspecifica(pos);
        if (regiao == null || regiao.isDono(jogador.getUUID())) return;

        if (!regiao.getFlagValor(FlagRegiao.QUEBRAR_BLOCOS)) {
            event.setCanceled(true);
            if (podeMostrarMensagem(jogador.getUUID())) {
                jogador.sendSystemMessage(Component.literal("§cVocê não pode quebrar blocos nesta região!"));
            }
        }
    }

    @SubscribeEvent
    public void aoColocarBloco(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer jogador)) return;
        if (usandoVarinhaProtecao(jogador)) return;

        BlockPos pos = event.getPos();
        Regiao regiao = GerenciadorRegioes.getInstance().getRegiaoMaisEspecifica(pos);
        if (regiao == null || regiao.isDono(jogador.getUUID())) return;

        if (!regiao.getFlagValor(FlagRegiao.COLOCAR_BLOCOS)) {
            event.setCanceled(true);
            if (podeMostrarMensagem(jogador.getUUID())) {
                jogador.sendSystemMessage(Component.literal("§cVocê não pode colocar blocos nesta região!"));
            }
        }
    }

    @SubscribeEvent
    public void aoInteragirBloco(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer jogador)) return;
        if (usandoVarinhaProtecao(jogador)) return;

        BlockPos pos = event.getPos();
        Regiao regiao = GerenciadorRegioes.getInstance().getRegiaoMaisEspecifica(pos);
        if (regiao == null || regiao.isDono(jogador.getUUID())) return;

        if (event.getItemStack().getItem() instanceof net.minecraft.world.item.BlockItem) {
            if (!regiao.getFlagValor(FlagRegiao.COLOCAR_BLOCOS)) {
                event.setCanceled(true);
                if (podeMostrarMensagem(jogador.getUUID())) {
                    jogador.sendSystemMessage(Component.literal("§cVocê não pode colocar blocos nesta região!"));
                }
            }
            return;
        }

        if (!regiao.getFlagValor(FlagRegiao.INTERAGIR_BLOCOS)) {
            event.setUseBlock(TriState.FALSE);
            event.setUseItem(TriState.FALSE);
            event.setCanceled(true);
            if (podeMostrarMensagem(jogador.getUUID())) {
                jogador.sendSystemMessage(Component.literal("§cVocê não pode interagir com blocos nesta região!"));
            }
        }
    }

    @SubscribeEvent
    public void aoUsarItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer jogador)) return;
        if (usandoVarinhaProtecao(jogador)) return;

        BlockPos pos = jogador.blockPosition();
        Regiao regiao = GerenciadorRegioes.getInstance().getRegiaoMaisEspecifica(pos);
        if (regiao == null || regiao.isDono(jogador.getUUID())) return;

        if (!regiao.getFlagValor(FlagRegiao.USAR_ITENS)) {
            event.setCanceled(true);
            if (podeMostrarMensagem(jogador.getUUID())) {
                jogador.sendSystemMessage(Component.literal("§cVocê não pode usar itens nesta região!"));
            }
        }
    }

    @SubscribeEvent
    public void aoDarDano(LivingDamageEvent.Pre event) {
        LivingEntity vitima = event.getEntity();
        if (vitima.level().isClientSide) return;

        BlockPos pos = vitima.blockPosition();
        Regiao regiao = GerenciadorRegioes.getInstance().getRegiaoMaisEspecifica(pos);
        if (regiao == null) return;

        DamageSource source = event.getContainer().getSource();
        Entity atacante = source.getEntity();

        if (atacante instanceof ServerPlayer jogadorAtacante) {
            if (regiao.isDono(jogadorAtacante.getUUID())) return;

            if (vitima instanceof Player) {
                if (!regiao.getFlagValor(FlagRegiao.PVP)) {
                    event.getContainer().setNewDamage(0f);
                    if (podeMostrarMensagem(jogadorAtacante.getUUID())) {
                        jogadorAtacante.sendSystemMessage(Component.literal("§cPvP não é permitido nesta região!"));
                    }
                    return;
                }
            }

            MobCategory categoria = vitima.getType().getCategory();
            if (categoria == MobCategory.CREATURE || categoria == MobCategory.WATER_CREATURE
                    || categoria == MobCategory.AMBIENT || categoria == MobCategory.AXOLOTLS) {
                if (!regiao.getFlagValor(FlagRegiao.DANO_ANIMAIS)) {
                    event.getContainer().setNewDamage(0f);
                    if (podeMostrarMensagem(jogadorAtacante.getUUID())) {
                        jogadorAtacante.sendSystemMessage(Component.literal("§cVocê não pode atacar animais nesta região!"));
                    }
                    return;
                }
            }

            if (categoria == MobCategory.MONSTER) {
                if (!regiao.getFlagValor(FlagRegiao.DANO_MONSTROS)) {
                    event.getContainer().setNewDamage(0f);
                    if (podeMostrarMensagem(jogadorAtacante.getUUID())) {
                        jogadorAtacante.sendSystemMessage(Component.literal("§cVocê não pode atacar monstros nesta região!"));
                    }
                    return;
                }
            }
        }

        if (source.is(DamageTypes.EXPLOSION) || source.is(DamageTypes.PLAYER_EXPLOSION)) {
            if (!regiao.getFlagValor(FlagRegiao.EXPLOSAO_DANO)) {
                event.getContainer().setNewDamage(0f);
                return;
            }
        }

        if (vitima instanceof Player) {
            if (source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE)
                    || source.is(DamageTypes.LAVA) || source.is(DamageTypes.HOT_FLOOR)) {
                if (!regiao.getFlagValor(FlagRegiao.DANO_FOGO)) {
                    event.getContainer().setNewDamage(0f);
                    return;
                }
            }
            if (source.is(DamageTypes.FALL)) {
                if (!regiao.getFlagValor(FlagRegiao.DANO_QUEDA)) {
                    event.getContainer().setNewDamage(0f);
                }
            }
        }
    }

    @SubscribeEvent
    public void aoExplodir(ExplosionEvent.Detonate event) {
        if (event.getLevel().isClientSide()) return;

        BlockPos pos = BlockPos.containing(event.getExplosion().center());
        Regiao regiao = GerenciadorRegioes.getInstance().getRegiaoMaisEspecifica(pos);
        if (regiao == null) return;

        if (!regiao.getFlagValor(FlagRegiao.EXPLOSAO_DESTRUIR)) {
            event.getAffectedBlocks().clear();
        }
    }

    @SubscribeEvent
    public void aoSpawnarMob(FinalizeSpawnEvent event) {
        if (event.getLevel().isClientSide()) return;

        BlockPos pos = event.getEntity().blockPosition();
        Regiao regiao = GerenciadorRegioes.getInstance().getRegiaoMaisEspecifica(pos);
        if (regiao == null) return;

        MobCategory categoria = event.getEntity().getType().getCategory();

        if (categoria == MobCategory.CREATURE || categoria == MobCategory.WATER_CREATURE
                || categoria == MobCategory.AMBIENT || categoria == MobCategory.AXOLOTLS) {
            if (!regiao.getFlagValor(FlagRegiao.SPAWN_ANIMAIS)) {
                event.setSpawnCancelled(true);
            }
        }

        if (categoria == MobCategory.MONSTER) {
            if (!regiao.getFlagValor(FlagRegiao.SPAWN_MONSTROS)) {
                event.setSpawnCancelled(true);
            }
        }
    }

    @SubscribeEvent
    public void aoFogoEspalhar(BlockEvent.NeighborNotifyEvent event) {
        if (event.getLevel().isClientSide()) return;

        BlockPos pos = event.getPos();
        if (!(event.getLevel().getBlockState(pos).getBlock() instanceof FireBlock)) return;

        Regiao regiao = GerenciadorRegioes.getInstance().getRegiaoMaisEspecifica(pos);
        if (regiao != null && !regiao.getFlagValor(FlagRegiao.FOGO_ESPALHAR)) {
            event.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void aoTeleportar(EntityTeleportEvent event) {
        BlockPos posDestino = BlockPos.containing(event.getTargetX(), event.getTargetY(), event.getTargetZ());
        Regiao regiao = GerenciadorRegioes.getInstance().getRegiaoMaisEspecifica(posDestino);
        if (regiao == null) return;

        if (event.getEntity() instanceof ServerPlayer jogador) {
            if (regiao.isDono(jogador.getUUID())) return;
            if (!regiao.getFlagValor(FlagRegiao.TELEPORTE)) {
                event.setCanceled(true);
                if (podeMostrarMensagem(jogador.getUUID())) {
                    jogador.sendSystemMessage(Component.literal("§cVocê não pode teleportar para esta região!"));
                }
            }
        } else {
            if (event.getEntity().level().isClientSide) return;
            if (!regiao.getFlagValor(FlagRegiao.TELEPORTE)) {
                event.setCanceled(true);
            }
        }
    }

    public static void verificarMovimentoJogador(ServerPlayer jogador) {
        BlockPos pos = jogador.blockPosition();
        Regiao regiaoAtual = GerenciadorRegioes.getInstance().getRegiaoMaisEspecifica(pos);

        String nomeRegiaoAtual = regiaoAtual != null ? regiaoAtual.getNome() : null;
        String ultimaRegiao = ultimaRegiaoJogador.get(jogador.getUUID());

        if (ultimaRegiao != null && nomeRegiaoAtual == null) {
            Regiao regiaoSaiu = GerenciadorRegioes.getInstance().getRegiao(ultimaRegiao);
            if (regiaoSaiu != null) aoSairRegiao(jogador, regiaoSaiu);
            ultimaRegiaoJogador.remove(jogador.getUUID());
        } else if (ultimaRegiao == null && nomeRegiaoAtual != null) {
            aoEntrarRegiao(jogador, regiaoAtual);
            ultimaRegiaoJogador.put(jogador.getUUID(), nomeRegiaoAtual);
        } else if (ultimaRegiao != null && !ultimaRegiao.equals(nomeRegiaoAtual)) {
            Regiao regiaoSaiu = GerenciadorRegioes.getInstance().getRegiao(ultimaRegiao);
            if (regiaoSaiu != null) aoSairRegiao(jogador, regiaoSaiu);
            if (regiaoAtual != null) aoEntrarRegiao(jogador, regiaoAtual);
            ultimaRegiaoJogador.put(jogador.getUUID(), nomeRegiaoAtual);
        }
    }

    private static String getNomeExibicao(ServerPlayer jogador) {
        try {
            Class<?> nickDataClass = Class.forName("net.aelysium.aelysiummod.nickname.NicknameData");
            Object data = nickDataClass.getMethod("get", net.minecraft.server.MinecraftServer.class)
                    .invoke(null, jogador.getServer());
            Object entry = nickDataClass.getMethod("getNickname", java.util.UUID.class)
                    .invoke(data, jogador.getUUID());
            if (entry != null) {
                String nick = (String) entry.getClass().getMethod("nick").invoke(entry);
                if (nick != null && !nick.isEmpty()) return nick;
            }
        } catch (Exception ignored) {}
        return jogador.getName().getString();
    }

    private static void aoEntrarRegiao(ServerPlayer jogador, Regiao regiao) {
        if (!regiao.getFlagValor(FlagRegiao.ENTRAR_REGIAO) && !regiao.isDono(jogador.getUUID())) {
            BlockPos centro = regiao.getPosicaoCentral();
            double dx = jogador.getX() - centro.getX();
            double dz = jogador.getZ() - centro.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > 0) {
                jogador.teleportTo(
                        jogador.getX() + (dx / dist) * 2.0,
                        jogador.getY(),
                        jogador.getZ() + (dz / dist) * 2.0
                );
            }
            jogador.sendSystemMessage(Component.literal("§cVocê não pode entrar nesta região!"));
            return;
        }

        if (!regiao.getMensagemEntrada().isEmpty()) {
            String mensagem = regiao.getMensagemEntrada()
                    .replace("&", "§")
                    .replace("{jogador}", getNomeExibicao(jogador))
                    .replace("{regiao}", regiao.getNome());
            jogador.connection.send(new ClientboundSetTitleTextPacket(Component.literal(mensagem)));
        }

        if (regiao.getFlagValor(FlagRegiao.AVISAR_DONO_ENTRADA)) {
            for (UUID donoUUID : regiao.getDonos()) {
                ServerPlayer dono = jogador.server.getPlayerList().getPlayer(donoUUID);
                if (dono != null && !dono.getUUID().equals(jogador.getUUID())) {
                    dono.sendSystemMessage(Component.literal(
                            "§e" + getNomeExibicao(jogador) + " §7entrou na região §e" + regiao.getNome()
                    ));
                }
            }
        }
    }

    private static void aoSairRegiao(ServerPlayer jogador, Regiao regiao) {
        if (!regiao.getFlagValor(FlagRegiao.SAIR_REGIAO) && !regiao.isDono(jogador.getUUID())) {
            BlockPos centro = regiao.getPosicaoCentral();
            jogador.teleportTo(centro.getX() + 0.5, centro.getY(), centro.getZ() + 0.5);
            jogador.sendSystemMessage(Component.literal("§cVocê não pode sair desta região!"));
            return;
        }

        if (!regiao.getMensagemSaida().isEmpty()) {
            String mensagem = regiao.getMensagemSaida()
                    .replace("&", "§")
                    .replace("{jogador}", getNomeExibicao(jogador))
                    .replace("{regiao}", regiao.getNome());
            jogador.connection.send(new ClientboundSetTitleTextPacket(Component.literal(mensagem)));
        }

        if (regiao.getFlagValor(FlagRegiao.AVISAR_DONO_SAIDA)) {
            for (UUID donoUUID : regiao.getDonos()) {
                ServerPlayer dono = jogador.server.getPlayerList().getPlayer(donoUUID);
                if (dono != null && !dono.getUUID().equals(jogador.getUUID())) {
                    dono.sendSystemMessage(Component.literal(
                            "§e" + getNomeExibicao(jogador) + " §7saiu da região §e" + regiao.getNome()
                    ));
                }
            }
        }
    }

    public static void limparJogador(UUID uuid) {
        ultimaRegiaoJogador.remove(uuid);
        cooldownMensagem.remove(uuid);
    }
}
