package fr.welltale.clazz.spell.interaction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.clazz.spell.SpellComponent;
import fr.welltale.clazz.spell.SpellManager;
import fr.welltale.player.Player;
import fr.welltale.player.PlayerRepository;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;

public class CastSpellInteraction extends SimpleInteraction {
    private static SpellManager staticSpellManager;
    private static PlayerRepository staticPlayerRepository;

    public static final String ROOT_INTERACTION = "welltale:cast_spell";
    public static final BuilderCodec<CastSpellInteraction> CODEC =
            BuilderCodec.builder(CastSpellInteraction.class, CastSpellInteraction::new, SimpleInteraction.CODEC).build();

    public void initStatics(
            @Nonnull SpellManager spellManager,
            @NonNull PlayerRepository playerRepository
    ) {
        staticSpellManager = spellManager;
        staticPlayerRepository = playerRepository;
    }

    @Override
    protected void tick0(boolean firstRun, float time, @NonNull InteractionType type, @NonNull InteractionContext context, @NonNull CooldownHandler cooldownHandler) {
        if (type != InteractionType.Ability1 && type != InteractionType.Ability2 && type != InteractionType.Ability3) {
            return;
        }

        Ref<EntityStore> owningEntityRef = context.getOwningEntity();
        Store<EntityStore> store = owningEntityRef.getStore();
        PlayerRef playerRef = store.getComponent(owningEntityRef, PlayerRef.getComponentType());
        if (playerRef == null) return;

        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
        if (commandBuffer == null) return;

        SpellComponent casterSpellComponent = store.getComponent(owningEntityRef, SpellComponent.getComponentType());
        if (casterSpellComponent == null) {
            commandBuffer.addComponent(owningEntityRef, SpellComponent.getComponentType());
        }

        com.hypixel.hytale.server.core.entity.entities.Player player = store.getComponent(owningEntityRef, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
        if (player == null) return;

        Player playerData = staticPlayerRepository.getPlayerByUuid(playerRef.getUuid());
        if (playerData == null) return;
        staticSpellManager.cast(player, playerData, type, commandBuffer);
    }
}
