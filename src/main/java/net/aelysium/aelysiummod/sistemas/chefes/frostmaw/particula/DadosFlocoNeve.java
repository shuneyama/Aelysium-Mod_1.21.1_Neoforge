package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.particula;

import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.RegistroFrostmaw;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record DadosFlocoNeve(float duration, boolean swirls) implements ParticleOptions {
    public static final MapCodec<DadosFlocoNeve> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("duration").forGetter(DadosFlocoNeve::duration),
                    Codec.BOOL.fieldOf("swirls").forGetter(DadosFlocoNeve::swirls)
            ).apply(instance, DadosFlocoNeve::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, DadosFlocoNeve> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, DadosFlocoNeve::duration,
            ByteBufCodecs.BOOL, DadosFlocoNeve::swirls,
            DadosFlocoNeve::new
    );

    @Override
    public @NotNull ParticleType<DadosFlocoNeve> getType() {
        return RegistroFrostmaw.SNOWFLAKE.get();
    }
}
