package net.aelysium.aelysiummod.sistemas.social.apelido.mixin;

import net.aelysium.aelysiummod.sistemas.social.apelido.cliente.ManipuladorApelidoCliente;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(ClientSuggestionProvider.class)
public abstract class ProvedorSugestoesClienteMixin {

    @Inject(method = "getOnlinePlayerNames", at = @At("RETURN"), cancellable = true)
    private void aelysium$injectNicknames(CallbackInfoReturnable<Collection<String>> cir) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) return;

        Set<String> realNamesToHide = new HashSet<>();
        Set<String> onlineNicknames = new LinkedHashSet<>();
        for (PlayerInfo info : connection.getOnlinePlayers()) {
            UUID uuid = info.getProfile().getId();
            if (ManipuladorApelidoCliente.hasNickname(uuid)) {
                realNamesToHide.add(info.getProfile().getName());
                String nick = ManipuladorApelidoCliente.getRawNick(uuid);
                if (nick != null && !nick.isEmpty()) onlineNicknames.add(nick);
            }
        }

        if (onlineNicknames.isEmpty() && realNamesToHide.isEmpty()) return;

        Collection<String> original = cir.getReturnValue();
        Set<String> combined = new LinkedHashSet<>();

        combined.addAll(onlineNicknames);

        for (String name : original) {
            if (!realNamesToHide.contains(name)) {
                combined.add(name);
            }
        }

        cir.setReturnValue(combined);
    }
}