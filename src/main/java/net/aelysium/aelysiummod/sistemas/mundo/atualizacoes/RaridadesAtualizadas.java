package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes;

import net.minecraft.world.item.Rarity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RaridadesAtualizadas {

    private static final Map<String, Rarity> MAPA = new HashMap<>();

    static {
        definir(Rarity.COMMON, "end_crystal", "golden_apple");
        definir(Rarity.UNCOMMON,
                "sniffer_egg", "chainmail_helmet", "chainmail_chestplate", "chainmail_leggings", "chainmail_boots",
                "recovery_compass", "disc_fragment_5", "nautilus_shell", "echo_shard", "goat_horn", "ominous_bottle",
                "netherite_upgrade_smithing_template",
                "sentry_armor_trim_smithing_template", "dune_armor_trim_smithing_template", "coast_armor_trim_smithing_template",
                "wild_armor_trim_smithing_template", "tide_armor_trim_smithing_template", "snout_armor_trim_smithing_template",
                "rib_armor_trim_smithing_template", "wayfinder_armor_trim_smithing_template", "shaper_armor_trim_smithing_template",
                "raiser_armor_trim_smithing_template", "host_armor_trim_smithing_template", "flow_armor_trim_smithing_template",
                "bolt_armor_trim_smithing_template",
                "music_disc_13", "music_disc_cat", "music_disc_blocks", "music_disc_chirp", "music_disc_creator_music_box",
                "music_disc_far", "music_disc_mall", "music_disc_mellohi", "music_disc_stal", "music_disc_strad",
                "music_disc_ward", "music_disc_11", "music_disc_wait", "music_disc_relic", "music_disc_5", "music_disc_precipice");
        definir(Rarity.RARE,
                "enchanted_golden_apple", "trident", "nether_star",
                "ward_armor_trim_smithing_template", "eye_armor_trim_smithing_template", "vex_armor_trim_smithing_template",
                "spire_armor_trim_smithing_template",
                "wither_skeleton_skull", "skull_banner_pattern", "mojang_banner_pattern",
                "music_disc_pigstep", "music_disc_otherside", "music_disc_creator");
        definir(Rarity.EPIC, "elytra", "dragon_head", "silence_armor_trim_smithing_template");
    }

    private RaridadesAtualizadas() {
    }

    private static void definir(Rarity raridade, String... caminhos) {
        for (String caminho : caminhos) MAPA.put(caminho, raridade);
    }

    public static Rarity de(String caminho) {
        if (caminho.endsWith("_pottery_sherd")) return Rarity.UNCOMMON;
        return MAPA.get(caminho);
    }
}
