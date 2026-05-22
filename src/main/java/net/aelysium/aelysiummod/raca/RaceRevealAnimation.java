package net.aelysium.aelysiummod.raca;

import net.aelysium.aelysiummod.network.ValkyriaFlightPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RaceRevealAnimation {

    private static final String ZALGO_CHARS = "\u1511\u0283\u14B5\u2193\u14B7\u2389\u2283\u2351\u2550\u22EE\uA596\uA596\u14B2\u30EA\u004A\u0021\u00A1\u1511\u2237\u14AD\u2138\u269A\u2368\u2234\u0307\u002F\u007C\u007C\u2A05";
    private static final int REVEAL_TICKS = 10;
    private static final int TOTAL_STEPS = 10;
    private static final int POST_REVEAL_DELAY = 10;

    private static final Map<UUID, AnimationData> activeAnimations = new ConcurrentHashMap<>();
    private static final Random random = new Random();

    private static final Map<RaceType, Integer> RACE_COLORS = Map.of(
            RaceType.HUMANO,   0xC4C4C4,
            RaceType.ELVARIN,  0x568203,
            RaceType.DRACONO,  0x000080,
            RaceType.TIEFLING, 0xB22222,
            RaceType.UNDYNE,   0x4682B4,
            RaceType.VALKYRIA, 0xFFEE8C,
            RaceType.NONE,     0xAAAAAA
    );

    private enum Phase {
        REVEALING,
        WAITING,
        DONE
    }

    private static class AnimationData {
        final ServerPlayer player;
        final RaceType race;
        final String raceName;
        final int color;
        int step;
        int tickCounter;
        final boolean[] revealed;
        Phase phase;

        AnimationData(ServerPlayer player, RaceType race, int color) {
            this.player = player;
            this.race = race;
            this.raceName = race.displayName;
            this.color = color;
            this.step = 0;
            this.tickCounter = 0;
            this.revealed = new boolean[raceName.length()];
            this.phase = Phase.REVEALING;
        }
    }

    public static boolean isAnimating(UUID uuid) {
        return activeAnimations.containsKey(uuid);
    }

    public static void start(ServerPlayer player, RaceType race, RaceType oldRace) {
        if (race == RaceType.NONE) return;

        player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 70, 20));

        int color = RACE_COLORS.getOrDefault(race, 0xFFFFFF);
        AnimationData data = new AnimationData(player, race, color);
        activeAnimations.put(player.getUUID(), data);

        sendFrame(data);
    }

    public static void tick() {
        Iterator<Map.Entry<UUID, AnimationData>> iter = activeAnimations.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<UUID, AnimationData> entry = iter.next();
            AnimationData data = entry.getValue();

            if (data.player.hasDisconnected() || !data.player.isAlive()) {
                applyRaceNow(data);
                iter.remove();
                continue;
            }

            data.tickCounter++;

            switch (data.phase) {
                case REVEALING -> {
                    if (data.tickCounter >= REVEAL_TICKS) {
                        data.tickCounter = 0;
                        data.step++;

                        if (data.step >= TOTAL_STEPS) {
                            sendFinal(data);
                            data.phase = Phase.WAITING;
                            data.tickCounter = 0;
                            continue;
                        }

                        int revealedCount = countRevealed(data);
                        revealNextLetters(data);
                        int newRevealedCount = countRevealed(data);

                        if (newRevealedCount > revealedCount) {
                            playNoteSound(data.player);
                        }

                        sendFrame(data);
                    }
                }
                case WAITING -> {
                    if (data.tickCounter >= POST_REVEAL_DELAY) {
                        playBeaconSound(data.player);
                        applyRaceNow(data);
                        data.phase = Phase.DONE;
                        iter.remove();
                    }
                }
                default -> iter.remove();
            }
        }
    }

    private static void applyRaceNow(AnimationData data) {
        ServerPlayer player = data.player;
        RaceType race = data.race;

        player.removeAllEffects();

        RaceManager.applyRace(player, race);
        RaceData.get(player.serverLevel()).setRace(player.getUUID(), race);
        RaceTicker.updateCache(player.getUUID(), race);

        if (race != RaceType.VALKYRIA) {
            PacketDistributor.sendToPlayer(player, new ValkyriaFlightPacket(false, 0, 0));
        }

        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.HEAL, 5, 10, false, false, false));
    }

    private static int countRevealed(AnimationData data) {
        int count = 0;
        for (boolean b : data.revealed) {
            if (b) count++;
        }
        return count;
    }

    private static void revealNextLetters(AnimationData data) {
        List<Integer> hidden = new ArrayList<>();
        for (int i = 0; i < data.raceName.length(); i++) {
            if (!data.revealed[i] && data.raceName.charAt(i) != ' ') {
                hidden.add(i);
            }
        }

        if (hidden.isEmpty()) return;

        int toReveal = Math.max(1, hidden.size() / Math.max(1, TOTAL_STEPS - data.step));
        Collections.shuffle(hidden, random);

        for (int i = 0; i < Math.min(toReveal, hidden.size()); i++) {
            data.revealed[hidden.get(i)] = true;
        }
    }

    private static void sendFrame(AnimationData data) {
        MutableComponent title = Component.literal("Raça Definida:")
                .withStyle(Style.EMPTY.withColor(0xAAAAAA).withBold(false));

        MutableComponent subtitle = Component.empty();
        for (int i = 0; i < data.raceName.length(); i++) {
            char real = data.raceName.charAt(i);
            if (real == ' ') {
                subtitle.append(Component.literal(" "));
                continue;
            }

            if (data.revealed[i]) {
                subtitle.append(Component.literal(String.valueOf(real))
                        .withStyle(Style.EMPTY.withColor(data.color).withBold(true)));
            } else {
                char zalgo = ZALGO_CHARS.charAt(random.nextInt(ZALGO_CHARS.length()));
                int glitchColor = mixColor(data.color, 0x333333, 0.4 + random.nextDouble() * 0.3);
                subtitle.append(Component.literal(String.valueOf(zalgo))
                        .withStyle(Style.EMPTY.withColor(glitchColor).withBold(false).withObfuscated(true)));
            }
        }

        data.player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 70, 20));
        data.player.connection.send(new ClientboundSetTitleTextPacket(title));
        data.player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
    }

    private static void sendFinal(AnimationData data) {
        MutableComponent title = Component.literal("Raça Definida:")
                .withStyle(Style.EMPTY.withColor(0xAAAAAA).withBold(false));

        MutableComponent subtitle = Component.literal(data.raceName)
                .withStyle(Style.EMPTY.withColor(data.color).withBold(true));

        data.player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 60, 20));
        data.player.connection.send(new ClientboundSetTitleTextPacket(title));
        data.player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
    }

    private static void playNoteSound(ServerPlayer player) {
        player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.MASTER, 0.6f, 0.8f + random.nextFloat() * 0.4f);
    }

    private static void playBeaconSound(ServerPlayer player) {
        player.playNotifySound(SoundEvents.BEACON_ACTIVATE, SoundSource.MASTER, 1.0f, 1.0f);
    }

    private static int mixColor(int a, int b, double t) {
        int rA = (a >> 16) & 0xFF, gA = (a >> 8) & 0xFF, bA = a & 0xFF;
        int rB = (b >> 16) & 0xFF, gB = (b >> 8) & 0xFF, bB = b & 0xFF;
        int r = (int) (rA * (1 - t) + rB * t);
        int g = (int) (gA * (1 - t) + gB * t);
        int blue = (int) (bA * (1 - t) + bB * t);
        return (r << 16) | (g << 8) | blue;
    }
}