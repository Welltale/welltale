package fr.welltale.clazz.spell;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public interface Spell {
    InteractionType getInteractionType();

    String getName();

    String getSlug();

    int getBaseDamage();

    int getManaCost();

    float getCooldown();

    void run(
            @Nonnull Player caster,
            @Nonnull Ref<EntityStore> casterRef,
            @Nonnull Store<EntityStore> casterStore,
            @Nonnull Universe universe,
            @Nonnull CommandBuffer<EntityStore> cmdBuffer
    );
}
