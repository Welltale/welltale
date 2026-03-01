package fr.welltale.inventory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StoredItemStack {
    private String itemId;
    private int quantity;

    public static StoredItemStack fromItemStack(ItemStack stack) {
        if (ItemStack.isEmpty(stack)) return null;

        String itemId = stack.getItemId();
        if (itemId.isBlank()) return null;

        return new StoredItemStack(itemId, Math.max(1, stack.getQuantity()));
    }

    public ItemStack toItemStack() {
        if (itemId == null || itemId.isBlank()) return null;
        return new ItemStack(itemId, Math.max(1, quantity));
    }

    public static List<StoredItemStack> fromItemStackList(List<ItemStack> source, int fixedSize) {
        ArrayList<StoredItemStack> result = new ArrayList<>(Math.max(0, fixedSize));
        int size = Math.max(0, fixedSize);
        for (int i = 0; i < size; i++) {
            ItemStack stack = source != null && i < source.size() ? source.get(i) : null;
            result.add(fromItemStack(stack));
        }
        return result;
    }

    public static List<ItemStack> toItemStackList(List<StoredItemStack> source, int fixedSize) {
        ArrayList<ItemStack> result = new ArrayList<>(Math.max(0, fixedSize));
        int size = Math.max(0, fixedSize);
        for (int i = 0; i < size; i++) {
            StoredItemStack stack = source != null && i < source.size() ? source.get(i) : null;
            result.add(stack == null ? null : stack.toItemStack());
        }
        return result;
    }
}
