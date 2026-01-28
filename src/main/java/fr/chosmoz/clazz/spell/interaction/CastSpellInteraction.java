package fr.chosmoz.clazz.spell.interaction;

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
import fr.chosmoz.clazz.spell.SpellManager;
import fr.chosmoz.player.Player;
import fr.chosmoz.player.PlayerRepository;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;

public class CastSpellInteraction extends SimpleInteraction {
    private static SpellManager staticSpellManager;
    private static PlayerRepository staticPlayerRepository;

    public static final String ROOT_INTERACTION = "chosmoz:cast_spell_ability";
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

        com.hypixel.hytale.server.core.entity.entities.Player player = store.getComponent(owningEntityRef, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
        if (player == null) return;

        Player playerData = staticPlayerRepository.getPlayerByUuid(playerRef.getUuid());
        if (playerData == null) return;

        staticSpellManager.cast(player, playerData, type, commandBuffer);
    }
}
