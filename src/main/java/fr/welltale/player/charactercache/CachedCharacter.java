package fr.welltale.player.charactercache;

import fr.welltale.characteristic.Characteristics;
import fr.welltale.inventory.StoredItemStack;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
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
    private List<StoredItemStack> hotbar;
    private List<StoredItemStack> storage;
    private List<StoredItemStack> armor;
    private List<StoredItemStack> loot;
    private List<StoredItemStack> equipment;
}
