package net.aelysium.aelysiummod.sistemas.rp.personagem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.nucleo.jogador.TipoDeus;
import net.aelysium.aelysiummod.sistemas.rp.deus.DadosDeus;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public final class AtributosPersonagem {
    public record Entrada(ResourceLocation atributo, String operacao, double valor) {
    }

    public static final Map<String, AttributeModifier.Operation> OPERACOES = new LinkedHashMap<>();

    static {
        OPERACOES.put("soma", AttributeModifier.Operation.ADD_VALUE);
        OPERACOES.put("porcentagem", AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    private static final String PREFIXO = "personagem_";
    private static final String SEPARADOR = "__";
    private static final double FATOR_PORCENTAGEM = 0.01;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, List<Entrada>> DADOS = new ConcurrentHashMap<>();
    private static Path arquivo;

    private AtributosPersonagem() {
    }

    public static synchronized void carregar(MinecraftServer servidor) {
        DADOS.clear();
        arquivo = net.aelysium.aelysiummod.sistemas.rp.PastaPersonagens.arquivo(servidor, "personagens_atributos.json");
        if (!Files.exists(arquivo)) return;
        try {
            JsonObject raiz = GSON.fromJson(Files.readString(arquivo, StandardCharsets.UTF_8), JsonObject.class);
            if (raiz == null) return;
            for (Map.Entry<String, JsonElement> e : raiz.entrySet()) {
                List<Entrada> lista = new ArrayList<>();
                for (JsonElement el : e.getValue().getAsJsonArray()) {
                    JsonObject o = el.getAsJsonObject();
                    ResourceLocation id = ResourceLocation.tryParse(o.get("atributo").getAsString());
                    String operacao = o.get("operacao").getAsString();
                    if (id != null && OPERACOES.containsKey(operacao)) lista.add(new Entrada(id, operacao, o.get("valor").getAsDouble()));
                }
                if (!lista.isEmpty()) DADOS.put(e.getKey(), lista);
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(AelysiumMod.MOD_ID).error("Erro ao carregar personagens_atributos.json: ", e);
        }
    }

    private static void salvar() {
        if (arquivo == null) return;
        try {
            Files.createDirectories(arquivo.getParent());
            JsonObject raiz = new JsonObject();
            for (Map.Entry<String, List<Entrada>> e : DADOS.entrySet()) {
                JsonArray lista = new JsonArray();
                for (Entrada entrada : e.getValue()) {
                    JsonObject o = new JsonObject();
                    o.addProperty("atributo", entrada.atributo().toString());
                    o.addProperty("operacao", entrada.operacao());
                    o.addProperty("valor", entrada.valor());
                    lista.add(o);
                }
                raiz.add(e.getKey(), lista);
            }
            Files.writeString(arquivo, GSON.toJson(raiz), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LoggerFactory.getLogger(AelysiumMod.MOD_ID).error("Erro ao salvar personagens_atributos.json: ", e);
        }
    }

    public static List<Entrada> obter(String personagemId) {
        return List.copyOf(DADOS.getOrDefault(personagemId, List.of()));
    }

    public static synchronized void definir(String personagemId, Entrada nova) {
        List<Entrada> lista = new ArrayList<>(DADOS.getOrDefault(personagemId, List.of()));
        lista.removeIf(e -> e.atributo().equals(nova.atributo()) && e.operacao().equals(nova.operacao()));
        lista.add(nova);
        DADOS.put(personagemId, lista);
        salvar();
    }

    public static synchronized int remover(String personagemId, ResourceLocation atributo) {
        List<Entrada> lista = new ArrayList<>(DADOS.getOrDefault(personagemId, List.of()));
        int antes = lista.size();
        lista.removeIf(e -> e.atributo().equals(atributo));
        int removidos = antes - lista.size();
        if (removidos > 0) {
            if (lista.isEmpty()) DADOS.remove(personagemId);
            else DADOS.put(personagemId, lista);
            salvar();
        }
        return removidos;
    }

    public static synchronized int limpar(String personagemId) {
        List<Entrada> removidas = DADOS.remove(personagemId);
        if (removidas == null) return 0;
        salvar();
        return removidas.size();
    }

    private static ResourceLocation idModificador(Entrada entrada) {
        ResourceLocation a = entrada.atributo();
        return ResourceLocation.fromNamespaceAndPath(AelysiumMod.MOD_ID,
                PREFIXO + a.getNamespace() + "_" + a.getPath().replace("/", "_").replace(":", "_") + SEPARADOR + entrada.operacao());
    }

    public static void removerDoJogador(ServerPlayer jogador) {
        for (Holder<Attribute> holder : BuiltInRegistries.ATTRIBUTE.holders().toList()) {
            AttributeInstance instancia = jogador.getAttribute(holder);
            if (instancia == null) continue;
            List<ResourceLocation> alvos = new ArrayList<>();
            for (AttributeModifier m : instancia.getModifiers()) {
                if (m.id().getNamespace().equals(AelysiumMod.MOD_ID) && m.id().getPath().startsWith(PREFIXO)) alvos.add(m.id());
            }
            for (ResourceLocation alvo : alvos) instancia.removeModifier(alvo);
        }
    }

    public static void aplicar(ServerPlayer jogador, String personagemId) {
        removerDoJogador(jogador);
        for (Entrada entrada : obter(personagemId)) {
            Holder<Attribute> holder = BuiltInRegistries.ATTRIBUTE.getHolder(entrada.atributo()).orElse(null);
            if (holder == null) continue;
            AttributeInstance instancia = jogador.getAttribute(holder);
            if (instancia == null) continue;
            AttributeModifier.Operation operacao = OPERACOES.get(entrada.operacao());
            double valor = operacao == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL ? entrada.valor() * FATOR_PORCENTAGEM : entrada.valor();
            instancia.addPermanentModifier(new AttributeModifier(idModificador(entrada), valor, operacao));
        }
    }

    public static final String PREFIXO_DEUS = "deus:";

    public static String chaveDeus(String deusId) {
        return PREFIXO_DEUS + deusId;
    }

    public static String chaveAtiva(ServerPlayer jogador) {
        TipoDeus deus = DadosDeus.get(jogador.serverLevel()).getDeus(jogador.getUUID());
        if (deus != TipoDeus.NONE) return chaveDeus(deus.id);
        String atual = GerenciadorPersonagens.atual(jogador);
        return GerenciadorPersonagens.SEM_PERSONAGEM.equals(atual) ? null : atual;
    }

    public static void reaplicarUsuarios(MinecraftServer servidor, String chave) {
        for (ServerPlayer p : servidor.getPlayerList().getPlayers()) {
            if (chave.equalsIgnoreCase(chaveAtiva(p))) aplicar(p, chave);
        }
    }

    @SubscribeEvent
    public static void aoEntrar(PlayerEvent.PlayerLoggedInEvent evento) {
        if (!(evento.getEntity() instanceof ServerPlayer jogador)) return;
        String chave = chaveAtiva(jogador);
        if (chave == null) removerDoJogador(jogador);
        else aplicar(jogador, chave);
    }

    @SubscribeEvent
    public static void aoPararServidor(ServerStoppedEvent evento) {
        DADOS.clear();
        arquivo = null;
    }
}
