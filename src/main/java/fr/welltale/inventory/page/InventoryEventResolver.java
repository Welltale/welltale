package fr.welltale.inventory.page;

final class InventoryEventResolver {
    private InventoryEventResolver() {}

    static int resolveSourceSlot(InventoryPage.CustomInventoryEventData data, String rawPayload) {
        return firstValid(
                parseIndex(data.sourceSlotId),
                parseIndex(data.fromSlotId),
                parseIndex(data.sourceItemGridIndex),
                parseIndex(data.fromItemGridIndex),
                extractIntFromRawPayload(rawPayload, "SourceSlotId"),
                extractIntFromRawPayload(rawPayload, "FromSlotId"),
                extractIntFromRawPayload(rawPayload, "SourceItemGridIndex"),
                extractIntFromRawPayload(rawPayload, "FromItemGridIndex"),
                extractIntFromRawPayload(rawPayload, "SlotIndex"),
                parseIndex(data.slotIndex),
                parseIndex(data.index)
        );
    }

    static int resolveTargetSlot(InventoryPage.CustomInventoryEventData data, String rawPayload) {
        return firstValid(
                parseIndex(data.toSlotId),
                parseIndex(data.targetSlotId),
                parseIndex(data.destinationSlotId),
                parseIndex(data.toItemGridIndex),
                parseIndex(data.inventorySlotIndex),
                extractIntFromRawPayload(rawPayload, "ToSlotId"),
                extractIntFromRawPayload(rawPayload, "TargetSlotId"),
                extractIntFromRawPayload(rawPayload, "DestinationSlotId"),
                extractIntFromRawPayload(rawPayload, "ToItemGridIndex"),
                extractIntFromRawPayload(rawPayload, "SlotIndex"),
                parseIndex(data.slotIndex),
                parseIndex(data.index)
        );
    }

    static int resolveHoveredSlot(InventoryPage.CustomInventoryEventData data, String rawPayload) {
        return firstValid(
                extractIntFromRawPayload(rawPayload, "SlotIndex"),
                resolveIndex(data)
        );
    }

    static int resolveIndex(InventoryPage.CustomInventoryEventData data) {
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

    private static int parseIndex(String rawIndex) {
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
            } else if (!digits.isEmpty()) {
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

    private static int extractIntFromRawPayload(String rawPayload, String key) {
        if (rawPayload == null || rawPayload.isBlank() || key == null || key.isBlank()) {
            return -1;
        }

        String token = "\"" + key + "\"";
        int keyIndex = rawPayload.indexOf(token);
        if (keyIndex < 0) return -1;

        int colonIndex = rawPayload.indexOf(':', keyIndex + token.length());
        if (colonIndex < 0) return -1;

        int valueIndex = colonIndex + 1;
        while (valueIndex < rawPayload.length() && Character.isWhitespace(rawPayload.charAt(valueIndex))) {
            valueIndex++;
        }

        if (valueIndex >= rawPayload.length()) return -1;
        if (rawPayload.charAt(valueIndex) == '"') valueIndex++;

        int sign = 1;
        if (valueIndex < rawPayload.length() && rawPayload.charAt(valueIndex) == '-') {
            sign = -1;
            valueIndex++;
        }

        int end = valueIndex;
        while (end < rawPayload.length() && Character.isDigit(rawPayload.charAt(end))) {
            end++;
        }

        if (end <= valueIndex) return -1;

        try {
            return Integer.parseInt(rawPayload.substring(valueIndex, end)) * sign;
        } catch (Exception ignored) {
            return -1;
        }
    }

    private static int firstValid(int... candidates) {
        for (int candidate : candidates) {
            if (candidate >= 0) return candidate;
        }
        return -1;
    }
}
