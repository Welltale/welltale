package fr.chosmoz.rank;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JsonRankFileLoader {
    public File loadJsonRanksFile() throws IOException {
        File jsonDataFile = new File("./mods/chosmoz/ranks.json");
        File parentFile = jsonDataFile.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }

        if (!jsonDataFile.exists()) {
            ObjectMapper mapper = new ObjectMapper();

            Rank[] exampleRanks = new Rank[] {
                    new Rank(UUID.randomUUID(), "Example", "EX", "RED", List.of("permission.example"))
            };
            mapper.writeValue(jsonDataFile, exampleRanks);
        }
        return jsonDataFile;
    }

    public List<Rank> getJsonData(File jsonFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return new ArrayList<>(List.of(mapper.readValue(jsonFile, Rank[].class)));
    }
}
