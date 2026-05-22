package net.aelysium.aelysiummod.mixin.nick;

import net.aelysium.aelysiummod.client.ClientNicknameHandler;
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
public abstract class ClientSuggestionProviderMixin {

    @Inject(method = "getOnlinePlayerNames", at = @At("RETURN"), cancellable = true)
    private void aelysium$injectNicknames(CallbackInfoReturnable<Collection<String>> cir) {
        Collection<String> allNicknames = ClientNicknameHandler.getAllNicknames();
        if (allNicknames.isEmpty()) return;

        Set<String> realNamesToHide = new HashSet<>();
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null) {
            for (PlayerInfo info : connection.getOnlinePlayers()) {
                UUID uuid = info.getProfile().getId();
                if (ClientNicknameHandler.hasNickname(uuid)) {
                    realNamesToHide.add(info.getProfile().getName());
                }
            }
        }

        Collection<String> original = cir.getReturnValue();
        Set<String> combined = new LinkedHashSet<>();

        combined.addAll(allNicknames);

        for (String name : original) {
            if (!realNamesToHide.contains(name)) {
                combined.add(name);
            }
        }

        cir.setReturnValue(combined);
    }
}