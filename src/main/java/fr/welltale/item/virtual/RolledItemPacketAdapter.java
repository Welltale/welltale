package fr.welltale.item.virtual;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InventorySection;
import com.hypixel.hytale.protocol.ItemBase;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateItems;
import com.hypixel.hytale.protocol.packets.interface_.CustomPage;
import com.hypixel.hytale.protocol.packets.interface_.CustomUICommand;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChain;
import com.hypixel.hytale.protocol.packets.interaction.SyncInteractionChains;
import com.hypixel.hytale.protocol.packets.inventory.UpdatePlayerInventory;
import com.hypixel.hytale.protocol.packets.player.MouseInteraction;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PacketFilter;
import com.hypixel.hytale.server.core.io.adapter.PlayerPacketWatcher;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import fr.welltale.item.ItemStatRoller;
import lombok.NonNull;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RolledItemPacketAdapter {
    private static final boolean DO_NOT_BLOCK_PACKET = false;
    private static final JsonWriterSettings JSON_WRITER_SETTINGS = JsonWriterSettings.builder().outputMode(JsonMode.SHELL).build();

    private final HytaleLogger logger;
    private final RolledVirtualItemRegistry registry;
    private final ConcurrentHashMap<UUID, Set<String>> sentVirtualIdsByPlayer = new ConcurrentHashMap<>();

    private PacketFilter outboundFilter;
    private PacketFilter inboundFilter;

    public RolledItemPacketAdapter(@NonNull HytaleLogger logger, @NonNull RolledVirtualItemRegistry registry) {
        this.logger = logger;
        this.registry = registry;
    }

    public void register() {
        outboundFilter = PacketAdapters.registerOutbound(this::onOutboundPacket);
        inboundFilter = PacketAdapters.registerInbound((PlayerPacketWatcher) this::handleInboundPacket);
        this.logger.atInfo().log("[ITEM] RolledItemPacketAdapter registered");
    }

    public void deregister() {
        if (outboundFilter != null) {
            try {
                PacketAdapters.deregisterOutbound(outboundFilter);
            } catch (Exception e) {
                this.logger.atWarning().log("[ITEM] Failed to deregister outbound rolled-item filter: " + e.getMessage());
            }
            outboundFilter = null;
        }

        if (inboundFilter != null) {
            try {
                PacketAdapters.deregisterInbound(inboundFilter);
            } catch (Exception e) {
                this.logger.atWarning().log("[ITEM] Failed to deregister inbound rolled-item filter: " + e.getMessage());
            }
            inboundFilter = null;
        }
    }

    public void onPlayerLeave(@NonNull UUID playerUuid) {
        sentVirtualIdsByPlayer.remove(playerUuid);
    }

    private boolean onOutboundPacket(@NonNull PlayerRef playerRef, @NonNull Packet packet) {
        try {
            if (packet instanceof UpdatePlayerInventory inventoryPacket) {
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
                Map<String, ItemBase> virtualItems = new LinkedHashMap<>();
                boolean changed = processCustomPage(customPage, virtualItems);
                if (changed) sendUnsentVirtualDefinitions(playerRef, virtualItems);
            }
        } catch (Exception e) {
            this.logger.atWarning().log("[ITEM] Rolled outbound packet adaptation failed: " + e.getMessage());
        }

        return DO_NOT_BLOCK_PACKET;
    }

    private void handleInboundPacket(@NonNull PlayerRef playerRef, @NonNull Packet packet) {
        try {
            boolean changed = false;
            if (packet instanceof MouseInteraction mouseInteraction) {
                if (registry.isVirtualId(mouseInteraction.itemInHandId)) {
                    String baseId = registry.getBaseItemId(mouseInteraction.itemInHandId);
                    if (baseId != null) {
                        mouseInteraction.itemInHandId = baseId;
                        changed = true;
                    }
                }
            } else if (packet instanceof SyncInteractionChains chains) {
                if (chains.updates.length == 0) return;

                for (SyncInteractionChain chain : chains.updates) {
                    changed |= rewriteChain(chain);
                }
            }

            if (changed) {
                this.logger.atFine().log("[ITEM] Rewrote inbound virtual item ids for player " + playerRef.getUuid());
            }
        } catch (Exception e) {
            this.logger.atWarning().log("[ITEM] Rolled inbound packet adaptation failed: " + e.getMessage());
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

        Set<String> sent = sentVirtualIdsByPlayer.computeIfAbsent(playerUuid, _ -> ConcurrentHashMap.newKeySet());
        LinkedHashMap<String, ItemBase> unsent = new LinkedHashMap<>();
        for (Map.Entry<String, ItemBase> entry : virtualItems.entrySet()) {
            String virtualId = entry.getKey();
            ItemBase itemBase = entry.getValue();
            if (virtualId == null || itemBase == null) continue;
            if (sent.add(virtualId)) unsent.put(virtualId, itemBase);
        }

        return unsent;
    }

    private BsonDocument tryParseMetadata(String metadataJson) {
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

        sendUpdateItems(playerRef, unsentVirtualItems);
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
            // On this first version we can safely resend AddOrUpdate definitions.
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
}
