package fr.welltale.mob;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor // <- Required for Jackson
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
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
        this.drops = drops;
    }

    public String getModelAsset() {
        return modelAsset;
    }

    public List<Drop> getDrops() {
        return drops;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Drop {
        private String itemId;
        private int minQuantity;
        private int maxQuantity;
        private float dropChance;

        public Drop() {}

        public Drop(String itemId, int minQuantity, int maxQuantity, float dropChance) {
            this.itemId = itemId;
            this.minQuantity = minQuantity;
            this.maxQuantity = maxQuantity;
            this.dropChance = dropChance;
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
    }
}
