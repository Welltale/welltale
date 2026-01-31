package fr.welltale.clazz.spell.spells;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionChain;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.clazz.spell.Spell;
import org.jspecify.annotations.NonNull;

public class Supershot implements Spell {
    @Override
    public InteractionType getInteractionType() {
        return InteractionType.Ability2;
    }

    @Override
    public String getName() {
        return "Supershot";
    }

    @Override
    public String getSlug() {
        return "supershot";
    }

    @Override
    public int getBaseDamage() {
        return 1;
    }

    @Override
    public int getManaCost() {
        return 15;
    }

    @Override
    public float getCooldown() {
        return 10;
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
            caster.sendMessage(Message.raw("MANAGER IS NULL"));
            return;
        }

        RootInteraction rootInteraction = RootInteraction.getAssetMap().getAsset("Bow_Bomb_Boomshot");
        if (rootInteraction == null) {
            caster.sendMessage(Message.raw("Bow_Bomb_Boomshot IS NULL"));
            return;
        }

        InteractionContext interactionContext = InteractionContext.forInteraction(
                manager,
                casterRef,
                InteractionType.Ability2,
                casterStore
        );

        InteractionChain interactionChain = manager.initChain(
                InteractionType.Ability2,
                interactionContext,
                rootInteraction,
                true
        );

        manager.queueExecuteChain(interactionChain);
    }
}
