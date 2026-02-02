package fr.welltale.spell.spells;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.server.core.entity.*;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.spell.Spell;
import org.jspecify.annotations.NonNull;

public class Jump implements Spell {
    @Override
    public InteractionType getInteractionType() {
        return InteractionType.Ability1;
    }

    @Override
    public String getName() {
        return "Bond";
    }

    @Override
    public String getSlug() {
        return "jump";
    }

    @Override
    public int getBaseDamage() {
        return 5;
    }

    @Override
    public int getManaCost() {
        return 10;
    }

    @Override
    public float getCooldown() {
        return 5;
    }

    @Override
    public void run(
            @NonNull Player caster,
            @NonNull Ref<EntityStore> casterRef,
            @NonNull Store<EntityStore> casterStore,
            @NonNull Universe universe,
            @NonNull CommandBuffer<EntityStore> cmdBuffer
    ) {
        InteractionManager manager = casterStore.getComponent(casterRef, InteractionModule.get().getInteractionManagerComponent());
        if (manager == null) {
            return;
        }

        com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction rootInteraction = RootInteraction.getAssetMap().getAsset("Mace_Swing_Down_Charged");
        if (rootInteraction == null) {
            return;
        }

        InteractionContext interactionContext = InteractionContext.forInteraction(
                manager,
                casterRef,
                this.getInteractionType(),
                casterStore
        );

        InteractionChain interactionChain = manager.initChain(
                this.getInteractionType(),
                interactionContext,
                rootInteraction,
                true
        );

        manager.queueExecuteChain(interactionChain);
    }
}
