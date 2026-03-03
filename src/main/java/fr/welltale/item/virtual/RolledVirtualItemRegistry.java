package fr.welltale.item.virtual;

import com.hypixel.hytale.protocol.ItemBase;
import com.hypixel.hytale.protocol.ItemTranslationProperties;
import com.hypixel.hytale.protocol.ItemUtility;
import com.hypixel.hytale.protocol.Modifier;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import fr.welltale.item.ItemStatRoller;
import fr.welltale.item.RolledItemConstants;
import fr.welltale.item.RolledItemBaseDamageCollector;
import fr.welltale.item.RolledTooltipDescriptionBuilder;
import lombok.NonNull;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonValue;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RolledVirtualItemRegistry {
    public static final String VIRTUAL_SEPARATOR = "__wtroll_";

    private final ConcurrentHashMap<String, ItemBase> cacheByVirtualId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> tooltipDescriptionByVirtualId = new ConcurrentHashMap<>();
    private final RolledTooltipDescriptionBuilder tooltipDescriptionBuilder = new RolledTooltipDescriptionBuilder();
    private final RolledItemBaseDamageCollector baseDamageCollector = new RolledItemBaseDamageCollector();
    private final LinkedHashMap<String, Boolean> lruAccessOrder = new LinkedHashMap<>(256, 0.75f, true);
    private final Object lruLock = new Object();
    private final int maxCacheEntries;

    public RolledVirtualItemRegistry() {
        this.maxCacheEntries = RolledItemConstants.VIRTUAL_CACHE_MAX_ENTRIES;
    }

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

        ItemBase cached = cacheByVirtualId.get(virtualId);
        if (cached != null) {
            touchAndPrune(virtualId);
            return cached;
        }

        ItemBase created = createVirtualItem(baseItemId, virtualId, metadata);
        if (created == null) return null;

        ItemBase existing = cacheByVirtualId.putIfAbsent(virtualId, created);
        ItemBase resolved = existing != null ? existing : created;
        touchAndPrune(virtualId);
        return resolved;
    }

    private ItemBase createVirtualItem(@NonNull String baseItemId, @NonNull String virtualId, @NonNull BsonDocument metadata) {
        Item item = Item.getAssetMap().getAsset(baseItemId);
        if (item == null) return null;

        ItemBase basePacket = item.toPacket();

        Map<Integer, ItemStatRoller.AmountRange> possibleRangeByStat = ItemStatRoller.getPossibleRollRangeByItemId(baseItemId);

        ItemBase clone = basePacket.clone();
        clone.id = virtualId;
        clone.variant = true;

        BsonDocument rollData = readRollData(metadata);
        if (rollData == null || rollData.isEmpty()) return clone;

        applyRolls(clone, rollData);
        applyTooltipDescription(baseItemId, clone, virtualId, possibleRangeByStat);
        hideDefaultTooltipStats(clone);
        return clone;
    }

    public Map<String, String> getTooltipTranslations(@NonNull Iterable<String> virtualIds) {
        LinkedHashMap<String, String> translations = new LinkedHashMap<>();
        for (String virtualId : virtualIds) {
            if (virtualId == null || virtualId.isBlank()) continue;

            String description = tooltipDescriptionByVirtualId.get(virtualId);
            if (description == null || description.isBlank()) continue;

            String translationKey = tooltipDescriptionBuilder.createTranslationKey(virtualId);
            translations.put(translationKey, description);
        }

        return translations;
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

    private void applyTooltipDescription(
            @NonNull String baseItemId,
            @NonNull ItemBase itemBase,
            @NonNull String virtualId,
            @NonNull Map<Integer, ItemStatRoller.AmountRange> possibleRangeByStat
    ) {
        var baseDamageRanges = baseDamageCollector.getBaseDamageRanges(baseItemId);
        RolledTooltipDescriptionBuilder.TooltipDescription tooltipDescription = tooltipDescriptionBuilder.build(virtualId, itemBase, baseItemId, possibleRangeByStat, baseDamageRanges);
        if (tooltipDescription == null) {
            tooltipDescriptionByVirtualId.remove(virtualId);
            return;
        }

        ItemTranslationProperties translationProperties = itemBase.translationProperties != null
                ? itemBase.translationProperties.clone()
                : new ItemTranslationProperties();

        translationProperties.description = tooltipDescription.translationKey();
        itemBase.translationProperties = translationProperties;

        tooltipDescriptionByVirtualId.put(virtualId, tooltipDescription.description());
    }

    private void hideDefaultTooltipStats(@NonNull ItemBase itemBase) {
        if (itemBase.weapon != null) itemBase.weapon.statModifiers = null;
        if (itemBase.armor != null) itemBase.armor.statModifiers = null;

        ItemUtility utility = itemBase.utility;
        if (utility != null) utility.statModifiers = null;

        itemBase.displayEntityStatsHUD = new int[0];
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

    private void touchAndPrune(@NonNull String virtualId) {
        if (maxCacheEntries <= 0) return;

        synchronized (lruLock) {
            lruAccessOrder.put(virtualId, Boolean.TRUE);
            while (lruAccessOrder.size() > maxCacheEntries) {
                Iterator<String> iterator = lruAccessOrder.keySet().iterator();
                if (!iterator.hasNext()) break;

                String evictedVirtualId = iterator.next();
                iterator.remove();
                cacheByVirtualId.remove(evictedVirtualId);
                tooltipDescriptionByVirtualId.remove(evictedVirtualId);
            }
        }
    }

}
