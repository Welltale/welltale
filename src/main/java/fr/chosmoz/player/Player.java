package fr.chosmoz.player;

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

    @Setter
    private UUID clazzUuid;

    @Setter
    private int level;

    @Setter
    private float experience;

    @Setter
    private UUID rankUuid;
}