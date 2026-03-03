package fr.welltale.item;

import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import fr.welltale.characteristic.Characteristics;
import lombok.NonNull;

import java.util.*;

public class RolledTooltipDescriptionBuilder {
    private static final double ZERO_EPSILON = 0.0001d;
    private static final String TRANSLATION_KEY_PREFIX = "wt.item.roll.description.";
    private static final String SECTION_DAMAGE_TITLE = "Dégâts";
    private static final String SECTION_EFFECTS_TITLE = "Effets";
    private static final String RANGE_SEPARATOR = " à ";

    private static final String DAMAGE_CAUSE_EARTH = "earth";
    private static final String DAMAGE_CAUSE_FIRE = "fire";
    private static final String DAMAGE_CAUSE_WATER = "water";
    private static final String DAMAGE_CAUSE_AIR = "air";

    private static final String DAMAGE_CAUSE_EARTH_TRANSLATION = "Terre";
    private static final String DAMAGE_CAUSE_FIRE_TRANSLATION = "Feu";
    private static final String DAMAGE_CAUSE_WATER_TRANSLATION = "Eau";
    private static final String DAMAGE_CAUSE_AIR_TRANSLATION = "Air";

    private static final String COLOR_STAT_VITALITY = "#df789a";
    private static final String COLOR_STAT_WISDOM = "#a775c7";
    private static final String COLOR_STAT_STRENGTH = "#d59f4b";
    private static final String COLOR_STAT_INTELLIGENCE = "#ff6666";
    private static final String COLOR_STAT_CHANCE = "#63c9ff";
    private static final String COLOR_STAT_AGILITY = "#6bdd89";
    private static final String COLOR_STAT_STAMINA = "#ffcf58";
    private static final String COLOR_STAT_CRITICAL_DAMAGE = "#ff5d5d";
    private static final String COLOR_STAT_CRITICAL_PCT = "#ffb63f";
    private static final String COLOR_STAT_CRITICAL_RESISTANCE = "#e98af3";
    private static final String COLOR_STAT_LIFE_REGEN = "#58e7a3";
    private static final String COLOR_STAT_DROP_CHANCE = "#ffc553";
    private static final String COLOR_STAT_MOVE_SPEED = "#53baff";
    private static final String COLOR_STAT_EARTH = "#d59f4b";
    private static final String COLOR_STAT_FIRE = "#ff6666";
    private static final String COLOR_STAT_WATER = "#63c9ff";
    private static final String COLOR_STAT_AIR = "#6bdd89";

    private static final String STAT_LABEL_VITALITY = "Vie";
    private static final String STAT_LABEL_WISDOM = "Sagesse";
    private static final String STAT_LABEL_STRENGTH = "Force";
    private static final String STAT_LABEL_INTELLIGENCE = "Intelligence";
    private static final String STAT_LABEL_CHANCE = "Chance";
    private static final String STAT_LABEL_AGILITY = "Agilité";
    private static final String STAT_LABEL_STAMINA = "Endurance";
    private static final String STAT_LABEL_CRITICAL_DAMAGE = "Dommages critiques";
    private static final String STAT_LABEL_CRITICAL_PCT = "Critique";
    private static final String STAT_LABEL_CRITICAL_RESISTANCE = "Résistance critique";
    private static final String STAT_LABEL_LIFE_REGEN = "Régénération";
    private static final String STAT_LABEL_DROP_CHANCE = "Prospection";
    private static final String STAT_LABEL_MOVE_SPEED = "Vitesse";
    private static final String STAT_LABEL_EARTH_RESISTANCE = "Résistance terre";
    private static final String STAT_LABEL_FIRE_RESISTANCE = "Résistance feu";
    private static final String STAT_LABEL_WATER_RESISTANCE = "Résistance eau";
    private static final String STAT_LABEL_AIR_RESISTANCE = "Résistance air";

