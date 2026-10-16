package net.aelysium.aelysiummod.sistemas.admin.discord.comando;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.aelysium.aelysiummod.sistemas.admin.discord.ConfiguracaoDiscord;

public class ComandoDiscord {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("discord")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("recarregar")
                        .executes(ctx -> {
                            ConfiguracaoDiscord.load();
                            ctx.getSource().sendSuccess(() -> Component.translatableEscape("mensagem.aelysiummod.discord.config_discord_recarregada_caminho", ConfiguracaoDiscord.getConfigPath()), true);
                            return 1;
                        })
                )
                .then(Commands.literal("status")
                        .executes(ctx -> {
                            ConfiguracaoDiscord.Dados d = ConfiguracaoDiscord.get();
                            MutableComponent texto = Component.literal("§8§m                              \n");
                            texto.append(Component.translatableEscape("mensagem.aelysiummod.discord.status_relay",
                                    estado(d.ativo, "mensagem.aelysiummod.discord.estado_ativo_caps", "mensagem.aelysiummod.discord.estado_inativo_caps"))).append("\n");
                            texto.append(Component.translatableEscape("mensagem.aelysiummod.discord.status_webhook_chat", webhook(d.webhookChat))).append("\n");
                            texto.append(Component.translatableEscape("mensagem.aelysiummod.discord.status_webhook_comandos", webhook(d.webhookComandos))).append("\n");
                            texto.append(Component.translatableEscape("mensagem.aelysiummod.discord.status_webhook_social_spy", webhook(d.webhookSocialSpy))).append("\n");
                            texto.append(Component.translatableEscape("mensagem.aelysiummod.discord.status_social_spy",
                                    estado(d.socialSpyAtivo, "mensagem.aelysiummod.discord.estado_ativo_caps", "mensagem.aelysiummod.discord.estado_inativo"))).append("\n");
                            texto.append(Component.translatableEscape("mensagem.aelysiummod.discord.status_webhook_mortes", webhook(d.webhookMortes))).append("\n");
                            texto.append(Component.translatableEscape("mensagem.aelysiummod.discord.status_relatar_mortes",
                                    estado(d.relatarMortes, "mensagem.aelysiummod.discord.estado_ativo_caps", "mensagem.aelysiummod.discord.estado_inativo"))).append("\n");
                            texto.append(Component.translatableEscape("mensagem.aelysiummod.discord.status_overrides", d.overridesPorJogador.size())).append("\n");
                            texto.append(Component.translatableEscape("mensagem.aelysiummod.discord.status_comandos_permitidos", d.comandosPermitidos.size())).append("\n");
                            texto.append(Component.translatableEscape("mensagem.aelysiummod.discord.status_relatar_command_blocks",
                                    estado(d.relatarCommandBlocks, "mensagem.aelysiummod.comum.sim", "mensagem.aelysiummod.comum.nao"))).append("\n");
                            texto.append(Component.translatableEscape("mensagem.aelysiummod.discord.status_incluir_resultados",
                                    estado(d.incluirResultados, "mensagem.aelysiummod.comum.sim", "mensagem.aelysiummod.comum.nao"))).append("\n");
                            texto.append(Component.translatableEscape("mensagem.aelysiummod.discord.status_arquivo", ConfiguracaoDiscord.getConfigPath())).append("\n");
                            texto.append("§8§m                              ");
                            ctx.getSource().sendSuccess(() -> texto, false);
                            return 1;
                        })
                );
    }

    private static Component estado(boolean ligado, String chaveLigado, String chaveDesligado) {
        return Component.translatable(ligado ? chaveLigado : chaveDesligado);
    }

    private static Component webhook(String url) {
        return estado(!vazio(url), "mensagem.aelysiummod.discord.webhook_configurado", "mensagem.aelysiummod.discord.webhook_nao_definido");
    }

    private static boolean vazio(String s) {
        return s == null || s.isBlank();
    }
}