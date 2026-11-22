package net.aelysium.aelysiummod.conteudo.item.medico;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MedkitItem extends Item {

    private static final int DURACAO_USO = 20 * 20;
    private static final int INTERVALO_VERIFICACAO = 5;
    private static final Map<UUID, UUID> ALVOS = new ConcurrentHashMap<>();

    public MedkitItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return DURACAO_USO;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (!player.isShiftKeyDown() || !(interactionTarget instanceof Player alvo) || alvo == player) return InteractionResult.PASS;

        iniciar(player, usedHand, alvo);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        Player alvo = player;
        if (player.isShiftKeyDown() && SeringaItem.alvoNaMira(player) instanceof Player mirado && mirado != player) {
            alvo = mirado;
        }

        return iniciar(player, hand, alvo) ? InteractionResultHolder.consume(stack) : InteractionResultHolder.fail(stack);
    }

    private static boolean iniciar(Player player, InteractionHand hand, Player alvo) {
        if (!precisaDeTratamento(alvo)) {
            if (!player.level().isClientSide) {
                String chave = alvo == player ? "mensagem.aelysiummod.medkit.sem_necessidade" : "mensagem.aelysiummod.medkit.alvo_sem_necessidade";
                player.displayClientMessage(Component.translatable(chave), true);
            }
            return false;
        }

        if (!player.level().isClientSide) {
            if (alvo == player) {
                ALVOS.remove(player.getUUID());
            } else {
                ALVOS.put(player.getUUID(), alvo.getUUID());
            }
        }
        player.startUsingItem(hand);
        return true;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide || !(livingEntity instanceof Player player)) return;

        UUID idAlvo = ALVOS.get(player.getUUID());
        if (idAlvo != null && remainingUseDuration % INTERVALO_VERIFICACAO == 0) {
            Player alvo = level.getPlayerByUUID(idAlvo);
            if (alvo == null || SeringaItem.alvoNaMira(player) != alvo) {
                player.stopUsingItem();
                player.displayClientMessage(Component.translatable("mensagem.aelysiummod.seringa.alvo_perdido"), true);
                return;
            }
        }

        if (remainingUseDuration % 20 != 0) return;

        player.displayClientMessage(Component.translatable("mensagem.aelysiummod.medkit.usando", remainingUseDuration / 20), true);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.PLAYERS, 0.4f, 1.2f);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        if (!level.isClientSide && livingEntity instanceof Player player) {
            ALVOS.remove(player.getUUID());
            player.displayClientMessage(Component.translatable("mensagem.aelysiummod.medkit.interrompido"), true);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (level.isClientSide) return stack;

        LivingEntity alvo = livingEntity;
        if (livingEntity instanceof Player player) {
            UUID idAlvo = ALVOS.remove(player.getUUID());
            if (idAlvo != null) {
                Player encontrado = level.getPlayerByUUID(idAlvo);
                if (encontrado == null || SeringaItem.alvoNaMira(player) != encontrado) {
                    player.displayClientMessage(Component.translatable("mensagem.aelysiummod.seringa.alvo_perdido"), true);
                    return stack;
                }
                alvo = encontrado;
            }
        }

        alvo.heal(alvo.getMaxHealth() - alvo.getHealth());
        efeitosNegativos(alvo).forEach(alvo::removeEffect);

        level.playSound(null, alvo.getX(), alvo.getY(), alvo.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5f, 1.5f);

        if (livingEntity instanceof Player player) {
            if (alvo == player) {
                player.displayClientMessage(Component.translatable("mensagem.aelysiummod.medkit.concluido"), true);
            } else {
                player.displayClientMessage(Component.translatable("mensagem.aelysiummod.medkit.tratou_alvo", alvo.getDisplayName()), true);
                if (alvo instanceof ServerPlayer jogadorAlvo) {
                    jogadorAlvo.displayClientMessage(Component.translatable("mensagem.aelysiummod.medkit.tratado_por", player.getDisplayName()), true);
                }
            }
        }

        stack.consume(1, livingEntity);
        return stack;
    }

    private static boolean precisaDeTratamento(LivingEntity entidade) {
        return entidade.getHealth() < entidade.getMaxHealth() || !efeitosNegativos(entidade).isEmpty();
    }

    private static List<Holder<MobEffect>> efeitosNegativos(LivingEntity entidade) {
        return entidade.getActiveEffects().stream()
                .map(MobEffectInstance::getEffect)
                .filter(efeito -> efeito.value().getCategory() == MobEffectCategory.HARMFUL)
                .toList();
    }
}
