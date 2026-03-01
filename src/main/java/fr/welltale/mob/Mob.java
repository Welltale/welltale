package fr.welltale.mob;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor // <- Required for Jackson
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Mob {
    public static final long MIN_MOB_GEMS_DROP = 1;

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
    private long gemsMaxDrop;

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
            List<Drop> drops,
            long gemsMaxDrop
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
        this.gemsMaxDrop = gemsMaxDrop;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
        public record Drop(String itemId, int minQuantity, int maxQuantity, float dropChance) {
    }
}
