package fr.welltale.item;

import com.hypixel.hytale.protocol.ItemArmor;
import com.hypixel.hytale.protocol.ItemBase;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.protocol.ItemWeapon;
import com.hypixel.hytale.protocol.Modifier;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import lombok.NonNull;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.BsonInt32;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class ItemStatRoller {
    public static final String ROLL_METADATA_KEY = "wtItemRoll";
    private static final int MIN_INTEGER_ROLL_ABS = 1;
    private static final double MIN_DECIMAL_ROLL_ABS = 1.0d;
    private static final double MIN_DECIMAL_ROLL_FRACTIONAL = 0.01d;

    private ItemStatRoller() {}

    public static ItemStack rollStats(ItemStack stack) {
        if (ItemStack.isEmpty(stack)) return stack;

        String itemId = stack.getItemId();
        if (itemId.isBlank()) return stack;

        Item item = Item.getAssetMap().getAsset(itemId);
        if (item == null) return stack;

        ItemBase itemBase = item.toPacket();

        BsonDocument rollRoot = new BsonDocument();
        boolean hasAnyRoll = false;

        BsonArray weaponRolls = rollModifiers(itemBase.weapon == null ? null : itemBase.weapon.statModifiers);
        if (!weaponRolls.isEmpty()) {
            rollRoot.put("weapon", weaponRolls);
            hasAnyRoll = true;
        }

        BsonArray armorRolls = rollModifiers(itemBase.armor == null ? null : itemBase.armor.statModifiers);
        if (!armorRolls.isEmpty()) {
            rollRoot.put("armor", armorRolls);
            hasAnyRoll = true;
        }

        if (!hasAnyRoll) return stack;

        BsonDocument metadata = readMetadata(stack);
        if (metadata == null) metadata = new BsonDocument();
        metadata.put(ROLL_METADATA_KEY, rollRoot);
        return stack.withMetadata(metadata);
    }

    public static Map<Integer, Double> getRollDeltaByStat(ItemStack stack) {
        HashMap<Integer, Double> deltaByStat = new HashMap<>();
        if (ItemStack.isEmpty(stack)) return deltaByStat;

        String itemId = stack.getItemId();
        if (itemId.isBlank()) return deltaByStat;

        Item item = Item.getAssetMap().getAsset(itemId);
        if (item == null) return deltaByStat;

        ItemBase itemBase = item.toPacket();

        BsonDocument rollRoot = getRollData(stack);
        if (rollRoot == null || rollRoot.isEmpty()) {
            return deltaByStat;
        }

        HashMap<Integer, Double> baseByStat = new HashMap<>();
        HashMap<Integer, Double> rolledByStat = new HashMap<>();

        collectBaseTotals(baseByStat, itemBase.weapon == null ? null : itemBase.weapon.statModifiers);
        collectBaseTotals(baseByStat, itemBase.armor == null ? null : itemBase.armor.statModifiers);

        collectRolledTotals(rolledByStat, rollRoot.getArray("weapon", new BsonArray()));
        collectRolledTotals(rolledByStat, rollRoot.getArray("armor", new BsonArray()));

        for (Map.Entry<Integer, Double> entry : rolledByStat.entrySet()) {
            Integer statIndex = entry.getKey();
            double rolledValue = entry.getValue();
            double baseValue = baseByStat.getOrDefault(statIndex, 0.0d);
            double delta = rolledValue - baseValue;
            if (delta != 0.0d) {
                deltaByStat.put(statIndex, delta);
            }
        }

        return deltaByStat;
    }

    public static BsonDocument getRollData(ItemStack stack) {
        BsonDocument metadata = readMetadata(stack);
        if (metadata == null || !metadata.containsKey(ROLL_METADATA_KEY) || !metadata.get(ROLL_METADATA_KEY).isDocument()) {
            return null;
        }
        return metadata.getDocument(ROLL_METADATA_KEY).clone();
    }

    private static BsonArray rollModifiers(Map<Integer, Modifier[]> modifiersByStat) {
        BsonArray out = new BsonArray();
        if (modifiersByStat == null || modifiersByStat.isEmpty()) return out;

        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (Map.Entry<Integer, Modifier[]> entry : modifiersByStat.entrySet()) {
            Integer statIndex = entry.getKey();
            Modifier[] modifiers = entry.getValue();

            if (statIndex == null || modifiers == null || modifiers.length == 0) continue;

            BsonArray rolls = new BsonArray();
            for (Modifier modifier : modifiers) {
                if (modifier == null) continue;

                double rolled = rollModifierAmount(modifier.amount, random);
                rolls.add(new BsonDouble(rolled));
            }

            if (rolls.isEmpty()) continue;

            BsonDocument statDoc = new BsonDocument();
            statDoc.put("statIndex", new BsonInt32(statIndex));
            statDoc.put("rolls", rolls);
            out.add(statDoc);
        }

        return out;
    }

    private static BsonDocument readMetadata(ItemStack stack) {
        if (ItemStack.isEmpty(stack)) return null;

        ItemWithAllMetadata packet = stack.toPacket();
        if (packet == null || packet.metadata == null || packet.metadata.isBlank()) return null;

        try {
            return BsonDocument.parse(packet.metadata);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static double rollModifierAmount(double baseAmount, @NonNull ThreadLocalRandom random) {
        if (baseAmount == 0.0d) return 0.0d;

        if (Math.rint(baseAmount) == baseAmount) {
            int magnitude = (int) Math.abs(baseAmount);
            if (magnitude < MIN_INTEGER_ROLL_ABS) return baseAmount;

            int rolledInt = random.nextInt(MIN_INTEGER_ROLL_ABS, magnitude + 1);
            return baseAmount > 0.0d ? rolledInt : -rolledInt;
        }

        double magnitude = Math.abs(baseAmount);
        if (magnitude <= MIN_DECIMAL_ROLL_FRACTIONAL) return baseAmount;

        double minMagnitude = magnitude <= MIN_DECIMAL_ROLL_ABS
                ? MIN_DECIMAL_ROLL_FRACTIONAL
                : MIN_DECIMAL_ROLL_ABS;

        double rolledMagnitude = random.nextDouble(minMagnitude, Math.nextUp(magnitude));
        return baseAmount > 0.0d ? rolledMagnitude : -rolledMagnitude;
    }

    private static void collectBaseTotals(@NonNull Map<Integer, Double> out, Map<Integer, Modifier[]> modifiersByStat) {
        if (modifiersByStat == null || modifiersByStat.isEmpty()) return;

        for (Map.Entry<Integer, Modifier[]> entry : modifiersByStat.entrySet()) {
            Integer statIndex = entry.getKey();
            Modifier[] modifiers = entry.getValue();
            if (statIndex == null || modifiers == null || modifiers.length == 0) continue;

            double total = 0.0d;
            for (Modifier modifier : modifiers) {
                if (modifier == null) continue;
                total += modifier.amount;
            }

            if (total != 0.0d) {
                out.merge(statIndex, total, Double::sum);
            }
        }
    }

    private static void collectRolledTotals(@NonNull Map<Integer, Double> out, BsonArray sectionRolls) {
        if (sectionRolls == null || sectionRolls.isEmpty()) return;

        for (var value : sectionRolls) {
            if (!value.isDocument()) continue;

            BsonDocument statDoc = value.asDocument();
            if (!statDoc.containsKey("statIndex") || !statDoc.containsKey("rolls")) continue;
            if (!statDoc.get("statIndex").isInt32() || !statDoc.get("rolls").isArray()) continue;

            int statIndex = statDoc.getInt32("statIndex").getValue();
            BsonArray rolls = statDoc.getArray("rolls");

            double total = 0.0d;
            for (var roll : rolls) {
                if (!roll.isNumber()) continue;
                total += roll.asNumber().doubleValue();
            }

            if (total != 0.0d) {
                out.merge(statIndex, total, Double::sum);
            }
        }
    }
}
