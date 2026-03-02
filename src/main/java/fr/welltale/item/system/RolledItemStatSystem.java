package fr.welltale.item.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.characteristic.Characteristics;
import fr.welltale.item.ItemStatRoller;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RolledItemStatSystem extends EntityTickingSystem<EntityStore> {
    private static final String MODIFIER_NAMESPACE = "WTRolledItem";
    private static final double DELTA_EPSILON = 0.0001d;
    private static final long EQUIPMENT_RESCAN_INTERVAL_MS = 250L;

    private final ConcurrentHashMap<UUID, Map<Integer, Double>> lastAppliedByPlayer = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, String> lastEquipmentSignatureByPlayer = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Long> nextRescanByPlayer = new ConcurrentHashMap<>();

    private final Map<Integer, StaticModifier.CalculationType> calculationTypeByStat = new HashMap<>();
    private volatile boolean statIndicesInitialized;

    public RolledItemStatSystem() {}

    private boolean ensureStatIndicesInitialized() {
        if (statIndicesInitialized) return true;

        try {
            calculationTypeByStat.clear();

            int health = DefaultEntityStatTypes.getHealth();
            int wisdom = EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_WISDOM_KEY);
            int strength = EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_STRENGTH_KEY);
            int intelligence = EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_INTELLIGENCE_KEY);
            int chance = EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_CHANCE_KEY);
            int agility = EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_AGILITY_KEY);
            int lifeRegen = EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_LIFE_REGEN_PCT_KEY);
            int dropChance = EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_DROP_CHANCE_KEY);
            int moveSpeed = EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_MOVE_SPEED_KEY);
            int stamina = EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_STAMINA_KEY);
            int criticalDamage = EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_CRITICAL_DAMAGE_KEY);
            int criticalPct = EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_CRITICAL_PCT_KEY);
            int criticalResistance = EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_CRITICAL_RESISTANCE_KEY);
            int earthResistance = EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_EARTH_RESISTANCE_PCT_KEY);
            int fireResistance = EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_FIRE_RESISTANCE_PCT_KEY);
            int waterResistance = EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_WATER_RESISTANCE_PCT_KEY);
            int airResistance = EntityStatType.getAssetMap().getIndex(Characteristics.STATIC_MODIFIER_AIR_RESISTANCE_PCT_KEY);

            calculationTypeByStat.put(health, StaticModifier.CalculationType.ADDITIVE);
            calculationTypeByStat.put(wisdom, StaticModifier.CalculationType.ADDITIVE);
            calculationTypeByStat.put(strength, StaticModifier.CalculationType.ADDITIVE);
            calculationTypeByStat.put(intelligence, StaticModifier.CalculationType.ADDITIVE);
            calculationTypeByStat.put(chance, StaticModifier.CalculationType.ADDITIVE);
            calculationTypeByStat.put(agility, StaticModifier.CalculationType.ADDITIVE);
            calculationTypeByStat.put(stamina, StaticModifier.CalculationType.ADDITIVE);
            calculationTypeByStat.put(criticalDamage, StaticModifier.CalculationType.ADDITIVE);
            calculationTypeByStat.put(criticalResistance, StaticModifier.CalculationType.ADDITIVE);

            calculationTypeByStat.put(lifeRegen, StaticModifier.CalculationType.MULTIPLICATIVE);
            calculationTypeByStat.put(dropChance, StaticModifier.CalculationType.MULTIPLICATIVE);
            calculationTypeByStat.put(moveSpeed, StaticModifier.CalculationType.MULTIPLICATIVE);
            calculationTypeByStat.put(criticalPct, StaticModifier.CalculationType.MULTIPLICATIVE);
            calculationTypeByStat.put(earthResistance, StaticModifier.CalculationType.MULTIPLICATIVE);
            calculationTypeByStat.put(fireResistance, StaticModifier.CalculationType.MULTIPLICATIVE);
            calculationTypeByStat.put(waterResistance, StaticModifier.CalculationType.MULTIPLICATIVE);
            calculationTypeByStat.put(airResistance, StaticModifier.CalculationType.MULTIPLICATIVE);

            statIndicesInitialized = true;
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    @Override
    public @NonNull Query<EntityStore> getQuery() {
        return Query.and(PlayerRef.getComponentType());
    }

    @Override
    public void tick(float dt, int index, @NonNull ArchetypeChunk<EntityStore> archetypeChunk, @NonNull Store<EntityStore> store, @NonNull CommandBuffer<EntityStore> commandBuffer) {
        if (!ensureStatIndicesInitialized()) return;

        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        if (!ref.isValid()) return;

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) return;

        UUID playerUuid = playerRef.getUuid();

        long now = System.currentTimeMillis();
        Long nextRescanAt = nextRescanByPlayer.get(playerUuid);
        if (nextRescanAt != null && now < nextRescanAt) return;
        nextRescanByPlayer.put(playerUuid, now + EQUIPMENT_RESCAN_INTERVAL_MS);

        EntityStatMap statMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (statMap == null) return;

        Player runtimePlayer = store.getComponent(ref, Player.getComponentType());
        if (runtimePlayer == null) return;

        Inventory inventory = runtimePlayer.getInventory();
        if (inventory == null) return;

        String currentSignature = buildEquipmentSignature(inventory);
        String previousSignature = lastEquipmentSignatureByPlayer.get(playerUuid);
        if (currentSignature.equals(previousSignature)) return;

        Map<Integer, Double> current = collectEquippedRollDeltas(inventory);
        Map<Integer, Double> previous = lastAppliedByPlayer.get(playerUuid);
        if (hasSameDeltas(previous, current)) {
            lastEquipmentSignatureByPlayer.put(playerUuid, currentSignature);
            return;
        }

        applyDelta(statMap, previous, current);
        lastAppliedByPlayer.put(playerUuid, current);
        lastEquipmentSignatureByPlayer.put(playerUuid, currentSignature);
    }

    public void clearPlayer(@NonNull UUID playerUuid) {
        lastAppliedByPlayer.remove(playerUuid);
        lastEquipmentSignatureByPlayer.remove(playerUuid);
        nextRescanByPlayer.remove(playerUuid);
    }

    private Map<Integer, Double> collectEquippedRollDeltas(@NonNull Inventory inventory) {
        HashMap<Integer, Double> total = new HashMap<>();

        mergeStatDeltas(total, ItemStatRoller.getRollDeltaByStat(inventory.getItemInHand()));
        mergeStatDeltas(total, ItemStatRoller.getRollDeltaByStat(inventory.getUtilityItem()));

        var armorSection = inventory.getSectionById(Inventory.ARMOR_SECTION_ID);
        if (armorSection != null) {
            short capacity = armorSection.getCapacity();
            for (short slot = 0; slot < capacity; slot++) {
                ItemStack stack = armorSection.getItemStack(slot);
                mergeStatDeltas(total, ItemStatRoller.getRollDeltaByStat(stack));
            }
        }

        total.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue() == 0.0d);
        return total;
    }

    private void mergeStatDeltas(@NonNull Map<Integer, Double> target, Map<Integer, Double> delta) {
        if (delta == null || delta.isEmpty()) return;
        for (Map.Entry<Integer, Double> entry : delta.entrySet()) {
            Integer statIndex = entry.getKey();
            Double value = entry.getValue();
            if (statIndex == null || value == null || value == 0.0d) continue;
            target.merge(statIndex, value, Double::sum);
        }
    }

    private boolean hasSameDeltas(Map<Integer, Double> a, Map<Integer, Double> b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (a.size() != b.size()) return false;

        for (Map.Entry<Integer, Double> entry : a.entrySet()) {
            Double other = b.get(entry.getKey());
            if (other == null) return false;
            if (Math.abs(entry.getValue() - other) > DELTA_EPSILON) return false;
        }

        return true;
    }

    private String buildEquipmentSignature(@NonNull Inventory inventory) {
        StringBuilder signature = new StringBuilder(256);
        appendStackSignature(signature, inventory.getItemInHand());
        appendStackSignature(signature, inventory.getUtilityItem());

        var armorSection = inventory.getSectionById(Inventory.ARMOR_SECTION_ID);
        if (armorSection != null) {
            short capacity = armorSection.getCapacity();
            for (short slot = 0; slot < capacity; slot++) {
                appendStackSignature(signature, armorSection.getItemStack(slot));
            }
        }

        return signature.toString();
    }

    private void appendStackSignature(@NonNull StringBuilder signature, ItemStack stack) {
        if (ItemStack.isEmpty(stack)) {
            signature.append("|-");
            return;
        }

        ItemWithAllMetadata packet = stack.toPacket();
        signature.append('|').append(stack.getItemId());
        signature.append('|').append(packet != null && packet.metadata != null ? packet.metadata : "");
    }

    private void applyDelta(
            @NonNull EntityStatMap statMap,
            Map<Integer, Double> previous,
            @NonNull Map<Integer, Double> current
    ) {

        if (previous != null && !previous.isEmpty()) {
            for (Integer statIndex : previous.keySet()) {
                if (statIndex == null || current.containsKey(statIndex)) continue;
                putModifier(statMap, statIndex, 0.0d);
            }
        }

        for (Map.Entry<Integer, Double> entry : current.entrySet()) {
            Integer statIndex = entry.getKey();
            Double delta = entry.getValue();
            if (statIndex == null || delta == null) continue;
            putModifier(statMap, statIndex, delta);
        }
    }

    private void putModifier(@NonNull EntityStatMap statMap, int statIndex, double amount) {
        StaticModifier.CalculationType calcType = calculationTypeByStat.getOrDefault(statIndex, StaticModifier.CalculationType.ADDITIVE);
        String key = calcType.createKey(MODIFIER_NAMESPACE + "_" + statIndex);
        StaticModifier modifier = new StaticModifier(Modifier.ModifierTarget.MAX, calcType, (float) amount);
        statMap.putModifier(statIndex, key, modifier);
    }
}
