package fr.welltale.clazz.spell;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class SpellComponent implements Component<EntityStore> {
    public static ComponentType<EntityStore, SpellComponent> spellComponentType;

    @Override
    public Component<EntityStore> clone() {
        return new SpellComponent();
    }

    @Nonnull
    public static ComponentType<EntityStore, SpellComponent> getComponentType() {
        return spellComponentType;
    }
}
