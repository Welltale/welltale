package fr.chosmoz.player;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypixel.hytale.logger.HytaleLogger;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    public void addPlayer(@NonNull Player player) throws Exception {
        if (player.getUuid() == null) {
            throw ERR_INVALID_PLAYER;
        }

        for (Player cachedPlayer : this.cachedPlayers) {
            if (cachedPlayer.getUuid() != player.getUuid()) {
                continue;
            }

            throw ERR_PLAYER_ALREADY_EXISTS;
        }

        this.cachedPlayers.add(player);
    }

    @Override
    public @Nullable Player getPlayerByUuid(@NonNull UUID playerUuid) {
        return cachedPlayers.stream()
                .filter(p -> p.getUuid().equals(playerUuid))
                .findFirst()
                .orElse(null);
    }

    @Override
    public @Nullable Player getPlayerByUsername(@Nonnull String playerName) {
        return cachedPlayers.stream()
                .filter(p -> p.getPlayerName().equalsIgnoreCase(playerName))
                .findFirst()
                .orElse(null);
    }

    public List<Player> getCachedPlayers() {
        return this.cachedPlayers;
    }

    @Override
    public void updatePlayer(@NonNull Player player) throws Exception {
        if (player.getUuid() == null) {
            throw ERR_INVALID_PLAYER;
        }

        for (int i = 0; i < cachedPlayers.size(); i++) {
            if (!cachedPlayers.get(i).getUuid().equals(player.getUuid())) {
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
