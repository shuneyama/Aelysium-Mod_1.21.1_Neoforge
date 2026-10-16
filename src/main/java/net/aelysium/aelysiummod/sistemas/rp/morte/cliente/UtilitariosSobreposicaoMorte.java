package net.aelysium.aelysiummod.sistemas.rp.morte.cliente;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)

public class UtilitariosSobreposicaoMorte {

    static float hashSalto(long seed, int canal) {
        long x = (seed * 2654435761L) ^ (canal * 0x9E3779B97F4A7C15L);
        x = (x ^ (x >>> 33)) * 0xFF51AFD7ED558CCDL;
        x = (x ^ (x >>> 33)) * 0xC4CEB9FE1A85EC53L;
        x = x ^ (x >>> 33);
        return ((x & 0xFFFF) / 65535f) * 2f - 1f;
    }

    static float smoothstep(float x) {
        x = Math.max(0f, Math.min(1f, x));
        return x * x * (3f - 2f * x);
    }
}
