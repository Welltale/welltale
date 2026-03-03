package fr.welltale.item;

import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.DamageEntityInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat.DamageCalculator;
import com.hypixel.hytale.server.core.modules.interaction.interaction.operation.Operation;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import lombok.NonNull;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RolledItemBaseDamageCollector {
    private final ConcurrentHashMap<String, List<RolledTooltipDescriptionBuilder.DamageRange>> cacheByItemId = new ConcurrentHashMap<>();

    private final Field interactionDamageCalculatorField;
    private final Field interactionAngledDamageField;
    private final Field interactionTargetedDamageField;
    private final Field targetedDamageCalculatorField;
    private final Field calculatorBaseDamageRawField;
    private final boolean reflectionReady;

    public RolledItemBaseDamageCollector() {
        Field interactionDamageCalculator = null;
        Field interactionAngledDamage = null;
        Field interactionTargetedDamage = null;
        Field targetedDamageCalculator = null;
        Field calculatorBaseDamageRaw = null;
        boolean ready = false;

        try {
            interactionDamageCalculator = DamageEntityInteraction.class.getDeclaredField("damageCalculator");
            interactionDamageCalculator.setAccessible(true);

            interactionAngledDamage = DamageEntityInteraction.class.getDeclaredField("angledDamage");
            interactionAngledDamage.setAccessible(true);

            interactionTargetedDamage = DamageEntityInteraction.class.getDeclaredField("targetedDamage");
            interactionTargetedDamage.setAccessible(true);

            Class<?> targetedDamageClass = Class.forName(
                    "com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.DamageEntityInteraction$TargetedDamage"
            );
            targetedDamageCalculator = targetedDamageClass.getDeclaredField("damageCalculator");
            targetedDamageCalculator.setAccessible(true);

            calculatorBaseDamageRaw = DamageCalculator.class.getDeclaredField("baseDamageRaw");
            calculatorBaseDamageRaw.setAccessible(true);

            ready = true;
        } catch (Exception ignored) {
        }

        this.interactionDamageCalculatorField = interactionDamageCalculator;
        this.interactionAngledDamageField = interactionAngledDamage;
        this.interactionTargetedDamageField = interactionTargetedDamage;
        this.targetedDamageCalculatorField = targetedDamageCalculator;
        this.calculatorBaseDamageRawField = calculatorBaseDamageRaw;
        this.reflectionReady = ready;
    }

    public List<RolledTooltipDescriptionBuilder.DamageRange> getBaseDamageRanges(@NonNull String itemId) {
        if (itemId.isBlank()) return List.of();
        return cacheByItemId.computeIfAbsent(itemId, this::scanBaseDamageRanges);
    }

    private List<RolledTooltipDescriptionBuilder.DamageRange> scanBaseDamageRanges(@NonNull String itemId) {
        if (!reflectionReady) return List.of();

        Item item = Item.getAssetMap().getAsset(itemId);
        if (item == null) return List.of();

        LinkedHashSet<String> rootInteractionIds = new LinkedHashSet<>();

        Map<?, ?> interactions = item.getInteractions();
        if (interactions != null && !interactions.isEmpty()) {
            for (Object value : interactions.values()) {
                if (value instanceof String id && !id.isBlank()) rootInteractionIds.add(id);
            }
        }

        Map<String, String> interactionVars = item.getInteractionVars();
        if (interactionVars != null && !interactionVars.isEmpty()) {
            for (String value : interactionVars.values()) {
                if (value != null && !value.isBlank()) rootInteractionIds.add(value);
            }
        }

        if (rootInteractionIds.isEmpty()) return List.of();

        LinkedHashMap<String, RangeAccumulator> rangesByCause = new LinkedHashMap<>();
        for (String rootId : rootInteractionIds) {
            RootInteraction rootInteraction = RootInteraction.getAssetMap().getAsset(rootId);
            if (rootInteraction == null) continue;

            rootInteraction.build();
            int operationMax = rootInteraction.getOperationMax();
            for (int i = 0; i < operationMax; i++) {
                Operation operation = rootInteraction.getOperation(i);
                if (operation == null) continue;

                Operation inner = operation.getInnerOperation();
                if (!(inner instanceof DamageEntityInteraction damageEntityInteraction)) continue;

                collectFromDamageInteraction(damageEntityInteraction, rangesByCause);
            }
        }

        if (rangesByCause.isEmpty()) return List.of();

        ArrayList<RolledTooltipDescriptionBuilder.DamageRange> ranges = new ArrayList<>(rangesByCause.size());
        for (Map.Entry<String, RangeAccumulator> entry : rangesByCause.entrySet()) {
            String causeId = entry.getKey();
            RangeAccumulator range = entry.getValue();
            if (causeId == null || causeId.isBlank() || range == null || !range.hasValue) continue;
            ranges.add(new RolledTooltipDescriptionBuilder.DamageRange(causeId, range.min, range.max));
        }

        return ranges.isEmpty() ? List.of() : List.copyOf(ranges);
    }

    private void collectFromDamageInteraction(
            @NonNull DamageEntityInteraction interaction,
            @NonNull Map<String, RangeAccumulator> rangesByCause
    ) {
        collectFromCalculator(readField(interactionDamageCalculatorField, interaction), rangesByCause);

        Object angledDamage = readField(interactionAngledDamageField, interaction);
        if (angledDamage instanceof Object[] entries) {
            for (Object entry : entries) {
                collectFromCalculator(readField(targetedDamageCalculatorField, entry), rangesByCause);
            }
        }

        Object targetedDamage = readField(interactionTargetedDamageField, interaction);
        if (targetedDamage instanceof Map<?, ?> targetMap && !targetMap.isEmpty()) {
            for (Object value : targetMap.values()) {
                collectFromCalculator(readField(targetedDamageCalculatorField, value), rangesByCause);
            }
        }
    }

    private void collectFromCalculator(Object calculator, @NonNull Map<String, RangeAccumulator> rangesByCause) {
        if (calculator == null) return;

        Object raw = readField(calculatorBaseDamageRawField, calculator);
        if (!(raw instanceof Object2FloatMap<?> baseDamageRaw) || baseDamageRaw.isEmpty()) return;

        for (Object entryObject : baseDamageRaw.object2FloatEntrySet()) {
            if (!(entryObject instanceof Object2FloatMap.Entry<?> entry)) continue;

            Object key = entry.getKey();
            if (!(key instanceof String causeId) || causeId.isBlank()) continue;

            float amount = entry.getFloatValue();
            if (amount <= 0.0f) continue;

            RangeAccumulator accumulator = rangesByCause.computeIfAbsent(causeId, _ -> new RangeAccumulator());
            accumulator.accept(amount);
        }
    }

    private Object readField(Field field, Object target) {
        if (field == null || target == null) return null;

        try {
            return field.get(target);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static final class RangeAccumulator {
        private double min;
        private double max;
        private boolean hasValue;

        private void accept(double value) {
            if (!hasValue) {
                min = value;
                max = value;
                hasValue = true;
                return;
            }

            if (value < min) min = value;
            if (value > max) max = value;
        }
    }
}
