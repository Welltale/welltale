package fr.welltale.spell;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.player.Player;
import fr.welltale.player.PlayerRepository;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;

public class CastSpellInteraction extends SimpleInteraction {
    private static SpellManager staticSpellManager;
    private static PlayerRepository staticPlayerRepository;
    private static HytaleLogger staticLogger;

    public static final String ROOT_INTERACTION = "welltale:cast_spell";
    public static final BuilderCodec<CastSpellInteraction> CODEC =
            BuilderCodec.builder(CastSpellInteraction.class, CastSpellInteraction::new, SimpleInteraction.CODEC).build();

    public void initStatics(
            @Nonnull SpellManager spellManager,
            @NonNull PlayerRepository playerRepository,
            @Nonnull HytaleLogger logger
    ) {
        staticSpellManager = spellManager;
        staticPlayerRepository = playerRepository;
        staticLogger = logger;
    }

    @Override
    protected void tick0(boolean firstRun, float time, @NonNull InteractionType type, @NonNull InteractionContext context, @NonNull CooldownHandler cooldownHandler) {
        if (type != InteractionType.Ability1 && type != InteractionType.Ability2 && type != InteractionType.Ability3) {
            return;
        }

        Ref<EntityStore> owningEntityRef = context.getOwningEntity();
        if (!owningEntityRef.isValid()) {
            staticLogger.atSevere()
                    .log("[SPELL] CastSpellInteraction Tick0 Failed: OwningEntityRef is null");
            return;
        }

        Store<EntityStore> store = owningEntityRef.getStore();
        PlayerRef playerRef = store.getComponent(owningEntityRef, PlayerRef.getComponentType());
        if (playerRef == null) {
            staticLogger.atSevere()
                    .log("[SPELL] CastSpellInteraction Tick0 Failed: PlayerRef is null");
            return;
        }

        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
        if (commandBuffer == null) {
            staticLogger.atSevere()
                    .log("[SPELL] CastSpellInteraction Tick0 Failed: CommandBuffer is null");
            return;
        }

        com.hypixel.hytale.server.core.entity.entities.Player player = store.getComponent(owningEntityRef, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
        if (player == null) {
            staticLogger.atSevere()
                    .log("[SPELL] CastSpellInteraction Tick0 Failed: Player is null");
            return;
        }

        Player playerData = staticPlayerRepository.getPlayerByUuid(playerRef.getUuid());
        if (playerData == null) {
            staticLogger.atSevere()
                    .log("[SPELL] CastSpellInteraction Tick0 Failed: PlayerData is null");
            return;
        }

        staticSpellManager.cast(player, playerData, type, commandBuffer);
    }
}
