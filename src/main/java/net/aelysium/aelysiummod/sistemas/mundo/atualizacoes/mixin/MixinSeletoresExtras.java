package net.aelysium.aelysiummod.sistemas.mundo.atualizacoes.mixin;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Mixin(EntitySelectorParser.class)
public abstract class MixinSeletoresExtras {

    @Shadow @Final private StringReader reader;
    @Shadow private int maxResults;
    @Shadow private boolean includesEntities;
    @Shadow private boolean usesSelectors;
    @Shadow @Final private List<Predicate<Entity>> predicates;
    @Shadow private BiConsumer<net.minecraft.world.phys.Vec3, List<? extends Entity>> order;
    @Shadow private BiFunction<SuggestionsBuilder, Consumer<SuggestionsBuilder>, CompletableFuture<Suggestions>> suggestions;

    @Shadow public abstract void limitToType(EntityType<?> type);
    @Shadow protected abstract void parseOptions() throws CommandSyntaxException;
    @Shadow private CompletableFuture<Suggestions> suggestOpenOptions(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> consumer) { return null; }
    @Shadow private CompletableFuture<Suggestions> suggestOptionsKeyOrClose(SuggestionsBuilder builder, Consumer<SuggestionsBuilder> consumer) { return null; }

    @Inject(method = "parseSelector", at = @At("HEAD"), cancellable = true)
    private void aelysium$seletoresExtras(CallbackInfo ci) throws CommandSyntaxException {
        if (!this.reader.canRead()) return;
        char tipo = this.reader.peek();
        if (tipo != 'i' && tipo != '!') return;
        this.usesSelectors = true;
        this.reader.skip();
        this.maxResults = Integer.MAX_VALUE;
        this.includesEntities = true;
        this.order = EntitySelector.ORDER_ARBITRARY;
        if (tipo == 'i') {
            this.limitToType(EntityType.ITEM);
        } else {
            this.predicates.add(entidade -> !(entidade instanceof Player));
        }
        this.predicates.add(Entity::isAlive);
        this.suggestions = (construtor, consumidor) -> this.suggestOpenOptions(construtor, consumidor);
        if (this.reader.canRead() && this.reader.peek() == '[') {
            this.reader.skip();
            this.suggestions = (construtor, consumidor) -> this.suggestOptionsKeyOrClose(construtor, consumidor);
            this.parseOptions();
        }
        ci.cancel();
    }

    @Inject(method = "fillSelectorSuggestions", at = @At("TAIL"))
    private static void aelysium$sugerirExtras(SuggestionsBuilder construtor, CallbackInfo ci) {
        construtor.suggest("@i", Component.literal("Todos os itens no chão"));
        construtor.suggest("@!", Component.literal("Todas as entidades que não são jogadores"));
    }
}
