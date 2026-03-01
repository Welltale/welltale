package fr.welltale.inventory;

import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class CharacterVanillaInventorySnapshot {
    public static final int HOTBAR_SLOT_COUNT = 9;
    public static final int STORAGE_SLOT_COUNT = 36;
    public static final int ARMOR_SLOT_COUNT = 4;

    @Getter
    private final List<ItemStack> hotbar;
    @Getter
    private final List<ItemStack> storage;
    @Getter
    private final List<ItemStack> armor;

    private CharacterVanillaInventorySnapshot(List<ItemStack> hotbar, List<ItemStack> storage, List<ItemStack> armor) {
        this.hotbar = cloneSlots(hotbar, HOTBAR_SLOT_COUNT);
        this.storage = cloneSlots(storage, STORAGE_SLOT_COUNT);
        this.armor = cloneSlots(armor, ARMOR_SLOT_COUNT);
    }

    public static CharacterVanillaInventorySnapshot fromStored(
            List<StoredItemStack> hotbar,
            List<StoredItemStack> storage,
            List<StoredItemStack> armor
    ) {
        return new CharacterVanillaInventorySnapshot(
                StoredItemStack.toItemStackList(hotbar, HOTBAR_SLOT_COUNT),
                StoredItemStack.toItemStackList(storage, STORAGE_SLOT_COUNT),
                StoredItemStack.toItemStackList(armor, ARMOR_SLOT_COUNT)
        );
    }

    public static CharacterVanillaInventorySnapshot capture(Inventory inventory) {
        if (inventory == null) {
            return new CharacterVanillaInventorySnapshot(null, null, null);
        }

        ArrayList<ItemStack> hotbar = new ArrayList<>(HOTBAR_SLOT_COUNT);
        ArrayList<ItemStack> storage = new ArrayList<>(STORAGE_SLOT_COUNT);
        ArrayList<ItemStack> armor = new ArrayList<>(ARMOR_SLOT_COUNT);

        for (int i = 0; i < HOTBAR_SLOT_COUNT; i++) {
            hotbar.add(cloneStack(inventory.getHotbar().getItemStack((short) i)));
        }
        for (int i = 0; i < STORAGE_SLOT_COUNT; i++) {
            storage.add(cloneStack(inventory.getStorage().getItemStack((short) i)));
        }

        var armorSection = inventory.getSectionById(Inventory.ARMOR_SECTION_ID);
        for (int i = 0; i < ARMOR_SLOT_COUNT; i++) {
            ItemStack stack = armorSection == null ? null : armorSection.getItemStack((short) i);
            armor.add(cloneStack(stack));
        }

        return new CharacterVanillaInventorySnapshot(hotbar, storage, armor);
    }

    public List<StoredItemStack> getStoredHotbar() {
        return StoredItemStack.fromItemStackList(this.hotbar, HOTBAR_SLOT_COUNT);
    }

    public List<StoredItemStack> getStoredStorage() {
        return StoredItemStack.fromItemStackList(this.storage, STORAGE_SLOT_COUNT);
    }

    public List<StoredItemStack> getStoredArmor() {
        return StoredItemStack.fromItemStackList(this.armor, ARMOR_SLOT_COUNT);
    }

    public void apply(Inventory inventory) {
        if (inventory == null) return;

        for (int i = 0; i < HOTBAR_SLOT_COUNT; i++) {
            inventory.getHotbar().setItemStackForSlot((short) i, stackOrNull(this.hotbar, i), true);
        }
        for (int i = 0; i < STORAGE_SLOT_COUNT; i++) {
            inventory.getStorage().setItemStackForSlot((short) i, stackOrNull(this.storage, i), true);
        }

        var armorSection = inventory.getSectionById(Inventory.ARMOR_SECTION_ID);
        if (armorSection == null) return;

        for (int i = 0; i < ARMOR_SLOT_COUNT; i++) {
            armorSection.setItemStackForSlot((short) i, stackOrNull(this.armor, i), true);
        }
    }

    private static List<ItemStack> cloneSlots(List<ItemStack> source, int size) {
        ArrayList<ItemStack> cloned = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ItemStack stack = source != null && i < source.size() ? source.get(i) : null;
            cloned.add(cloneStack(stack));
        }
        return cloned;
    }

    private static ItemStack stackOrNull(List<ItemStack> source, int index) {
        if (source == null || index < 0 || index >= source.size()) return null;
        ItemStack stack = source.get(index);
        return cloneStack(stack);
    }

    private static ItemStack cloneStack(ItemStack stack) {
        if (ItemStack.isEmpty(stack)) return null;
        return new ItemStack(stack.getItemId(), Math.max(1, stack.getQuantity()));
    }
}
