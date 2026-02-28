package fr.welltale.player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface PlayerRepository {
    Exception ERR_PLAYER_NOT_FOUND = new Exception("player not found");
    Exception ERR_INVALID_PLAYER = new Exception("invalid player");
    Exception ERR_PLAYER_ALREADY_EXISTS = new Exception("player already exists");

    void addPlayer(@Nonnull Player player) throws Exception;

    @Nullable Player getPlayer(@Nonnull UUID playerUuid);

    List<Player> getPlayers();

    void updatePlayer(@Nonnull Player player) throws Exception;

    void saveData();
}
