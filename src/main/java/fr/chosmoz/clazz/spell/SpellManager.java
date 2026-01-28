package fr.chosmoz.clazz.spell;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.chosmoz.clazz.Class;
import fr.chosmoz.clazz.ClassRepository;
import fr.chosmoz.clazz.spell.spells.Jump;
import fr.chosmoz.util.Color;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static fr.chosmoz.clazz.spell.SpellCooldownScheduler.COOLDOWN_DELAY;

public class SpellManager {
    private static final List<Spell> spellRegistry = new ArrayList<>();
    private final Map<String, Float> playerCooldowns = new ConcurrentHashMap<>(); // playerUUID_spellName → remaining

    private final HytaleLogger logger;
    private final Universe universe;
    private final ClassRepository classRepository;
    private final SpellScheduler spellScheduler;

    public SpellManager(
            @Nonnull HytaleLogger logger,
            @Nonnull Universe universe,
            @Nonnull ClassRepository classRepository, SpellScheduler spellScheduler
    ) {
        this.logger = logger;
        this.universe = universe;
        this.classRepository = classRepository;
        this.spellScheduler = spellScheduler;
        registerSpells();
    }

    void registerSpells() {
        spellRegistry.add(new Jump(spellScheduler, logger));
        this.logger.atInfo().log("Spells registered: " + spellRegistry.size());
    }

    public void cast(
            @Nonnull Player caster,
            @Nonnull fr.chosmoz.player.Player casterData,
            @Nonnull InteractionType type,
            @Nonnull CommandBuffer<EntityStore> cmdBuffer
    ) {
        Class casterClass = this.classRepository.getClass(casterData.getClassUuid());
        if (casterClass == null) return;

        Spell spell = this.getSpell(casterClass, type);
        if (spell == null) {
            caster.sendMessage(Message.raw("No spell Binding with this ability").color(Color.DARK_RED));
            return;
        }

        Ref<EntityStore> ref = caster.getReference();
        if (ref == null) return;

        Store<EntityStore> store = ref.getStore();
        EntityStatMap casterStatMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (casterStatMap == null) return;

        if (!canCast(caster, casterData, casterClass, spell, casterStatMap)) return;

        this.consumeMana(casterStatMap, spell.getManaCost());
        spell.run(caster, ref, store, this.universe, cmdBuffer);
        playerCooldowns.put(casterData.getUuid().toString() + "_" + spell.getSlug(), spell.getCooldown());
        this.startCooldown(casterData.getUuid(), spell.getSlug(), spell.getCooldown());

        SpellComponent casterSpellComponent = store.getComponent(ref, SpellComponent.getComponentType());
        if (casterSpellComponent == null) {
            cmdBuffer.addComponent(ref, SpellComponent.getComponentType());
        }

        caster.sendMessage(Message.raw("Cast spell: " + spell.getName()).color(Color.DARK_GREEN));
    }

    private boolean canCast(
            //TODO REMOVE @Nonnull Player caster
            @Nonnull Player caster,
            @Nonnull fr.chosmoz.player.Player casterData,
            @Nonnull Class casterClass,
            @Nonnull Spell spell,
            @Nonnull EntityStatMap casterStatMap
    ) {
        if (!casterClass.getSpellSlugs().contains(spell.getSlug())) return false;

        EntityStatValue casterCurrentMana = casterStatMap.get(DefaultEntityStatTypes.getMana());
        if (casterCurrentMana == null) return false;

        if (casterCurrentMana.get() < spell.getManaCost()) {
            caster.sendMessage(Message.raw("Cannot cast spell: " + spell.getName() + " no enough mana").color(Color.DARK_RED));
            return false;
        }

        String key = casterData.getUuid().toString() + "_" + spell.getSlug();
        if (playerCooldowns.containsKey(key)) {
            Float cooldownDelay = playerCooldowns.get(key);
            caster.sendMessage(Message.raw("Cannot cast spell: " + spell.getName() + " spell cooldown (" + cooldownDelay + "s)").color(Color.DARK_RED));
            return false;
        }

        return true;
    }

    private void consumeMana(@Nonnull EntityStatMap casterStatMap, float manaCost) {
        EntityStatValue casterCurrentMana = casterStatMap.get(DefaultEntityStatTypes.getMana());
        if (casterCurrentMana == null) return;

        casterStatMap.setStatValue(DefaultEntityStatTypes.getMana(), casterCurrentMana.get() - manaCost);
    }

    private void startCooldown(@Nonnull UUID casterId, @Nonnull String spellSlug, float spellCooldown) {
        String key = casterId + "_" + spellSlug;
        playerCooldowns.put(key, spellCooldown);
    }

    public void tickCooldown() {
        Iterator<Map.Entry<String, Float>> it = playerCooldowns.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Float> entry = it.next();
            float remaining = entry.getValue() - COOLDOWN_DELAY;
            if (remaining <= 0f) {
                it.remove();
            } else {
                entry.setValue(remaining);
            }
        }
    }

    private @Nullable Spell getSpell(@Nonnull Class clazz, @Nonnull InteractionType interactionType) {
        for (Spell spell : spellRegistry) {
            if (!clazz.getSpellSlugs().contains(spell.getSlug())) continue;

            if (!spell.getInteractionType().equals(interactionType)) continue;

            return spell;
        }

        return null;
    }
}

