package fr.welltale.player.charactercache;

import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class MemoryCharacterCache implements CharacterCacheRepository {
    private ArrayList<CachedCharacter> cachedCharacters = new ArrayList<>();

    @Override
    public void addCharacterCache(CachedCharacter character) throws Exception {
        if (character.getPlayerUuid() == null) {
            throw ERR_INVALID_CHARACTER;
        }

        CachedCharacter cachedCharacter =  this.cachedCharacters.stream()
                .filter(c -> c.getPlayerUuid().equals(character.getPlayerUuid()))
                .findFirst()
                .orElse(null);

        if (cachedCharacter != null) {
            throw ERR_CHARACTER_ALREADY_EXISTS;
        }

        this.cachedCharacters.add(character);
    }

    @Override
    public @Nullable CachedCharacter getCharacterCache(UUID playerUuid) {
        return this.cachedCharacters.stream()
                .filter(c -> c.getPlayerUuid().equals(playerUuid))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<CachedCharacter> getCharacters() {
        return List.copyOf(this.cachedCharacters);
    }

    @Override
    public void updateCharacter(CachedCharacter character) throws Exception {
        if (character.getPlayerUuid() == null) {
            throw ERR_INVALID_CHARACTER;
        }

        for (int i = 0; i < this.cachedCharacters.size(); i++) {
            if (!this.cachedCharacters.get(i).getPlayerUuid().equals(character.getPlayerUuid())) {
                continue;
            }

            this.cachedCharacters.set(i, character);
            return;
        }

        throw ERR_CHARACTER_NOT_FOUND;
    }

    @Override
    public void removeCharacter(UUID playerUuid) throws Exception {
        if (playerUuid == null) {
            throw ERR_INVALID_CHARACTER;
        }

        for (int i = 0; i < this.cachedCharacters.size(); i++) {
            if (!this.cachedCharacters.get(i).getPlayerUuid().equals(playerUuid)) {
                continue;
            }

            this.cachedCharacters.remove(i);
            return;
        }

        throw ERR_CHARACTER_NOT_FOUND;
    }
}
