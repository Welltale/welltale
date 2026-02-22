package fr.welltale.mob;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor // <- Required for Jackson
@Getter
public class Mob {
    private String modelAsset;
    private int level;
    private long baseXP;
    private int criticalDamage;
    private float criticalPct;
    private int criticalResistance;
    private float resistancePct;
    private float earthResistance;
    private float fireResistance;
    private float waterResistance;
    private float airResistance;
    private int lootRollsMin;
    private int lootRollsMax;
    private List<Drop> drops;

    public Mob(
            String modelAsset,
            int level,
            long baseXP,
            int criticalDamage,
            float criticalPct,
            int criticalResistance,
            float resistancePct,
            float earthResistance,
            float fireResistance,
            float waterResistance,
            float airResistance,
            int lootRollsMin,
            int lootRollsMax,
            List<Drop> drops
    ) {
        this.modelAsset = modelAsset;
        this.level = level;
        this.baseXP = baseXP;
        this.criticalDamage = criticalDamage;
        this.criticalPct = criticalPct;
        this.criticalResistance = criticalResistance;
        this.resistancePct = resistancePct;
        this.earthResistance = earthResistance;
        this.fireResistance = fireResistance;
        this.waterResistance = waterResistance;
        this.airResistance = airResistance;
        this.lootRollsMin = lootRollsMin;
        this.lootRollsMax = lootRollsMax;
        this.drops = drops;
    }

    public String getModelAsset() {
        return modelAsset;
    }

    public int getLootRollsMin() {
        return lootRollsMin;
    }

    public int getLootRollsMax() {
        return lootRollsMax;
    }

    public List<Drop> getDrops() {
        return drops;
    }

    public static class Drop {
        private String itemId;
        private int minQuantity;
        private int maxQuantity;
        private float dropChance;
        private int weight;

        public Drop() {}

        public Drop(String itemId, int minQuantity, int maxQuantity, float dropChance, int weight) {
            this.itemId = itemId;
            this.minQuantity = minQuantity;
            this.maxQuantity = maxQuantity;
            this.dropChance = dropChance;
            this.weight = weight;
        }

        public String getItemId() {
            return itemId;
        }

        public int getMinQuantity() {
            return minQuantity;
        }

        public int getMaxQuantity() {
            return maxQuantity;
        }

        public float getDropChance() {
            return dropChance;
        }

        public int getWeight() {
            return weight;
        }
    }
}
