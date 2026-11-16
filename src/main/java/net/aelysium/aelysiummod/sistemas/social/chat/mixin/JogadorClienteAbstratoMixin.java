package net.aelysium.aelysiummod.sistemas.social.chat.mixin;

import net.aelysium.aelysiummod.nucleo.texto.CodigosEstilo;
import com.mojang.authlib.GameProfile;
import net.aelysium.aelysiummod.sistemas.social.chat.ConfiguracaoBalao;
import net.aelysium.aelysiummod.sistemas.social.chat.EstiloBalao;
import net.aelysium.aelysiummod.sistemas.social.chat.IJogadorAelysium;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Supplier;

@Mixin(AbstractClientPlayer.class)
public abstract class JogadorClienteAbstratoMixin extends Player implements IJogadorAelysium {

    @Unique
    private List<IJogadorAelysium.MensagemBalao> aelysium$balloons;

    @Unique
    private final Collection<Supplier<Boolean>> aelysium$tickEvents = new ConcurrentLinkedDeque<>();

    public JogadorClienteAbstratoMixin(Level level, BlockPos pos, float yRot, GameProfile profile) {
        super(level, pos, yRot, profile);
    }

    @Override
    public void aelysium$createBalloon(String text, int duration, int corTexto, int corFundo, int corBorda, float altura, EstiloBalao estilo) {
        if (aelysium$balloons == null) {
            aelysium$balloons = new ArrayList<>();
        }

        int[] charDelays = aelysium$calculateCharDelays(CodigosEstilo.semCodigos(text));

        long totalTypingTime = 0;
        for (int delay : charDelays) {
            totalTypingTime += delay;
        }

        IJogadorAelysium.MensagemBalao msg = new IJogadorAelysium.MensagemBalao(
                text, corTexto, corFundo, corBorda, altura, estilo,
                System.currentTimeMillis(), charDelays
        );
        aelysium$balloons.add(0, msg);

        while (aelysium$balloons.size() > ConfiguracaoBalao.MAX_BALLOONS_PER_PLAYER) {
            aelysium$balloons.remove(aelysium$balloons.size() - 1);
        }

        final long typingDurationMs = totalTypingTime;
        final long startTime = System.currentTimeMillis();
        final long displayDurationMs = (duration * 50L);

        aelysium$tickEvents.add(() -> {
            long elapsed = System.currentTimeMillis() - startTime;

            if (elapsed >= typingDurationMs + displayDurationMs) {
                if (aelysium$balloons != null) {
                    aelysium$balloons.remove(msg);
                }
                return true;
            }
            return false;
        });
    }

    @Unique
    private int[] aelysium$calculateCharDelays(String text) {
        int[] delays = new int[text.length()];

        for (int i = 0; i < text.length(); i++) {
            if (i > 0) {
                char prevChar = text.charAt(i - 1);
                if (ConfiguracaoBalao.PUNCTUATION_CHARS.indexOf(prevChar) != -1) {
                    delays[i] = ConfiguracaoBalao.PUNCTUATION_DELAY_MS;
                } else {
                    delays[i] = ConfiguracaoBalao.CHAR_DELAY_MS;
                }
            } else {
                delays[i] = ConfiguracaoBalao.CHAR_DELAY_MS;
            }
        }

        return delays;
    }

    @Override
    public List<MensagemBalao> aelysium$getBalloons() {
        return aelysium$balloons;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void aelysium$processTick(CallbackInfo ci) {
        var toRemove = new HashSet<Supplier<Boolean>>();
        for (Supplier<Boolean> event : aelysium$tickEvents) {
            if (event.get()) {
                toRemove.add(event);
            }
        }
        if (!toRemove.isEmpty()) {
            aelysium$tickEvents.removeAll(toRemove);
        }
    }
}
