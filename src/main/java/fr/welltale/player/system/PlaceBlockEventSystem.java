package fr.welltale.player.system;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.constant.Constant;
import fr.welltale.player.Player;
import fr.welltale.player.PlayerRepository;
import fr.welltale.rank.Rank;
import fr.welltale.rank.RankRepository;
import fr.welltale.util.Permission;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class PlaceBlockEventSystem extends EntityEventSystem<EntityStore, com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent> {
    private final PlayerRepository playerRepository;
    private final RankRepository rankRepository;
    private final HytaleLogger logger;

    public PlaceBlockEventSystem(PlayerRepository playerRepository, RankRepository rankRepository, HytaleLogger logger) {
        super(com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent.class);
        this.playerRepository = playerRepository;
        this.rankRepository = rankRepository;
        this.logger = logger;
    }

    @Override
    public void handle(
            int i,
            @NonNull ArchetypeChunk<EntityStore> archetypeChunk,
            @NonNull Store<EntityStore> store,
            @NonNull CommandBuffer<EntityStore> commandBuffer,
            com.hypixel.hytale.server.core.event.events.ecs.@NonNull PlaceBlockEvent event
    ) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(i);
        if (!ref.isValid()) {
            this.logger.atSevere()
                    .log("[PLAYER] BreakBlockEvent Handle Failed: Ref is not valid");
            return;
        }

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) {
            this.logger.atSevere()
                    .log("[PLAYER] BreakBlockEvent Handle Failed: PlayerRef is null");
            return;
        }

        Player playerData = this.playerRepository.getPlayerByUuid(playerRef.getUuid());
        if (playerData == null) {
            this.logger.atSevere()
                    .log("[PLAYER] PlaceBlockEvent Handle Failed: PlayerData is null");
            return;
        }

        if (playerData.getRankUuid() == null) {
            if (event.isCancelled()) {
                return;
            }

            event.setCancelled(true);
            return;
        }

        Rank rank = this.rankRepository.getRank(playerData.getRankUuid());
        if (rank == null) {
            this.logger.atSevere()
                    .log("[PLAYER] PlaceBlockEvent Handle Failed: PlayerData is null");
            return;
        }

        boolean ok = Permission.hasPermissions(rank.getPermissions(), List.of(Constant.Permission.PLACE_BLOCK, Constant.Permission.OPERATOR));
        if (!ok) {
            if (event.isCancelled()) {
                return;
            }

            event.setCancelled(true);
        }
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
