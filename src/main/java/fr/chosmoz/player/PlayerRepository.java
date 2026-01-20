package fr.chosmoz.player;

import java.util.List;
import java.util.UUID;

public interface PlayerRepository {
    Exception ERR_PLAYER_NOT_FOUND = new Exception("player not found");
    Exception ERR_INVALID_PLAYER = new Exception("invalid player");
    Exception ERR_PLAYER_ALREADY_EXISTS = new Exception("player already exists");

    void addPlayer(Player player) throws Exception;

    Player getPlayerByUuid(UUID playerUuid) throws Exception;

    Player getPlayerByUsername(String playerName) throws Exception;

    List<Player> getCachedPlayers() throws Exception;

    void updatePlayer(Player player) throws Exception;

    void saveData();
}