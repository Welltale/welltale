package fr.welltale.spell;

import com.hypixel.hytale.server.core.HytaleServer;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SpellCooldownScheduler {
    public static final float COOLDOWN_DELAY = 1;
    public static final TimeUnit COOLDOWN_DELAY_UNIT = TimeUnit.SECONDS;

    @SuppressWarnings("unchecked")
    public ScheduledFuture<Void> run(SpellManager spellManager) {
        return (ScheduledFuture<Void>) HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(
                spellManager::tickCooldown,
            (long) COOLDOWN_DELAY, (long) COOLDOWN_DELAY, COOLDOWN_DELAY_UNIT);
    }
}
