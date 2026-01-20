package fr.chosmoz.clazz;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class JsonClassRepository implements ClassRepository {
    private List<Class> cachedClasses;

    @Override
    public Class getClass(UUID classId) throws Exception {
        for (Class cls : this.cachedClasses) {
            if (!cls.getUuid().equals(classId)) {
                continue;
            }

            return cls;
        }

        throw ERR_CLASS_NOT_FOUND;
    }

    @Override
    public List<Class> getClasses() throws Exception {
        return this.cachedClasses;
    }
}
