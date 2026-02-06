package fr.welltale.mob;

import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

@AllArgsConstructor
public class JsonMobRepository implements MobRepository{
    private List<Mob> cachedMobs;

    @Override
    public @Nullable Mob getMob(@NonNull String modelAsset) {
        for (Mob cachedMob : this.cachedMobs) {
            if (!cachedMob.getModelAsset().equals(modelAsset)) continue;

            return cachedMob;
        }

        return null;
    }

    @Override
    public @Nullable List<Mob> getMobs() {
        return this.cachedMobs;
    }
}
