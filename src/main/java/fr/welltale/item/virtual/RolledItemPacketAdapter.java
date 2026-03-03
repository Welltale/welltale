package fr.welltale.item.virtual;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.protocol.packets.assets.UpdateItems;
import com.hypixel.hytale.protocol.packets.assets.UpdateTranslations;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChain;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChains;
import com.hypixel.hytale.protocol.packets.interface_.CustomPage;
import com.hypixel.hytale.protocol.packets.interface_.CustomUICommand;
import com.hypixel.hytale.protocol.packets.inventory.UpdatePlayerInventory;
import com.hypixel.hytale.protocol.packets.player.MouseInteraction;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PacketFilter;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.item.ItemStatRoller;
import fr.welltale.item.RolledItemConstants;
import lombok.NonNull;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RolledItemPacketAdapter {
    private static final JsonWriterSettings JSON_WRITER_SETTINGS = JsonWriterSettings.builder().outputMode(JsonMode.SHELL).build();

    private final HytaleLogger logger;
    private final RolledVirtualItemRegistry registry;
    private final ConcurrentHashMap<UUID, BoundedVirtualIdSet> sentVirtualIdsByPlayer = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, Boolean> creativeStateByPlayer = new ConcurrentHashMap<>();
    private final int maxSentIdsPerPlayer;

    private PacketFilter outboundFilter;
    private PacketFilter inboundFilter;

    public RolledItemPacketAdapter(@NonNull HytaleLogger logger, @NonNull RolledVirtualItemRegistry registry) {
        this.logger = logger;
        this.registry = registry;
        this.maxSentIdsPerPlayer = RolledItemConstants.VIRTUAL_SENT_IDS_MAX_ENTRIES_PER_PLAYER;
    }

    public void register() {
        outboundFilter = PacketAdapters.registerOutbound(this::handleOutboundPacket);
        inboundFilter = PacketAdapters.registerInbound(this::handleInboundPacket);
        this.logger.atInfo().log("[ITEM] RolledItemPacketAdapter registered");
    }

    public void deregister() {
        if (outboundFilter != null) {
            try {
                PacketAdapters.deregisterOutbound(outboundFilter);
            } catch (Exception e) {
                this.logger.atWarning().log("[ITEM] RolledItemPacketAdapter DeRegister Failed: " + e.getMessage());
            }
            outboundFilter = null;
        }

        if (inboundFilter != null) {
            try {
                PacketAdapters.deregisterInbound(inboundFilter);
            } catch (Exception e) {
                this.logger.atWarning().log("[ITEM] RolledItemPacketAdapter DeRegister Failed: " + e.getMessage());
            }
            inboundFilter = null;
        }
    }

    public void onPlayerLeave(@NonNull UUID playerUuid) {
        sentVirtualIdsByPlayer.remove(playerUuid);
        creativeStateByPlayer.remove(playerUuid);
    }

    public void onPlayerReady(@NonNull PlayerReadyEvent event) {
        Player player = event.getPlayer();
        Ref<EntityStore> ref = player.getReference();
        if (ref == null || !ref.isValid()) return;

        Store<EntityStore> store = ref.getStore();
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) return;

        setCreativeState(playerRef, player.getGameMode() == GameMode.Creative);
    }

    public void onGameModeChanged(@NonNull PlayerRef playerRef, @NonNull GameMode gameMode) {
        setCreativeState(playerRef, gameMode == GameMode.Creative);
    }

    private void handleOutboundPacket(@NonNull PlayerRef playerRef, @NonNull Packet packet) {
        try {
            if (packet instanceof UpdatePlayerInventory inventoryPacket) {
                if (isCreative(playerRef.getUuid())) return;

                Map<String, ItemBase> virtualItems = new LinkedHashMap<>();
                boolean anyRewritten = false;
                anyRewritten |= processSection(inventoryPacket.hotbar, virtualItems);
                anyRewritten |= processSection(inventoryPacket.utility, virtualItems);
                anyRewritten |= processSection(inventoryPacket.tools, virtualItems);
                anyRewritten |= processSection(inventoryPacket.armor, virtualItems);
                anyRewritten |= processSection(inventoryPacket.storage, virtualItems);
                anyRewritten |= processSection(inventoryPacket.backpack, virtualItems);
                anyRewritten |= processSection(inventoryPacket.builderMaterial, virtualItems);
                if (anyRewritten) sendUnsentVirtualDefinitions(playerRef, virtualItems);
            }

            if (packet instanceof CustomPage customPage) {
                if (isCreative(playerRef.getUuid())) return;

                Map<String, ItemBase> virtualItems = new LinkedHashMap<>();
                boolean changed = processCustomPage(customPage, virtualItems);
                if (changed) sendUnsentVirtualDefinitions(playerRef, virtualItems);
            }
        } catch (Exception e) {
            this.logger.atWarning().log("[ITEM] RolledItemPacketAdapter HandleOutboundPacket Failed: " + e.getMessage());
        }
    }

    private boolean isCreative(@NonNull UUID playerUuid) {
        return creativeStateByPlayer.getOrDefault(playerUuid, false);
    }

    private void setCreativeState(@NonNull PlayerRef playerRef, boolean creative) {
        UUID playerUuid = playerRef.getUuid();
        boolean previous = creativeStateByPlayer.getOrDefault(playerUuid, false);
        creativeStateByPlayer.put(playerUuid, creative);

        if (creative && !previous) removeSentVirtualDefinitions(playerRef, playerUuid);
    }

    private void removeSentVirtualDefinitions(@NonNull PlayerRef playerRef, @NonNull UUID playerUuid) {
        BoundedVirtualIdSet sentVirtualIds = sentVirtualIdsByPlayer.remove(playerUuid);
        if (sentVirtualIds == null) return;

        String[] removedVirtualIds = sentVirtualIds.snapshotAndClear();
        if (removedVirtualIds.length == 0) return;

        try {
            UpdateItems packet = new UpdateItems();
            packet.type = UpdateType.Remove;
            packet.items = Map.of();
            packet.removedItems = removedVirtualIds;
            packet.updateModels = false;
            packet.updateIcons = false;
            playerRef.getPacketHandler().writeNoCache(packet);
        } catch (Exception e) {
            this.logger.atWarning().log("[ITEM] RolledItemPacketAdapter RemoveSentVirtualDefinitions Failed: " + e.getMessage());
        }
    }

    private void handleInboundPacket(@NonNull PlayerRef playerRef, @NonNull Packet packet) {
        try {
            if (packet instanceof MouseInteraction mouseInteraction) {
                if (registry.isVirtualId(mouseInteraction.itemInHandId)) {
                    String baseId = registry.getBaseItemId(mouseInteraction.itemInHandId);
                    if (baseId != null) {
                        mouseInteraction.itemInHandId = baseId;
                    }
                }
            } else if (packet instanceof SyncInteractionChains chains) {
                if (chains.updates.length == 0) return;

                for (SyncInteractionChain chain : chains.updates) {
                    rewriteChain(chain);
                }
            }
        } catch (Exception e) {
            this.logger.atWarning().log("[ITEM] RolledItemPacketAdapter HandleInboundPacket Failed: " + e.getMessage());
        }
    }

    private boolean rewriteChain(SyncInteractionChain chain) {
        if (chain == null) return false;

        boolean changed = false;

        if (registry.isVirtualId(chain.itemInHandId)) {
            String base = registry.getBaseItemId(chain.itemInHandId);
            if (base != null) {
                chain.itemInHandId = base;
                changed = true;
            }
        }

        if (registry.isVirtualId(chain.utilityItemId)) {
            String base = registry.getBaseItemId(chain.utilityItemId);
            if (base != null) {
                chain.utilityItemId = base;
                changed = true;
            }
        }

        if (registry.isVirtualId(chain.toolsItemId)) {
            String base = registry.getBaseItemId(chain.toolsItemId);
            if (base != null) {
                chain.toolsItemId = base;
                changed = true;
            }
        }

        if (chain.newForks == null) return changed;
        for (SyncInteractionChain fork : chain.newForks) {
            changed |= rewriteChain(fork);
        }

        return changed;
    }

    private boolean processSection(InventorySection section, Map<String, ItemBase> virtualItems) {
        if (section == null || section.items == null || section.items.isEmpty()) return false;

        boolean changed = false;

        for (Map.Entry<Integer, ItemWithAllMetadata> entry : section.items.entrySet()) {
            ItemWithAllMetadata item = entry.getValue();
            if (item == null || item.itemId.isBlank()) continue;
            if (registry.isVirtualId(item.itemId)) continue;
            if (item.metadata == null || item.metadata.isBlank()) continue;
            if (ItemStatRoller.mayContainRollMetadata(item.metadata)) continue;

            BsonDocument metadata = tryParseMetadata(item.metadata);
            if (metadata == null || !metadata.containsKey(ItemStatRoller.ROLL_METADATA_KEY)) continue;

            String virtualId = registry.createVirtualId(item.itemId, metadata);
            if (virtualId == null) continue;

            ItemBase virtualBase = registry.getOrCreate(item.itemId, virtualId, metadata);
            if (virtualBase == null) continue;

            ItemWithAllMetadata clone = item.clone();
            clone.itemId = virtualId;
            section.items.put(entry.getKey(), clone);
            virtualItems.put(virtualId, virtualBase);
            changed = true;
        }

        return changed;
    }

    private Map<String, ItemBase> filterUnsentVirtualItems(@NonNull UUID playerUuid, @NonNull Map<String, ItemBase> virtualItems) {
        if (virtualItems.isEmpty()) return Map.of();

        BoundedVirtualIdSet sent = sentVirtualIdsByPlayer.computeIfAbsent(playerUuid, _ -> new BoundedVirtualIdSet(maxSentIdsPerPlayer));
        LinkedHashMap<String, ItemBase> unsent = new LinkedHashMap<>();
        for (Map.Entry<String, ItemBase> entry : virtualItems.entrySet()) {
            String virtualId = entry.getKey();
            ItemBase itemBase = entry.getValue();
            if (virtualId == null || itemBase == null) continue;
            if (sent.markSent(virtualId)) unsent.put(virtualId, itemBase);
        }

        return unsent;
    }

    private BsonDocument tryParseMetadata(String metadataJson) {
        if (metadataJson == null || metadataJson.isBlank() || ItemStatRoller.mayContainRollMetadata(metadataJson)) return null;

        try {
            return BsonDocument.parse(metadataJson);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void sendUnsentVirtualDefinitions(@NonNull PlayerRef playerRef, @NonNull Map<String, ItemBase> virtualItems) {
        if (virtualItems.isEmpty()) return;

        Map<String, ItemBase> unsentVirtualItems = filterUnsentVirtualItems(playerRef.getUuid(), virtualItems);
        if (unsentVirtualItems.isEmpty()) return;

        sendTooltipTranslations(playerRef, unsentVirtualItems.keySet());
        sendUpdateItems(playerRef, unsentVirtualItems);
    }

    private void sendTooltipTranslations(@NonNull PlayerRef playerRef, @NonNull Iterable<String> virtualIds) {
        Map<String, String> translations = registry.getTooltipTranslations(virtualIds);
        if (translations.isEmpty()) return;

        try {
            UpdateTranslations packet = new UpdateTranslations();
            packet.type = UpdateType.AddOrUpdate;
            packet.translations = translations;
            playerRef.getPacketHandler().writeNoCache(packet);
        } catch (Exception e) {
            this.logger.atWarning().log("[ITEM] Failed to send rolled tooltip translations: " + e.getMessage());
        }
    }

    private boolean processCustomPage(CustomPage customPage, Map<String, ItemBase> virtualItems) {
        if (customPage == null || customPage.commands == null || customPage.commands.length == 0) return false;

        boolean changed = false;
        for (CustomUICommand command : customPage.commands) {
            if (command == null || command.data == null || command.data.isBlank()) continue;

            String modified = processCustomCommandData(command.data, virtualItems);
            if (modified == null) continue;

            command.data = modified;
            changed = true;
        }

        return changed;
    }

    private String processCustomCommandData(@NonNull String data, @NonNull Map<String, ItemBase> virtualItems) {
        if (ItemStatRoller.mayContainRollMetadata(data)) return null;

        BsonDocument root;
        try {
            root = BsonDocument.parse(data);
        } catch (Exception ignored) {
            return null;
        }

        BsonValue rootValue = root.get("0");
        if (rootValue == null || !rootValue.isArray()) return null;

        BsonArray slots = rootValue.asArray();
        boolean changed = false;
        for (BsonValue slotValue : slots) {
            if (!slotValue.isDocument()) continue;

            BsonDocument slotDoc = slotValue.asDocument();
            BsonValue itemStackValue = slotDoc.get("ItemStack");
            if (itemStackValue == null || !itemStackValue.isDocument()) continue;

            BsonDocument itemStackDoc = itemStackValue.asDocument();
            String idKey = itemStackDoc.containsKey("Id") ? "Id" : (itemStackDoc.containsKey("ItemId") ? "ItemId" : null);
            if (idKey == null) continue;

            BsonValue idValue = itemStackDoc.get(idKey);
            if (idValue == null || !idValue.isString()) continue;

            String baseItemId = idValue.asString().getValue();
            if (baseItemId == null || baseItemId.isBlank() || registry.isVirtualId(baseItemId)) continue;

            BsonValue metadataValue = itemStackDoc.get("Metadata");
            if (metadataValue == null || !metadataValue.isDocument()) continue;

            BsonDocument metadata = metadataValue.asDocument();
            if (!metadata.containsKey(ItemStatRoller.ROLL_METADATA_KEY)) continue;

            String virtualId = registry.createVirtualId(baseItemId, metadata);
            if (virtualId == null) continue;

            ItemBase virtualBase = registry.getOrCreate(baseItemId, virtualId, metadata);
            if (virtualBase == null) continue;

            itemStackDoc.put(idKey, new BsonString(virtualId));
            itemStackDoc.remove("Metadata");
            virtualItems.put(virtualId, virtualBase);
            changed = true;
        }

        return changed ? root.toJson(JSON_WRITER_SETTINGS) : null;
    }

    private void sendUpdateItems(PlayerRef playerRef, Map<String, ItemBase> virtualItems) {
        try {
            UpdateItems packet = new UpdateItems();
            packet.type = UpdateType.AddOrUpdate;
            packet.items = virtualItems;
            packet.removedItems = new String[0];
            packet.updateModels = false;
            packet.updateIcons = false;
            playerRef.getPacketHandler().writeNoCache(packet);
        } catch (Exception e) {
            this.logger.atWarning().log("[ITEM] Failed to send rolled virtual items: " + e.getMessage());
        }
    }

    private static final class BoundedVirtualIdSet {
        private final int maxEntries;
        private final LinkedHashMap<String, Boolean> accessOrderedIds = new LinkedHashMap<>(256, 0.75f, true);

        private BoundedVirtualIdSet(int maxEntries) {
            this.maxEntries = Math.max(1, maxEntries);
        }

        private synchronized boolean markSent(@NonNull String virtualId) {
            boolean isNew = !accessOrderedIds.containsKey(virtualId);
            accessOrderedIds.put(virtualId, Boolean.TRUE);
            trimToMax();
            return isNew;
        }

        private synchronized String[] snapshotAndClear() {
            if (accessOrderedIds.isEmpty()) return new String[0];
            String[] snapshot = accessOrderedIds.keySet().toArray(new String[0]);
            accessOrderedIds.clear();
            return snapshot;
        }

        private void trimToMax() {
            while (accessOrderedIds.size() > maxEntries) {
                Iterator<String> iterator = accessOrderedIds.keySet().iterator();
                if (!iterator.hasNext()) break;
                iterator.next();
                iterator.remove();
            }
        }
    }
}
