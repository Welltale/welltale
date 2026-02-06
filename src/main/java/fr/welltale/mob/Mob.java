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
    private int criticalPct;
    private int criticalResistance;
    private int resistancePct;
}
