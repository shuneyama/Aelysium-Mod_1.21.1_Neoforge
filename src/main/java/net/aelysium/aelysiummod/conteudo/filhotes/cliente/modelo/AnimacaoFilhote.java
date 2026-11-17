package net.aelysium.aelysiummod.conteudo.filhotes.cliente.modelo;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class AnimacaoFilhote {

    private final AnimationDefinition definicao;
    private final List<Entrada> entradas;
    private final Vector3f vetor = new Vector3f();

    private AnimacaoFilhote(AnimationDefinition definicao, List<Entrada> entradas) {
        this.definicao = definicao;
        this.entradas = entradas;
    }

    public static AnimacaoFilhote preparar(ModelPart raiz, AnimationDefinition definicao) {
        List<Entrada> entradas = new ArrayList<>();
        for (Map.Entry<String, List<AnimationChannel>> entrada : definicao.boneAnimations().entrySet()) {
            ModelPart parte = buscar(raiz, entrada.getKey());
            if (parte == null) continue;
            for (AnimationChannel canal : entrada.getValue()) {
                entradas.add(new Entrada(parte, canal.target(), canal.keyframes()));
            }
        }
        return new AnimacaoFilhote(definicao, List.copyOf(entradas));
    }

    private static ModelPart buscar(ModelPart raiz, String nome) {
        ModelPart parte = raiz.getAllParts().filter(p -> p.hasChild(nome)).findFirst().map(p -> p.getChild(nome)).orElse(null);
        if (parte == null && "root".equals(nome)) return raiz;
        return parte;
    }

    public void andar(float posicao, float velocidade, float fatorVelocidade, float fatorEscala) {
        aplicar((long) (posicao * 50.0F * fatorVelocidade), Math.min(velocidade * fatorEscala, 1.0F));
    }

    public void estado(AnimationState estado, float ageInTicks) {
        estado(estado, ageInTicks, 1.0F);
    }

    public void estado(AnimationState estado, float ageInTicks, float velocidade) {
        estado.updateTime(ageInTicks, velocidade);
        estado.ifStarted(s -> aplicar(s.getAccumulatedTime(), 1.0F));
    }

    public void estatico() {
        aplicar(0L, 1.0F);
    }

    public void aplicar(long milissegundos, float escala) {
        float segundos = (float) milissegundos / 1000.0F;
        if (definicao.looping()) segundos %= definicao.lengthInSeconds();
        for (Entrada entrada : entradas) {
            entrada.aplicar(segundos, escala, vetor);
        }
    }

    private record Entrada(ModelPart parte, AnimationChannel.Target alvo, Keyframe[] quadros) {
        void aplicar(float segundos, float escala, Vector3f vetor) {
            int anterior = Math.max(0, Mth.binarySearch(0, quadros.length, i -> segundos <= quadros[i].timestamp()) - 1);
            int proximo = Math.min(quadros.length - 1, anterior + 1);
            Keyframe quadroAnterior = quadros[anterior];
            Keyframe quadroProximo = quadros[proximo];
            float delta = segundos - quadroAnterior.timestamp();
            float alfa = proximo != anterior ? Mth.clamp(delta / (quadroProximo.timestamp() - quadroAnterior.timestamp()), 0.0F, 1.0F) : 0.0F;
            quadroProximo.interpolation().apply(vetor, alfa, quadros, anterior, proximo, escala);
            alvo.apply(parte, vetor);
        }
    }
}
