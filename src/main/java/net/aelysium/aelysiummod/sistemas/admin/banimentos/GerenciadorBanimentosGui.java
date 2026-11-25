package net.aelysium.aelysiummod.sistemas.admin.banimentos;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.rede.PacoteEstadoAlvo;
import net.aelysium.aelysiummod.sistemas.social.apelido.DadosApelido;
import net.aelysium.aelysiummod.sistemas.admin.banimentos.rede.RedeListaBanimentos;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public final class GerenciadorBanimentosGui {
    public static final int PERMISSAO = 2;
    private static final Pattern NICK = Pattern.compile("[A-Za-z0-9_]{1,16}");

    private record Alvo(UUID uuid, String nome, ServerPlayer online) {
    }

    private GerenciadorBanimentosGui() {
    }

    public static void tratar(ServerPlayer autor, String acao, String alvo, String item) {
        if (!autor.hasPermissions(PERMISSAO)) return;
        MinecraftServer servidor = autor.getServer();
        if (servidor == null) return;
        switch (acao) {
            case "banir" -> {
                ResourceLocation id = itemValido(item);
                if (id != null && ConfiguracaoListaBanimentos.addBannedItem(id)) aplicarGlobal(servidor);
            }
            case "desbanir" -> {
                ResourceLocation id = ResourceLocation.tryParse(item);
                if (id != null && ConfiguracaoListaBanimentos.removeBannedItem(id)) aplicarGlobal(servidor);
            }
            case "banir_mod" -> {
                ResourceLocation ar = BuiltInRegistries.ITEM.getKey(net.minecraft.world.item.Items.AIR);
                List<ResourceLocation> itens = BuiltInRegistries.ITEM.keySet().stream()
                        .filter(rl -> rl.getNamespace().equals(item) && !rl.equals(ar))
                        .toList();
                int alterados = ConfiguracaoListaBanimentos.addBannedItems(itens);
                if (alterados > 0) aplicarGlobal(servidor);
                autor.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.banimentos.mod_bloqueado_item_ns_adicionado_s_no", item, alterados, itens.size()).withStyle(ChatFormatting.GREEN));
            }
            case "desbanir_mod" -> {
                List<ResourceLocation> itens = ConfiguracaoListaBanimentos.getBannedItems().stream()
                        .filter(rl -> rl.getNamespace().equals(item))
                        .toList();
                int alterados = ConfiguracaoListaBanimentos.removeBannedItems(itens);
                if (alterados > 0) aplicarGlobal(servidor);
                autor.sendSystemMessage(Component.translatableEscape("mensagem.aelysiummod.banimentos.mod_desbloqueado_item_ns_removido_s", item, alterados).withStyle(ChatFormatting.GREEN));
            }
            case "liberar", "revogar" -> {
                List<Alvo> alvos = resolver(autor, alvo);
                ResourceLocation id = ResourceLocation.tryParse(item);
                if (alvos.isEmpty() || id == null) {
                    enviarEstado(autor, alvo);
                    return;
                }
                DadosBanimentoJogador dados = DadosBanimentoJogador.get(servidor);
                for (Alvo a : alvos) {
                    boolean mudou = "liberar".equals(acao)
                            ? ConfiguracaoListaBanimentos.isBanned(id) && dados.allowItem(a.uuid(), id, autor.getName().getString(), autor.getUUID())
                            : dados.revokeItem(a.uuid(), id);
                    if (mudou && a.online() != null) RedeListaBanimentos.syncToPlayer(a.online());
                }
                enviarEstado(autor, alvo);
            }
            case "liberar_mod", "revogar_mod" -> {
                List<Alvo> alvos = resolver(autor, alvo);
                if (alvos.isEmpty()) {
                    enviarEstado(autor, alvo);
                    return;
                }
                DadosBanimentoJogador dados = DadosBanimentoJogador.get(servidor);
                boolean liberar = "liberar_mod".equals(acao);
                int alterados = 0;
                for (Alvo a : alvos) {
                    int deste = 0;
                    if (liberar) {
                        for (ResourceLocation rl : ConfiguracaoListaBanimentos.getBannedItems()) {
                            if (rl.getNamespace().equals(item) && dados.allowItem(a.uuid(), rl, autor.getName().getString(), autor.getUUID())) deste++;
                        }
                    } else {
                        for (ResourceLocation rl : new ArrayList<>(dados.getAllowedItems(a.uuid()))) {
                            if (rl.getNamespace().equals(item) && dados.revokeItem(a.uuid(), rl)) deste++;
                        }
                    }
                    if (deste > 0 && a.online() != null) RedeListaBanimentos.syncToPlayer(a.online());
                    alterados += deste;
                }
                autor.sendSystemMessage(Component.translatableEscape(
                        liberar ? "mensagem.aelysiummod.banimentos.mod_liberado_para" : "mensagem.aelysiummod.banimentos.mod_revogado_para",
                        item, descricao(alvos), alterados).withStyle(ChatFormatting.GREEN));
                enviarEstado(autor, alvo);
            }
            case "consultar" -> enviarEstado(autor, alvo);
            default -> {
            }
        }
    }

    private static ResourceLocation itemValido(String texto) {
        ResourceLocation id = ResourceLocation.tryParse(texto);
        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) return null;
        return id;
    }

    private static void aplicarGlobal(MinecraftServer servidor) {
        RedeListaBanimentos.syncAllPlayers(servidor);
        GerenciadorFiltroReceitas.reapplyFilter(servidor);
    }

    private static String descricao(List<Alvo> alvos) {
        return alvos.size() == 1 ? alvos.get(0).nome() : alvos.size() + " jogadores";
    }

    private static List<Alvo> resolver(ServerPlayer autor, String texto) {
        List<Alvo> resultado = new ArrayList<>();
        MinecraftServer servidor = autor.getServer();
        if (servidor == null || texto == null) return resultado;
        String limpo = texto.trim();
        if (limpo.isEmpty()) return resultado;
        if (limpo.startsWith("@")) {
            try {
                StringReader leitor = new StringReader(limpo);
                var seletor = EntityArgument.players().parse(leitor);
                if (leitor.canRead()) return resultado;
                for (ServerPlayer p : seletor.findPlayers(autor.createCommandSourceStack())) {
                    resultado.add(new Alvo(p.getUUID(), p.getName().getString(), p));
                }
            } catch (CommandSyntaxException e) {
                return new ArrayList<>();
            }
            return resultado;
        }
        ServerPlayer online = servidor.getPlayerList().getPlayerByName(limpo);
        if (online != null) {
            resultado.add(new Alvo(online.getUUID(), online.getName().getString(), online));
            return resultado;
        }
        DadosApelido apelidos = DadosApelido.get(servidor);
        UUID porNick = apelidos.getUUIDByNick(limpo);
        if (porNick != null) {
            ServerPlayer dono = servidor.getPlayerList().getPlayer(porNick);
            String real = dono != null ? dono.getGameProfile().getName()
                    : (servidor.getProfileCache() == null ? Optional.<GameProfile>empty() : servidor.getProfileCache().get(porNick)).map(GameProfile::getName).orElse(limpo);
            var entrada = apelidos.getNickname(porNick);
            String nick = entrada != null && !entrada.nick().isEmpty() ? net.aelysium.aelysiummod.nucleo.texto.CodigosEstilo.semCodigos(entrada.nick()) : limpo;
            resultado.add(new Alvo(porNick, real.equalsIgnoreCase(nick) ? real : real + " (" + nick + ")", dono));
            return resultado;
        }
        if (!NICK.matcher(limpo).matches()) return resultado;
        Optional<GameProfile> perfil = servidor.getProfileCache() == null ? Optional.empty() : servidor.getProfileCache().get(limpo);
        if (perfil.isPresent()) {
            resultado.add(new Alvo(perfil.get().getId(), perfil.get().getName(), null));
        } else {
            resultado.add(new Alvo(UUIDUtil.createOfflinePlayerUUID(limpo), limpo, null));
        }
        return resultado;
    }

    private static void enviarEstado(ServerPlayer autor, String texto) {
        String limpo = texto == null ? "" : texto.trim();
        List<Alvo> alvos = resolver(autor, limpo);
        if (alvos.isEmpty()) {
            PacketDistributor.sendToPlayer(autor, new PacoteEstadoAlvo(limpo, PacoteEstadoAlvo.NENHUM, "", 0, List.of(), List.of()));
            return;
        }
        DadosBanimentoJogador dados = DadosBanimentoJogador.get(autor.getServer());
        Map<ResourceLocation, List<String>> quem = new LinkedHashMap<>();
        List<String> todos = new ArrayList<>();
        Map<ResourceLocation, int[]> contagem = new LinkedHashMap<>();
        Map<ResourceLocation, DadosBanimentoJogador.Liberacao> detalhes = new LinkedHashMap<>();
        Set<UUID> vistos = new LinkedHashSet<>();
        for (Alvo a : alvos) {
            if (!vistos.add(a.uuid())) continue;
            todos.add(a.nome());
            for (Map.Entry<ResourceLocation, DadosBanimentoJogador.Liberacao> e : dados.getLiberacoes(a.uuid()).entrySet()) {
                contagem.computeIfAbsent(e.getKey(), k -> new int[1])[0]++;
                detalhes.putIfAbsent(e.getKey(), e.getValue());
                quem.computeIfAbsent(e.getKey(), k -> new ArrayList<>()).add(a.nome());
            }
        }
        List<PacoteEstadoAlvo.Item> itens = new ArrayList<>();
        for (Map.Entry<ResourceLocation, int[]> e : contagem.entrySet()) {
            DadosBanimentoJogador.Liberacao lib = detalhes.get(e.getKey());
            itens.add(new PacoteEstadoAlvo.Item(e.getKey(), e.getValue()[0], lib == null ? "" : lib.por(), lib == null ? "" : lib.em(), quem.getOrDefault(e.getKey(), List.of())));
        }
        int tipo = vistos.size() > 1 ? PacoteEstadoAlvo.VARIOS : (alvos.get(0).online() != null ? PacoteEstadoAlvo.ONLINE : PacoteEstadoAlvo.OFFLINE);
        String nome = tipo == PacoteEstadoAlvo.VARIOS ? "" : alvos.get(0).nome();
        PacketDistributor.sendToPlayer(autor, new PacoteEstadoAlvo(limpo, tipo, nome, vistos.size(), todos, itens));
    }
}
