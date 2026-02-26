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
import com.hypixel.hytale.server.core.modules.item.ItemModule;
import com.hypixel.hytale.server.core.ui.ItemGridSlot;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.characteristic.page.CharacteristicsPage;
import fr.welltale.inventory.CustomInventoryService;
import fr.welltale.player.PlayerRepository;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

    private static final String AREA_HOTBAR = "HOTBAR";
    private static final String AREA_STORAGE = "STORAGE";
    private static final String AREA_LOOT = "LOOT";
    private static final String AREA_EQUIPMENT_HEAD = "EQUIP_HEAD";
    private static final String AREA_EQUIPMENT_WEAPON = "EQUIP_WEAPON";
    private static final String AREA_EQUIPMENT_AMULET = "EQUIP_AMULET";
    private static final String AREA_EQUIPMENT_GAUNTLETS = "EQUIP_GAUNTLETS";
    private static final String AREA_EQUIPMENT_RING_1 = "EQUIP_RING_1";
    private static final String AREA_EQUIPMENT_RING_2 = "EQUIP_RING_2";
    private static final String AREA_EQUIPMENT_CHEST = "EQUIP_CHEST";
    private static final String AREA_EQUIPMENT_PANTS = "EQUIP_PANTS";
    private static final String AREA_EQUIPMENT_BELT = "EQUIP_BELT";
    private static final String AREA_EQUIPMENT_PET = "EQUIP_PET";
    private static final String AREA_EQUIPMENT_BOOTS = "EQUIP_BOOTS";
    private static final String AREA_EQUIPMENT_TROPHY_1 = "EQUIP_TROPHY_1";
    private static final String AREA_EQUIPMENT_TROPHY_2 = "EQUIP_TROPHY_2";
    private static final String AREA_EQUIPMENT_TROPHY_3 = "EQUIP_TROPHY_3";
    private static final String AREA_EQUIPMENT_TROPHY_4 = "EQUIP_TROPHY_4";
    private static final String AREA_EQUIPMENT_TROPHY_5 = "EQUIP_TROPHY_5";
    private static final String AREA_EQUIPMENT_TROPHY_6 = "EQUIP_TROPHY_6";

    private static final int ARMOR_SLOT_HEAD = 0;
    private static final int ARMOR_SLOT_CHEST = 1;
    private static final int ARMOR_SLOT_GAUNTLETS = 2;
    private static final int ARMOR_SLOT_PANTS = 3;

    private final CustomInventoryService customInventoryService;
    private final PlayerRepository playerRepository;
    private final HytaleLogger logger;
    private final PlayerRef playerRef;
    private static final String[] EQUIPMENT_AREAS = {
            AREA_EQUIPMENT_HEAD,
            AREA_EQUIPMENT_WEAPON,
            AREA_EQUIPMENT_AMULET,
            AREA_EQUIPMENT_GAUNTLETS,
            AREA_EQUIPMENT_RING_1,
            AREA_EQUIPMENT_RING_2,
            AREA_EQUIPMENT_CHEST,
            AREA_EQUIPMENT_PANTS,
            AREA_EQUIPMENT_BELT,
            AREA_EQUIPMENT_PET,
            AREA_EQUIPMENT_BOOTS,
            AREA_EQUIPMENT_TROPHY_1,
            AREA_EQUIPMENT_TROPHY_2,
            AREA_EQUIPMENT_TROPHY_3,
            AREA_EQUIPMENT_TROPHY_4,
            AREA_EQUIPMENT_TROPHY_5,
            AREA_EQUIPMENT_TROPHY_6
    };

    private static final Map<String, Integer> EQUIPMENT_SLOT_BY_AREA = buildEquipmentSlotByArea();
    private static final Set<String> EQUIPMENT_AREA_SET = EQUIPMENT_SLOT_BY_AREA.keySet();

    private static final String[][] EQUIPMENT_GRID_BINDINGS = {
            {"#EquipHeadGrid", AREA_EQUIPMENT_HEAD},
            {"#EquipWeaponGrid", AREA_EQUIPMENT_WEAPON},
            {"#EquipAmuletGrid", AREA_EQUIPMENT_AMULET},
            {"#EquipGauntletsGrid", AREA_EQUIPMENT_GAUNTLETS},
            {"#EquipRing1Grid", AREA_EQUIPMENT_RING_1},
            {"#EquipRing2Grid", AREA_EQUIPMENT_RING_2},
            {"#EquipChestGrid", AREA_EQUIPMENT_CHEST},
            {"#EquipPantsGrid", AREA_EQUIPMENT_PANTS},
            {"#EquipBeltGrid", AREA_EQUIPMENT_BELT},
            {"#EquipPetGrid", AREA_EQUIPMENT_PET},
            {"#EquipBootsGrid", AREA_EQUIPMENT_BOOTS},
            {"#EquipTrophy1Grid", AREA_EQUIPMENT_TROPHY_1},
            {"#EquipTrophy2Grid", AREA_EQUIPMENT_TROPHY_2},
            {"#EquipTrophy3Grid", AREA_EQUIPMENT_TROPHY_3},
            {"#EquipTrophy4Grid", AREA_EQUIPMENT_TROPHY_4},
            {"#EquipTrophy5Grid", AREA_EQUIPMENT_TROPHY_5},
            {"#EquipTrophy6Grid", AREA_EQUIPMENT_TROPHY_6}
    };

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
            @NonNull CustomInventoryService customInventoryService,
            @NonNull PlayerRepository playerRepository,
            @NonNull HytaleLogger logger
    ) {
        super(playerRef, CustomPageLifetime.CanDismiss, CustomInventoryEventData.CODEC);
        this.playerRef = playerRef;
        this.customInventoryService = customInventoryService;
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
        bindActivating(event, "#CollectAllButton", ACTION_COLLECT_ALL, null);
        bindActivating(event, "#CloseButton", ACTION_CLOSE, null);
        bindActivating(event, "#TabCharacteristics", ACTION_OPEN_CHARACTERISTICS, null);
        for (String[] binding : EQUIPMENT_GRID_BINDINGS) {
            bindItemGridEvents(event, binding[0], binding[1]);
        }
        bindItemGridEvents(event, "#HotbarGrid", AREA_HOTBAR);
        bindItemGridEvents(event, "#StorageGrid", AREA_STORAGE);
        bindItemGridEvents(event, "#LootGrid", AREA_LOOT);
        applyState(cmd, ref, store, playerRepository);
    }

    private void bindActivating(@NonNull UIEventBuilder event, @NonNull String selector, @NonNull String action, String area) {
        if (area == null) {
            event.addEventBinding(CustomUIEventBindingType.Activating, selector, EventData.of("Action", action));
            return;
        }

        event.addEventBinding(
                CustomUIEventBindingType.Activating,
                selector,
                new EventData().append("Action", action).append("Area", area),
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

        if (ACTION_CLOSE.equals(data.action)) {
            this.closed = true;
            player.getPageManager().setPage(ref, store, Page.None);
            return;
        }

        if (ACTION_COLLECT_ALL.equals(data.action)) {
            collectAllLoot(ref, store, player);
            return;
        }

        if (ACTION_TRANSFER_DROP.equals(data.action)
                || ACTION_TRANSFER_DRAG_COMPLETED.equals(data.action)
                || ACTION_TRANSFER_RELEASE.equals(data.action)) {
            boolean changed = handleTransfer(ref, store, data);
            if (changed) {
                safeSendUpdate(ref, store);
            }
            return;
        }

        if (ACTION_DRAG_HOVER.equals(data.action)) {
            boolean changed = handleDragHover(data, ref, store);
            if (changed) {
                safeSendUpdate(ref, store);
            }
            return;
        }

        if (ACTION_OPEN_CHARACTERISTICS.equals(data.action)) {
            this.closed = true;
            player.getPageManager().openCustomPage(
                    ref,
                    store,
                    new CharacteristicsPage(playerRef, customInventoryService, playerRepository, logger)
            );
            return;
        }

        safeSendUpdate(ref, store);
    }

    private void collectAllLoot(@NonNull Ref<EntityStore> ref, @NonNull Store<EntityStore> store, @NonNull Player player) {
        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent == null) {
            this.logger.atSevere().log("[INVENTORY] InventoryPage CollectAllLoot Failed: Player UUID is null");
            return;
        }

        UUID playerUuid = uuidComponent.getUuid();
        List<ItemStack> loot = customInventoryService.getLootSnapshot(playerUuid);
        if (loot.isEmpty()) {
            safeSendUpdate(ref, store);
            return;
        }

        Inventory inventory = player.getInventory();
        List<ItemStack> remaining = new ArrayList<>();
        int collectedStacks = 0;

        for (ItemStack itemStack : loot) {
            ItemStackTransaction transaction = inventory.getCombinedHotbarFirst().addItemStack(itemStack);
            ItemStack remainder = transaction.getRemainder();
            if (!ItemStack.isEmpty(remainder)) {
                remaining.add(remainder);
            }

            if (ItemStack.isEmpty(remainder) || remainder.getQuantity() < itemStack.getQuantity()) {
                collectedStacks++;
            }
        }

        customInventoryService.replaceLoot(playerUuid, remaining);
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
        int sourceSlot = resolveSourceSlot(data);
        int targetSlot = resolveTargetSlot(data);
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
            int pendingTargetSlot = targetSlot >= 0 ? targetSlot : firstValid(resolveIndex(data), sourceSlot);
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

    private int resolveSourceSlot(@NonNull CustomInventoryEventData data) {
        return firstValid(
                parseIndex(data.sourceSlotId),
                parseIndex(data.fromSlotId),
                parseIndex(data.sourceItemGridIndex),
                parseIndex(data.fromItemGridIndex),
                extractIntFromRawPayload(lastRawEventPayload, "SourceSlotId"),
                extractIntFromRawPayload(lastRawEventPayload, "FromSlotId"),
                extractIntFromRawPayload(lastRawEventPayload, "SourceItemGridIndex"),
                extractIntFromRawPayload(lastRawEventPayload, "FromItemGridIndex"),
                extractIntFromRawPayload(lastRawEventPayload, "SlotIndex"),
                parseIndex(data.slotIndex),
                parseIndex(data.index)
        );
    }

    private int resolveTargetSlot(@NonNull CustomInventoryEventData data) {
        return firstValid(
                parseIndex(data.toSlotId),
                parseIndex(data.targetSlotId),
                parseIndex(data.destinationSlotId),
                parseIndex(data.toItemGridIndex),
                parseIndex(data.inventorySlotIndex),
                extractIntFromRawPayload(lastRawEventPayload, "ToSlotId"),
                extractIntFromRawPayload(lastRawEventPayload, "TargetSlotId"),
                extractIntFromRawPayload(lastRawEventPayload, "DestinationSlotId"),
                extractIntFromRawPayload(lastRawEventPayload, "ToItemGridIndex"),
                extractIntFromRawPayload(lastRawEventPayload, "SlotIndex"),
                parseIndex(data.slotIndex),
                parseIndex(data.index)
        );
    }

    private boolean handleDragHover(@NonNull CustomInventoryEventData data,
                                    @NonNull Ref<EntityStore> ref,
                                    @NonNull Store<EntityStore> store) {
        int hovered = firstValid(
                extractIntFromRawPayload(lastRawEventPayload, "SlotIndex"),
                resolveIndex(data)
        );
        if (hovered < 0 || data.area == null) {
            return false;
        }

        this.pendingHoverArea = data.area;
        this.pendingHoverSlot = hovered;
        this.pendingHoverAt = System.currentTimeMillis();
        return false;
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
            if (fromSectionId == Integer.MIN_VALUE || toSectionId == Integer.MIN_VALUE || fromSlotId < 0 || toSlotId < 0) {
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

        List<ItemStack> loot = new ArrayList<>(customInventoryService.getLootSnapshot(uuidComponent.getUuid()));
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
        customInventoryService.replaceLoot(uuidComponent.getUuid(), loot);
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
        if (AREA_HOTBAR.equals(area)) {
            return Inventory.HOTBAR_SECTION_ID;
        }
        if (AREA_STORAGE.equals(area)) {
            return Inventory.STORAGE_SECTION_ID;
        }
        if (AREA_EQUIPMENT_WEAPON.equals(area)) {
            return Inventory.HOTBAR_SECTION_ID;
        }
        if (AREA_EQUIPMENT_HEAD.equals(area)
                || AREA_EQUIPMENT_CHEST.equals(area)
                || AREA_EQUIPMENT_GAUNTLETS.equals(area)
                || AREA_EQUIPMENT_PANTS.equals(area)) {
            return Inventory.ARMOR_SECTION_ID;
        }
        return Integer.MIN_VALUE;
    }

    private int toInventorySectionSlot(String area, int slot) {
        if (AREA_HOTBAR.equals(area) || AREA_STORAGE.equals(area)) {
            return slot;
        }
        if (AREA_EQUIPMENT_WEAPON.equals(area)) {
            return 0;
        }
        if (AREA_EQUIPMENT_HEAD.equals(area)) {
            return ARMOR_SLOT_HEAD;
        }
        if (AREA_EQUIPMENT_CHEST.equals(area)) {
            return ARMOR_SLOT_CHEST;
        }
        if (AREA_EQUIPMENT_GAUNTLETS.equals(area)) {
            return ARMOR_SLOT_GAUNTLETS;
        }
        if (AREA_EQUIPMENT_PANTS.equals(area)) {
            return ARMOR_SLOT_PANTS;
        }
        return -1;
    }

    private boolean isLinkedEquipmentArea(String area) {
        return AREA_EQUIPMENT_WEAPON.equals(area)
                || AREA_EQUIPMENT_HEAD.equals(area)
                || AREA_EQUIPMENT_CHEST.equals(area)
                || AREA_EQUIPMENT_GAUNTLETS.equals(area)
                || AREA_EQUIPMENT_PANTS.equals(area);
    }

    private boolean isEquipmentArea(String area) {
        return area != null && EQUIPMENT_AREA_SET.contains(area);
    }

    private int toEquipmentSlotIndex(String area) {
        if (area == null) return -1;
        Integer index = EQUIPMENT_SLOT_BY_AREA.get(area);
        return index == null ? -1 : index;
    }

    private static Map<String, Integer> buildEquipmentSlotByArea() {
        Map<String, Integer> byArea = new HashMap<>(EQUIPMENT_AREAS.length);
        for (int i = 0; i < EQUIPMENT_AREAS.length; i++) {
            byArea.put(EQUIPMENT_AREAS[i], i);
        }
        return byArea;
    }

    private boolean canEquip(String area, ItemStack stack) {
        if (ItemStack.isEmpty(stack)) {
            return true;
        }

        String itemId = stack.getItemId();
        String id = itemId.toLowerCase();
        return switch (area) {
            case AREA_EQUIPMENT_HEAD -> containsAny(id, "head");
            case AREA_EQUIPMENT_WEAPON -> containsAny(id, "weapon");
            case AREA_EQUIPMENT_AMULET -> containsAny(id, "amulet");
            case AREA_EQUIPMENT_GAUNTLETS -> containsAny(id, "hands");
            case AREA_EQUIPMENT_RING_1, AREA_EQUIPMENT_RING_2 -> containsAny(id, "ring");
            case AREA_EQUIPMENT_CHEST -> containsAny(id, "chest");
            case AREA_EQUIPMENT_PANTS -> containsAny(id, "legs");
            case AREA_EQUIPMENT_BELT -> containsAny(id, "belt");
            case AREA_EQUIPMENT_PET -> containsAny(id, "companion");
            case AREA_EQUIPMENT_BOOTS -> containsAny(id, "boots");
            case AREA_EQUIPMENT_TROPHY_1,
                 AREA_EQUIPMENT_TROPHY_2,
                 AREA_EQUIPMENT_TROPHY_3,
                 AREA_EQUIPMENT_TROPHY_4,
                 AREA_EQUIPMENT_TROPHY_5,
                 AREA_EQUIPMENT_TROPHY_6 -> true;
            default -> false;
        };
    }

    private boolean containsAny(String value, String... needles) {
        for (String needle : needles) {
            if (value.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private int extractIntFromRawPayload(String rawPayload, String key) {
        if (rawPayload == null || rawPayload.isBlank() || key == null || key.isBlank()) {
            return -1;
        }

        String token = "\"" + key + "\"";
        int keyIndex = rawPayload.indexOf(token);
        if (keyIndex < 0) {
            return -1;
        }

        int colonIndex = rawPayload.indexOf(':', keyIndex + token.length());
        if (colonIndex < 0) {
            return -1;
        }

        int valueIndex = colonIndex + 1;
        while (valueIndex < rawPayload.length() && Character.isWhitespace(rawPayload.charAt(valueIndex))) {
            valueIndex++;
        }

        if (valueIndex >= rawPayload.length()) {
            return -1;
        }

        if (rawPayload.charAt(valueIndex) == '"') {
            valueIndex++;
        }

        int sign = 1;
        if (valueIndex < rawPayload.length() && rawPayload.charAt(valueIndex) == '-') {
            sign = -1;
            valueIndex++;
        }

        int end = valueIndex;
        while (end < rawPayload.length() && Character.isDigit(rawPayload.charAt(end))) {
            end++;
        }

        if (end <= valueIndex) {
            return -1;
        }

        try {
            return Integer.parseInt(rawPayload.substring(valueIndex, end)) * sign;
        } catch (Exception ignored) {
            return -1;
        }
    }

    private int firstValid(int... candidates) {
        for (int candidate : candidates) {
            if (candidate >= 0) {
                return candidate;
            }
        }
        return -1;
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

            List<ItemStack> loot = customInventoryService.getLootSnapshot(uuidComponent.getUuid());
            return index >= 0 && index < loot.size() ? loot.get(index) : null;
        }

        if (isEquipmentArea(area)) {
            UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
            if (uuidComponent == null) return null;
            int equipmentIndex = toEquipmentSlotIndex(area);
            return customInventoryService.getEquipmentSlot(uuidComponent.getUuid(), equipmentIndex);
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

            List<ItemStack> loot = new ArrayList<>(customInventoryService.getLootSnapshot(uuidComponent.getUuid()));
            if (index < 0 || index >= loot.size()) return null;

            ItemStack stack = loot.get(index);
            loot.set(index, null);
            compactLoot(loot);
            customInventoryService.replaceLoot(uuidComponent.getUuid(), loot);
            return stack;
        }

        if (isEquipmentArea(area)) {
            UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
            if (uuidComponent == null) return null;

            int equipmentIndex = toEquipmentSlotIndex(area);
            if (equipmentIndex < 0) return null;

            ItemStack stack = customInventoryService.getEquipmentSlot(uuidComponent.getUuid(), equipmentIndex);
            customInventoryService.setEquipmentSlot(uuidComponent.getUuid(), equipmentIndex, null);
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

            List<ItemStack> loot = new ArrayList<>(customInventoryService.getLootSnapshot(uuidComponent.getUuid()));
            while (loot.size() <= index) {
                loot.add(null);
            }

            loot.set(index, stack);
            compactLoot(loot);
            customInventoryService.replaceLoot(uuidComponent.getUuid(), loot);
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

            customInventoryService.setEquipmentSlot(uuidComponent.getUuid(), equipmentIndex, stack);
            return true;
        }

        return false;
    }

    private void compactLoot(List<ItemStack> loot) {
        for (int i = loot.size() - 1; i >= 0; i--) {
            if (ItemStack.isEmpty(loot.get(i))) {
                loot.remove(i);
            }
        }
    }

    private int parseIndex(String rawIndex) {
        if (rawIndex == null || rawIndex.isBlank()) return -1;

        try {
            return Integer.parseInt(rawIndex.trim());
        } catch (Exception ignored) {
        }

        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < rawIndex.length(); i++) {
            char c = rawIndex.charAt(i);
            if (Character.isDigit(c)) {
                digits.append(c);
            } else if (digits.length() > 0) {
                break;
            }
        }

        if (digits.isEmpty()) return -1;

        try {
            return Integer.parseInt(digits.toString());
        } catch (Exception ignored) {
            return -1;
        }
    }

    private int resolveIndex(@NonNull CustomInventoryEventData data) {
        int resolved = parseIndex(data.index);
        if (resolved >= 0) return resolved;

        resolved = parseIndex(data.slotIndex);
        if (resolved >= 0) return resolved;

        resolved = parseIndex(data.toSlotId);
        if (resolved >= 0) return resolved;

        resolved = parseIndex(data.fromSlotId);
        if (resolved >= 0) return resolved;

        return parseIndex(data.inventorySlotIndex);
    }

    private void applyState(
            @NonNull UICommandBuilder cmd,
            @NonNull Ref<EntityStore> ref,
            @NonNull Store<EntityStore> store,
            @NonNull PlayerRepository playerRepository
    ) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent == null) return;

        fr.welltale.player.Player playerData = playerRepository.getPlayerByUuid(uuidComponent.getUuid());
        if (playerData == null) return;

        Inventory inventory = player.getInventory();
        List<ItemStack> loot = customInventoryService.getLootSnapshot(uuidComponent.getUuid());

        for (String[] binding : EQUIPMENT_GRID_BINDINGS) {
            cmd.set(binding[0] + ".Slots", oneSlotFromArea(ref, store, binding[1]));
        }
        cmd.set("#HotbarGrid.Slots", buildInventorySlots(inventory, true));
        cmd.set("#StorageGrid.Slots", buildInventorySlots(inventory, false));
        cmd.set("#LootGrid.Slots", buildLootSlots(loot, LOOT_SLOT_COUNT));

        cmd.set("#LootCountLabel.Text", loot.size() + "/" + CustomInventoryService.LOOT_SLOT_CAPACITY);
        cmd.set("#GoldFooterAmount.Text", String.valueOf(playerData.getGems()));
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
        return AREA_HOTBAR.equals(area) || AREA_STORAGE.equals(area);
    }

    private int toInventoryIndex(String area, int rawIndex) {
        if (rawIndex < 0) return -1;
        if (AREA_HOTBAR.equals(area)) {
            return rawIndex;
        }

        if (AREA_STORAGE.equals(area)) {
            return HOTBAR_SLOT_COUNT + rawIndex;
        }

        return -1;
    }

    private ItemGridSlot[] buildLootSlots(@NonNull List<ItemStack> loot, int minCapacity) {
        int capacity = Math.max(minCapacity, loot.size());
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
        if (itemId == null || itemId.isBlank()) {
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

}
