package net.aelysium.aelysiummod.sistemas.admin.protecao.evento;

import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.FlagRegiao;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.GerenciadorRegioes;
import net.aelysium.aelysiummod.sistemas.admin.protecao.regiao.Regiao;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

public class ManipuladorEventosCombateProtecao {

    @SubscribeEvent
    public void aoDarDano(LivingDamageEvent.Pre event) {
        LivingEntity vitima = event.getEntity();
        if (vitima.level().isClientSide) return;

        BlockPos pos = vitima.blockPosition();
        Regiao regiao = GerenciadorRegioes.getInstance().getRegiaoMaisEspecifica(pos, UtilitariosEventoProtecao.dimDe(vitima));
        if (regiao == null) return;

        DamageSource source = event.getContainer().getSource();
        Entity atacante = source.getEntity();

        if (atacante instanceof ServerPlayer jogadorAtacante) {
            if (regiao.isDono(jogadorAtacante)) return;

            if (vitima instanceof Player) {
                if (!regiao.getFlagValor(FlagRegiao.PVP)) {
                    event.getContainer().setNewDamage(0f);
                    if (ControleRegiaoJogador.podeMostrarMensagem(jogadorAtacante.getUUID())) {
                        jogadorAtacante.sendSystemMessage(Component.translatable("mensagem.aelysiummod.protecao.pvp_nao_e_permitido_nesta_regiao"));
                    }
                    return;
                }
            }

            MobCategory categoria = vitima.getType().getCategory();
            if (categoria == MobCategory.CREATURE || categoria == MobCategory.WATER_CREATURE
                    || categoria == MobCategory.AMBIENT || categoria == MobCategory.AXOLOTLS) {
                if (!regiao.getFlagValor(FlagRegiao.DANO_ANIMAIS)) {
                    event.getContainer().setNewDamage(0f);
                    if (ControleRegiaoJogador.podeMostrarMensagem(jogadorAtacante.getUUID())) {
                        jogadorAtacante.sendSystemMessage(Component.translatable("mensagem.aelysiummod.protecao.voce_nao_pode_atacar_animais_nesta"));
                    }
                    return;
                }
            }

            if (categoria == MobCategory.MONSTER) {
                if (!regiao.getFlagValor(FlagRegiao.DANO_MONSTROS)) {
                    event.getContainer().setNewDamage(0f);
                    if (ControleRegiaoJogador.podeMostrarMensagem(jogadorAtacante.getUUID())) {
                        jogadorAtacante.sendSystemMessage(Component.translatable("mensagem.aelysiummod.protecao.voce_nao_pode_atacar_monstros_nesta"));
                    }
                    return;
                }
            }
        } else if (vitima instanceof Player && atacante instanceof LivingEntity) {
            if (!regiao.getFlagValor(FlagRegiao.DANO_JOGADOR)) {
                event.getContainer().setNewDamage(0f);
                return;
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
        Regiao regiao = GerenciadorRegioes.getInstance().getRegiaoMaisEspecifica(pos, UtilitariosEventoProtecao.dimDe(event.getLevel()));
        if (regiao == null) return;

        if (!regiao.getFlagValor(FlagRegiao.EXPLOSAO_DESTRUIR)) {
            event.getAffectedBlocks().clear();
        }
    }

    @SubscribeEvent
    public void aoSpawnarMob(FinalizeSpawnEvent event) {
        if (event.getLevel().isClientSide()) return;

        BlockPos pos = event.getEntity().blockPosition();
        Regiao regiao = GerenciadorRegioes.getInstance().getRegiaoMaisEspecifica(pos, UtilitariosEventoProtecao.dimDe(event.getEntity()));
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
    public void aoAtacarDecoracao(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer jogador)) return;

        Entity alvo = event.getTarget();
        if (!(alvo instanceof ItemFrame) && !(alvo instanceof Painting) && !(alvo instanceof ArmorStand)) return;

        Regiao regiao = GerenciadorRegioes.getInstance().getRegiaoMaisEspecifica(alvo.blockPosition(), UtilitariosEventoProtecao.dimDe(alvo));
        if (regiao == null || regiao.isDono(jogador)) return;

        if (!regiao.getFlagValor(FlagRegiao.ROUBAR_DECORACAO)) {
            event.setCanceled(true);
            if (ControleRegiaoJogador.podeMostrarMensagem(jogador.getUUID())) {
                jogador.sendSystemMessage(Component.translatable("mensagem.aelysiummod.protecao.voce_nao_pode_mexer_nessa_decoracao"));
            }
        }
    }

    @SubscribeEvent
    public void aoInteragirDecoracao(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer jogador)) return;
        if (bloquearInteracaoDecoracao(jogador, event.getTarget())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void aoInteragirDecoracaoEspecifico(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!(event.getEntity() instanceof ServerPlayer jogador)) return;
        if (bloquearInteracaoDecoracao(jogador, event.getTarget())) {
            event.setCanceled(true);
        }
    }

    private boolean bloquearInteracaoDecoracao(ServerPlayer jogador, Entity alvo) {
        if (!(alvo instanceof ArmorStand) && !(alvo instanceof ItemFrame)) return false;

        Regiao regiao = GerenciadorRegioes.getInstance().getRegiaoMaisEspecifica(alvo.blockPosition(), UtilitariosEventoProtecao.dimDe(alvo));
        if (regiao == null || regiao.isDono(jogador)) return false;

        if (regiao.getFlagValor(FlagRegiao.ROUBAR_DECORACAO)) return false;

        if (ControleRegiaoJogador.podeMostrarMensagem(jogador.getUUID())) {
            jogador.sendSystemMessage(Component.translatable("mensagem.aelysiummod.protecao.voce_nao_pode_mexer_nessa_decoracao"));
        }
        return true;
    }
}
