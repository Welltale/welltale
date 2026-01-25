package fr.chosmoz.clazz;

import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class JsonClassRepository implements ClassRepository {
    private List<Class> cachedClasses;

    @Override
    public @Nullable Class getClass(@NonNull UUID classId) {
        for (Class cls : this.cachedClasses) {
            if (!cls.getUuid().equals(classId)) {
                continue;
            }

            return cls;
        }

        return null;
    }

    @Override
    public List<Class> getClasses() {
        return this.cachedClasses;
    }
}
