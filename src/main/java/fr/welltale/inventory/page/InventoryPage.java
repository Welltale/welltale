package fr.welltale.inventory.page;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.modules.item.ItemModule;
import com.hypixel.hytale.server.core.ui.ItemGridSlot;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.characteristic.page.CharacteristicsPage;
import fr.welltale.inventory.CharacterVanillaInventorySnapshot;
import fr.welltale.inventory.InventoryService;
import fr.welltale.inventory.StoredItemStack;
import fr.welltale.player.PlayerRepository;
import fr.welltale.player.charactercache.CachedCharacter;
import fr.welltale.player.charactercache.CharacterCacheRepository;
import org.jspecify.annotations.NonNull;

import java.util.*;

import static fr.welltale.inventory.page.InventoryAreaRules.*;

public class InventoryPage extends InteractiveCustomUIPage<InventoryPage.CustomInventoryEventData> {
    private static final int STORAGE_SLOT_COUNT = 36;
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int INVENTORY_SLOT_COUNT = HOTBAR_SLOT_COUNT + STORAGE_SLOT_COUNT;
    private static final int LOOT_SLOT_COUNT = 30;
    private static final String ACTION_COLLECT_ALL = "COLLECT_ALL";
    private static final String ACTION_CLOSE = "CLOSE";
    private static final String ACTION_TRANSFER_DROP = "TRANSFER_DROP";
    private static final String ACTION_TRANSFER_DRAG_COMPLETED = "TRANSFER_DRAG_COMPLETED";
    private static final String ACTION_TRANSFER_RELEASE = "TRANSFER_RELEASE";
    private static final String ACTION_DRAG_HOVER = "DRAG_HOVER";
    private static final String ACTION_OPEN_CHARACTERISTICS = "OPEN_CHARACTERISTICS";


    private final InventoryService inventoryService;
    private final CharacterCacheRepository characterCacheRepository;
    private final PlayerRepository playerRepository;
    private final HytaleLogger logger;
    private final PlayerRef playerRef;
    private boolean closed;
    private String lastRawEventPayload;
    private String pendingDragArea;
    private int pendingDragSlot = -1;
    private long pendingDragAt;
    private String pendingHoverArea;
    private int pendingHoverSlot = -1;
    private long pendingHoverAt;

    public static class CustomInventoryEventData {
        public String action;
        public String area;
        public String index;
        public String slotIndex;
        public String inventorySlotIndex;
        public String itemStackId;
        public String fromSlotId;
        public String toSlotId;
        public String sourceSlotId;
        public String sourceItemGridIndex;
        public String targetSlotId;
        public String destinationSlotId;
        public String fromItemGridIndex;
        public String toItemGridIndex;

        public static final BuilderCodec<CustomInventoryEventData> CODEC =
                BuilderCodec.builder(CustomInventoryEventData.class, CustomInventoryEventData::new)
                        .append(
                                new KeyedCodec<>("Action", Codec.STRING),
                                (obj, val) -> obj.action = val,
                                obj -> obj.action
                        )
                        .add()
                        .append(
                                new KeyedCodec<>("Area", Codec.STRING),
                                (obj, val) -> obj.area = val,
                                obj -> obj.area
                        )
                        .add()
                        .append(
                                new KeyedCodec<>("ItemStackId", Codec.STRING),
                                (obj, val) -> obj.itemStackId = val,
                                obj -> obj.itemStackId
                        )
                        .add()
                        .build();
    }

    public InventoryPage(
            @NonNull PlayerRef playerRef,
            @NonNull InventoryService inventoryService,
            @NonNull CharacterCacheRepository characterCacheRepository, PlayerRepository playerRepository,
            @NonNull HytaleLogger logger
    ) {
        super(playerRef, CustomPageLifetime.CanDismiss, CustomInventoryEventData.CODEC);
        this.playerRef = playerRef;
        this.inventoryService = inventoryService;
        this.characterCacheRepository = characterCacheRepository;
        this.playerRepository = playerRepository;
        this.logger = logger;
    }

