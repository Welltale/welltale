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
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.ui.ItemGridSlot;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.characteristic.Characteristics;
import fr.welltale.inventory.CustomInventoryService;
import fr.welltale.player.PlayerRepository;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.lang.reflect.Method;

public class CustomInventoryPage extends InteractiveCustomUIPage<CustomInventoryPage.CustomInventoryEventData> {
    private static final int STORAGE_SLOT_COUNT = 36;
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int INVENTORY_SLOT_COUNT = HOTBAR_SLOT_COUNT + STORAGE_SLOT_COUNT;
    private static final int LOOT_SLOT_COUNT = 12;

    private static final String ACTION_COLLECT_ALL = "COLLECT_ALL";
    private static final String ACTION_CLOSE = "CLOSE";
    private static final String ACTION_SLOT_CLICK = "SLOT_CLICK";
    private static final String ACTION_SWITCH_TAB = "SWITCH_TAB";
    private static final String ACTION_ADD_CHARACTERISTIC = "ADD_CHARACTERISTIC";

    private static final String AREA_HOTBAR = "HOTBAR";
    private static final String AREA_STORAGE = "STORAGE";
    private static final String AREA_WEAPON = "WEAPON";
    private static final String AREA_LOOT = "LOOT";

    private static final String TAB_CHARACTERISTICS = "CHARACTERISTICS";
    private static final String TAB_INVENTORY = "INVENTORY";
    private static final String TAB_QUESTS = "QUESTS";
    private static final String TAB_GUILD = "GUILD";

    private static final String CHARACTERISTIC_HEALTH = "HEALTH";
    private static final String CHARACTERISTIC_WISDOM = "WISDOM";
    private static final String CHARACTERISTIC_STRENGTH = "STRENGTH";
    private static final String CHARACTERISTIC_INTELLIGENCE = "INTELLIGENCE";
    private static final String CHARACTERISTIC_CHANCE = "CHANCE";
    private static final String CHARACTERISTIC_AGILITY = "AGILITY";

    private final CustomInventoryService customInventoryService;
    private final PlayerRepository playerRepository;
    private final HytaleLogger logger;

    private ItemStack cursorStack;
    private String selectedTab = TAB_INVENTORY;
    private String lastSlotActionKey;
    private long lastSlotActionAt;
    private boolean closed;
    private String lastRawEventPayload;

