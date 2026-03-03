# Fixes later (objectif 500 joueurs)

## P0 - Critique avant charge élevée

- [ ] **Sécuriser la persistance joueur** (`src/main/java/fr/welltale/player/JsonPlayerRepository.java`, `src/main/java/fr/welltale/player/PlayerSaveDataScheduler.java`)
  - Éviter les conflits mutation/save (snapshot immuable + lock léger ou pipeline save unique).
  - Ajouter logs de durée et taille de sauvegarde.

- [ ] **Indexer les données joueurs/cache en Map**
  - `src/main/java/fr/welltale/player/JsonPlayerRepository.java`
  - `src/main/java/fr/welltale/player/charactercache/MemoryCharacterCache.java`
  - Remplacer les recherches `ArrayList + stream/findFirst` par `Map<UUID, ...>` pour lookup O(1).

- [ ] **Réduire le coût HUD stats** (`src/main/java/fr/welltale/hud/system/UpdateStatsHudSystem.java`)
  - Passer en event/delta-driven.
  - Ne push HUD que si HP/Stamina ont changé.

## P1 - Important (stabilité/mémoire)

- [ ] **Ajouter cleanup inventaire runtime au leave** (`src/main/java/fr/welltale/inventory/InventoryService.java`)
  - Ajouter une méthode clear par `(playerUuid, characterUuid)` ou par joueur.
  - L’appeler dans le flow leave/déconnexion.

- [ ] **Améliorer observabilité des erreurs**
  - Éviter les logs message-only.
  - Inclure `playerUuid` / `characterUuid` dans les logs critiques.
  - Sur erreurs save/inventaire/item packets, loguer contexte + stacktrace utile.

## P2 - Qualité / maintenabilité

- [ ] **Refactor `InventoryPage`** (`src/main/java/fr/welltale/inventory/page/InventoryPage.java`)
  - Extraire logique métier transfert/stack/loot dans un service dédié.
  - Garder la page pour wiring UI uniquement.

- [ ] **Ajouter des tests ciblés**
  - Repositories (add/get/update/remove).
  - Cycle personnage (select -> cache -> leave -> persist).
  - Module item rolled (virtual/base ID rewrite, delta stats).

## Notes module item (état actuel)

- Globalement bon: architecture propre (`RolledItemPacketAdapter`, `RolledVirtualItemRegistry`, `RolledItemStatSystem`).
- À surveiller: catches silencieux et logs peu détaillés sur quelques paths packet/metadata.

## Cible perf

- ~100 joueurs: état actuel acceptable.
- ~500 joueurs: viser P0 terminé + tests de charge join/leave + combat/loot + sauvegardes sous stress.
