package fr.welltale.clazz;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface ClassRepository {
    @Nullable Class getClassConfig(@Nonnull UUID classId);
    List<Class> getClassesConfigs();
}