    private static final String TYPE_TOKEN_SWORD = "sword";
    private static final String TYPE_TOKEN_BOW = "bow";
    private static final String TYPE_TOKEN_DAGGER = "dagger";
    private static final String TYPE_TOKEN_AXE = "axe";
    private static final String TYPE_TOKEN_HAMMER = "hammer";
    private static final String TYPE_TOKEN_STAFF = "staff";
    private static final String TYPE_TOKEN_WAND = "wand";
    private static final String TYPE_TOKEN_SPEAR = "spear";
    private static final String TYPE_TOKEN_LANCE = "lance";
    private static final String TYPE_TOKEN_SHIELD = "shield";
    private static final String TYPE_TOKEN_HELMET = "helmet";
    private static final String TYPE_TOKEN_CHEST = "chest";
    private static final String TYPE_TOKEN_TORSO = "torso";
    private static final String TYPE_TOKEN_PANTS = "pants";
    private static final String TYPE_TOKEN_LEGS = "legs";
    private static final String TYPE_TOKEN_BOOTS = "boots";
    private static final String TYPE_TOKEN_RING = "ring";
    private static final String TYPE_TOKEN_AMULET = "amulet";
    private static final String TYPE_TOKEN_NECKLACE = "necklace";
    private static final String TYPE_TOKEN_TOOL = "tool";
    private static final String TYPE_TOKEN_WEAPON = "weapon";

    private static final String TYPE_LABEL_SWORD = "Épée";
    private static final String TYPE_LABEL_BOW = "Arc";
    private static final String TYPE_LABEL_DAGGER = "Dague";
    private static final String TYPE_LABEL_AXE = "Hache";
    private static final String TYPE_LABEL_HAMMER = "Marteau";
    private static final String TYPE_LABEL_STAFF = "Bâton";
    private static final String TYPE_LABEL_WAND = "Baguette";
    private static final String TYPE_LABEL_SPEAR = "Lance";
    private static final String TYPE_LABEL_SHIELD = "Bouclier";
    private static final String TYPE_LABEL_HELMET = "Casque";
    private static final String TYPE_LABEL_CHEST = "Plastron";
    private static final String TYPE_LABEL_PANTS = "Jambières";
    private static final String TYPE_LABEL_BOOTS = "Bottes";
    private static final String TYPE_LABEL_RING = "Anneau";
    private static final String TYPE_LABEL_AMULET = "Amulette";
    private static final String TYPE_LABEL_TOOL = "Outil";
    private static final String TYPE_LABEL_WEAPON = "Arme";

    private final Map<Integer, StatDisplay> displayByStatIndex = new HashMap<>();
    private volatile boolean initialized;

    public TooltipDescription build(
            @NonNull String virtualId,
            @NonNull ItemBase itemBase,
            @NonNull String baseItemId,
            Map<Integer, ItemStatRoller.AmountRange> possibleRangeByStat,
            List<DamageRange> baseDamageRanges
    ) {
        if (virtualId.isBlank()) return null;
        if (!ensureInitialized()) return null;

        Map<Integer, Double> totalsByStat = collectTotals(itemBase);

        ArrayList<StatLine> statLines = new ArrayList<>();
        for (Map.Entry<Integer, Double> entry : totalsByStat.entrySet()) {
            Integer statIndex = entry.getKey();
            Double amount = entry.getValue();
            if (statIndex == null || amount == null || Math.abs(amount) < ZERO_EPSILON) continue;

            StatDisplay display = resolveDisplay(statIndex);
            if (display == null) continue;

            String formattedValue = formatSignedValue(amount, display.percent(), display.colorHex());
            if (formattedValue == null) continue;

            String line = formattedValue + " " + display.label();

            String rangeText = formatPossibleRange(possibleRangeByStat == null ? null : possibleRangeByStat.get(statIndex), display.percent());
            if (rangeText != null) line += " (" + rangeText + ")";

            statLines.add(new StatLine(display.known(), display.knownOrder(), statIndex, line));
        }

        ArrayList<DamageLine> damageLines = buildDamageLines(baseDamageRanges);
        String itemTypeLine = buildItemTypeLine(itemBase, baseItemId);

        if (statLines.isEmpty() && damageLines.isEmpty() && itemTypeLine == null) return null;

        statLines.sort(Comparator
                .comparing(StatLine::known).reversed()
                .thenComparingInt(StatLine::knownOrder)
                .thenComparingInt(StatLine::statIndex)
        );

        damageLines.sort(Comparator
                .comparingInt(DamageLine::order)
                .thenComparing(DamageLine::line)
        );

        StringBuilder description = new StringBuilder(192);

        if (itemTypeLine != null) {
            description.append(itemTypeLine);
        }

        if (!damageLines.isEmpty()) {
            if (!description.isEmpty()) description.append("\n\n");
            description.append(SECTION_DAMAGE_TITLE);
            for (DamageLine line : damageLines) {
                description.append('\n').append(line.line());
            }
        }

        if (!statLines.isEmpty()) {
            if (!description.isEmpty()) description.append("\n\n");
            description.append(SECTION_EFFECTS_TITLE);
            for (StatLine line : statLines) {
                description.append('\n').append(line.line());
            }
        }

        String translationKey = createTranslationKey(virtualId);
        return new TooltipDescription(translationKey, description.toString());
    }

