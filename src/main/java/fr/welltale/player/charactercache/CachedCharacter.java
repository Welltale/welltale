package fr.welltale.player.charactercache;

import fr.welltale.characteristic.Characteristics;
import fr.welltale.player.Player;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CachedCharacter extends Player.Character {
    private UUID playerUuid;
    private long gems;

    public CachedCharacter(
            UUID playerUuid,
            UUID characterUuid,
            UUID classUuid,
            long experience,
            long gems,
            Characteristics.EditableCharacteristics editableCharacteristics,
            int characteristicPoints,
            UUID guildUuid
    ) {
        super(
                characterUuid,
                classUuid,
                experience,
                editableCharacteristics,
                characteristicPoints,
                guildUuid
        );
        this.playerUuid = playerUuid;
        this.gems = gems;
    }
}
