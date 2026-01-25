package fr.chosmoz.clazz.spell;

import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.logger.HytaleLogger;
import fr.chosmoz.clazz.Class;
import fr.chosmoz.clazz.ClassRepository;
import fr.chosmoz.clazz.spell.spells.Jump;
import fr.chosmoz.player.PlayerRepository;
import fr.chosmoz.util.Color;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class SpellManager {
    private static final List<Spell> spellRegistry = new ArrayList<>();
    private final Map<String, Float> playerCooldowns = new HashMap<>();  // playerUUID_spellName → remaining
    private final HytaleLogger logger;
    private final PlayerRepository playerRepository;
    private final ClassRepository classRepository;

    public SpellManager(HytaleLogger logger, PlayerRepository playerRepository, ClassRepository classRepository) {
        this.logger = logger;
        this.playerRepository = playerRepository;
        this.classRepository = classRepository;
        registerSpells();
    }

     void registerSpells() {
        spellRegistry.add(new Jump(logger));
        this.logger.atInfo().log("Spells registered: " + spellRegistry.size());
    }

    public @Nullable Spell getSpell(Class clazz, InteractionType interactionType) {
        for (Spell spell : spellRegistry) {
            if (!clazz.getSpellSlugs().contains(spell.getSlug())) continue;

            if (!spell.getInteractionType().equals(interactionType)) continue;

            return spell;
        }

        return null;
    }

    public boolean canCast(@Nonnull fr.chosmoz.player.Player player, @Nonnull String spellSlug) {
        Optional<Spell> spell = spellRegistry.stream().filter(s -> s.getSlug().equals(spellSlug)).findFirst();
        if (spell.isEmpty()) return false;

        Class playerClass = this.classRepository.getClass(player.getClassUuid());
        if (playerClass == null) return false;

        if (player.getCharacteristic().getMana() < spell.get().getManaCost()) return false;

        if (!playerClass.getSpellSlugs().contains(spell.get().getSlug())) return false;

        String key = player.getUuid().toString() + "_" + spellSlug;
        return playerCooldowns.getOrDefault(key, 0f) <= 0;
    }

    public void cast(@Nonnull Player caster, @Nonnull String spellSlug) {
        fr.chosmoz.player.Player casterData = this.playerRepository.getPlayerByUsername(caster.getDisplayName());
        if (casterData == null) return;

        Optional<Spell> spell = spellRegistry.stream().filter(s -> s.getSlug().equals(spellSlug)).findFirst();
        if (spell.isEmpty()) return;

        if (!canCast(casterData, spellSlug)) {
            caster.sendMessage(Message.raw("Cannot cast: " + spell.get().getName())
                    .color(Color.DARK_RED)
            );
            return;
        }

        spell.get().run(caster);

        playerCooldowns.put(casterData.getUuid().toString() + "_" + spellSlug, spell.get().getCooldown());
        casterData.getCharacteristic().setMana(casterData.getCharacteristic().getMana() - spell.get().getManaCost());
        startCooldown(casterData.getUuid(), spellSlug);

        caster.sendMessage(Message.raw("Cast spell: " + spell.get().getName())
                .color(Color.DARK_GREEN)
        );
    }

    private void startCooldown(@Nonnull UUID playerId, @Nonnull String spellName) { /* Scheduler */ }
}

