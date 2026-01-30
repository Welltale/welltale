package fr.welltale.util;

import javax.annotation.Nonnull;
import java.util.List;

public class Permission {
    public static boolean hasPermission(@Nonnull List<String> rankPermissions, @Nonnull String permission) {
        return rankPermissions.contains(permission);
    }

    public static boolean hasPermissions(@Nonnull List<String> rankPermissions, @Nonnull List<String> permissions) {
        for (String p : rankPermissions) {
            if (!permissions.contains(p)) {
                continue;
            }

            return true;
        }

        return false;
    }
}
