package fr.welltale.clazz;

import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface ClassRepository {
    @Nullable Class getClassConfig(@NonNull UUID classId);
    List<Class> getClassesConfigs();
}
