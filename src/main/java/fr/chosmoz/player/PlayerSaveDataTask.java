package fr.chosmoz.player;

import com.hypixel.hytale.server.core.HytaleServer;
import lombok.AllArgsConstructor;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class PlayerSaveDataTask {
    private PlayerRepository playerRepository;

    public ScheduledFuture<Void> run() {
        return HytaleServer.SCHEDULED_EXECUTOR.schedule(
                () -> {
                    this.playerRepository.saveData();
                    return null;
                },
                5, TimeUnit.SECONDS
        );
    }
}
