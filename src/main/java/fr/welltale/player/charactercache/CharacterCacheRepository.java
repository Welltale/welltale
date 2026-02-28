package fr.welltale.player.charactercache;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface CharacterCacheRepository {
    Exception ERR_CHARACTER_NOT_FOUND = new Exception("character not found");
    Exception ERR_INVALID_CHARACTER = new Exception("invalid character");
    Exception ERR_CHARACTER_ALREADY_EXISTS = new Exception("character already exists");

    void addCharacterCache(CachedCharacter character) throws Exception;
    @Nullable
    CachedCharacter getCharacterCache(UUID playerUuid);
    List<CachedCharacter> getCharacters();
    void updateCharacter(CachedCharacter CachedCharacter) throws Exception;
    void removeCharacter(UUID playerUuid) throws Exception;
}
