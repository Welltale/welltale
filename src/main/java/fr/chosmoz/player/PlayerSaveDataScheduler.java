package fr.chosmoz.player;

import com.hypixel.hytale.server.core.HytaleServer;
import lombok.AllArgsConstructor;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class PlayerSaveDataScheduler {
    private PlayerRepository playerRepository;

    @SuppressWarnings("unchecked")
    public ScheduledFuture<Void> run() {
        return (ScheduledFuture<Void>) HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(
                () -> this.playerRepository.saveData(),
                5, 5, TimeUnit.MINUTES
        );
    }
}
