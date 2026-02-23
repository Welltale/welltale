package fr.welltale.mob.loot;

import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.item.ItemModule;
import fr.welltale.mob.Mob;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class MobLootGenerator {
    private static final float MIN_DROP_MULTIPLIER = 0.0f;
    private static final float MIN_DROP_CHANCE_PERCENT = 0.0f;
    private static final float MAX_DROP_CHANCE_PERCENT = 100.0f;
    private static final ConcurrentHashMap<String, Boolean> ITEM_ID_VALIDATION_CACHE = new ConcurrentHashMap<>();

    private MobLootGenerator() {}

    public static List<ItemStack> rollLoot(Mob mobConfig) {
        return rollLoot(mobConfig, 1.0f);
    }

    public static List<ItemStack> rollLoot(Mob mobConfig, float dropChanceMultiplier) {
        if (mobConfig == null || mobConfig.getDrops() == null || mobConfig.getDrops().isEmpty()) {
            return List.of();
        }

        return rollFromMobConfig(mobConfig, Math.max(MIN_DROP_MULTIPLIER, dropChanceMultiplier));
    }

    private static List<ItemStack> rollFromMobConfig(Mob mobConfig, float dropChanceMultiplier) {
        if (mobConfig.getDrops() == null || mobConfig.getDrops().isEmpty()) return List.of();

        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<Mob.Drop> entries = mobConfig.getDrops();
        List<ItemStack> out = new ArrayList<>(entries.size());
        for (Mob.Drop entry : entries) {
            if (entry == null) continue;

            String itemId = normalizeItemId(entry.getItemId());
            if (itemId == null) continue;

            float dropChancePercent = Math.min(
                    MAX_DROP_CHANCE_PERCENT,
                    Math.max(MIN_DROP_CHANCE_PERCENT, entry.getDropChance() * dropChanceMultiplier)
            );
            if (random.nextFloat() * 100.0f >= dropChancePercent) continue;

            int minQty = Math.max(1, entry.getMinQuantity());
            int maxQty = Math.max(minQty, entry.getMaxQuantity());
            int quantity = random.nextInt(minQty, maxQty + 1);
            out.add(new ItemStack(itemId, quantity));
        }

        return out;
    }

    private static String normalizeItemId(String rawItemId) {
        if (rawItemId == null) return null;

        String trimmed = rawItemId.trim();
        if (trimmed.isBlank()) return null;

        if (!ITEM_ID_VALIDATION_CACHE.computeIfAbsent(trimmed, ItemModule::exists)) return null;
        return trimmed;
    }
}
