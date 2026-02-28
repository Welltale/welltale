package fr.welltale.player.charactercache;

import fr.welltale.characteristic.Characteristics;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class CachedCharacter {
    private UUID playerUuid;
    private long gems;
    private UUID characterUuid;
    private UUID classUuid;
    private Characteristics.EditableCharacteristics editableCharacteristics;
    private int characteristicPoints;
    private UUID guildUuid;
}
