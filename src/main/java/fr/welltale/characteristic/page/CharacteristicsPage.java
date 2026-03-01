package fr.welltale.characteristic.page;

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
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import fr.welltale.characteristic.Characteristics;
import fr.welltale.constant.Constant;
import fr.welltale.inventory.InventoryService;
import fr.welltale.inventory.page.InventoryPage;
import fr.welltale.level.PlayerLevelComponent;
import fr.welltale.level.XPTable;
import fr.welltale.player.PlayerRepository;
import fr.welltale.player.charactercache.CachedCharacter;
import fr.welltale.player.charactercache.CharacterCacheRepository;
import org.jspecify.annotations.NonNull;

import java.util.Locale;

public class CharacteristicsPage extends InteractiveCustomUIPage<CharacteristicsPage.CharacteristicsEventData> {
    private static final String ACTION_CLOSE = "CLOSE";
    private static final String ACTION_SWITCH_TAB = "SWITCH_TAB";
    private static final String ACTION_ADD_CHARACTERISTIC = "ADD_CHARACTERISTIC";

    private static final String TAB_CHARACTERISTICS = "CHARACTERISTICS";
    private static final String TAB_INVENTORY = "INVENTORY";

    private static final String CHARACTERISTIC_HEALTH = "HEALTH";
    private static final String CHARACTERISTIC_WISDOM = "WISDOM";
    private static final String CHARACTERISTIC_STRENGTH = "STRENGTH";
    private static final String CHARACTERISTIC_INTELLIGENCE = "INTELLIGENCE";
    private static final String CHARACTERISTIC_CHANCE = "CHANCE";
    private static final String CHARACTERISTIC_AGILITY = "AGILITY";

    private final PlayerRef playerRef;
    private final InventoryService inventoryService;
    private final CharacterCacheRepository characterCacheRepository;
    private final PlayerRepository playerRepository;
    private final HytaleLogger logger;

    private boolean closed;

    public static class CharacteristicsEventData {
        public String action;
        public String area;

        public static final BuilderCodec<CharacteristicsEventData> CODEC =
                BuilderCodec.builder(CharacteristicsEventData.class, CharacteristicsEventData::new)
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
                        .build();
    }

