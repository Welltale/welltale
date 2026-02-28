package fr.welltale.player;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class JsonPlayerFileLoader {
    public File loadJsonPlayersFile() throws IOException {
        File jsonDataFile = new File("./mods/welltale/players.json");

        File parentFile = jsonDataFile.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }

        if (!jsonDataFile.exists()) {
            ObjectMapper mapper = new ObjectMapper();

            Player[] examplePlayer = new Player[] {};
            mapper.writeValue(jsonDataFile, examplePlayer);
        }
        return jsonDataFile;
    }

    public ArrayList<Player> getJsonData(File jsonFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return new ArrayList<>(List.of(mapper.readValue(jsonFile, Player[].class)));
    }
}
