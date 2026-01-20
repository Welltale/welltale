package fr.chosmoz.player;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypixel.hytale.logger.HytaleLogger;
import lombok.AllArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class JsonPlayerRepository implements PlayerRepository {
    private List<Player> cachedPlayers;
    private File jsonFile;
    private HytaleLogger logger;

    @Override
    public void addPlayer(Player player) throws Exception {
        if (player == null || player.getPlayerUuid() == null) {
            throw ERR_INVALID_PLAYER;
        }

        for (Player cachedPlayer : this.cachedPlayers) {
            if (cachedPlayer.getPlayerUuid() != player.getPlayerUuid()) {
                continue;
            }

            throw ERR_PLAYER_ALREADY_EXISTS;
        }

        this.cachedPlayers.add(player);
    }

    @Override
    public Player getPlayerByUuid(UUID playerUuid) throws Exception {
        return cachedPlayers.stream()
                .filter(p -> p.getPlayerUuid().equals(playerUuid))
                .findFirst()
                .orElseThrow(() -> ERR_PLAYER_NOT_FOUND);
    }

    @Override
    public Player getPlayerByUsername(String playerName) throws Exception {
        return cachedPlayers.stream()
                .filter(p -> p.getPlayerName().equalsIgnoreCase(playerName))
                .findFirst()
                .orElseThrow(() -> ERR_PLAYER_NOT_FOUND);
    }

    public List<Player> getCachedPlayers() throws Exception {
        return this.cachedPlayers;
    }

    @Override
    public void updatePlayer(Player player) throws Exception {
        if (player == null || player.getPlayerUuid() == null) {
            throw ERR_INVALID_PLAYER;
        }

        for (int i = 0; i < cachedPlayers.size(); i++) {
            if (!cachedPlayers.get(i).getPlayerUuid().equals(player.getPlayerUuid())) {
                continue;
            }

            cachedPlayers.set(i, player);
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