    @Override
    public void build(
            @NonNull Ref<EntityStore> ref,
            @NonNull UICommandBuilder cmd,
            @NonNull UIEventBuilder event,
            @NonNull Store<EntityStore> store
    ) {
        if (!ref.isValid()) return;

        cmd.append("Pages/Inventory/InventoryPage.ui");
        bindActivating(event, "#CollectAllButton", ACTION_COLLECT_ALL);
        bindActivating(event, "#CloseButton", ACTION_CLOSE);
        bindActivating(event, "#TabCharacteristics", ACTION_OPEN_CHARACTERISTICS);
        for (String[] binding : EQUIPMENT_GRID_BINDINGS) {
            bindItemGridEvents(event, binding[0], binding[1]);
        }
        bindItemGridEvents(event, "#HotbarGrid", AREA_HOTBAR);
        bindItemGridEvents(event, "#StorageGrid", AREA_STORAGE);
        bindItemGridEvents(event, "#LootGrid", AREA_LOOT);
        applyState(cmd, ref, store);
    }

    private void bindActivating(@NonNull UIEventBuilder event, @NonNull String selector, @NonNull String action) {
        event.addEventBinding(
                CustomUIEventBindingType.Activating,
                selector,
                new EventData().append("Action", action).append("Area", action),
                false
        );
    }

    private void bindItemGridEvents(@NonNull UIEventBuilder event, @NonNull String selector, @NonNull String area) {
        event.addEventBinding(
                CustomUIEventBindingType.Dropped,
                selector,
                new EventData().append("Action", ACTION_TRANSFER_DROP).append("Area", area),
                false
        );
        event.addEventBinding(
                CustomUIEventBindingType.SlotMouseDragCompleted,
                selector,
                new EventData().append("Action", ACTION_TRANSFER_DRAG_COMPLETED).append("Area", area),
                false
        );
        event.addEventBinding(
                CustomUIEventBindingType.SlotClickReleaseWhileDragging,
                selector,
                new EventData().append("Action", ACTION_TRANSFER_RELEASE).append("Area", area),
                false
        );
        event.addEventBinding(
                CustomUIEventBindingType.SlotMouseEntered,
                selector,
                new EventData().append("Action", ACTION_DRAG_HOVER).append("Area", area),
                false
        );
    }

    @Override
    public void handleDataEvent(
            @NonNull Ref<EntityStore> ref,
            @NonNull Store<EntityStore> store,
            @NonNull CustomInventoryEventData data
    ) {
        if (!ref.isValid()) return;

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        if (data.action == null) {
            safeSendUpdate(ref, store);
            return;
        }

        switch (data.action) {
            case ACTION_CLOSE -> closePage(ref, store, player);
            case ACTION_COLLECT_ALL -> collectAllLoot(ref, store, player);
            case ACTION_TRANSFER_DROP, ACTION_TRANSFER_DRAG_COMPLETED, ACTION_TRANSFER_RELEASE -> {
                boolean changed = handleTransfer(ref, store, data);
                if (changed) {
                    persistActiveCharacterInventory(ref, store);
                    safeSendUpdate(ref, store);
                }
            }
            case ACTION_DRAG_HOVER -> handleDragHover(data);
            case ACTION_OPEN_CHARACTERISTICS -> openCharacteristicsPage(ref, store, player);
            default -> safeSendUpdate(ref, store);
        }
    }

    private void closePage(@NonNull Ref<EntityStore> ref, @NonNull Store<EntityStore> store, @NonNull Player player) {
        this.closed = true;
        player.getPageManager().setPage(ref, store, Page.None);
    }

    private void openCharacteristicsPage(@NonNull Ref<EntityStore> ref, @NonNull Store<EntityStore> store, @NonNull Player player) {
        this.closed = true;
        player.getPageManager().openCustomPage(
                ref,
                store,
                new CharacteristicsPage(this.playerRef, this.inventoryService, this.characterCacheRepository, this.playerRepository, this.logger)
        );
    }

    private void collectAllLoot(@NonNull Ref<EntityStore> ref, @NonNull Store<EntityStore> store, @NonNull Player player) {
        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent == null) {
            this.logger.atSevere().log("[INVENTORY] InventoryPage CollectAllLoot Failed: Player UUID is null");
            return;
        }

        UUID playerUuid = uuidComponent.getUuid();
        ArrayList<ItemStack> loot = getLootSnapshot(playerUuid);
        if (loot.isEmpty()) {
            safeSendUpdate(ref, store);
            return;
        }

