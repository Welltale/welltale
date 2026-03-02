package fr.welltale.item.virtual;

import com.hypixel.hytale.protocol.ItemBase;
import com.hypixel.hytale.protocol.Modifier;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import fr.welltale.item.ItemStatRoller;
import lombok.NonNull;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonValue;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RolledVirtualItemRegistry {
    public static final String VIRTUAL_SEPARATOR = "__wtroll_";

    private final ConcurrentHashMap<String, ItemBase> cacheByVirtualId = new ConcurrentHashMap<>();

    public boolean isVirtualId(String itemId) {
        return itemId != null && itemId.contains(VIRTUAL_SEPARATOR);
    }

    public String getBaseItemId(String virtualOrBaseId) {
        if (virtualOrBaseId == null) return null;

        int separatorIndex = virtualOrBaseId.indexOf(VIRTUAL_SEPARATOR);
        if (separatorIndex <= 0) return virtualOrBaseId;
        return virtualOrBaseId.substring(0, separatorIndex);
    }

    public String createVirtualId(@NonNull String baseItemId, @NonNull BsonDocument metadata) {
        BsonDocument rollData = readRollData(metadata);
        if (baseItemId.isBlank() || rollData == null || rollData.isEmpty()) return null;

        String hash = shortHash(rollData.toJson());
        return baseItemId + VIRTUAL_SEPARATOR + hash;
    }

    public ItemBase getOrCreate(@NonNull String baseItemId, @NonNull String virtualId, @NonNull BsonDocument metadata) {
        if (baseItemId.isBlank() || virtualId.isBlank()) return null;

        return cacheByVirtualId.computeIfAbsent(virtualId, _ -> {
            Item item = Item.getAssetMap().getAsset(baseItemId);
            if (item == null) return null;

            ItemBase basePacket = item.toPacket();

            ItemBase clone = basePacket.clone();
            clone.id = virtualId;

            BsonDocument rollData = readRollData(metadata);
            if (rollData == null || rollData.isEmpty()) return clone;

            applyRolls(clone, rollData);
            return clone;
        });
    }

    private BsonDocument readRollData(@NonNull BsonDocument metadata) {
        if (metadata.isEmpty() || !metadata.containsKey(ItemStatRoller.ROLL_METADATA_KEY)) return null;

        BsonValue value = metadata.get(ItemStatRoller.ROLL_METADATA_KEY);
        return value != null && value.isDocument() ? value.asDocument() : null;
    }

    private void applyRolls(@NonNull ItemBase itemBase, BsonDocument rollData) {
        if (rollData == null || rollData.isEmpty()) return;

        if (itemBase.weapon != null) {
            itemBase.weapon.statModifiers = applySection(itemBase.weapon.statModifiers, rollData.getArray("weapon", new BsonArray()));
        }
        if (itemBase.armor != null) {
            itemBase.armor.statModifiers = applySection(itemBase.armor.statModifiers, rollData.getArray("armor", new BsonArray()));
        }
    }

    private Map<Integer, Modifier[]> applySection(Map<Integer, Modifier[]> baseMap, BsonArray sectionRolls) {
        if (baseMap == null || baseMap.isEmpty() || sectionRolls == null || sectionRolls.isEmpty()) return baseMap;

        HashMap<Integer, Modifier[]> out = new HashMap<>(baseMap.size());
        for (Map.Entry<Integer, Modifier[]> entry : baseMap.entrySet()) {
            Integer statIndex = entry.getKey();
            Modifier[] source = entry.getValue();
            if (statIndex == null || source == null) continue;

            Modifier[] clone = new Modifier[source.length];
            for (int i = 0; i < source.length; i++) {
                Modifier current = source[i];
                if (current == null) continue;
                clone[i] = new Modifier(current.target, current.calculationType, current.amount);
            }
            out.put(statIndex, clone);
        }

        for (BsonValue value : sectionRolls) {
            if (!value.isDocument()) continue;
            BsonDocument doc = value.asDocument();
            if (!doc.containsKey("statIndex") || !doc.containsKey("rolls")) continue;
            if (!doc.get("statIndex").isInt32() || !doc.get("rolls").isArray()) continue;

            int statIndex = doc.getInt32("statIndex").getValue();
            Modifier[] modifiers = out.get(statIndex);
            if (modifiers == null || modifiers.length == 0) continue;

            BsonArray rolls = doc.getArray("rolls");
            int max = Math.min(modifiers.length, rolls.size());
            for (int i = 0; i < max; i++) {
                Modifier current = modifiers[i];
                BsonValue rollValue = rolls.get(i);
                if (current == null || !rollValue.isNumber()) continue;

                float amount = (float) rollValue.asNumber().doubleValue();
                modifiers[i] = new Modifier(current.target, current.calculationType, amount);
            }
        }

        return out;
    }

    private String shortHash(@NonNull String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] raw = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                builder.append(String.format("%02x", raw[i]));
            }
            return builder.toString();
        } catch (Exception e) {
            return Integer.toHexString(input.hashCode());
        }
    }
}
