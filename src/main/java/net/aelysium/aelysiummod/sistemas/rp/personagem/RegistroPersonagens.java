package net.aelysium.aelysiummod.sistemas.rp.personagem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.aelysium.aelysiummod.AelysiumMod;
import net.aelysium.aelysiummod.nucleo.jogador.ConsultaInsignia;
import net.aelysium.aelysiummod.nucleo.jogador.TipoInsignia;
import net.aelysium.aelysiummod.nucleo.jogador.TipoRaca;
import net.aelysium.aelysiummod.sistemas.rp.deus.RegistroDeuses;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = AelysiumMod.MOD_ID)
public final class RegistroPersonagens {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<Personagem> LISTA = new ArrayList<>();
    private static Path arquivo;

    private RegistroPersonagens() {
    }

    public static synchronized void carregar(MinecraftServer servidor) {
        arquivo = net.aelysium.aelysiummod.sistemas.rp.PastaPersonagens.arquivo(servidor, "personagens.json");
        LISTA.clear();
        if (!Files.exists(arquivo)) {
            padroes();
            salvar();
            return;
        }
        try {
            JsonArray lista = GSON.fromJson(Files.readString(arquivo, StandardCharsets.UTF_8), JsonArray.class);
            if (lista == null) return;
            for (JsonElement elemento : lista) {
                LISTA.add(Personagem.deJson(elemento.getAsJsonObject()));
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(AelysiumMod.MOD_ID).error("Erro ao carregar personagens.json: ", e);
        }
    }

    private static void padroes() {
        adicionarPadrao("Shune", TipoRaca.ELVARIN);
        adicionarPadrao("Amelie", TipoRaca.TIEFLING);
        adicionarPadrao("Y.S.I.S", TipoRaca.ROBO);
        adicionarPadrao("Akari", TipoRaca.DRACONO);
        adicionarPadrao("Eldrax", TipoRaca.DRACONO);
        adicionarPadrao("Alexander", TipoRaca.ELVARIN);
        adicionarPadrao("Alina", TipoRaca.HUMANO);
        adicionarPadrao("Eilowen", TipoRaca.FAUNARI);
    }

    private static void adicionarPadrao(String nome, TipoRaca raca) {
        TipoInsignia insignia = TipoInsignia.forRace(raca);
        String badge = insignia == null ? ConsultaInsignia.AUTOMATICA : insignia.id;
        LISTA.add(new Personagem(RegistroDeuses.gerarId(nome), nome, "", 0xFFFFFF, -1, 0, 0xFFFFFF, 0,
                "", 0xFFFFFF, -1, 0, "", badge, raca.id).saneado());
    }

    public static synchronized List<Personagem> todos() {
        return new ArrayList<>(LISTA);
    }

    public static synchronized Personagem buscar(String id) {
        if (id == null) return null;
        for (Personagem p : LISTA) {
            if (p.id().equalsIgnoreCase(id)) return p;
        }
        return null;
    }

    public static synchronized Personagem salvarOuCriar(Personagem bruto) {
        Personagem p = bruto.saneado();
        if (p.nome().isEmpty()) return null;
        if (!p.id().isEmpty()) {
            for (int i = 0; i < LISTA.size(); i++) {
                if (LISTA.get(i).id().equalsIgnoreCase(p.id())) {
                    LISTA.set(i, p);
                    salvar();
                    return p;
                }
            }
        }
        String base = RegistroDeuses.gerarId(p.nome());
        if (base.isEmpty()) base = "personagem";
        String id = base;
        int n = 2;
        while (buscar(id) != null) id = base + "_" + n++;
        Personagem novo = p.comId(id);
        LISTA.add(novo);
        salvar();
        return novo;
    }

    public static synchronized boolean excluir(String id) {
        boolean removido = LISTA.removeIf(p -> p.id().equalsIgnoreCase(id));
        if (removido) salvar();
        return removido;
    }

    private static void salvar() {
        if (arquivo == null) return;
        try {
            Files.createDirectories(arquivo.getParent());
            JsonArray lista = new JsonArray();
            for (Personagem p : LISTA) lista.add(p.paraJson());
            Files.writeString(arquivo, GSON.toJson(lista), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LoggerFactory.getLogger(AelysiumMod.MOD_ID).error("Erro ao salvar personagens.json: ", e);
        }
    }

    @SubscribeEvent
    public static void aoPararServidor(ServerStoppedEvent evento) {
        synchronized (RegistroPersonagens.class) {
            LISTA.clear();
            arquivo = null;
        }
    }
}
