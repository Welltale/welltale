package fr.welltale.player;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypixel.hytale.logger.HytaleLogger;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class JsonPlayerRepository implements PlayerRepository {
    private ArrayList<Player> cachedPlayers;
    private File jsonFile;
    private HytaleLogger logger;

    @Override
    public void addPlayer(@NonNull Player player) throws Exception {
        if (player.getUuid() == null) {
            throw ERR_INVALID_PLAYER;
        }

        Player cachedPlayer = this.cachedPlayers.stream()
                .filter(p -> p.getUuid().equals(player.getUuid()))
                .findFirst()
                .orElse(null);

        if (cachedPlayer != null) {
            throw ERR_PLAYER_ALREADY_EXISTS;
        }

        this.cachedPlayers.add(player);
    }

    @Override
    public @Nullable Player getPlayer(@NonNull UUID playerUuid) {
        return this.cachedPlayers.stream()
                .filter(p -> p.getUuid().equals(playerUuid))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Player> getPlayers() {
        return List.copyOf(this.cachedPlayers);
    }

    @Override
    public void updatePlayer(@NonNull Player player) throws Exception {
        if (player.getUuid() == null) {
            throw ERR_INVALID_PLAYER;
        }

        for (int i = 0; i < this.cachedPlayers.size(); i++) {
            if (!this.cachedPlayers.get(i).getUuid().equals(player.getUuid())) {
                continue;
            }

            this.cachedPlayers.set(i, player);
            return;
        }

        throw ERR_PLAYER_NOT_FOUND;
    }

    @Override
    public void saveData() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(this.jsonFile, this.cachedPlayers);

            this.logger.atInfo().log("Players saved!");
        } catch (IOException e) {
            this.logger.atSevere().log("Failed to save data into JSON file: " + e.getMessage());
        }
    }
}
