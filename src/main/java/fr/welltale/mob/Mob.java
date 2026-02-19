package fr.welltale.mob;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
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
    //TODO ADD DROPPABLE RESOURCES & GEMS
}
