package fr.welltale.mob;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface MobRepository {
    @Nullable Mob getMobConfig(@NonNull String modelAsset);
    @Nullable List<Mob> getMobsConfig();
}