        Inventory inventory = player.getInventory();
        ArrayList<ItemStack> remaining = new ArrayList<>();

        for (ItemStack itemStack : loot) {
            ItemStackTransaction transaction = inventory.getCombinedHotbarFirst().addItemStack(itemStack);
            ItemStack remainder = transaction.getRemainder();
            if (!ItemStack.isEmpty(remainder)) {
                remaining.add(remainder);
            }
        }

        replaceLoot(playerUuid, remaining);
        persistActiveCharacterInventory(ref, store);
        safeSendUpdate(ref, store);
    }

    private void safeSendUpdate(@NonNull Ref<EntityStore> ref, @NonNull Store<EntityStore> store) {
        if (closed) {
            return;
        }

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        if (player.getPageManager().getCustomPage() != this) {
            return;
        }

        this.rebuild();
    }

    @Override
    public void handleDataEvent(@NonNull Ref<EntityStore> ref, @NonNull Store<EntityStore> store, String rawData) {
        this.lastRawEventPayload = rawData;
        super.handleDataEvent(ref, store, rawData);
    }

    @Override
    public void onDismiss(@NonNull Ref<EntityStore> ref, @NonNull Store<EntityStore> store) {
        this.closed = true;
        resetDragState();
    }

    private boolean handleTransfer(@NonNull Ref<EntityStore> ref,
                                   @NonNull Store<EntityStore> store,
                                   @NonNull CustomInventoryEventData data) {
        int sourceSlot = InventoryEventResolver.resolveSourceSlot(data, lastRawEventPayload);
        int targetSlot = InventoryEventResolver.resolveTargetSlot(data, lastRawEventPayload);
        String sourceItemId = data.itemStackId;

        if (data.area == null) {
            return false;
        }

        String resolvedSourceArea = sourceSlot >= 0
                ? resolveSourceAreaForTransfer(ref, store, data.area, sourceSlot, sourceItemId)
                : null;

        if (resolvedSourceArea != null && targetSlot >= 0) {
            boolean applied = tryApplyTransfer(ref, store, resolvedSourceArea, sourceSlot, data.area, targetSlot);
            if (applied) {
                resetDragState();
                return true;
            }
        }

        long now = System.currentTimeMillis();
        if (pendingDragArea != null && pendingDragSlot >= 0 && now - pendingDragAt <= 1500L) {
            int fallbackIndex = InventoryEventResolver.resolveIndex(data);
            int pendingTargetSlot = targetSlot >= 0 ? targetSlot : (fallbackIndex >= 0 ? fallbackIndex : sourceSlot);
            if (pendingTargetSlot >= 0 && !(pendingDragArea.equals(data.area) && pendingDragSlot == pendingTargetSlot)) {
                boolean applied = tryApplyTransfer(ref, store, pendingDragArea, pendingDragSlot, data.area, pendingTargetSlot);
                if (!applied) {
                    resetDragState();
                }
                return applied;
            }

            if (pendingHoverArea != null
                    && pendingHoverSlot >= 0
                    && now - pendingHoverAt <= 1200L
                    && !(pendingDragArea.equals(pendingHoverArea) && pendingDragSlot == pendingHoverSlot)) {
                boolean applied = tryApplyTransfer(ref, store, pendingDragArea, pendingDragSlot, pendingHoverArea, pendingHoverSlot);
                if (!applied) {
                    resetDragState();
                }
                return applied;
            }
        }

        if (sourceSlot < 0) {
            return false;
        }

        pendingDragArea = resolvedSourceArea != null ? resolvedSourceArea : data.area;
        pendingDragSlot = sourceSlot;
        pendingDragAt = now;
        return false;
    }

    private void handleDragHover(@NonNull CustomInventoryEventData data) {
        int hovered = InventoryEventResolver.resolveHoveredSlot(data, lastRawEventPayload);

        if (hovered < 0 || data.area == null) {
            return;
        }

        this.pendingHoverArea = data.area;
        this.pendingHoverSlot = hovered;
        this.pendingHoverAt = System.currentTimeMillis();
    }

    private boolean tryApplyTransfer(@NonNull Ref<EntityStore> ref,
                                   @NonNull Store<EntityStore> store,
                                    String fromArea,
                                    int fromSlot,
                                    String toArea,
                                  int toSlot) {
        if (fromArea == null || toArea == null || fromSlot < 0 || toSlot < 0) {
            return false;
        }

        if (fromArea.equals(toArea) && fromSlot == toSlot) {
            return false;
        }

        // Loot behavior is one-way: loot -> inventory only.
        if (AREA_LOOT.equals(toArea) && !AREA_LOOT.equals(fromArea)) {
            return false;
        }

        if (AREA_LOOT.equals(fromArea) && !isInventoryArea(toArea)) {
            return false;
        }

        if (AREA_LOOT.equals(fromArea) && isInventoryArea(toArea)) {
            boolean moved = transferLootToInventory(ref, store, fromSlot, toArea, toSlot);
            if (moved) {
                resetDragState();
            }
            return moved;
        }

        int fromSectionId = toInventorySectionId(fromArea);
        int toSectionId = toInventorySectionId(toArea);
        if (fromSectionId != Integer.MIN_VALUE && toSectionId != Integer.MIN_VALUE) {
            Player player = store.getComponent(ref, Player.getComponentType());
            if (player == null) {
                return false;
            }

            Inventory inventory = player.getInventory();
            int fromSlotId = toInventorySectionSlot(fromArea, fromSlot);
            int toSlotId = toInventorySectionSlot(toArea, toSlot);
            if (fromSlotId < 0 || toSlotId < 0) {
                return false;
            }

            var fromContainer = inventory.getSectionById(fromSectionId);
            var toContainer = inventory.getSectionById(toSectionId);
            if (fromContainer == null || toContainer == null) {
                return false;
            }

            if (fromSlotId >= fromContainer.getCapacity() || toSlotId >= toContainer.getCapacity()) {
                return false;
            }

            ItemStack source = fromContainer.getItemStack((short) fromSlotId);
            if (ItemStack.isEmpty(source)) {
                return false;
            }

            var moveTransaction = fromContainer.moveItemStackFromSlotToSlot(
                    (short) fromSlotId,
                    source.getQuantity(),
                    toContainer,
                    (short) toSlotId
            );
            if (!moveTransaction.succeeded()) {
                return false;
            }

            resetDragState();
            return true;
        }

        ItemStack picked = removeStackFromArea(ref, store, fromArea, fromSlot);
        if (ItemStack.isEmpty(picked)) {
            return false;
        }

        ItemStack target = getStackFromArea(ref, store, toArea, toSlot);
        if (ItemStack.isEmpty(target)) {
            if (!setStackToArea(ref, store, toArea, toSlot, picked)) {
                setStackToArea(ref, store, fromArea, fromSlot, picked);
                return false;
            }
        } else {
            if (!setStackToArea(ref, store, toArea, toSlot, picked)) {
                setStackToArea(ref, store, fromArea, fromSlot, picked);
                return false;
            }

            if (!setStackToArea(ref, store, fromArea, fromSlot, target)) {
                ItemStack rollback = removeStackFromArea(ref, store, toArea, toSlot);
                if (!ItemStack.isEmpty(rollback)) {
                    setStackToArea(ref, store, fromArea, fromSlot, rollback);
                }
                return false;
            }
        }

        resetDragState();
        return true;
    }

    private boolean transferLootToInventory(
            @NonNull Ref<EntityStore> ref,
            @NonNull Store<EntityStore> store,
            int lootSlot,
            @NonNull String toArea,
            int toSlot
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return false;

        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent == null) return false;

        ArrayList<ItemStack> loot = new ArrayList<>(getLootSnapshot(uuidComponent.getUuid()));
        if (lootSlot < 0 || lootSlot >= loot.size()) return false;

        ItemStack source = loot.get(lootSlot);
        if (ItemStack.isEmpty(source)) return false;

        ItemStack target = getStackFromArea(ref, store, toArea, toSlot);
        if (!ItemStack.isEmpty(target)) {
            return false;
        }

        if (!setStackToArea(ref, store, toArea, toSlot, source)) {
            return false;
        }

        loot.set(lootSlot, null);
        compactLoot(loot);
        replaceLoot(uuidComponent.getUuid(), loot);
        return true;
    }

    private void resetDragState() {
        this.pendingDragArea = null;
        this.pendingDragSlot = -1;
        this.pendingDragAt = 0;
        this.pendingHoverArea = null;
        this.pendingHoverSlot = -1;
        this.pendingHoverAt = 0;
    }

    private String resolveSourceAreaForTransfer(@NonNull Ref<EntityStore> ref,
                                                @NonNull Store<EntityStore> store,
                                                String targetArea,
                                                int slot,
                                                String itemId) {
        if (matchesItemAt(ref, store, pendingDragArea, pendingDragSlot, itemId)) {
            return pendingDragArea;
        }

        if (AREA_LOOT.equals(targetArea)) {
            String inventorySource = findMatchingInventoryOrEquipmentArea(ref, store, slot, itemId);
            if (inventorySource != null) {
                return inventorySource;
            }

            if (matchesItemAt(ref, store, AREA_LOOT, slot, itemId)) {
                return AREA_LOOT;
            }
            return null;
        }

        if (isInventoryArea(targetArea) || isEquipmentArea(targetArea)) {
            if (matchesItemAt(ref, store, AREA_LOOT, slot, itemId)) {
                return AREA_LOOT;
            }

            return findMatchingInventoryOrEquipmentArea(ref, store, slot, itemId);
        }

        if (matchesItemAt(ref, store, AREA_LOOT, slot, itemId)) {
            return AREA_LOOT;
        }

        return findMatchingInventoryOrEquipmentArea(ref, store, slot, itemId);
    }

    private String findMatchingInventoryOrEquipmentArea(@NonNull Ref<EntityStore> ref,
                                                        @NonNull Store<EntityStore> store,
                                                        int slot,
                                                        String itemId) {
        if (matchesItemAt(ref, store, AREA_HOTBAR, slot, itemId)) {
            return AREA_HOTBAR;
        }
        if (matchesItemAt(ref, store, AREA_STORAGE, slot, itemId)) {
            return AREA_STORAGE;
        }
        for (String equipmentArea : EQUIPMENT_AREAS) {
            if (matchesItemAt(ref, store, equipmentArea, 0, itemId)) {
                return equipmentArea;
            }
        }
        return null;
    }

    private boolean matchesItemAt(@NonNull Ref<EntityStore> ref,
                                  @NonNull Store<EntityStore> store,
                                  String area,
                                  int slot,
                                  String itemId) {
        if (area == null || slot < 0 || itemId == null || itemId.isBlank()) {
            return false;
        }
        ItemStack stack = getStackFromArea(ref, store, area, slot);
        return !ItemStack.isEmpty(stack) && itemId.equals(stack.getItemId());
    }

    private int toInventorySectionId(String area) {
        return InventoryAreaRules.toInventorySectionId(area);
    }

    private int toInventorySectionSlot(String area, int slot) {
        return InventoryAreaRules.toInventorySectionSlot(area, slot);
    }

    private boolean isLinkedEquipmentArea(String area) {
        return InventoryAreaRules.isLinkedEquipmentArea(area);
    }

    private boolean isEquipmentArea(String area) {
        return InventoryAreaRules.isEquipmentArea(area);
    }

    private int toEquipmentSlotIndex(String area) {
        return InventoryAreaRules.toEquipmentSlotIndex(area);
    }

    private boolean canEquip(String area, ItemStack stack) {
        return InventoryAreaRules.canEquip(area, stack);
    }

    private ItemStack getStackFromArea(@NonNull Ref<EntityStore> ref, @NonNull Store<EntityStore> store, String area, int index) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return null;

        Inventory inventory = player.getInventory();

        if (isInventoryArea(area)) {
            int inventoryIndex = toInventoryIndex(area, index);
            if (inventoryIndex < 0 || inventoryIndex >= INVENTORY_SLOT_COUNT) return null;
            return getInventoryItem(inventory, inventoryIndex);
        }

        if (isLinkedEquipmentArea(area)) {
            int sectionId = toInventorySectionId(area);
            int sectionSlot = toInventorySectionSlot(area, index);
            if (sectionId == Integer.MIN_VALUE || sectionSlot < 0) return null;
            var container = inventory.getSectionById(sectionId);
            return container == null ? null : container.getItemStack((short) sectionSlot);
        }

        if (AREA_LOOT.equals(area)) {
            UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
            if (uuidComponent == null) return null;

            ArrayList<ItemStack> loot = getLootSnapshot(uuidComponent.getUuid());
            return index >= 0 && index < loot.size() ? loot.get(index) : null;
        }

        if (isEquipmentArea(area)) {
            UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
            if (uuidComponent == null) return null;
            int equipmentIndex = toEquipmentSlotIndex(area);
            return getEquipmentSlot(uuidComponent.getUuid(), equipmentIndex);
        }

        return null;
    }

    private ItemStack removeStackFromArea(@NonNull Ref<EntityStore> ref, @NonNull Store<EntityStore> store, String area, int index) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return null;

        Inventory inventory = player.getInventory();

        if (isInventoryArea(area)) {
            int inventoryIndex = toInventoryIndex(area, index);
            if (inventoryIndex < 0 || inventoryIndex >= INVENTORY_SLOT_COUNT) return null;

            if (inventoryIndex < HOTBAR_SLOT_COUNT) {
                return inventory.getHotbar().setItemStackForSlot((short) inventoryIndex, null).getSlotBefore();
            }

            int storageIndex = inventoryIndex - HOTBAR_SLOT_COUNT;
            return inventory.getStorage().setItemStackForSlot((short) storageIndex, null).getSlotBefore();
        }

        if (isLinkedEquipmentArea(area)) {
            int sectionId = toInventorySectionId(area);
            int sectionSlot = toInventorySectionSlot(area, index);
            if (sectionId == Integer.MIN_VALUE || sectionSlot < 0) return null;
            var container = inventory.getSectionById(sectionId);
            if (container == null) return null;
            return container.setItemStackForSlot((short) sectionSlot, null, true).getSlotBefore();
        }

        if (AREA_LOOT.equals(area)) {
            UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
            if (uuidComponent == null) return null;

            ArrayList<ItemStack> loot = new ArrayList<>(getLootSnapshot(uuidComponent.getUuid()));
            if (index < 0 || index >= loot.size()) return null;

            ItemStack stack = loot.get(index);
            loot.set(index, null);
            compactLoot(loot);
            replaceLoot(uuidComponent.getUuid(), loot);
            return stack;
        }

        if (isEquipmentArea(area)) {
            UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
            if (uuidComponent == null) return null;

            int equipmentIndex = toEquipmentSlotIndex(area);
            if (equipmentIndex < 0) return null;

            ItemStack stack = getEquipmentSlot(uuidComponent.getUuid(), equipmentIndex);
            setEquipmentSlot(uuidComponent.getUuid(), equipmentIndex, null);
            return stack;
        }

        return null;
    }

    private boolean setStackToArea(@NonNull Ref<EntityStore> ref, @NonNull Store<EntityStore> store, String area, int index, ItemStack stack) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return false;

        Inventory inventory = player.getInventory();

        if (isInventoryArea(area)) {
            int inventoryIndex = toInventoryIndex(area, index);
            if (inventoryIndex < 0 || inventoryIndex >= INVENTORY_SLOT_COUNT) return false;

            if (inventoryIndex < HOTBAR_SLOT_COUNT) {
                return inventory.getHotbar().setItemStackForSlot((short) inventoryIndex, stack, true).succeeded();
            }

            int storageIndex = inventoryIndex - HOTBAR_SLOT_COUNT;
            return inventory.getStorage().setItemStackForSlot((short) storageIndex, stack, true).succeeded();
        }

        if (isLinkedEquipmentArea(area)) {
            int sectionId = toInventorySectionId(area);
            int sectionSlot = toInventorySectionSlot(area, index);
            if (sectionId == Integer.MIN_VALUE || sectionSlot < 0) return false;
            var container = inventory.getSectionById(sectionId);
            if (container == null) return false;
            return container.setItemStackForSlot((short) sectionSlot, stack, true).succeeded();
        }

        if (AREA_LOOT.equals(area)) {
            UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
            if (uuidComponent == null) return false;

            ArrayList<ItemStack> loot = new ArrayList<>(getLootSnapshot(uuidComponent.getUuid()));
            while (loot.size() <= index) {
                loot.add(null);
            }

            loot.set(index, stack);
            compactLoot(loot);
            replaceLoot(uuidComponent.getUuid(), loot);
            return true;
        }

        if (isEquipmentArea(area)) {
            UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
            if (uuidComponent == null) return false;

            int equipmentIndex = toEquipmentSlotIndex(area);
            if (equipmentIndex < 0) return false;

            if (!ItemStack.isEmpty(stack) && !canEquip(area, stack)) {
                return false;
            }

            setEquipmentSlot(uuidComponent.getUuid(), equipmentIndex, stack);
            return true;
        }

        return false;
    }

    private void compactLoot(ArrayList<ItemStack> loot) {
        for (int i = loot.size() - 1; i >= 0; i--) {
            if (ItemStack.isEmpty(loot.get(i))) {
                loot.remove(i);
            }
        }
    }

    private void applyState(
            @NonNull UICommandBuilder cmd,
            @NonNull Ref<EntityStore> ref,
            @NonNull Store<EntityStore> store
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent == null) return;

        fr.welltale.player.charactercache.CachedCharacter cachedCharacter = this.characterCacheRepository.getCharacterCache(uuidComponent.getUuid());
        if (cachedCharacter == null) return;

        Inventory inventory = player.getInventory();
        ArrayList<ItemStack> loot = getLootSnapshot(uuidComponent.getUuid());

        for (String[] binding : EQUIPMENT_GRID_BINDINGS) {
            cmd.set(binding[0] + ".Slots", oneSlotFromArea(ref, store, binding[1]));
        }
        cmd.set("#HotbarGrid.Slots", buildInventorySlots(inventory, true));
        cmd.set("#StorageGrid.Slots", buildInventorySlots(inventory, false));
        cmd.set("#LootGrid.Slots", buildLootSlots(loot));

        cmd.set("#LootCountLabel.Text", loot.size() + "/" + InventoryService.LOOT_SLOT_CAPACITY);
        cmd.set("#GoldFooterAmount.Text", String.valueOf(cachedCharacter.getGems()));
    }

    private ItemStack getInventoryItem(Inventory inventory, int index) {
        if (index < HOTBAR_SLOT_COUNT) {
            return inventory.getHotbar().getItemStack((short) index);
        }

        int storageIndex = index - HOTBAR_SLOT_COUNT;
        return inventory.getStorage().getItemStack((short) storageIndex);
    }

    private ItemGridSlot[] oneSlotFromArea(@NonNull Ref<EntityStore> ref, @NonNull Store<EntityStore> store, String area) {
        ItemStack stack = getStackFromArea(ref, store, area, 0);
        return new ItemGridSlot[]{createSafeLootSlot(stack)};
    }

    private boolean isInventoryArea(String area) {
        return InventoryAreaRules.isInventoryArea(area);
    }

    private int toInventoryIndex(String area, int rawIndex) {
        return InventoryAreaRules.toInventoryIndex(area, rawIndex);
    }

    private ItemGridSlot[] buildLootSlots(@NonNull ArrayList<ItemStack> loot) {
        int capacity = Math.max(InventoryPage.LOOT_SLOT_COUNT, loot.size());
        ItemGridSlot[] slots = new ItemGridSlot[capacity];
        for (int i = 0; i < capacity; i++) {
            ItemStack itemStack = i < loot.size() ? loot.get(i) : null;
            slots[i] = createSafeLootSlot(itemStack);
        }

        return slots;
    }

    private ItemGridSlot[] buildInventorySlots(@NonNull Inventory inventory, boolean hotbar) {
        int slotCount = hotbar ? HOTBAR_SLOT_COUNT : STORAGE_SLOT_COUNT;
        int indexOffset = hotbar ? 0 : HOTBAR_SLOT_COUNT;
        ItemGridSlot[] slots = new ItemGridSlot[slotCount];
        for (int i = 0; i < slotCount; i++) {
            slots[i] = createSafeLootSlot(getInventoryItem(inventory, indexOffset + i));
        }

        return slots;
    }

    private ItemGridSlot createSafeLootSlot(ItemStack itemStack) {
        if (ItemStack.isEmpty(itemStack)) {
            return createActivatableSlot(null);
        }

        String itemId = itemStack.getItemId();
        if (itemId.isBlank()) {
            return createActivatableSlot(null);
        }

        if (!ItemModule.exists(itemId)) {
            this.logger.atSevere().log("[INVENTORY] InventoryPage CreateSafeLootSlot Failed: Unknown item id hidden from UI to avoid client crash:" + itemId);
            return createActivatableSlot(null);
        }

        try {
            return createActivatableSlot(itemStack);
        } catch (Exception e) {
            this.logger.atSevere().log("[INVENTORY] InventoryPage CreateSafeLootSlot Failed: Invalid loot stack rendered as empty slot:" + itemId);
            return createActivatableSlot(null);
        }
    }

    private ItemGridSlot createActivatableSlot(ItemStack itemStack) {
        ItemGridSlot slot = itemStack == null ? new ItemGridSlot() : new ItemGridSlot(itemStack);
        slot.setActivatable(true);
        return slot;
    }

    private UUID resolveCharacterUuid(UUID playerUuid) {
        if (playerUuid == null) return null;

        CachedCharacter cachedCharacter = this.characterCacheRepository.getCharacterCache(playerUuid);
        return cachedCharacter == null ? null : cachedCharacter.getCharacterUuid();
    }

    private void ensureCharacterInventory(UUID playerUuid) {
        if (playerUuid == null) return;

        CachedCharacter cachedCharacter = this.characterCacheRepository.getCharacterCache(playerUuid);
        if (cachedCharacter == null || cachedCharacter.getCharacterUuid() == null) return;

        this.inventoryService.ensureCharacterInventory(
                playerUuid,
                cachedCharacter.getCharacterUuid(),
                cachedCharacter.getLoot(),
                cachedCharacter.getEquipment()
        );
    }

    private ArrayList<ItemStack> getLootSnapshot(UUID playerUuid) {
        ensureCharacterInventory(playerUuid);
        return this.inventoryService.getLootSnapshot(playerUuid, resolveCharacterUuid(playerUuid));
    }

    private void replaceLoot(UUID playerUuid, List<ItemStack> loot) {
        ensureCharacterInventory(playerUuid);
        this.inventoryService.replaceLoot(playerUuid, resolveCharacterUuid(playerUuid), loot);
    }

    private ItemStack getEquipmentSlot(UUID playerUuid, int slotIndex) {
        ensureCharacterInventory(playerUuid);
        return this.inventoryService.getEquipmentSlot(playerUuid, resolveCharacterUuid(playerUuid), slotIndex);
    }

    private void setEquipmentSlot(UUID playerUuid, int slotIndex, ItemStack stack) {
        ensureCharacterInventory(playerUuid);
        this.inventoryService.setEquipmentSlot(playerUuid, resolveCharacterUuid(playerUuid), slotIndex, stack);
    }

    private ArrayList<ItemStack> getEquipmentSnapshot(UUID playerUuid) {
        ensureCharacterInventory(playerUuid);
        return this.inventoryService.getEquipmentSnapshot(playerUuid, resolveCharacterUuid(playerUuid));
    }

    private void persistActiveCharacterInventory(@NonNull Ref<EntityStore> ref, @NonNull Store<EntityStore> store) {
        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent == null) return;

        UUID playerUuid = uuidComponent.getUuid();
        CachedCharacter cachedCharacter = this.characterCacheRepository.getCharacterCache(playerUuid);
        if (cachedCharacter == null || cachedCharacter.getCharacterUuid() == null) return;

        Player runtimePlayer = store.getComponent(ref, Player.getComponentType());
        if (runtimePlayer == null) return;

        CharacterVanillaInventorySnapshot snapshot = CharacterVanillaInventorySnapshot.capture(runtimePlayer.getInventory());
        cachedCharacter.setHotbar(snapshot.getStoredHotbar());
        cachedCharacter.setStorage(snapshot.getStoredStorage());
        cachedCharacter.setArmor(snapshot.getStoredArmor());
        cachedCharacter.setLoot(StoredItemStack.fromItemStackList(getLootSnapshot(playerUuid), InventoryService.LOOT_SLOT_CAPACITY));
        cachedCharacter.setEquipment(StoredItemStack.fromItemStackList(getEquipmentSnapshot(playerUuid), InventoryService.EQUIPMENT_SLOT_COUNT));

        try {
            this.characterCacheRepository.updateCharacter(cachedCharacter);
        } catch (Exception e) {
            this.logger.atSevere().log("[INVENTORY] InventoryPage PersistActiveCharacterInventory Failed: " + e.getMessage());
        }
    }

}
