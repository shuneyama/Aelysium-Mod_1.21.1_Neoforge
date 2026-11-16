package net.aelysium.aelysiummod.sistemas.social.apelido.cliente;

import net.aelysium.aelysiummod.sistemas.social.apelido.UtilitariosApelido;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class ManipuladorApelidoCliente {

    private static final Map<UUID, CacheApelido> NICKNAME_MAP = new HashMap<>();
    private static final Map<String, UUID> NICK_TO_UUID = new HashMap<>();

    public static void set(UUID uuid, String prefix, int prefixCor1, int prefixCor2, int prefixFormat,
                           String nick, int nickCor, int nickFormat,
                           String suffix, int suffixCor1, int suffixCor2, int suffixFormat) {
        CacheApelido old = NICKNAME_MAP.get(uuid);
        if (old != null && !old.nick.isEmpty()) {
            NICK_TO_UUID.remove(old.nick.toLowerCase(Locale.ROOT));
        }

        CacheApelido cache = new CacheApelido(prefix, prefixCor1, prefixCor2, prefixFormat,
                nick, nickCor, nickFormat,
                suffix, suffixCor1, suffixCor2, suffixFormat);
        NICKNAME_MAP.put(uuid, cache);
        if (!nick.isEmpty()) {
            NICK_TO_UUID.put(nick.toLowerCase(Locale.ROOT), uuid);
        }
    }

    public static void remove(UUID uuid) {
        CacheApelido old = NICKNAME_MAP.remove(uuid);
        if (old != null && !old.nick.isEmpty()) {
            NICK_TO_UUID.remove(old.nick.toLowerCase(Locale.ROOT));
        }
    }

    public static boolean hasNickname(UUID uuid) {
        return NICKNAME_MAP.containsKey(uuid);
    }

    public static String getRawNick(UUID uuid) {
        CacheApelido cache = NICKNAME_MAP.get(uuid);
        return cache != null ? cache.nick : null;
    }

    public static MutableComponent getStyledFullName(UUID uuid) {
        CacheApelido c = NICKNAME_MAP.get(uuid);
        if (c == null) return null;

        if (c.nomeMontado == null) c.nomeMontado = montarNome(c);
        return c.nomeMontado.copy();
    }

    private static MutableComponent montarNome(CacheApelido c) {
        MutableComponent result = Component.empty();
        boolean hasPrefix = !c.prefix.isEmpty();
        boolean hasSuffix = !c.suffix.isEmpty();

        if (hasPrefix) {
            result.append(UtilitariosApelido.renderDualColor(c.prefix, c.prefixCor1, c.prefixCor2, c.prefixFormat));
            result.append(Component.literal(" "));
        }

        result.append(UtilitariosApelido.renderSingleColor(
                c.nick.isEmpty() ? "???" : c.nick,
                c.nickCor, c.nickFormat
        ));

        if (hasSuffix) {
            result.append(Component.literal(" "));
            result.append(UtilitariosApelido.renderDualColor(c.suffix, c.suffixCor1, c.suffixCor2, c.suffixFormat));
        }

        return result;
    }

    public static Collection<String> getAllNicknames() {
        List<String> nicks = new ArrayList<>();
        for (CacheApelido c : NICKNAME_MAP.values()) {
            if (!c.nick.isEmpty()) nicks.add(c.nick);
        }
        return nicks;
    }

    public static UUID getUUIDByNick(String nick) {
        return NICK_TO_UUID.get(nick.toLowerCase(Locale.ROOT));
    }

    public static void clear() {
        NICKNAME_MAP.clear();
        NICK_TO_UUID.clear();
    }

    private static class CacheApelido {
        final String prefix;
        final int prefixCor1, prefixCor2, prefixFormat;
        final String nick;
        final int nickCor, nickFormat;
        final String suffix;
        final int suffixCor1, suffixCor2, suffixFormat;
        MutableComponent nomeMontado;

        CacheApelido(String prefix, int prefixCor1, int prefixCor2, int prefixFormat,
                      String nick, int nickCor, int nickFormat,
                      String suffix, int suffixCor1, int suffixCor2, int suffixFormat) {
            this.prefix = prefix;
            this.prefixCor1 = prefixCor1;
            this.prefixCor2 = prefixCor2;
            this.prefixFormat = prefixFormat;
            this.nick = nick;
            this.nickCor = nickCor;
            this.nickFormat = nickFormat;
            this.suffix = suffix;
            this.suffixCor1 = suffixCor1;
            this.suffixCor2 = suffixCor2;
            this.suffixFormat = suffixFormat;
        }
    }
}