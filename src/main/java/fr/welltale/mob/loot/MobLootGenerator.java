package fr.welltale.mob.loot;

import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import fr.welltale.item.ItemStatRoller;
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

    public static ArrayList<ItemStack> rollLoot(Mob mobConfig, float dropChanceMultiplier) {
        if (mobConfig == null || mobConfig.getDrops() == null || mobConfig.getDrops().isEmpty()) {
            return new ArrayList<>();
        }

        return rollFromMobConfig(mobConfig, Math.max(MIN_DROP_MULTIPLIER, dropChanceMultiplier));
    }

    private static ArrayList<ItemStack> rollFromMobConfig(Mob mobConfig, float dropChanceMultiplier) {
        if (mobConfig.getDrops() == null || mobConfig.getDrops().isEmpty()) return new ArrayList<>();

        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<Mob.Drop> entries = mobConfig.getDrops();
        ArrayList<ItemStack> out = new ArrayList<>(entries.size());
        for (Mob.Drop entry : entries) {
            if (entry == null) continue;

            String itemId = normalizeItemId(entry.itemId());
            if (itemId == null) continue;

            float dropChancePercent = Math.min(
                    MAX_DROP_CHANCE_PERCENT,
                    Math.max(MIN_DROP_CHANCE_PERCENT, entry.dropChance() * dropChanceMultiplier)
            );
            if (random.nextFloat() * 100.0f >= dropChancePercent) continue;

            int minQty = Math.max(1, entry.minQuantity());
            int maxQty = Math.max(minQty, entry.maxQuantity());
            int quantity = random.nextInt(minQty, maxQty + 1);
            ItemStack rolledStack = ItemStatRoller.rollStats(new ItemStack(itemId, quantity));
            out.add(rolledStack);
        }

        return out;
    }

    private static String normalizeItemId(String rawItemId) {
        if (rawItemId == null) return null;

        String trimmed = rawItemId.trim();
        if (trimmed.isBlank()) return null;

        if (!ITEM_ID_VALIDATION_CACHE.computeIfAbsent(trimmed, id -> Item.getAssetMap().getAsset(id) != null)) return null;
        return trimmed;
    }
}