    public static class CustomInventoryEventData {
        public String action;
        public String area;
        public String index;
        public String slotIndex;
        public String inventorySlotIndex;
        public String itemStackId;
        public String inventorySectionId;
        public String fromSectionId;
        public String fromSlotId;
        public String toSectionId;
        public String toSlotId;

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
                                new KeyedCodec<>("Index", Codec.STRING),
                                (obj, val) -> obj.index = val,
                                obj -> obj.index
                        )
                        .add()
                        .append(
                                new KeyedCodec<>("SlotIndex", Codec.STRING),
                                (obj, val) -> obj.slotIndex = val,
                                obj -> obj.slotIndex
                        )
                        .add()
                        .append(
                                new KeyedCodec<>("InventorySlotIndex", Codec.STRING),
                                (obj, val) -> obj.inventorySlotIndex = val,
                                obj -> obj.inventorySlotIndex
                        )
                        .add()
                        .append(
                                new KeyedCodec<>("ItemStackId", Codec.STRING),
                                (obj, val) -> obj.itemStackId = val,
                                obj -> obj.itemStackId
                        )
                        .add()
                        .append(
                                new KeyedCodec<>("InventorySectionId", Codec.STRING),
                                (obj, val) -> obj.inventorySectionId = val,
                                obj -> obj.inventorySectionId
                        )
                        .add()
                        .append(
                                new KeyedCodec<>("FromSectionId", Codec.STRING),
                                (obj, val) -> obj.fromSectionId = val,
                                obj -> obj.fromSectionId
                        )
                        .add()
                        .append(
                                new KeyedCodec<>("FromSlotId", Codec.STRING),
                                (obj, val) -> obj.fromSlotId = val,
                                obj -> obj.fromSlotId
                        )
                        .add()
                        .append(
                                new KeyedCodec<>("ToSectionId", Codec.STRING),
                                (obj, val) -> obj.toSectionId = val,
                                obj -> obj.toSectionId
                        )
                        .add()
                        .append(
                                new KeyedCodec<>("ToSlotId", Codec.STRING),
                                (obj, val) -> obj.toSlotId = val,
                                obj -> obj.toSlotId
                        )
                        .add()
                        .build();
    }

    public CustomInventoryPage(
            @NonNull PlayerRef playerRef,
            @NonNull CustomInventoryService customInventoryService,
            @NonNull PlayerRepository playerRepository,
            @NonNull HytaleLogger logger
    ) {
        super(playerRef, CustomPageLifetime.CanDismiss, CustomInventoryEventData.CODEC);
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

        cmd.append("Pages/Inventory/CustomInventoryPage.ui");
        event.addEventBinding(CustomUIEventBindingType.Activating, "#CollectAllButton", EventData.of("Action", ACTION_COLLECT_ALL));
        event.addEventBinding(CustomUIEventBindingType.Activating, "#CloseButton", EventData.of("Action", ACTION_CLOSE));
        event.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#TabCharacteristics",
                new EventData().append("Action", ACTION_SWITCH_TAB).append("Area", TAB_CHARACTERISTICS),
                false
        );
        event.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#TabInventory",
                new EventData().append("Action", ACTION_SWITCH_TAB).append("Area", TAB_INVENTORY),
                false
        );
        event.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#TabQuests",
                new EventData().append("Action", ACTION_SWITCH_TAB).append("Area", TAB_QUESTS),
                false
        );
        event.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#TabGuild",
                new EventData().append("Action", ACTION_SWITCH_TAB).append("Area", TAB_GUILD),
                false
        );
        event.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#AddHealthButton",
                new EventData().append("Action", ACTION_ADD_CHARACTERISTIC).append("Area", CHARACTERISTIC_HEALTH),
                false
        );
        event.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#AddWisdomButton",
                new EventData().append("Action", ACTION_ADD_CHARACTERISTIC).append("Area", CHARACTERISTIC_WISDOM),
                false
        );
        event.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#AddStrengthButton",
                new EventData().append("Action", ACTION_ADD_CHARACTERISTIC).append("Area", CHARACTERISTIC_STRENGTH),
                false
        );
        event.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#AddIntelligenceButton",
                new EventData().append("Action", ACTION_ADD_CHARACTERISTIC).append("Area", CHARACTERISTIC_INTELLIGENCE),
                false
        );
        event.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#AddChanceButton",
                new EventData().append("Action", ACTION_ADD_CHARACTERISTIC).append("Area", CHARACTERISTIC_CHANCE),
                false
        );
        event.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#AddAgilityButton",
                new EventData().append("Action", ACTION_ADD_CHARACTERISTIC).append("Area", CHARACTERISTIC_AGILITY),
                false
        );
        event.addEventBinding(CustomUIEventBindingType.SlotClicking, "#WeaponGrid", new EventData().append("Action", ACTION_SLOT_CLICK).append("Area", AREA_WEAPON), false);
        event.addEventBinding(CustomUIEventBindingType.SlotClicking, "#HotbarGrid", new EventData().append("Action", ACTION_SLOT_CLICK).append("Area", AREA_HOTBAR), false);
        event.addEventBinding(CustomUIEventBindingType.SlotClicking, "#StorageGrid", new EventData().append("Action", ACTION_SLOT_CLICK).append("Area", AREA_STORAGE), false);
        event.addEventBinding(CustomUIEventBindingType.SlotClicking, "#LootGrid", new EventData().append("Action", ACTION_SLOT_CLICK).append("Area", AREA_LOOT), false);
        applyState(cmd, ref, store);
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

        if (ACTION_SLOT_CLICK.equals(data.action)) {
            int resolvedIndex = resolveIndex(data);
            if (resolvedIndex < 0) {
                resolvedIndex = resolveIndexFromRawPayload(lastRawEventPayload);
            }

            if (resolvedIndex < 0) {
                this.logger.atInfo().log("[INVENTORY] Ignored slot event with unresolved index area=" + data.area
                        + " index=" + data.index
                        + " slotIndex=" + data.slotIndex
                        + " inventorySlotIndex=" + data.inventorySlotIndex
                        + " raw=" + lastRawEventPayload);
                return;
            }

            this.logger.atInfo().log("[INVENTORY] Slot event area=" + data.area
                    + " resolvedIndex=" + resolvedIndex
                    + " index=" + data.index
                    + " slotIndex=" + data.slotIndex
                    + " inventorySlotIndex=" + data.inventorySlotIndex
                    + " cursorEmpty=" + ItemStack.isEmpty(cursorStack));

            handleSlotClick(ref, store, data.area, resolvedIndex);
            return;
        }

        if (ACTION_SWITCH_TAB.equals(data.action)) {
            if (data.area != null) {
                this.selectedTab = data.area;
            }
            safeSendUpdate(ref, store);
            return;
        }

        if (ACTION_ADD_CHARACTERISTIC.equals(data.action)) {
            if (data.area != null) {
                spendCharacteristicPoint(ref, store, data.area);
            }
            safeSendUpdate(ref, store);
            return;
        }

        safeSendUpdate(ref, store);
    }

    private void collectAllLoot(@NonNull Ref<EntityStore> ref, @NonNull Store<EntityStore> store, @NonNull Player player) {
        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent == null) {
            this.logger.atSevere().log("[INVENTORY] CollectAll failed: player UUID is null");
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
        if (collectedStacks > 0) {
            player.sendMessage(Message.raw("Butin collecte: " + collectedStacks + " pile(s)"));
        }

        safeSendUpdate(ref, store);
    }

    private UICommandBuilder buildStateUpdate(@NonNull Ref<EntityStore> ref, @NonNull Store<EntityStore> store) {
        UICommandBuilder update = new UICommandBuilder();
        applyState(update, ref, store);
        return update;
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
        this.logger.atInfo().log("[INVENTORY] Raw event payload: " + rawData);
        super.handleDataEvent(ref, store, rawData);
    }

    @Override
    public void onDismiss(@NonNull Ref<EntityStore> ref, @NonNull Store<EntityStore> store) {
        this.closed = true;
        this.cursorStack = null;
        this.logger.atInfo().log("[INVENTORY] Custom inventory page dismissed");
    }

    private void handleSlotClick(@NonNull Ref<EntityStore> ref, @NonNull Store<EntityStore> store, String area, int index) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;
        if (index < 0) return;

        long now = System.currentTimeMillis();
        String actionKey = area + ":" + index + ":" + (ItemStack.isEmpty(cursorStack) ? "EMPTY" : "HELD");
        if (actionKey.equals(lastSlotActionKey) && now - lastSlotActionAt < 80) {
            return;
        }
        lastSlotActionKey = actionKey;
        lastSlotActionAt = now;

        if (cursorStack == null || ItemStack.isEmpty(cursorStack)) {
            cursorStack = removeStackFromArea(ref, store, area, index);
            safeSendUpdate(ref, store);
            return;
        }

        ItemStack target = getStackFromArea(ref, store, area, index);
        if (ItemStack.isEmpty(target)) {
            boolean placed = setStackToArea(ref, store, area, index, cursorStack);
            if (placed) {
                cursorStack = null;
            }
            safeSendUpdate(ref, store);
            return;
        }

        boolean placed = setStackToArea(ref, store, area, index, cursorStack);
        if (!placed) {
            safeSendUpdate(ref, store);
            return;
        }

        cursorStack = target;
        safeSendUpdate(ref, store);
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

        if (AREA_LOOT.equals(area)) {
            UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
            if (uuidComponent == null) return null;

            List<ItemStack> loot = customInventoryService.getLootSnapshot(uuidComponent.getUuid());
            return index >= 0 && index < loot.size() ? loot.get(index) : null;
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

        return false;
    }

    private void compactLoot(List<ItemStack> loot) {
        loot.removeIf(item -> ItemStack.isEmpty(item));
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

    private int resolveIndexFromRawPayload(String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) return -1;

        String lower = rawPayload.toLowerCase();
        int cursor = 0;
        while (true) {
            int idx = lower.indexOf("index", cursor);
            if (idx < 0) return -1;

            int colon = rawPayload.indexOf(':', idx);
            if (colon < 0) return -1;

            int start = colon + 1;
            while (start < rawPayload.length() && Character.isWhitespace(rawPayload.charAt(start))) {
                start++;
            }

            if (start < rawPayload.length() && rawPayload.charAt(start) == '"') {
                start++;
            }

            int end = start;
            while (end < rawPayload.length() && Character.isDigit(rawPayload.charAt(end))) {
                end++;
            }

            if (end > start) {
                try {
                    return Integer.parseInt(rawPayload.substring(start, end));
                } catch (Exception ignored) {
                }
            }

            cursor = idx + 5;
        }
    }

    private void applyState(@NonNull UICommandBuilder cmd, @NonNull Ref<EntityStore> ref, @NonNull Store<EntityStore> store) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent == null) return;

        Inventory inventory = player.getInventory();
        List<ItemStack> loot = customInventoryService.getLootSnapshot(uuidComponent.getUuid());

        cmd.set("#WeaponGrid.Slots", new ItemGridSlot[]{createSafeLootSlot(getInventoryItem(inventory, 0))});
        cmd.set("#ArmorGrid.Slots", buildEquipmentSlots(inventory, new String[]{"getArmor", "getArmorSection"}, 4));
        cmd.set("#UtilityGrid.Slots", buildEquipmentSlots(inventory, new String[]{"getUtility", "getUtilitySection"}, 3));
        cmd.set("#HotbarGrid.Slots", buildInventorySlots(inventory, true));
        cmd.set("#StorageGrid.Slots", buildInventorySlots(inventory, false));
        cmd.set("#LootGrid.Slots", buildLootSlots(loot, LOOT_SLOT_COUNT));

        cmd.set("#InventoryCountLabel.Text", String.valueOf(STORAGE_SLOT_COUNT + HOTBAR_SLOT_COUNT));
        cmd.set("#LootCountLabel.Text", String.valueOf(loot.size()));
        cmd.set("#CursorLabel.Text", ItemStack.isEmpty(cursorStack) ? "CURSEUR: vide" : "CURSEUR: " + formatStack(cursorStack));

        fr.welltale.player.Player playerData = getPlayerData(ref, store);
        int points = playerData != null ? playerData.getCharacteristicPoints() : 0;
        Characteristics.EditableCharacteristics editable = playerData != null ? playerData.getEditableCharacteristics() : null;
        int podsFromCharacteristics = Characteristics.DEFAULT_PODS + ((editable != null ? editable.getStrength() : 0) * Characteristics.STRENGTH_TO_PODS);

        Characteristics.AdditionalCharacteristics additionalCharacteristics = Characteristics.getAdditionalCharacteristicsFromPlayer(ref, store);
        cmd.set("#PodsValueLabel.Text", String.valueOf(podsFromCharacteristics));

        cmd.set("#CharacteristicPointsLabel.Text", String.valueOf(points));
        cmd.set("#HealthStatValueLabel.Text", String.valueOf(editable != null ? editable.getHealth() : 0));
        cmd.set("#WisdomStatValueLabel.Text", String.valueOf(editable != null ? editable.getWisdom() : 0));
        cmd.set("#StrengthStatValueLabel.Text", String.valueOf(editable != null ? editable.getStrength() : 0));
        cmd.set("#IntelligenceStatValueLabel.Text", String.valueOf(editable != null ? editable.getIntelligence() : 0));
        cmd.set("#ChanceStatValueLabel.Text", String.valueOf(editable != null ? editable.getChance() : 0));
        cmd.set("#AgilityStatValueLabel.Text", String.valueOf(editable != null ? editable.getAgility() : 0));
        cmd.set("#PodsTotalStatValueLabel.Text", String.valueOf(podsFromCharacteristics));
        cmd.set("#LifeRegenStatValueLabel.Text", formatPercent(additionalCharacteristics.getLifeRegenPct()));
        cmd.set("#DropChanceStatValueLabel.Text", formatPercent(additionalCharacteristics.getDropChance()));
        cmd.set("#MoveSpeedStatValueLabel.Text", formatPercent(additionalCharacteristics.getMoveSpeed()));
        cmd.set("#StaminaStatValueLabel.Text", String.valueOf(additionalCharacteristics.getStamina()));
        cmd.set("#CriticalDamageStatValueLabel.Text", String.valueOf(additionalCharacteristics.getCriticalDamage()));
        cmd.set("#CriticalPctStatValueLabel.Text", formatPercent(additionalCharacteristics.getCriticalPct()));
        cmd.set("#CriticalResistanceStatValueLabel.Text", String.valueOf(additionalCharacteristics.getCriticalResistance()));
        cmd.set("#EarthResistanceStatValueLabel.Text", formatPercent(additionalCharacteristics.getEarthResistance()));
        cmd.set("#FireResistanceStatValueLabel.Text", formatPercent(additionalCharacteristics.getFireResistance()));
        cmd.set("#WaterResistanceStatValueLabel.Text", formatPercent(additionalCharacteristics.getWaterResistance()));
        cmd.set("#AirResistanceStatValueLabel.Text", formatPercent(additionalCharacteristics.getAirResistance()));

        boolean showCharacteristics = TAB_CHARACTERISTICS.equals(selectedTab);
        boolean showInventory = TAB_INVENTORY.equals(selectedTab);

        cmd.set("#CharacteristicsPanel.Visible", showCharacteristics);
        cmd.set("#MainInventorySection.Visible", showInventory);
        cmd.set("#AltPanel.Visible", !showInventory && !showCharacteristics);

        if (TAB_QUESTS.equals(selectedTab)) {
            cmd.set("#AltPanelTitle.Text", "QUETES");
            cmd.set("#AltPanelDescription.Text", "Journal de quetes - bientot disponible");
        } else if (TAB_GUILD.equals(selectedTab)) {
            cmd.set("#AltPanelTitle.Text", "GUILDE");
            cmd.set("#AltPanelDescription.Text", "Gestion de guilde - bientot disponible");
        } else {
            cmd.set("#AltPanelTitle.Text", "");
            cmd.set("#AltPanelDescription.Text", "");
        }
    }

    private ItemStack getInventoryItem(Inventory inventory, int index) {
        if (index < HOTBAR_SLOT_COUNT) {
            return inventory.getHotbar().getItemStack((short) index);
        }

        int storageIndex = index - HOTBAR_SLOT_COUNT;
        return inventory.getStorage().getItemStack((short) storageIndex);
    }

    private boolean isInventoryArea(String area) {
        return AREA_HOTBAR.equals(area) || AREA_STORAGE.equals(area) || AREA_WEAPON.equals(area);
    }

    private int toInventoryIndex(String area, int rawIndex) {
        if (rawIndex < 0) return -1;
        if (AREA_HOTBAR.equals(area)) {
            return rawIndex;
        }

        if (AREA_WEAPON.equals(area)) {
            return 0;
        }

        if (AREA_STORAGE.equals(area)) {
            return HOTBAR_SLOT_COUNT + rawIndex;
        }

        return -1;
    }

    private ItemGridSlot[] buildLootSlots(@NonNull List<ItemStack> loot, int capacity) {
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

    private ItemGridSlot[] buildEmptySlots(int count) {
        ItemGridSlot[] slots = new ItemGridSlot[count];
        for (int i = 0; i < count; i++) {
            slots[i] = new ItemGridSlot();
        }

        return slots;
    }

    private ItemGridSlot[] buildEquipmentSlots(@NonNull Inventory inventory, String[] getterCandidates, int capacity) {
        Object section = resolveSection(inventory, getterCandidates);
        if (section == null) {
            return buildEmptySlots(capacity);
        }

        ItemGridSlot[] slots = new ItemGridSlot[capacity];
        for (int i = 0; i < capacity; i++) {
            slots[i] = createSafeLootSlot(readSectionStack(section, i));
        }

        return slots;
    }

    private Object resolveSection(@NonNull Inventory inventory, String[] getterCandidates) {
        for (String getter : getterCandidates) {
            try {
                Method method = inventory.getClass().getMethod(getter);
                return method.invoke(inventory);
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private ItemStack readSectionStack(Object section, int slotIndex) {
        try {
            Method getItemStack = section.getClass().getMethod("getItemStack", short.class);
            return (ItemStack) getItemStack.invoke(section, (short) slotIndex);
        } catch (Exception ignored) {
        }

        try {
            Method getItemStackForSlot = section.getClass().getMethod("getItemStackForSlot", short.class);
            return (ItemStack) getItemStackForSlot.invoke(section, (short) slotIndex);
        } catch (Exception ignored) {
        }

        return null;
    }

    private ItemGridSlot createSafeLootSlot(ItemStack itemStack) {
        if (ItemStack.isEmpty(itemStack)) {
            return new ItemGridSlot();
        }

        String itemId = itemStack.getItemId();
        if (itemId == null || itemId.isBlank()) {
            return new ItemGridSlot();
        }

        try {
            return new ItemGridSlot(itemStack);
        } catch (Exception e) {
            this.logger.atSevere().log("[INVENTORY] Invalid loot stack rendered as empty slot: " + itemId);
            return new ItemGridSlot();
        }
    }

    private String formatStack(ItemStack itemStack) {
        if (ItemStack.isEmpty(itemStack)) return "";

        String itemId = itemStack.getItemId();
        int namespaceSeparator = itemId.indexOf(':');
        if (namespaceSeparator >= 0 && namespaceSeparator < itemId.length() - 1) {
            itemId = itemId.substring(namespaceSeparator + 1);
        }

        if (itemId.length() > 10) {
            itemId = itemId.substring(0, 10);
        }

        return itemStack.getQuantity() > 1
                ? itemId + " x" + itemStack.getQuantity()
                : itemId;
    }

    private String formatPercent(float value) {
        return String.format("%.1f%%", value);
    }

    private fr.welltale.player.Player getPlayerData(@NonNull Ref<EntityStore> ref, @NonNull Store<EntityStore> store) {
        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent == null) {
            return null;
        }

        return this.playerRepository.getPlayerByUuid(uuidComponent.getUuid());
    }

    private void spendCharacteristicPoint(@NonNull Ref<EntityStore> ref, @NonNull Store<EntityStore> store, @NonNull String characteristicType) {
        fr.welltale.player.Player playerData = getPlayerData(ref, store);
        if (playerData == null) {
            return;
        }

        if (playerData.getCharacteristicPoints() <= 0) {
            return;
        }

        Characteristics.EditableCharacteristics editableCharacteristics = playerData.getEditableCharacteristics();
        if (editableCharacteristics == null) {
            editableCharacteristics = new Characteristics.EditableCharacteristics();
            playerData.setEditableCharacteristics(editableCharacteristics);
        }

        switch (characteristicType) {
            case CHARACTERISTIC_HEALTH -> editableCharacteristics.setHealth(editableCharacteristics.getHealth() + 1);
            case CHARACTERISTIC_WISDOM -> editableCharacteristics.setWisdom(editableCharacteristics.getWisdom() + 1);
            case CHARACTERISTIC_STRENGTH -> editableCharacteristics.setStrength(editableCharacteristics.getStrength() + 1);
            case CHARACTERISTIC_INTELLIGENCE -> editableCharacteristics.setIntelligence(editableCharacteristics.getIntelligence() + 1);
            case CHARACTERISTIC_CHANCE -> editableCharacteristics.setChance(editableCharacteristics.getChance() + 1);
            case CHARACTERISTIC_AGILITY -> editableCharacteristics.setAgility(editableCharacteristics.getAgility() + 1);
            default -> {
                return;
            }
        }

        playerData.setCharacteristicPoints(playerData.getCharacteristicPoints() - 1);
        Characteristics.setCharacteristicsToPlayer(ref, store, editableCharacteristics);

        try {
            this.playerRepository.updatePlayer(playerData);
        } catch (Exception e) {
            this.logger.atSevere().log("[INVENTORY] Failed to update player characteristics: " + e.getMessage());
        }
    }
}
