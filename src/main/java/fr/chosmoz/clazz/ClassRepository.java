package fr.chosmoz.clazz;

import java.util.List;
import java.util.UUID;

public interface ClassRepository {
    Exception ERR_CLASS_NOT_FOUND = new Exception("class not found");

    Class getClass(UUID classId) throws Exception;
    List<Class> getClasses() throws Exception;
}