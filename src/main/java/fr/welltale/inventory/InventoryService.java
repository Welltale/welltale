package fr.welltale.inventory;

import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import fr.welltale.item.virtual.RolledVirtualItemRegistry;
import lombok.NonNull;
import org.bson.BsonDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryService {
    public static final int EQUIPMENT_SLOT_COUNT = 17;
    public static final int LOOT_SLOT_CAPACITY = 30;

    public record AddLootResult(int addedStacks, int skippedStacks) {
        public boolean isFull() {
            return skippedStacks > 0;
        }
    }

    private final ConcurrentHashMap<InventoryOwnerKey, ArrayList<ItemStack>> pendingLootByCharacter = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<InventoryOwnerKey, ArrayList<ItemStack>> equipmentByCharacter = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> itemIdValidationCache = new ConcurrentHashMap<>();

    public record InventoryOwnerKey(UUID playerUuid, UUID characterUuid) {
        public InventoryOwnerKey {
            Objects.requireNonNull(playerUuid, "playerUuid");
            Objects.requireNonNull(characterUuid, "characterUuid");
        }
    }

    private InventoryOwnerKey ownerKey(@NonNull UUID playerUuid, @NonNull UUID characterUuid) {
        return new InventoryOwnerKey(playerUuid, characterUuid);
    }

    public void ensureCharacterInventory(UUID playerUuid, UUID characterUuid, List<StoredItemStack> loot, List<StoredItemStack> equipment) {
        if (playerUuid == null || characterUuid == null) return;

        InventoryOwnerKey ownerKey = ownerKey(playerUuid, characterUuid);
        pendingLootByCharacter.computeIfAbsent(ownerKey, _ -> sanitizeAndTrimLoot(StoredItemStack.toItemStackList(loot, LOOT_SLOT_CAPACITY)));
        equipmentByCharacter.computeIfAbsent(ownerKey, _ -> sanitizeAndTrimEquipment(StoredItemStack.toItemStackList(equipment, EQUIPMENT_SLOT_COUNT)));
    }

    public AddLootResult addLoot(UUID playerUuid, UUID characterUuid, List<ItemStack> loot) {
        if (playerUuid == null || characterUuid == null || loot == null || loot.isEmpty()) {
            return new AddLootResult(0, 0);
        }

        int[] counts = new int[2];
        pendingLootByCharacter.compute(ownerKey(playerUuid, characterUuid), (_, existing) -> {
            ArrayList<ItemStack> next = sanitizeAndTrimLoot(existing);
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

                next.add(cloneStack(itemId, stack));
                counts[0]++;
                remainingSlots--;
            }

            return next.isEmpty() ? null : next;
        });

        return new AddLootResult(counts[0], counts[1]);
    }

    public ArrayList<ItemStack> getLootSnapshot(UUID playerUuid, UUID characterUuid) {
        if (playerUuid == null || characterUuid == null) return new ArrayList<>();

        ArrayList<ItemStack> loot = pendingLootByCharacter.get(ownerKey(playerUuid, characterUuid));
        if (loot == null) return new ArrayList<>();
        if (loot.isEmpty()) return new ArrayList<>();

        return new ArrayList<>(loot);
    }

    public void replaceLoot(UUID playerUuid, UUID characterUuid, List<ItemStack> loot) {
        if (playerUuid == null || characterUuid == null) return;

        ArrayList<ItemStack> sanitizedLoot = sanitizeAndTrimLoot(loot);
        pendingLootByCharacter.put(ownerKey(playerUuid, characterUuid), sanitizedLoot);
    }

    public ItemStack getEquipmentSlot(UUID playerUuid, UUID characterUuid, int slotIndex) {
        if (playerUuid == null || characterUuid == null || slotIndex < 0 || slotIndex >= EQUIPMENT_SLOT_COUNT) {
            return null;
        }

        ArrayList<ItemStack> equipment = equipmentByCharacter.get(ownerKey(playerUuid, characterUuid));
        if (equipment == null || slotIndex >= equipment.size()) {
            return null;
        }

        ItemStack stack = equipment.get(slotIndex);
        return ItemStack.isEmpty(stack) ? null : cloneStack(stack.getItemId(), stack);
    }

    public void setEquipmentSlot(UUID playerUuid, UUID characterUuid, int slotIndex, ItemStack stack) {
        if (playerUuid == null || characterUuid == null || slotIndex < 0 || slotIndex >= EQUIPMENT_SLOT_COUNT) {
            return;
        }

        equipmentByCharacter.compute(ownerKey(playerUuid, characterUuid), (_, existing) -> {
            ArrayList<ItemStack> next = existing == null ? new ArrayList<>(EQUIPMENT_SLOT_COUNT) : new ArrayList<>(existing);
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
                    next.set(slotIndex, cloneStack(itemId, stack));
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

    public ArrayList<ItemStack> getEquipmentSnapshot(UUID playerUuid, UUID characterUuid) {
        if (playerUuid == null || characterUuid == null) return new ArrayList<>();

        ArrayList<ItemStack> equipment = equipmentByCharacter.get(ownerKey(playerUuid, characterUuid));
        if (equipment == null || equipment.isEmpty()) return new ArrayList<>();

        ArrayList<ItemStack> snapshot = new ArrayList<>(equipment.size());
        for (ItemStack stack : equipment) {
            snapshot.add(ItemStack.isEmpty(stack) ? null : cloneStack(stack.getItemId(), stack));
        }
        return snapshot;
    }

    private String sanitizeEquipmentItemId(String rawItemId) {
        if (rawItemId == null) return null;
        String trimmed = rawItemId.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private ArrayList<ItemStack> sanitizeAndTrimLoot(List<ItemStack> loot) {
        if (loot == null || loot.isEmpty()) return new ArrayList<>();

        ArrayList<ItemStack> sanitized = new ArrayList<>(Math.min(loot.size(), LOOT_SLOT_CAPACITY));
        for (ItemStack stack : loot) {
            if (sanitized.size() >= LOOT_SLOT_CAPACITY) break;

            if (ItemStack.isEmpty(stack)) continue;

            String itemId = normalizeItemId(stack.getItemId());
            if (itemId == null) continue;

            sanitized.add(cloneStack(itemId, stack));
        }

        return sanitized;
    }

    private ArrayList<ItemStack> sanitizeAndTrimEquipment(List<ItemStack> equipment) {
        if (equipment == null || equipment.isEmpty()) return new ArrayList<>();

        ArrayList<ItemStack> sanitized = new ArrayList<>(EQUIPMENT_SLOT_COUNT);
        for (int i = 0; i < EQUIPMENT_SLOT_COUNT; i++) {
            ItemStack stack = i < equipment.size() ? equipment.get(i) : null;
            if (ItemStack.isEmpty(stack)) {
                sanitized.add(null);
                continue;
            }

            String itemId = sanitizeEquipmentItemId(stack.getItemId());
            sanitized.add(itemId == null ? null : cloneStack(itemId, stack));
        }

        return sanitized;
    }

    private String normalizeItemId(String rawItemId) {
        if (rawItemId == null) return null;

        String trimmed = toBaseItemId(rawItemId.trim());
        if (trimmed.isBlank()) return null;
        if (!itemIdValidationCache.computeIfAbsent(trimmed, id -> Item.getAssetMap().getAsset(id) != null)) return null;

        return trimmed;
    }

    private String toBaseItemId(String itemId) {
        if (itemId == null || itemId.isBlank()) return "";

        int separatorIndex = itemId.indexOf(RolledVirtualItemRegistry.VIRTUAL_SEPARATOR);
        if (separatorIndex <= 0) return itemId;
        return itemId.substring(0, separatorIndex);
    }

    private ItemStack cloneStack(@NonNull String itemId, @NonNull ItemStack source) {
        if (itemId.isBlank()) return null;

        BsonDocument metadata = readMetadata(source);
        ItemStack clone = new ItemStack(
                itemId,
                Math.max(1, source.getQuantity()),
                source.getDurability(),
                source.getMaxDurability(),
                metadata == null || metadata.isEmpty() ? null : metadata
        );
        clone.setOverrideDroppedItemAnimation(source.getOverrideDroppedItemAnimation());
        return clone;
    }

    private BsonDocument readMetadata(@NonNull ItemStack stack) {
        ItemWithAllMetadata packet = stack.toPacket();
        if (packet == null || packet.metadata == null || packet.metadata.isBlank()) return null;

        try {
            return BsonDocument.parse(packet.metadata);
        } catch (Exception ignored) {
            return null;
        }
    }
}
