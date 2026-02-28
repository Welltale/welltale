package fr.welltale.inventory.event;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.packets.window.ClientOpenWindow;
import com.hypixel.hytale.protocol.packets.window.CloseWindow;
import com.hypixel.hytale.protocol.packets.window.UpdateWindow;
import com.hypixel.hytale.protocol.packets.window.WindowType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.entity.entities.player.windows.Window;
import com.hypixel.hytale.server.core.io.handlers.game.GamePacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.characteristic.page.CharacteristicsPage;
import fr.welltale.inventory.CustomInventoryService;
import fr.welltale.inventory.page.InventoryPage;
import fr.welltale.player.charactercache.CharacterCacheRepository;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class PlayerReadyInventoryPacketInterceptor {
    private final CustomInventoryService customInventoryService;
    private final CharacterCacheRepository characterCacheRepository;
    private final HytaleLogger logger;
    private final ConcurrentHashMap<UUID, Boolean> installedPlayers = new ConcurrentHashMap<>();

    public PlayerReadyInventoryPacketInterceptor(
            @Nonnull CustomInventoryService customInventoryService,
            @Nonnull CharacterCacheRepository characterCacheRepository,
            @Nonnull HytaleLogger logger
    ) {
        this.customInventoryService = customInventoryService;
        this.characterCacheRepository = characterCacheRepository;
        this.logger = logger;
    }

    public void onPlayerReady(com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent event) {
        Player player = event.getPlayer();
        Ref<EntityStore> ref = player.getReference();
        if (ref == null || !ref.isValid()) return;

        Store<EntityStore> store = ref.getStore();
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) return;

        if (!(playerRef.getPacketHandler() instanceof GamePacketHandler packetHandler)) {
            this.logger.atSevere().log("[INVENTORY] PlayerReadyInventoryPacketInterceptor OnPlayerReady Failed: Cannot install packet interceptor: unexpected packet handler type");
            return;
        }

        if (installedPlayers.putIfAbsent(playerRef.getUuid(), true) != null) return;

        packetHandler.registerHandler(ClientOpenWindow.PACKET_ID, p -> this.handleClientOpenWindow((ClientOpenWindow) p, playerRef));
        packetHandler.registerHandler(CloseWindow.PACKET_ID, p -> this.handleCloseWindow((CloseWindow) p, playerRef));
    }

    private void handleClientOpenWindow(ClientOpenWindow packet, PlayerRef playerRef) {
        Ref<EntityStore> ref = playerRef.getReference();
        if (ref == null || !ref.isValid()) return;

        Store<EntityStore> store = ref.getStore();
        World world = store.getExternalData().getWorld();
        world.execute(() -> {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) return;

            if (packet.type == WindowType.PocketCrafting) {
                if (player.getGameMode() == GameMode.Creative) {
                    this.openVanillaWindow(packet, playerRef, ref, store, player);
                    return;
                }

                CustomUIPage currentPage = player.getPageManager().getCustomPage();
                if (currentPage instanceof InventoryPage || currentPage instanceof CharacteristicsPage) {
                    player.getPageManager().setPage(ref, store, com.hypixel.hytale.protocol.packets.interface_.Page.None);
                    return;
                }

                player.getPageManager().openCustomPage(
                        ref,
                        store,
                        new InventoryPage(playerRef, customInventoryService, characterCacheRepository, logger)
                );
                return;
            }

            this.openVanillaWindow(packet, playerRef, ref, store, player);
        });
    }

    private void openVanillaWindow(
            ClientOpenWindow packet,
            PlayerRef playerRef,
            Ref<EntityStore> ref,
            Store<EntityStore> store,
            Player player
    ) {
        Supplier<? extends Window> supplier = Window.CLIENT_REQUESTABLE_WINDOW_TYPES.get(packet.type);
        if (supplier == null) {
            this.logger.atSevere().log("[INVENTORY] PlayerReadyInventoryPacketInterceptor OpenVanillaWindow Failed: Unsupported window request: " + packet.type);
            return;
        }

        UpdateWindow updateWindowPacket = player.getWindowManager().clientOpenWindow(ref, supplier.get(), store);
        if (updateWindowPacket != null) {
            playerRef.getPacketHandler().writeNoCache(updateWindowPacket);
        }
    }

    private void handleCloseWindow(CloseWindow packet, PlayerRef playerRef) {
        Ref<EntityStore> ref = playerRef.getReference();
        if (ref == null || !ref.isValid()) return;

        Store<EntityStore> store = ref.getStore();
        World world = store.getExternalData().getWorld();
        world.execute(() -> {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) return;

            try {
                if (packet.id == 0 && player.getWindowManager().getWindow(0) == null) {
                    return;
                }

                player.getWindowManager().closeWindow(ref, packet.id, store);
            } catch (IllegalStateException e) {
                if (packet.id != 0) {
                    this.logger.atInfo().log("[INVENTORY] PlayerReadyIventoryPacketInterceptor HandleCloseWindow Failed: Ignored close window for non-open id" + packet.id);
                }
            }
        });
    }
}
