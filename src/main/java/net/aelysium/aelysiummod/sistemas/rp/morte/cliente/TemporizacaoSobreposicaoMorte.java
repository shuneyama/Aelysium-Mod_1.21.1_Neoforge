package net.aelysium.aelysiummod.sistemas.rp.morte.cliente;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)

public class TemporizacaoSobreposicaoMorte {

    static final long POP_MS = 500L;
    static final long BUMP_MS = 180L;
    static final long DECAY_MS = 500L;
    static final long HOLD_MS = 3250L;
    static final long FADEOUT_MS = 500L;
    static final long TOTAL_MS = POP_MS + BUMP_MS + DECAY_MS + HOLD_MS + FADEOUT_MS;
}
