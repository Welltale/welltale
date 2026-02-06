package fr.welltale.mob;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

@Getter
public class MobStatsComponent implements Component<EntityStore> {
    private int level = 0;
    private long baseXP = 0;
    private int criticalDamage = 0;
    private int criticalPct = 0;
    private int criticalResistance = 0;
    private int resistancePct = 0;

    public static void setComponentType(ComponentType<EntityStore, MobStatsComponent> type) {
        TYPE = type;
    }

    public static ComponentType<EntityStore, MobStatsComponent> getComponentType() {
        return TYPE;
    }

    private static ComponentType<EntityStore, MobStatsComponent> TYPE;

    public MobStatsComponent() {}

    public MobStatsComponent(int level, long baseXP, int criticalDamage, int criticalPct, int criticalResistance, int resistancePct) {
        this.level = level;
        this.baseXP = baseXP;
        this.criticalDamage = criticalDamage;
        this.criticalPct = criticalPct;
        this.criticalResistance = criticalResistance;
        this.resistancePct = resistancePct;
    }

    public static final BuilderCodec<MobStatsComponent> CODEC = BuilderCodec
            .builder(MobStatsComponent.class, MobStatsComponent::new)
            .append(
                    new KeyedCodec<>("Level", BuilderCodec.INTEGER),
                    (component, value) -> component.level = value,
                    component -> component.level
            ).add()
            .append(
                    new KeyedCodec<>("BaseXP", BuilderCodec.LONG),
                    (component, value) -> component.baseXP = value,
                    component -> component.baseXP
            ).add()
            .append(
                    new KeyedCodec<>("CriticalDamage", BuilderCodec.INTEGER),
                    (component, value) -> component.criticalDamage = value,
                    component -> component.criticalDamage
            ).add()
            .append(
                    new KeyedCodec<>("CriticalPct", BuilderCodec.INTEGER),
                    (component, value) -> component.criticalPct = value,
                    component -> component.criticalPct
            ).add()
            .append(
                    new KeyedCodec<>("CriticalResistance", BuilderCodec.INTEGER),
                    (component, value) -> component.criticalResistance = value,
                    component -> component.criticalResistance
            ).add()
            .append(
                    new KeyedCodec<>("ResistancePct", BuilderCodec.INTEGER),
                    (component, value) -> component.resistancePct = value,
                    component -> component.resistancePct
            ).add()
            .build();

    @Override
    public @Nullable Component<EntityStore> clone() {
        return new MobStatsComponent(
                level,
                baseXP,
                criticalDamage,
                criticalPct,
                criticalResistance,
                resistancePct
        );
    }

    @Override
    public String toString() {
        return "MobLevelComponent{baseXP=" + this.baseXP +
                ", level=" + this.level +
                ", criticalDamage=" + this.criticalDamage +
                ", criticalPct=" + this.criticalPct +
                ", criticalResistance=" + this.criticalResistance +
                ", resistancePct=" + this.resistancePct + "}";
    }
}
