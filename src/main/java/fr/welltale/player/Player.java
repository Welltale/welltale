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
    private UUID classUuid;

    @Setter
    private long experience;

    @Setter
    private UUID rankUuid;

    @Getter
    @Setter
    private Characteristics.EditableCharacteristics editableCharacteristics;

    @Getter
    @Setter
    private int characteristicPoints;

    @Getter
    @Setter
    private UUID guildUuid;

    @Getter
    @Setter
    private List<UUID> friendsUuids;

    @Getter
    @Setter
    private long gems;
}

