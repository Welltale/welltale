package fr.chosmoz.clazz;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JsonClassFileLoader {
    public File loadJsonClassesFile() throws IOException {
        File jsonDataFile = new File("./mods/chosmoz/classes.json");

        File parentFile = jsonDataFile.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }

        if (!jsonDataFile.exists()) {
            ObjectMapper mapper = new ObjectMapper();

            Class[] exampleClass = new Class[] {
                    new Class(
                            UUID.randomUUID(),
                            "Example",
                            "GOLD",
                            ""
                            )
            };
            mapper.writeValue(jsonDataFile, exampleClass);
        }
        return jsonDataFile;
    }

    public List<Class> getJsonData(File jsonFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return new ArrayList<>(List.of(mapper.readValue(jsonFile, Class[].class)));
    }
}
