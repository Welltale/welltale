package fr.welltale.mob;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonMobFileLoader {
    public File loadJsonMobsFile() throws IOException {
        File jsonDataFile = new File("./mods/welltale/mobs.json");
        File parentFile = jsonDataFile.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }

        if (!jsonDataFile.exists()) {
            ObjectMapper mapper = new ObjectMapper();

            Mob[] exampleMobs = new Mob[]{
                    new Mob(
                            "Zombie",
                            1,
                            10,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0,
                            1,
                            3,
                            List.of(
                                    new Mob.Drop("hytale:dirt", 1, 3, 0.90f, 10),
                                    new Mob.Drop("hytale:stick", 1, 2, 0.60f, 8),
                                    new Mob.Drop("hytale:coal", 1, 1, 0.20f, 3)
                            )
                    )
            };
            mapper.writeValue(jsonDataFile, exampleMobs);
        }
        return jsonDataFile;
    }

    public List<Mob> getJsonData(File jsonFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return new ArrayList<>(List.of(mapper.readValue(jsonFile, Mob[].class)));
    }
}
