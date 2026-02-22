package fr.welltale.mob;

import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

@AllArgsConstructor
public class JsonMobRepository implements MobRepository{
    private List<Mob> cachedMobs;

    @Override
    public @Nullable Mob getMobConfig(@NonNull String modelAsset) {
        String normalizedModelAsset = normalizeModelAsset(modelAsset);

        for (Mob cachedMob : this.cachedMobs) {
            if (cachedMob == null || cachedMob.getModelAsset() == null) continue;

            String cachedModelAsset = cachedMob.getModelAsset();
            if (cachedModelAsset.equalsIgnoreCase(modelAsset)) {
                return cachedMob;
            }

            String normalizedCachedAsset = normalizeModelAsset(cachedModelAsset);
            if (normalizedCachedAsset.equals(normalizedModelAsset)
                    || normalizedModelAsset.contains(normalizedCachedAsset)
                    || normalizedCachedAsset.contains(normalizedModelAsset)) {
                return cachedMob;
            }
        }

        return null;
    }

    @Override
    public @Nullable List<Mob> getMobsConfig() {
        return this.cachedMobs;
    }

    private String normalizeModelAsset(@NonNull String modelAssetId) {
        String normalized = modelAssetId.toLowerCase();
        int slashIndex = Math.max(normalized.lastIndexOf('/'), normalized.lastIndexOf('\\'));
        if (slashIndex >= 0 && slashIndex < normalized.length() - 1) {
            normalized = normalized.substring(slashIndex + 1);
        }

        int dotIndex = normalized.lastIndexOf('.');
        if (dotIndex > 0) {
            normalized = normalized.substring(0, dotIndex);
        }

        int colonIndex = normalized.lastIndexOf(':');
        if (colonIndex >= 0 && colonIndex < normalized.length() - 1) {
            normalized = normalized.substring(colonIndex + 1);
        }

        return normalized;
    }
}
