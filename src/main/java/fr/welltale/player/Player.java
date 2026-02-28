package fr.welltale.player;

import fr.welltale.characteristic.Characteristics;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor // <- Required for Jackson
@Getter
public class Player {
    private UUID uuid;

    @Setter
    private UUID rankUuid;

    @Setter
    @Getter
    private List<Character> characters;

    @Getter
    @Setter
    private List<UUID> friendsUuids;

    @Getter
    @Setter
    private long gems;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Character {
        private UUID characterUuid;
        private UUID classUuid;
        private long experience;
        private Characteristics.EditableCharacteristics editableCharacteristics;
        private int characteristicPoints;
        private UUID guildUuid;
        //TODO ADD CHARACTER STORAGE
    }
}