    public CharacteristicsPage(
            @NonNull PlayerRef playerRef,
            @NonNull InventoryService inventoryService,
            @NonNull CharacterCacheRepository characterCacheRepository, PlayerRepository playerRepository,
            @NonNull HytaleLogger logger
    ) {
        super(playerRef, CustomPageLifetime.CanDismiss, CharacteristicsEventData.CODEC);
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

        cmd.append("Pages/Characteristics/CharacteristicsPage.ui");
        bindActivating(event, "#TabInventory", ACTION_SWITCH_TAB, TAB_INVENTORY);
        bindActivating(event, "#TabCharacteristics", ACTION_SWITCH_TAB, TAB_CHARACTERISTICS);
        bindActivating(event, "#AddHealthButton", ACTION_ADD_CHARACTERISTIC, CHARACTERISTIC_HEALTH);
        bindActivating(event, "#AddWisdomButton", ACTION_ADD_CHARACTERISTIC, CHARACTERISTIC_WISDOM);
        bindActivating(event, "#AddStrengthButton", ACTION_ADD_CHARACTERISTIC, CHARACTERISTIC_STRENGTH);
        bindActivating(event, "#AddIntelligenceButton", ACTION_ADD_CHARACTERISTIC, CHARACTERISTIC_INTELLIGENCE);
        bindActivating(event, "#AddChanceButton", ACTION_ADD_CHARACTERISTIC, CHARACTERISTIC_CHANCE);
        bindActivating(event, "#AddAgilityButton", ACTION_ADD_CHARACTERISTIC, CHARACTERISTIC_AGILITY);
        applyState(cmd, ref, store);
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

    @Override
    public void handleDataEvent(
            @NonNull Ref<EntityStore> ref,
            @NonNull Store<EntityStore> store,
            @NonNull CharacteristicsEventData data
    ) {
        if (!ref.isValid()) return;

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        if (ACTION_CLOSE.equals(data.action)) {
            this.closed = true;
            player.getPageManager().setPage(ref, store, Page.None);
            return;
        }

        if (ACTION_SWITCH_TAB.equals(data.action) && TAB_INVENTORY.equals(data.area)) {
            this.closed = true;
            player.getPageManager().openCustomPage(
                    ref,
                    store,
                    new InventoryPage(this.playerRef, this.inventoryService, this.characterCacheRepository, this.playerRepository, this.logger)
            );
            return;
        }

        if (ACTION_ADD_CHARACTERISTIC.equals(data.action) && data.area != null) {
            spendCharacteristicPoint(ref, store, data.area);
            safeSendUpdate(ref, store);
            return;
        }

        safeSendUpdate(ref, store);
    }

    @Override
    public void onDismiss(@NonNull Ref<EntityStore> ref, @NonNull Store<EntityStore> store) {
        this.closed = true;
    }

    private void safeSendUpdate(@NonNull Ref<EntityStore> ref, @NonNull Store<EntityStore> store) {
        if (closed) return;

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;

        if (player.getPageManager().getCustomPage() != this) return;

        this.rebuild();
    }

    private void applyState(@NonNull UICommandBuilder cmd, @NonNull Ref<EntityStore> ref, @NonNull Store<EntityStore> store) {
        if (!ref.isValid()) {
            this.logger.atSevere().log("[CHARACTERISTIC] CharacteristicsPage ApplyState Failed: Ref is invalid");
            return;
        }

        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent == null) {
            this.logger.atSevere().log("[CHARACTERISTIC] CharacteristicsPage ApplyState Failed: UUIDComponent is null");
            return;
        }

        CachedCharacter cachedCharacter = this.characterCacheRepository.getCharacterCache(uuidComponent.getUuid());
        if (cachedCharacter == null) {
            this.logger.atSevere().log("[CHARACTERISTIC] CharacteristicsPage ApplyState Failed: CachedCharacter is null");
            return;
        }

        int points = cachedCharacter.getCharacteristicPoints();
        Characteristics.EditableCharacteristics editable = cachedCharacter.getEditableCharacteristics();
        PlayerLevelComponent playerLevelComponent = store.getComponent(ref, PlayerLevelComponent.getComponentType());
        if (playerLevelComponent == null) {
            fr.welltale.player.Player playerData = this.playerRepository.getPlayer(uuidComponent.getUuid());
            if (playerData == null) {
                this.logger.atSevere().log("[CHARACTERISTIC] CharacteristicsPage ApplyState Failed: PlayerData is null");
                return;
            }
            fr.welltale.player.Player.Character currentCharacter = playerData.getCharacters().stream()
                    .filter(c -> c.getCharacterUuid().equals(cachedCharacter.getPlayerUuid()))
                    .findFirst()
                    .orElse(null);
            if (currentCharacter == null) {
                this.logger.atSevere().log("[CHARACTERISTIC] CharacteristicsPage ApplyState Failed: Character is null");
                return;
            }

            playerLevelComponent = new PlayerLevelComponent(currentCharacter.getExperience());
            store.addComponent(ref, PlayerLevelComponent.getComponentType(), playerLevelComponent);
        }

        long totalXp;
        int level;
        long currentLevelXp;
        long xpToNextLevel;
        float progressToNextLevel;

        totalXp = Math.max(0L, playerLevelComponent.getTotalExperience());
        level = XPTable.getLevelForXP(totalXp);
        currentLevelXp = XPTable.getXPInCurrentLevel(totalXp);
        xpToNextLevel = XPTable.getXPToNextLevel(totalXp);
        progressToNextLevel = XPTable.getProgressToNextLevel(totalXp);

        Characteristics.AdditionalCharacteristics additionalCharacteristics = Characteristics.getAdditionalCharacteristicsFromPlayer(ref, store);

        cmd.set("#LevelLabel.Text", Constant.Prefix.LEVEL_PREFIX.toUpperCase() + level);
        cmd.set("#XpLabel.Text", currentLevelXp + " / " + xpToNextLevel + " XP");
        cmd.set("#XpProgressBar.Value", progressToNextLevel);
        cmd.set("#CharacteristicPointsLabel.Text", String.valueOf(points));
        cmd.set("#HealthStatValueLabel.Text", String.valueOf(editable != null ? editable.getHealth() : 0));
        cmd.set("#WisdomStatValueLabel.Text", String.valueOf(editable != null ? editable.getWisdom() : 0));
        cmd.set("#StrengthStatValueLabel.Text", String.valueOf(editable != null ? editable.getStrength() : 0));
        cmd.set("#IntelligenceStatValueLabel.Text", String.valueOf(editable != null ? editable.getIntelligence() : 0));
        cmd.set("#ChanceStatValueLabel.Text", String.valueOf(editable != null ? editable.getChance() : 0));
        cmd.set("#AgilityStatValueLabel.Text", String.valueOf(editable != null ? editable.getAgility() : 0));
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
    }

    private String formatPercent(float value) {
        return String.format(Locale.ROOT, "%.1f%%", value);
    }

    private void spendCharacteristicPoint(@NonNull Ref<EntityStore> ref, @NonNull Store<EntityStore> store, @NonNull String characteristicType) {
        if (!ref.isValid()) {
            this.logger.atSevere().log("[CHARACTERISTIC] CharacteristicsPage SpendCharacteristicPoint Failed: Ref is invalid");
            return;
        }

        UUIDComponent uuidComponent = store.getComponent(ref, UUIDComponent.getComponentType());
        if (uuidComponent == null) {
            this.logger.atSevere().log("[CHARACTERISTIC] CharacteristicsPage SpendCharacteristicPoint Failed: UUIDComponent is null");
            return;
        }

        CachedCharacter cachedCharacter = this.characterCacheRepository.getCharacterCache(uuidComponent.getUuid());
        if (cachedCharacter == null) {
            this.logger.atSevere().log("[CHARACTERISTIC] CharacteristicsPage SpendCharacteristicPoint Failed: CachedCharacter is null");
            return;
        }

        if (cachedCharacter.getCharacteristicPoints() <= 0) return;

        Characteristics.EditableCharacteristics editableCharacteristics = cachedCharacter.getEditableCharacteristics();
        if (editableCharacteristics == null) {
            editableCharacteristics = new Characteristics.EditableCharacteristics();
            cachedCharacter.setEditableCharacteristics(editableCharacteristics);
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

        cachedCharacter.setCharacteristicPoints(cachedCharacter.getCharacteristicPoints() - 1);
        Characteristics.setCharacteristicsToPlayer(ref, store, editableCharacteristics);

        try {
            this.characterCacheRepository.updateCharacter(cachedCharacter);
        } catch (Exception e) {
            this.logger.atSevere().log("[CHARACTERISTIC] CharacteristicsPage SpendCharacteristicPoint: " + e.getMessage());
        }
    }
}
