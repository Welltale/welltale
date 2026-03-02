package fr.welltale.util;

import lombok.NonNull;

import java.util.List;

public class Permission {
    public static boolean hasPermission(@NonNull List<String> rankPermissions, @NonNull String permission) {
        return rankPermissions.contains(permission);
    }

    public static boolean hasPermissions(@NonNull List<String> rankPermissions, @NonNull List<String> permissions) {
        for (String p : rankPermissions) {
            if (!permissions.contains(p)) {
                continue;
            }

            return true;
        }

        return false;
    }
}
