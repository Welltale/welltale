package fr.welltale.inventory;

import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.item.ItemModule;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CustomInventoryService {
    public static final int EQUIPMENT_SLOT_COUNT = 17;

    private final ConcurrentHashMap<UUID, List<ItemStack>> pendingLootByPlayer = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, List<ItemStack>> equipmentByPlayer = new ConcurrentHashMap<>();

    public void addLoot(UUID playerUuid, List<ItemStack> loot) {
        if (playerUuid == null || loot == null || loot.isEmpty()) return;

        List<ItemStack> sanitizedLoot = sanitizeLoot(loot);
        if (sanitizedLoot.isEmpty()) return;

        pendingLootByPlayer.compute(playerUuid, (_, existing) -> {
            List<ItemStack> next = existing == null ? new ArrayList<>() : new ArrayList<>(existing);
            next.addAll(sanitizedLoot);
            return next;
        });
    }

    public List<ItemStack> getLootSnapshot(UUID playerUuid) {
        if (playerUuid == null) return List.of();

        List<ItemStack> loot = pendingLootByPlayer.get(playerUuid);
        return loot == null ? List.of() : sanitizeLoot(loot);
    }

    public void replaceLoot(UUID playerUuid, List<ItemStack> loot) {
        if (playerUuid == null) return;

        List<ItemStack> sanitizedLoot = sanitizeLoot(loot);
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

    private List<ItemStack> sanitizeLoot(List<ItemStack> loot) {
        if (loot == null || loot.isEmpty()) {
            return List.of();
        }

        List<ItemStack> sanitized = new ArrayList<>(loot.size());
        for (ItemStack stack : loot) {
            if (ItemStack.isEmpty(stack)) continue;

            String itemId = normalizeItemId(stack.getItemId());
            if (itemId == null) continue;

            sanitized.add(new ItemStack(itemId, Math.max(1, stack.getQuantity())));
        }

        return sanitized;
    }

    private String normalizeItemId(String rawItemId) {
        if (rawItemId == null) return null;

        String normalized = rawItemId.trim().toLowerCase().replace(' ', '_');
        if (normalized.isBlank()) return null;
        if (!normalized.contains(":")) {
            normalized = "hytale:" + normalized;
        }

        int separator = normalized.indexOf(':');
        if (separator <= 0 || separator >= normalized.length() - 1) {
            return null;
        }

        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);
            boolean allowed = (c >= 'a' && c <= 'z')
                    || (c >= '0' && c <= '9')
                    || c == ':'
                    || c == '_'
                    || c == '/'
                    || c == '.'
                    || c == '-';
            if (!allowed) {
                return null;
            }
        }

        if (!ItemModule.exists(normalized)) {
            return null;
        }

        return normalized;
    }
}
