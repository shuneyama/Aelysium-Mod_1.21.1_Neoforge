package net.aelysium.aelysiummod.sistemas.rp.morte.cliente;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)

public class EstadoSobreposicaoMorte {

    static long impactoInicioMs = -1L;
    static boolean habilitando = true;
    static boolean somTocado = false;
    static boolean piscaAcesoAnterior = false;
    static long glitchInicioMs = -1L;
}
