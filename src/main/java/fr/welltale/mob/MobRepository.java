package fr.welltale.mob;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface MobRepository {
    @Nullable Mob getMob(@NonNull String modelAsset);
    @Nullable List<Mob> getMobs();
}
