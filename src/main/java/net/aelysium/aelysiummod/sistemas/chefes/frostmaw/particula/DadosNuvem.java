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
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jetbrains.annotations.NotNull;

public record DadosNuvem(float red, float green, float blue, float scale, int duration, EnumCloudBehavior behavior, float airDrag) implements ParticleOptions {
    public static final Codec<EnumCloudBehavior> BEHAVIOUR_CODEC = StringRepresentable.fromEnum(EnumCloudBehavior::values);

    public static final MapCodec<DadosNuvem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("red").forGetter(data -> data.red),
                    Codec.FLOAT.fieldOf("green").forGetter(data -> data.green),
                    Codec.FLOAT.fieldOf("blue").forGetter(data -> data.blue),
                    Codec.FLOAT.fieldOf("scale").forGetter(data -> data.scale),
                    Codec.INT.fieldOf("duration").forGetter(data -> data.duration),
                    BEHAVIOUR_CODEC.fieldOf("behaviour").forGetter(data -> data.behavior),
                    Codec.FLOAT.fieldOf("air_drag").forGetter(data -> data.airDrag)
            ).apply(instance, DadosNuvem::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, DadosNuvem> STREAM_CODEC = NeoForgeStreamCodecs.composite(
            ByteBufCodecs.FLOAT, DadosNuvem::red,
            ByteBufCodecs.FLOAT, DadosNuvem::green,
            ByteBufCodecs.FLOAT, DadosNuvem::blue,
            ByteBufCodecs.FLOAT, DadosNuvem::scale,
            ByteBufCodecs.INT, DadosNuvem::duration,
            NeoForgeStreamCodecs.enumCodec(EnumCloudBehavior.class), DadosNuvem::behavior,
            ByteBufCodecs.FLOAT, DadosNuvem::airDrag,
            DadosNuvem::new
    );

    @Override
    public @NotNull ParticleType<?> getType() {
        return RegistroFrostmaw.CLOUD.value();
    }

    public enum EnumCloudBehavior implements StringRepresentable {
        SHRINK("shrink"),
        GROW("grow"),
        CONSTANT("constant");

        private final String key;

        EnumCloudBehavior(final String key) {
            this.key = key;
        }

        @Override
        public @NotNull String getSerializedName() {
            return key;
        }
    }
}
