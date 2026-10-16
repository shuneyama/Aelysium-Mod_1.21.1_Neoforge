package net.aelysium.aelysiummod.sistemas.social.apelido;

public record PredefinicaoApelido(
        String nome,
        String prefixo, int prefixoCor1, int prefixoCor2, int prefixoFormato,
        String nick, int nickCor, int nickFormato,
        String sufixo, int sufixoCor1, int sufixoCor2, int sufixoFormato,
        String pronomes, String badge
) {}
