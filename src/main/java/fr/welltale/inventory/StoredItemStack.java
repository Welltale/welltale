package fr.welltale.inventory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import fr.welltale.item.ItemStatRoller;
import fr.welltale.item.virtual.RolledVirtualItemRegistry;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.BsonDocument;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StoredItemStack {
    private String itemId;
    private int quantity;
    private String rollData;

    public StoredItemStack(String itemId, int quantity, String rollData) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.rollData = rollData;
    }

    public static StoredItemStack fromItemStack(ItemStack stack) {
        if (ItemStack.isEmpty(stack)) return null;

        String itemId = toBaseItemId(stack.getItemId());
        if (itemId.isBlank()) return null;

        BsonDocument rollData = ItemStatRoller.getRollData(stack);
        String rollDataJson = rollData == null || rollData.isEmpty() ? null : rollData.toJson();

        return new StoredItemStack(itemId, Math.max(1, stack.getQuantity()), rollDataJson);
    }

    public ItemStack toItemStack() {
        String baseItemId = toBaseItemId(itemId);
        if (baseItemId.isBlank()) return null;

        ItemStack baseStack = new ItemStack(baseItemId, Math.max(1, quantity));
        if (rollData == null || rollData.isBlank()) return baseStack;

        try {
            BsonDocument metadata = new BsonDocument();
            metadata.put(ItemStatRoller.ROLL_METADATA_KEY, BsonDocument.parse(rollData));
            return baseStack.withMetadata(metadata);
        } catch (Exception ignored) {
            return baseStack;
        }
    }

    private static String toBaseItemId(String rawItemId) {
        if (rawItemId == null || rawItemId.isBlank()) return "";

        int separatorIndex = rawItemId.indexOf(RolledVirtualItemRegistry.VIRTUAL_SEPARATOR);
        if (separatorIndex <= 0) return rawItemId;
        return rawItemId.substring(0, separatorIndex);
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
