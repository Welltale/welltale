package fr.welltale.inventory;

import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.item.ItemModule;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CustomInventoryService {
    public static final int EQUIPMENT_SLOT_COUNT = 17;
    public static final int LOOT_SLOT_CAPACITY = 30;

    public record AddLootResult(int addedStacks, int skippedStacks) {
        public boolean isFull() {
            return skippedStacks > 0;
        }
    }

    private final ConcurrentHashMap<UUID, List<ItemStack>> pendingLootByPlayer = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, List<ItemStack>> equipmentByPlayer = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> itemIdValidationCache = new ConcurrentHashMap<>();

    public AddLootResult addLoot(UUID playerUuid, List<ItemStack> loot) {
        if (playerUuid == null || loot == null || loot.isEmpty()) {
            return new AddLootResult(0, 0);
        }

        int[] counts = new int[2];
        pendingLootByPlayer.compute(playerUuid, (_, existing) -> {
            List<ItemStack> next = sanitizeAndTrimLoot(existing);
            if (next.isEmpty()) {
                next = new ArrayList<>(Math.min(LOOT_SLOT_CAPACITY, loot.size()));
            }

            int remainingSlots = Math.max(0, LOOT_SLOT_CAPACITY - next.size());
            for (ItemStack stack : loot) {
                if (ItemStack.isEmpty(stack)) continue;

                String itemId = normalizeItemId(stack.getItemId());
                if (itemId == null) continue;

                if (remainingSlots <= 0) {
                    counts[1]++;
                    continue;
                }

                next.add(new ItemStack(itemId, Math.max(1, stack.getQuantity())));
                counts[0]++;
                remainingSlots--;
            }

            return next.isEmpty() ? null : next;
        });

        return new AddLootResult(counts[0], counts[1]);
    }

    public List<ItemStack> getLootSnapshot(UUID playerUuid) {
        if (playerUuid == null) return List.of();

        List<ItemStack> loot = pendingLootByPlayer.get(playerUuid);
        if (loot == null) return List.of();
        if (loot.isEmpty()) return List.of();

        return new ArrayList<>(loot);
    }

    public void replaceLoot(UUID playerUuid, List<ItemStack> loot) {
        if (playerUuid == null) return;

        List<ItemStack> sanitizedLoot = sanitizeAndTrimLoot(loot);
        if (sanitizedLoot.isEmpty()) {
            pendingLootByPlayer.remove(playerUuid);
            return;
        }

        pendingLootByPlayer.put(playerUuid, sanitizedLoot);
    }

    public ItemStack getEquipmentSlot(UUID playerUuid, int slotIndex) {
        if (playerUuid == null || slotIndex < 0 || slotIndex >= EQUIPMENT_SLOT_COUNT) {
            return null;
        }

        List<ItemStack> equipment = equipmentByPlayer.get(playerUuid);
        if (equipment == null || slotIndex >= equipment.size()) {
            return null;
        }

        ItemStack stack = equipment.get(slotIndex);
        return ItemStack.isEmpty(stack) ? null : new ItemStack(stack.getItemId(), Math.max(1, stack.getQuantity()));
    }

    public void setEquipmentSlot(UUID playerUuid, int slotIndex, ItemStack stack) {
        if (playerUuid == null || slotIndex < 0 || slotIndex >= EQUIPMENT_SLOT_COUNT) {
            return;
        }

        equipmentByPlayer.compute(playerUuid, (_, existing) -> {
            List<ItemStack> next = existing == null ? new ArrayList<>(EQUIPMENT_SLOT_COUNT) : new ArrayList<>(existing);
            while (next.size() < EQUIPMENT_SLOT_COUNT) {
                next.add(null);
            }

            if (ItemStack.isEmpty(stack)) {
                next.set(slotIndex, null);
            } else {
                String itemId = sanitizeEquipmentItemId(stack.getItemId());
                if (itemId == null || itemId.isBlank()) {
                    next.set(slotIndex, null);
                } else {
                    next.set(slotIndex, new ItemStack(itemId, Math.max(1, stack.getQuantity())));
                }
            }

            boolean hasAny = false;
            for (ItemStack item : next) {
                if (!ItemStack.isEmpty(item)) {
                    hasAny = true;
                    break;
                }
            }

            return hasAny ? next : null;
        });
    }

    private String sanitizeEquipmentItemId(String rawItemId) {
        if (rawItemId == null) return null;
        String trimmed = rawItemId.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private List<ItemStack> sanitizeAndTrimLoot(List<ItemStack> loot) {
        if (loot == null || loot.isEmpty()) return List.of();

        List<ItemStack> sanitized = new ArrayList<>(Math.min(loot.size(), LOOT_SLOT_CAPACITY));
        for (ItemStack stack : loot) {
            if (sanitized.size() >= LOOT_SLOT_CAPACITY) break;

            if (ItemStack.isEmpty(stack)) continue;

            String itemId = normalizeItemId(stack.getItemId());
            if (itemId == null) continue;

            sanitized.add(new ItemStack(itemId, Math.max(1, stack.getQuantity())));
        }

        return sanitized;
    }

    private String normalizeItemId(String rawItemId) {
        if (rawItemId == null) return null;

        String trimmed = rawItemId.trim();
        if (trimmed.isBlank()) return null;
        if (!itemIdValidationCache.computeIfAbsent(trimmed, ItemModule::exists)) return null;

        return trimmed;
    }
}
