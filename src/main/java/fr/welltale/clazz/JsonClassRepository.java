package fr.welltale.clazz;

import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class JsonClassRepository implements ClassRepository {
    private ArrayList<Class> cachedClasses;

    @Override
    public @Nullable Class getClassConfig(@NonNull UUID classId) {
        for (Class cls : this.cachedClasses) {
            if (!cls.getUuid().equals(classId)) {
                continue;
            }

            return cls;
        }

        return null;
    }

    @Override
    public List<Class> getClassesConfigs() {
        return List.copyOf(this.cachedClasses);
    }
}
