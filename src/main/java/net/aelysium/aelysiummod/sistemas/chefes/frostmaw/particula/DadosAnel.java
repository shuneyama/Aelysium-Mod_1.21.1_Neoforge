package net.aelysium.aelysiummod.sistemas.chefes.frostmaw.particula;

import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.RegistroFrostmaw;
import net.aelysium.aelysiummod.sistemas.chefes.frostmaw.CodecsFrostmaw;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jetbrains.annotations.NotNull;

public record DadosAnel(float red, float green, float blue, float alpha, float yaw, float pitch, float scale, int duration, boolean facesCamera, EnumRingBehavior behavior) implements ParticleOptions {
    public static final Codec<EnumRingBehavior> BEHAVIOUR_CODEC = StringRepresentable.fromEnum(EnumRingBehavior::values);

    public static final MapCodec<DadosAnel> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("red").forGetter(DadosAnel::red),
                    Codec.FLOAT.fieldOf("green").forGetter(DadosAnel::green),
                    Codec.FLOAT.fieldOf("blue").forGetter(DadosAnel::blue),
                    Codec.FLOAT.fieldOf("alpha").forGetter(DadosAnel::alpha),
                    Codec.FLOAT.fieldOf("yaw").forGetter(DadosAnel::yaw),
                    Codec.FLOAT.fieldOf("pitch").forGetter(DadosAnel::pitch),
                    Codec.FLOAT.fieldOf("scale").forGetter(DadosAnel::scale),
                    Codec.INT.fieldOf("duration").forGetter(DadosAnel::duration),
                    Codec.BOOL.fieldOf("facesCamera").forGetter(DadosAnel::facesCamera),
                    BEHAVIOUR_CODEC.fieldOf("behaviour").forGetter(DadosAnel::behavior)
            ).apply(instance, DadosAnel::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, DadosAnel> STREAM_CODEC = CodecsFrostmaw.composite(
            ByteBufCodecs.FLOAT, DadosAnel::red,
            ByteBufCodecs.FLOAT, DadosAnel::green,
            ByteBufCodecs.FLOAT, DadosAnel::blue,
            ByteBufCodecs.FLOAT, DadosAnel::alpha,
            ByteBufCodecs.FLOAT, DadosAnel::yaw,
            ByteBufCodecs.FLOAT, DadosAnel::pitch,
            ByteBufCodecs.FLOAT, DadosAnel::scale,
            ByteBufCodecs.INT, DadosAnel::duration,
            ByteBufCodecs.BOOL, DadosAnel::facesCamera,
            NeoForgeStreamCodecs.enumCodec(EnumRingBehavior.class), DadosAnel::behavior,
            DadosAnel::new
    );

    @Override
    public @NotNull ParticleType<DadosAnel> getType() {
        return RegistroFrostmaw.RING.get();
    }

    public enum EnumRingBehavior implements StringRepresentable {
        SHRINK("shrink"),
        GROW("grow"),
        CONSTANT("constant"),
        GROW_THEN_SHRINK("grow_then_shrink");

        private final String key;

        EnumRingBehavior(final String key) {
            this.key = key;
        }

        @Override
        public String getSerializedName() {
            return key;
        }
    }
}
