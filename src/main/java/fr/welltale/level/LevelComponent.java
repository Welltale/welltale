package fr.welltale.level;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

@Getter
public class LevelComponent implements Component<EntityStore> {
    private static ComponentType<EntityStore, LevelComponent> TYPE;

    public static void setComponentType(ComponentType<EntityStore, LevelComponent> type) {
        TYPE = type;
    }

    public static ComponentType<EntityStore, LevelComponent> getComponentType() {
        return TYPE;
    }

    public static final BuilderCodec<LevelComponent> CODEC = BuilderCodec
            .builder(LevelComponent.class, LevelComponent::new)
            .append(
                    new KeyedCodec<>("TotalExperience", BuilderCodec.LONG),
                    (component, value) -> component.totalExperience = value,
                    component -> component.totalExperience
            ).add()
            .build();

    private long totalExperience = 0;

    public LevelComponent() {}

    public LevelComponent(long totalExperience) {
        this.totalExperience = Math.max(0L, totalExperience);
    }

    public void setTotalExperience(long XP) {
        this.totalExperience = Math.max(0L, XP);
    }

    public int getLevel() {
        return XPTable.getLevelForXP(totalExperience);
    }

    public long getCurrentLevelExp() {
        return XPTable.getXPInCurrentLevel(totalExperience);
    }

    public long getXPToNextLevel() {
        return XPTable.getXPToNextLevel(totalExperience);
    }

    public float getProgress() {
        return XPTable.getProgressToNextLevel(totalExperience);
    }

    public boolean isMaxLevel() {
        return this.getLevel() >= XPTable.MAX_LEVEL;
    }

    public boolean addExperience(long amount) {
        if (amount <= 0) return false;

        int oldLevel = this.getLevel();
        totalExperience += amount;
        int newLevel = getLevel();

        return newLevel > oldLevel;
    }

    @Override
    public @Nullable Component<EntityStore> clone() {
        return new LevelComponent(this.totalExperience);
    }

    @Override
    public String toString() {
        return "LevelComponent{level =" + getLevel() +
                ", totalXP=" + this.totalExperience +
                ", toNext=" + getXPToNextLevel()  + "}";
    }
}
