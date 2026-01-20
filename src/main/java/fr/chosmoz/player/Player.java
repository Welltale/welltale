package fr.chosmoz.player;

import fr.chosmoz.clazz.Class;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor // <- Required for Jackson
@Getter
public class Player {
    private UUID playerUuid;
    private String playerName;
    private Class clazz;

    @Setter
    private int level;

    @Setter
    private float experience;

    @Setter
    private UUID rankUuid;
}