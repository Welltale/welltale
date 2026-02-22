package fr.welltale.inventory;

import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.item.ItemModule;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CustomInventoryService {
    private final ConcurrentHashMap<UUID, List<ItemStack>> pendingLootByPlayer = new ConcurrentHashMap<>();

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
