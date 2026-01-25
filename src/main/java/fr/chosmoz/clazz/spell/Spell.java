package fr.chosmoz.clazz.spell;

import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.entities.Player;

import javax.annotation.Nonnull;

public interface Spell {
    InteractionType getInteractionType();
    String getName();
    String getSlug();
    int getBaseDamage();
    int getManaCost();
    float getCooldown();
    float getRange();
    void run(@Nonnull Player caster);
}
