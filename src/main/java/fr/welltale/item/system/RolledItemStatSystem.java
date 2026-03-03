package fr.welltale.item.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
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
import fr.welltale.item.RolledItemConstants;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RolledItemStatSystem extends EntityTickingSystem<EntityStore> {
    private static final String MODIFIER_NAMESPACE = "WTRolledItem";
    private static final long FNV64_OFFSET_BASIS = 0xcbf29ce484222325L;
    private static final long FNV64_PRIME = 0x100000001b3L;
    private static final int EMPTY_STACK_FINGERPRINT = -1;

    private final ConcurrentHashMap<UUID, Map<Integer, Double>> lastAppliedByPlayer = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Long> lastEquipmentFingerprintByPlayer = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Long> nextRescanByPlayer = new ConcurrentHashMap<>();
    private final long equipmentRescanIntervalMs;

    private final Map<Integer, StaticModifier.CalculationType> calculationTypeByStat = new HashMap<>();
    private volatile boolean statIndicesInitialized;

    public RolledItemStatSystem() {
        this.equipmentRescanIntervalMs = RolledItemConstants.STATS_EQUIPMENT_RESCAN_INTERVAL_MS;
    }

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
            int stamina = DefaultEntityStatTypes.getStamina();
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

        EntityStatMap statMap = store.getComponent(ref, EntityStatMap.getComponentType());
        if (statMap == null) return;

        Player runtimePlayer = store.getComponent(ref, Player.getComponentType());
        if (runtimePlayer == null) return;

        Inventory inventory = runtimePlayer.getInventory();
        if (inventory == null) return;

        long now = System.currentTimeMillis();
        EquipmentScan equipmentScan = scanEquipment(inventory);
        Long previousFingerprint = lastEquipmentFingerprintByPlayer.get(playerUuid);
        boolean equipmentChanged = previousFingerprint == null || previousFingerprint != equipmentScan.fingerprint();

        if (!equipmentChanged) {
            Long nextRescanAt = nextRescanByPlayer.get(playerUuid);
            if (nextRescanAt != null && now < nextRescanAt) return;
        }

        nextRescanByPlayer.put(playerUuid, now + equipmentRescanIntervalMs);

        Map<Integer, Double> previous = lastAppliedByPlayer.get(playerUuid);
        if (!equipmentScan.hasRolledMetadata()) {
            if (previous != null && !previous.isEmpty()) {
                applyDelta(statMap, previous, Map.of());
                lastAppliedByPlayer.put(playerUuid, Map.of());
            }

            lastEquipmentFingerprintByPlayer.put(playerUuid, equipmentScan.fingerprint());
            return;
        }

        Map<Integer, Double> current = collectEquippedRollDeltas(inventory);
        if (hasSameDeltas(previous, current)) {
            lastEquipmentFingerprintByPlayer.put(playerUuid, equipmentScan.fingerprint());
            return;
        }

        applyDelta(statMap, previous, current);
        lastAppliedByPlayer.put(playerUuid, current);
        lastEquipmentFingerprintByPlayer.put(playerUuid, equipmentScan.fingerprint());
    }

    public void clearPlayer(@NonNull UUID playerUuid) {
        lastAppliedByPlayer.remove(playerUuid);
        lastEquipmentFingerprintByPlayer.remove(playerUuid);
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
            if (Math.abs(entry.getValue() - other) > RolledItemConstants.STATS_DELTA_EPSILON) return false;
        }

        return true;
    }

    private EquipmentScan scanEquipment(@NonNull Inventory inventory) {
        long fingerprint = FNV64_OFFSET_BASIS;
        boolean hasRolledMetadata = false;

        ItemStatRoller.RollMetadataHint inHandHint = ItemStatRoller.inspectRollMetadata(inventory.getItemInHand());
        fingerprint = mixStackFingerprint(fingerprint, inventory.getItemInHand(), inHandHint.hash(), inHandHint.length());
        if (inHandHint.present()) hasRolledMetadata = true;

        ItemStatRoller.RollMetadataHint utilityHint = ItemStatRoller.inspectRollMetadata(inventory.getUtilityItem());
        fingerprint = mixStackFingerprint(fingerprint, inventory.getUtilityItem(), utilityHint.hash(), utilityHint.length());
        if (utilityHint.present()) hasRolledMetadata = true;

        var armorSection = inventory.getSectionById(Inventory.ARMOR_SECTION_ID);
        if (armorSection != null) {
            short capacity = armorSection.getCapacity();
            for (short slot = 0; slot < capacity; slot++) {
                ItemStack stack = armorSection.getItemStack(slot);
                ItemStatRoller.RollMetadataHint hint = ItemStatRoller.inspectRollMetadata(stack);
                fingerprint = mixStackFingerprint(fingerprint, stack, hint.hash(), hint.length());
                if (hint.present()) hasRolledMetadata = true;
            }
        }

        return new EquipmentScan(fingerprint, hasRolledMetadata);
    }

    private long mixStackFingerprint(long fingerprint, ItemStack stack, int rollHash, int rollLength) {
        fingerprint = mixFingerprint(fingerprint, 31);
        if (ItemStack.isEmpty(stack)) {
            return mixFingerprint(fingerprint, EMPTY_STACK_FINGERPRINT);
        }

        String itemId = stack.getItemId();
        fingerprint = mixFingerprint(fingerprint, itemId.hashCode());
        fingerprint = mixFingerprint(fingerprint, rollHash);
        return mixFingerprint(fingerprint, rollLength);
    }

    private long mixFingerprint(long fingerprint, int value) {
        long mixed = fingerprint ^ (value & 0xffffffffL);
        return mixed * FNV64_PRIME;
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
            if (previous != null) {
                Double previousAmount = previous.get(statIndex);
                if (previousAmount != null && Math.abs(previousAmount - delta) <= RolledItemConstants.STATS_DELTA_EPSILON) {
                    continue;
                }
            }

            putModifier(statMap, statIndex, delta);
        }
    }

    private void putModifier(@NonNull EntityStatMap statMap, int statIndex, double amount) {
        StaticModifier.CalculationType calcType = calculationTypeByStat.getOrDefault(statIndex, StaticModifier.CalculationType.ADDITIVE);
        String key = calcType.createKey(MODIFIER_NAMESPACE + "_" + statIndex);
        StaticModifier modifier = new StaticModifier(Modifier.ModifierTarget.MAX, calcType, (float) amount);
        statMap.putModifier(statIndex, key, modifier);
    }

    private record EquipmentScan(long fingerprint, boolean hasRolledMetadata) {}
}