    private ArrayList<DamageLine> buildDamageLines(List<DamageRange> baseDamageRanges) {
        ArrayList<DamageLine> lines = new ArrayList<>();
        if (baseDamageRanges == null || baseDamageRanges.isEmpty()) return lines;

        for (DamageRange range : baseDamageRanges) {
            if (range == null || range.causeId() == null || range.causeId().isBlank()) continue;

            String minText = formatAbsoluteValue(range.min());
            String maxText = formatAbsoluteValue(range.max());
            if (minText == null || maxText == null) continue;

            String damageValue = Math.abs(range.max() - range.min()) < ZERO_EPSILON
                    ? minText
                    : (minText + "-" + maxText);

            String causeLabel = damageCauseLabel(range.causeId());
            String causeColor = damageCauseColorHex(range.causeId());
            int causeOrder = damageCauseOrder(range.causeId());
            lines.add(new DamageLine(causeOrder, colorize(damageValue, causeColor) + " " + SECTION_DAMAGE_TITLE + " " + causeLabel));
        }

        return lines;
    }

    public String createTranslationKey(@NonNull String virtualId) {
        if (virtualId.isBlank()) return TRANSLATION_KEY_PREFIX + "unknown";

        StringBuilder out = new StringBuilder(TRANSLATION_KEY_PREFIX.length() + virtualId.length());
        out.append(TRANSLATION_KEY_PREFIX);

        for (int i = 0; i < virtualId.length(); i++) {
            char c = virtualId.charAt(i);
            if ((c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9')
                    || c == '.'
                    || c == '_'
                    || c == '-') {
                out.append(c);
            } else {
                out.append('_');
            }
        }

        return out.toString();
    }

    private Map<Integer, Double> collectTotals(@NonNull ItemBase itemBase) {
        HashMap<Integer, Double> out = new HashMap<>();
        collectSection(out, itemBase.weapon);
        collectSection(out, itemBase.armor);
        collectSection(out, itemBase.utility);
        return out;
    }

    private void collectSection(@NonNull Map<Integer, Double> out, ItemWeapon weapon) {
        if (weapon == null) return;
        collectModifiers(out, weapon.statModifiers);
    }

    private void collectSection(@NonNull Map<Integer, Double> out, ItemArmor armor) {
        if (armor == null) return;
        collectModifiers(out, armor.statModifiers);
    }

    private void collectSection(@NonNull Map<Integer, Double> out, ItemUtility utility) {
        if (utility == null) return;
        collectModifiers(out, utility.statModifiers);
    }

    private void collectModifiers(@NonNull Map<Integer, Double> out, Map<Integer, Modifier[]> modifiersByStat) {
        if (modifiersByStat == null || modifiersByStat.isEmpty()) return;

        for (Map.Entry<Integer, Modifier[]> entry : modifiersByStat.entrySet()) {
            Integer statIndex = entry.getKey();
            Modifier[] modifiers = entry.getValue();
            if (statIndex == null || modifiers == null || modifiers.length == 0) continue;

            double total = 0.0d;
            for (Modifier modifier : modifiers) {
                if (modifier == null) continue;
                total += modifier.amount;
            }

            if (Math.abs(total) < ZERO_EPSILON) continue;
            out.merge(statIndex, total, Double::sum);
        }
    }

    private String formatSignedValue(double value, boolean percent, String signColorHex) {
        double rounded = Math.round(value * 10.0d) / 10.0d;
        if (Math.abs(rounded) < ZERO_EPSILON) return null;

        String sign = rounded > 0.0d ? "+" : "-";
        double absolute = Math.abs(rounded);
        boolean whole = Math.abs(absolute - Math.rint(absolute)) < ZERO_EPSILON;
        String number = whole
                ? Long.toString(Math.round(absolute))
                : String.format(Locale.ROOT, "%.1f", absolute);

        if (signColorHex == null || signColorHex.isBlank()) return sign + number + (percent ? "%" : "");
        return colorize(sign + number + (percent ? "%" : ""), signColorHex);
    }

    private String formatAbsoluteValue(double value) {
        double rounded = Math.round(value * 10.0d) / 10.0d;
        if (Math.abs(rounded) < ZERO_EPSILON) return null;

        double absolute = Math.abs(rounded);
        boolean whole = Math.abs(absolute - Math.rint(absolute)) < ZERO_EPSILON;
        return whole
                ? Long.toString(Math.round(absolute))
                : String.format(Locale.ROOT, "%.1f", absolute);
    }

    private String formatPossibleRange(ItemStatRoller.AmountRange range, boolean percent) {
        if (range == null) return null;

        double min = range.min();
        double max = range.max();
        if (Math.abs(min) < ZERO_EPSILON && Math.abs(max) < ZERO_EPSILON) return null;

        String minText = formatRangeNumber(min, percent);
        String maxText = formatRangeNumber(max, percent);
        if (minText == null || maxText == null) return null;

        if (Math.abs(max - min) < ZERO_EPSILON) return minText;
        return minText + RANGE_SEPARATOR + maxText;
    }

    private String formatRangeNumber(double value, boolean percent) {
        double rounded = Math.round(value * 10.0d) / 10.0d;
        if (Math.abs(rounded) < ZERO_EPSILON) rounded = 0.0d;

        boolean whole = Math.abs(rounded - Math.rint(rounded)) < ZERO_EPSILON;
        String number = whole
                ? Long.toString(Math.round(rounded))
                : String.format(Locale.ROOT, "%.1f", rounded);

        return percent ? number + "%" : number;
    }

    private String damageCauseLabel(@NonNull String causeId) {
        return switch (causeId.toLowerCase(Locale.ROOT)) {
            case DAMAGE_CAUSE_EARTH -> DAMAGE_CAUSE_EARTH_TRANSLATION;
            case DAMAGE_CAUSE_FIRE -> DAMAGE_CAUSE_FIRE_TRANSLATION;
            case DAMAGE_CAUSE_WATER -> DAMAGE_CAUSE_WATER_TRANSLATION;
            case DAMAGE_CAUSE_AIR -> DAMAGE_CAUSE_AIR_TRANSLATION;
            default -> causeId;
        };
    }

    private int damageCauseOrder(@NonNull String causeId) {
        return switch (causeId.toLowerCase(Locale.ROOT)) {
            case DAMAGE_CAUSE_EARTH -> 0;
            case DAMAGE_CAUSE_FIRE -> 1;
            case DAMAGE_CAUSE_WATER -> 2;
            case DAMAGE_CAUSE_AIR -> 3;
            default -> 10;
        };
    }

    private String damageCauseColorHex(@NonNull String causeId) {
        return switch (causeId.toLowerCase(Locale.ROOT)) {
            case DAMAGE_CAUSE_EARTH -> COLOR_STAT_EARTH;
            case DAMAGE_CAUSE_FIRE -> COLOR_STAT_FIRE;
            case DAMAGE_CAUSE_WATER -> COLOR_STAT_WATER;
            case DAMAGE_CAUSE_AIR -> COLOR_STAT_AIR;
            default -> null;
        };
    }

    private String colorize(@NonNull String value, String colorHex) {
        if (colorHex == null || colorHex.isBlank()) return value;
        return "<color is=\"" + colorHex + "\">" + value + "</color>";
    }

    private String buildItemTypeLine(@NonNull ItemBase itemBase, @NonNull String baseItemId) {
        return resolveTypeLabel(itemBase, baseItemId);
    }

    private String resolveTypeLabel(@NonNull ItemBase itemBase, @NonNull String baseItemId) {
        String animationsId = itemBase.playerAnimationsId;
        if (animationsId != null && !animationsId.isBlank()) {
            String mapped = mapTypeToken(animationsId);
            if (mapped != null) return mapped;
        }

        if (itemBase.categories != null) {
            for (String category : itemBase.categories) {
                if (category == null || category.isBlank()) continue;
                String mapped = mapTypeToken(category);
                if (mapped != null) return mapped;
            }
        }

        return mapTypeToken(baseItemId);
    }

    private String mapTypeToken(String source) {
        if (source == null || source.isBlank()) return null;

        String normalized = source.toLowerCase(Locale.ROOT);
        if (normalized.contains(TYPE_TOKEN_SWORD)) return TYPE_LABEL_SWORD;
        if (normalized.contains(TYPE_TOKEN_BOW)) return TYPE_LABEL_BOW;
        if (normalized.contains(TYPE_TOKEN_DAGGER)) return TYPE_LABEL_DAGGER;
        if (normalized.contains(TYPE_TOKEN_AXE)) return TYPE_LABEL_AXE;
        if (normalized.contains(TYPE_TOKEN_HAMMER)) return TYPE_LABEL_HAMMER;
        if (normalized.contains(TYPE_TOKEN_STAFF)) return TYPE_LABEL_STAFF;
        if (normalized.contains(TYPE_TOKEN_WAND)) return TYPE_LABEL_WAND;
        if (normalized.contains(TYPE_TOKEN_SPEAR) || normalized.contains(TYPE_TOKEN_LANCE)) return TYPE_LABEL_SPEAR;
        if (normalized.contains(TYPE_TOKEN_SHIELD)) return TYPE_LABEL_SHIELD;
        if (normalized.contains(TYPE_TOKEN_HELMET)) return TYPE_LABEL_HELMET;
        if (normalized.contains(TYPE_TOKEN_CHEST) || normalized.contains(TYPE_TOKEN_TORSO)) return TYPE_LABEL_CHEST;
        if (normalized.contains(TYPE_TOKEN_PANTS) || normalized.contains(TYPE_TOKEN_LEGS)) return TYPE_LABEL_PANTS;
        if (normalized.contains(TYPE_TOKEN_BOOTS)) return TYPE_LABEL_BOOTS;
        if (normalized.contains(TYPE_TOKEN_RING)) return TYPE_LABEL_RING;
        if (normalized.contains(TYPE_TOKEN_AMULET) || normalized.contains(TYPE_TOKEN_NECKLACE)) return TYPE_LABEL_AMULET;
        if (normalized.contains(TYPE_TOKEN_TOOL)) return TYPE_LABEL_TOOL;
        if (normalized.contains(TYPE_TOKEN_WEAPON)) return TYPE_LABEL_WEAPON;
        return null;
    }

    private StatDisplay resolveDisplay(int statIndex) {
        StatDisplay known = displayByStatIndex.get(statIndex);
        if (known != null) return known;

        EntityStatType statType = EntityStatType.getAssetMap().getAsset(statIndex);
        if (statType == null || statType.getId() == null || statType.getId().isBlank()) return null;

        return new StatDisplay(statType.getId(), false, 0, false, null);
    }

    private boolean ensureInitialized() {
        if (initialized) return true;

        try {
            displayByStatIndex.clear();

            int order = 0;
            register(DefaultEntityStatTypes.getHealth(), STAT_LABEL_VITALITY, false, order++, COLOR_STAT_VITALITY);
            register(Characteristics.STATIC_MODIFIER_WISDOM_KEY, STAT_LABEL_WISDOM, false, order++, COLOR_STAT_WISDOM);
            register(Characteristics.STATIC_MODIFIER_STRENGTH_KEY, STAT_LABEL_STRENGTH, false, order++, COLOR_STAT_STRENGTH);
            register(Characteristics.STATIC_MODIFIER_INTELLIGENCE_KEY, STAT_LABEL_INTELLIGENCE, false, order++, COLOR_STAT_INTELLIGENCE);
            register(Characteristics.STATIC_MODIFIER_CHANCE_KEY, STAT_LABEL_CHANCE, false, order++, COLOR_STAT_CHANCE);
            register(Characteristics.STATIC_MODIFIER_AGILITY_KEY, STAT_LABEL_AGILITY, false, order++, COLOR_STAT_AGILITY);
            register(Characteristics.STATIC_MODIFIER_STAMINA_KEY, STAT_LABEL_STAMINA, false, order++, COLOR_STAT_STAMINA);
            register(Characteristics.STATIC_MODIFIER_CRITICAL_DAMAGE_KEY, STAT_LABEL_CRITICAL_DAMAGE, false, order++, COLOR_STAT_CRITICAL_DAMAGE);
            register(Characteristics.STATIC_MODIFIER_CRITICAL_PCT_KEY, STAT_LABEL_CRITICAL_PCT, true, order++, COLOR_STAT_CRITICAL_PCT);
            register(Characteristics.STATIC_MODIFIER_CRITICAL_RESISTANCE_KEY, STAT_LABEL_CRITICAL_RESISTANCE, false, order++, COLOR_STAT_CRITICAL_RESISTANCE);
            register(Characteristics.STATIC_MODIFIER_LIFE_REGEN_PCT_KEY, STAT_LABEL_LIFE_REGEN, true, order++, COLOR_STAT_LIFE_REGEN);
            register(Characteristics.STATIC_MODIFIER_DROP_CHANCE_KEY, STAT_LABEL_DROP_CHANCE, true, order++, COLOR_STAT_DROP_CHANCE);
            register(Characteristics.STATIC_MODIFIER_MOVE_SPEED_KEY, STAT_LABEL_MOVE_SPEED, true, order++, COLOR_STAT_MOVE_SPEED);
            register(Characteristics.STATIC_MODIFIER_EARTH_RESISTANCE_PCT_KEY, STAT_LABEL_EARTH_RESISTANCE, true, order++, COLOR_STAT_EARTH);
            register(Characteristics.STATIC_MODIFIER_FIRE_RESISTANCE_PCT_KEY, STAT_LABEL_FIRE_RESISTANCE, true, order++, COLOR_STAT_FIRE);
            register(Characteristics.STATIC_MODIFIER_WATER_RESISTANCE_PCT_KEY, STAT_LABEL_WATER_RESISTANCE, true, order++, COLOR_STAT_WATER);
            register(Characteristics.STATIC_MODIFIER_AIR_RESISTANCE_PCT_KEY, STAT_LABEL_AIR_RESISTANCE, true, order, COLOR_STAT_AIR);

            initialized = true;
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void register(@NonNull String statKey, @NonNull String label, boolean percent, int order, String colorHex) {
        int index = EntityStatType.getAssetMap().getIndex(statKey);
        if (index < 0) return;
        register(index, label, percent, order, colorHex);
    }

    private void register(int index, @NonNull String label, boolean percent, int order, String colorHex) {
        if (index < 0) return;
        displayByStatIndex.put(index, new StatDisplay(label, percent, order, true, colorHex));
    }

    private record StatDisplay(String label, boolean percent, int knownOrder, boolean known, String colorHex) {}

    private record DamageLine(int order, String line) {}

    private record StatLine(boolean known, int knownOrder, int statIndex, String line) {}

    public record DamageRange(String causeId, double min, double max) {}

    public record TooltipDescription(String translationKey, String description) {}
}
