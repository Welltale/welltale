package fr.chosmoz.player;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
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
    private String playerName;

    @Setter
    private UUID classUuid;

    @Setter
    private int level = 1;

    @Setter
    private float experience;

    @Setter
    private UUID rankUuid;

    @Getter
    @Setter
    private UUID guildUuid;

    @Getter
    @Setter
    private List<UUID> friendsUuids;

    private SpawnPoint spawnPoint;
    private Characteristic characteristic = new Characteristic();

    @Getter
    @Setter
    public static class SpawnPoint {
        private Vector3f position;
        private Vector3d rotation;
    }

    @Getter
    @Setter
    public static class Characteristic {
        private int health = 10;
        private int mana = 10;
        private int damage = 10;
        private int critical = 0;
        private int resistance = 0;
        private int regeneration = 0;

        //TODO MAYBE REMOVE IT
        private float attackSpeed = 1.0f;
    }
}