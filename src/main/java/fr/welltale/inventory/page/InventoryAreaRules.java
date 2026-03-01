package fr.welltale.inventory.page;

import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

final class InventoryAreaRules {
    static final String AREA_HOTBAR = "HOTBAR";
    static final String AREA_STORAGE = "STORAGE";
    static final String AREA_LOOT = "LOOT";
    static final String AREA_EQUIPMENT_HEAD = "EQUIP_HEAD";
    static final String AREA_EQUIPMENT_WEAPON = "EQUIP_WEAPON";
    static final String AREA_EQUIPMENT_AMULET = "EQUIP_AMULET";
    static final String AREA_EQUIPMENT_GAUNTLETS = "EQUIP_GAUNTLETS";
    static final String AREA_EQUIPMENT_RING_1 = "EQUIP_RING_1";
    static final String AREA_EQUIPMENT_RING_2 = "EQUIP_RING_2";
    static final String AREA_EQUIPMENT_CHEST = "EQUIP_CHEST";
    static final String AREA_EQUIPMENT_PANTS = "EQUIP_PANTS";
    static final String AREA_EQUIPMENT_BELT = "EQUIP_BELT";
    static final String AREA_EQUIPMENT_PET = "EQUIP_PET";
    static final String AREA_EQUIPMENT_BOOTS = "EQUIP_BOOTS";
    static final String AREA_EQUIPMENT_TROPHY_1 = "EQUIP_TROPHY_1";
    static final String AREA_EQUIPMENT_TROPHY_2 = "EQUIP_TROPHY_2";
    static final String AREA_EQUIPMENT_TROPHY_3 = "EQUIP_TROPHY_3";
    static final String AREA_EQUIPMENT_TROPHY_4 = "EQUIP_TROPHY_4";
    static final String AREA_EQUIPMENT_TROPHY_5 = "EQUIP_TROPHY_5";
    static final String AREA_EQUIPMENT_TROPHY_6 = "EQUIP_TROPHY_6";

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int ARMOR_SLOT_HEAD = 0;
    private static final int ARMOR_SLOT_CHEST = 1;
    private static final int ARMOR_SLOT_GAUNTLETS = 2;
    private static final int ARMOR_SLOT_PANTS = 3;

    static final String[] EQUIPMENT_AREAS = {
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

    static final String[][] EQUIPMENT_GRID_BINDINGS = {
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

    private static final Map<String, Integer> EQUIPMENT_SLOT_BY_AREA = buildEquipmentSlotByArea();
    private static final Set<String> EQUIPMENT_AREA_SET = EQUIPMENT_SLOT_BY_AREA.keySet();

    private InventoryAreaRules() {}

    static boolean isInventoryArea(String area) {
        return AREA_HOTBAR.equals(area) || AREA_STORAGE.equals(area);
    }

    static int toInventoryIndex(String area, int rawIndex) {
        if (rawIndex < 0) return -1;
        if (AREA_HOTBAR.equals(area)) return rawIndex;
        if (AREA_STORAGE.equals(area)) return HOTBAR_SLOT_COUNT + rawIndex;
        return -1;
    }

    static int toInventorySectionId(String area) {
        if (AREA_HOTBAR.equals(area)) return Inventory.HOTBAR_SECTION_ID;
        if (AREA_STORAGE.equals(area)) return Inventory.STORAGE_SECTION_ID;
        if (AREA_EQUIPMENT_WEAPON.equals(area)) return Inventory.HOTBAR_SECTION_ID;
        if (AREA_EQUIPMENT_HEAD.equals(area)
                || AREA_EQUIPMENT_CHEST.equals(area)
                || AREA_EQUIPMENT_GAUNTLETS.equals(area)
                || AREA_EQUIPMENT_PANTS.equals(area)) {
            return Inventory.ARMOR_SECTION_ID;
        }
        return Integer.MIN_VALUE;
    }

    static int toInventorySectionSlot(String area, int slot) {
        if (AREA_HOTBAR.equals(area) || AREA_STORAGE.equals(area)) return slot;
        if (AREA_EQUIPMENT_WEAPON.equals(area)) return 0;
        if (AREA_EQUIPMENT_HEAD.equals(area)) return ARMOR_SLOT_HEAD;
        if (AREA_EQUIPMENT_CHEST.equals(area)) return ARMOR_SLOT_CHEST;
        if (AREA_EQUIPMENT_GAUNTLETS.equals(area)) return ARMOR_SLOT_GAUNTLETS;
        if (AREA_EQUIPMENT_PANTS.equals(area)) return ARMOR_SLOT_PANTS;
        return -1;
    }

    static boolean isLinkedEquipmentArea(String area) {
        return AREA_EQUIPMENT_WEAPON.equals(area)
                || AREA_EQUIPMENT_HEAD.equals(area)
                || AREA_EQUIPMENT_CHEST.equals(area)
                || AREA_EQUIPMENT_GAUNTLETS.equals(area)
                || AREA_EQUIPMENT_PANTS.equals(area);
    }

    static boolean isEquipmentArea(String area) {
        return area != null && EQUIPMENT_AREA_SET.contains(area);
    }

    static int toEquipmentSlotIndex(String area) {
        if (area == null) return -1;
        Integer index = EQUIPMENT_SLOT_BY_AREA.get(area);
        return index == null ? -1 : index;
    }

    static boolean canEquip(String area, ItemStack stack) {
        if (ItemStack.isEmpty(stack)) return true;

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

    private static Map<String, Integer> buildEquipmentSlotByArea() {
        Map<String, Integer> byArea = new HashMap<>(EQUIPMENT_AREAS.length);
        for (int i = 0; i < EQUIPMENT_AREAS.length; i++) {
            byArea.put(EQUIPMENT_AREAS[i], i);
        }
        return byArea;
    }

    private static boolean containsAny(String value, String... needles) {
        for (String needle : needles) {
            if (value.contains(needle)) return true;
        }
        return false;
    }
}
