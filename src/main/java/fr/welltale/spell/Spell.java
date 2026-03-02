package fr.welltale.spell;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.NonNull;

public interface Spell {
    InteractionType getInteractionType();

    String getName();

    String getSlug();

    int getStaminaCost();

    float getCooldown();

    void run(
            @NonNull Player caster,
            @NonNull Ref<EntityStore> casterRef,
            @NonNull Store<EntityStore> casterStore,
            @NonNull Universe universe,
            @NonNull CommandBuffer<EntityStore> cmdBuffer
    );
}
