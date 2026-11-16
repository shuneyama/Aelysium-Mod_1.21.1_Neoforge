package net.aelysium.aelysiummod.registro;

import com.mojang.serialization.Codec;
import net.aelysium.aelysiummod.AelysiumMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.UUID;
import java.util.function.Supplier;

public class ModComponentesDados {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE, AelysiumMod.MOD_ID);

    public static final Supplier<DataComponentType<Boolean>> ISQUEIRO_ACESO = DATA_COMPONENT_TYPES.register("isqueiro_aceso",
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.BOOL).build());

    public static final Supplier<DataComponentType<Boolean>> ISQUEIRO_TAMPA_ABERTA = DATA_COMPONENT_TYPES.register("isqueiro_tampa_aberta",
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.BOOL).build());

    public static final Supplier<DataComponentType<Long>> ISQUEIRO_TAMPA_TICK = DATA_COMPONENT_TYPES.register("isqueiro_tampa_tick",
            () -> DataComponentType.<Long>builder().persistent(Codec.LONG).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_LONG).build());

    public static final Supplier<DataComponentType<Boolean>> CIGARRO_ACESO = DATA_COMPONENT_TYPES.register("cigarro_aceso",
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.BOOL).build());

    public static final Supplier<DataComponentType<Integer>> CIGARRO_FUMADAS = DATA_COMPONENT_TYPES.register("cigarro_fumadas",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT).build());

    public static final Supplier<DataComponentType<UUID>> CARTAO_PORTA_VINCULADA = DATA_COMPONENT_TYPES.register("cartao_porta_vinculada",
            () -> DataComponentType.<UUID>builder().persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC).build());

    public static final Supplier<DataComponentType<String>> CARTAO_TIPO_VINCULO = DATA_COMPONENT_TYPES.register("cartao_tipo_vinculo",
            () -> DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8).build());

    public static final Supplier<DataComponentType<Integer>> GLOCK_MUNICAO = DATA_COMPONENT_TYPES.register("glock_municao",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT).build());

    public static final Supplier<DataComponentType<Long>> TRIDENTE_COOLDOWN_FIM = DATA_COMPONENT_TYPES.register("tridente_cooldown_fim",
            () -> DataComponentType.<Long>builder().persistent(Codec.LONG).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_LONG).build());

    public static final Supplier<DataComponentType<Long>> TRIDENTE_COOLDOWN_TOTAL = DATA_COMPONENT_TYPES.register("tridente_cooldown_total",
            () -> DataComponentType.<Long>builder().persistent(Codec.LONG).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_LONG).build());

    public static final Supplier<DataComponentType<Integer>> SABRE_CARGAS = DATA_COMPONENT_TYPES.register("sabre_cargas",
            () -> DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT).build());

    public static final Supplier<DataComponentType<Long>> SABRE_RECARGA_FIM = DATA_COMPONENT_TYPES.register("sabre_recarga_fim",
            () -> DataComponentType.<Long>builder().persistent(Codec.LONG).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_LONG).build());

    public static final Supplier<DataComponentType<Long>> SABRE_COOLDOWN_FIM = DATA_COMPONENT_TYPES.register("sabre_cooldown_fim",
            () -> DataComponentType.<Long>builder().persistent(Codec.LONG).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_LONG).build());

    public static final Supplier<DataComponentType<Long>> ESCUDO_COOLDOWN_FIM = DATA_COMPONENT_TYPES.register("escudo_cooldown_fim",
            () -> DataComponentType.<Long>builder().persistent(Codec.LONG).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_LONG).build());

    public static final Supplier<DataComponentType<ItemContainerContents>> SACOLA_VIP_CONTEUDO = DATA_COMPONENT_TYPES.register("sacola_vip_conteudo",
            () -> DataComponentType.<ItemContainerContents>builder().persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC).build());

    public static final Supplier<DataComponentType<String>> SACOLA_VIP_NOME = DATA_COMPONENT_TYPES.register("sacola_vip_nome",
            () -> DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8).build());

    public static final Supplier<DataComponentType<Boolean>> LANTERNA_LIGADA = DATA_COMPONENT_TYPES.register("lanterna_ligada",
            () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.BOOL).build());

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}
