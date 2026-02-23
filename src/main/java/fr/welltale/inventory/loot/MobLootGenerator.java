package fr.welltale.inventory.loot;

import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.ui.ItemGridSlot;
import com.hypixel.hytale.server.core.modules.item.ItemModule;
import fr.welltale.mob.Mob;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class MobLootGenerator {
    private static final float MIN_DROP_MULTIPLIER = 0.0f;
    private static final float MAX_DROP_CHANCE = 1.0f;

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

        List<Mob.Drop> entries = mobConfig.getDrops();
        int[] cumulativeWeights = buildCumulativeWeights(entries);
        if (cumulativeWeights.length == 0) return List.of();

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int minRolls = Math.max(1, mobConfig.getLootRollsMin());
        int maxRolls = Math.max(minRolls, mobConfig.getLootRollsMax());
        int rolls = random.nextInt(minRolls, maxRolls + 1);

        List<ItemStack> out = new ArrayList<>(rolls);
        for (int i = 0; i < rolls; i++) {
            Mob.Drop picked = pickEntry(entries, cumulativeWeights, random);
            if (picked == null) continue;

            String itemId = normalizeItemId(picked.getItemId());
            if (itemId == null) continue;
            if (!isValidItemId(itemId)) continue;

            float dropChance = Math.min(MAX_DROP_CHANCE, Math.max(0f, picked.getDropChance() * dropChanceMultiplier));
            if (random.nextFloat() > dropChance) continue;

            int minQty = Math.max(1, picked.getMinQuantity());
            int maxQty = Math.max(minQty, picked.getMaxQuantity());
            int quantity = random.nextInt(minQty, maxQty + 1);
            out.add(new ItemStack(itemId, quantity));
        }
        return out;
    }

    private static String normalizeItemId(String rawItemId) {
        if (rawItemId == null) return null;

        String normalized = rawItemId.trim().toLowerCase().replace(' ', '_');
        if (normalized.isBlank()) return null;

        if (!normalized.contains(":")) {
            normalized = "hytale:" + normalized;
        }

        if (!isProbablyValidItemId(normalized)) {
            return null;
        }

        if (!ItemModule.exists(normalized)) {
            return null;
        }
        return normalized;
    }

    private static boolean isProbablyValidItemId(String itemId) {
        if (itemId == null) return false;
        int separator = itemId.indexOf(':');
        if (separator <= 0 || separator >= itemId.length() - 1) return false;

        for (int i = 0; i < itemId.length(); i++) {
            char c = itemId.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == ':' || c == '_' || c == '/' || c == '.' || c == '-') {
                continue;
            }
            return false;
        }
        return true;
    }

    private static boolean isValidItemId(String itemId) {
        try {
            new ItemGridSlot(new ItemStack(itemId, 1));
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static int[] buildCumulativeWeights(List<Mob.Drop> entries) {
        int[] cumulative = new int[entries.size()];
        int weightSum = 0;
        int count = 0;
        for (Mob.Drop entry : entries) {
            weightSum += Math.max(1, entry.getWeight());
            cumulative[count++] = weightSum;
        }

        if (weightSum <= 0) return new int[0];
        return cumulative;
    }

    private static Mob.Drop pickEntry(List<Mob.Drop> entries, int[] cumulativeWeights, ThreadLocalRandom random) {
        if (entries.isEmpty() || cumulativeWeights.length == 0) return null;

        int roll = random.nextInt(cumulativeWeights[cumulativeWeights.length - 1]);
        for (int i = 0; i < cumulativeWeights.length; i++) {
            if (roll < cumulativeWeights[i]) {
                return entries.get(i);
            }
        }

        return null;
    }
}
