package fr.welltale;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import fr.welltale.characteristic.system.DamageSystem;
import fr.welltale.characteristic.system.DropChanceSystem;
import fr.welltale.characteristic.system.LifeRegenSystem;
import fr.welltale.characteristic.system.MoveSpeedSystem;
import fr.welltale.clazz.Class;
import fr.welltale.clazz.JsonClassFileLoader;
import fr.welltale.clazz.JsonClassRepository;
import fr.welltale.inventory.InventoryService;
import fr.welltale.inventory.event.OpenInventoryPacketInterceptor;
import fr.welltale.level.PlayerLevelComponent;
import fr.welltale.level.event.GiveXPEvent;
import fr.welltale.level.event.LevelUpEvent;
import fr.welltale.level.handler.GiveXPHandler;
import fr.welltale.level.handler.LevelUpHandler;
import fr.welltale.level.system.OnDeathSystem;
import fr.welltale.mob.JsonMobFileLoader;
import fr.welltale.mob.JsonMobRepository;
import fr.welltale.mob.Mob;
import fr.welltale.mob.MobStatsComponent;
import fr.welltale.mob.system.MobLootOnDeathSystem;
import fr.welltale.mob.system.MobNameplateAssignSystem;
import fr.welltale.mob.system.MobStatsAssignSystem;
import fr.welltale.player.*;
import fr.welltale.player.charactercache.MemoryCharacterCache;
import fr.welltale.player.system.BreakBlockEventSystem;
import fr.welltale.player.system.DropItemEventSystem;
import fr.welltale.player.system.PlaceBlockEventSystem;
import fr.welltale.player.system.PlayerJoinSystem;
import fr.welltale.rank.JsonRankFileLoader;
import fr.welltale.rank.JsonRankRepository;
import fr.welltale.rank.Rank;
import fr.welltale.spell.CastSpellInteraction;
import fr.welltale.spell.SpellCooldownScheduler;
import fr.welltale.spell.SpellManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;


public class Welltale extends JavaPlugin {
    //Remove this if you don't use JsonPlayerRepository
    private PlayerRepository playerRepository;

    public Welltale(@NotNull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        super.setup();

        HytaleLogger logger = this.getLogger();
        Universe universe = Universe.get();
        try {
            //Rank
            logger.atInfo().log("Loading ranks...");
            JsonRankFileLoader jsonRankFileLoader = new JsonRankFileLoader();
            File jsonRankFile = jsonRankFileLoader.loadJsonRanksFile();
            ArrayList<Rank> jsonRankData = jsonRankFileLoader.getJsonData(jsonRankFile);
            JsonRankRepository rankRepository = new JsonRankRepository(jsonRankData);
            logger.atInfo().log(jsonRankData.size() + " rank(s) loaded!");
            //Rank

            //Class
            logger.atInfo().log("Loading classes...");
            JsonClassFileLoader jsonClassFileLoader = new JsonClassFileLoader();
            File jsonClassFile = jsonClassFileLoader.loadJsonClassesFile();
            ArrayList<Class> jsonClassData = jsonClassFileLoader.getJsonData(jsonClassFile);
            JsonClassRepository classRepository = new JsonClassRepository(jsonClassData);
            logger.atInfo().log(jsonClassData.size() + " class(es) loaded!");
            //Class

            //Player
            logger.atInfo().log("Loading players...");
            JsonPlayerFileLoader jsonPlayerFileLoader = new JsonPlayerFileLoader();
            File jsonPlayerFile = jsonPlayerFileLoader.loadJsonPlayersFile();
            ArrayList<Player> jsonPlayerData = jsonPlayerFileLoader.getJsonData(jsonPlayerFile);
            JsonPlayerRepository playerRepository = new JsonPlayerRepository(jsonPlayerData, jsonPlayerFile, logger);
            MemoryCharacterCache characterCache = new MemoryCharacterCache();

            //Remove this if you don't use JsonPlayerRepository
            this.playerRepository = playerRepository;
            PlayerSaveDataScheduler playerSaveDataScheduler = new PlayerSaveDataScheduler(playerRepository);

            //TODO ENABLE IT
            //this.getTaskRegistry().registerTask(playerSaveDataScheduler.run());

            this.getEventRegistry().registerGlobal(
                    PlayerConnectEvent.class,
                    new fr.welltale.player.event.PlayerConnectEvent(playerRepository, logger, universe)::onPlayerConnect
            );
            this.getEventRegistry().registerGlobal(
                    PlayerReadyEvent.class,
                    new fr.welltale.player.event.PlayerReadyEvent(playerRepository, rankRepository, characterCache, classRepository, logger, universe)::onPlayerReady
            );
            this.getEntityStoreRegistry().registerSystem(new BreakBlockEventSystem(playerRepository, rankRepository, logger));
            this.getEntityStoreRegistry().registerSystem(new PlaceBlockEventSystem(playerRepository, rankRepository, logger));
            this.getEntityStoreRegistry().registerSystem(new DropItemEventSystem(playerRepository, rankRepository, logger));

            this.getEventRegistry().registerGlobal(
                    PlayerChatEvent.class,
                    new fr.welltale.player.event.PlayerChatEvent(playerRepository, rankRepository, logger, universe)::onPlayerChatEvent
            );
            logger.atInfo().log(jsonPlayerData.size() + " player(s) loaded!");
            //Player

            //Characteristic
            DamageSystem damageSystem = new DamageSystem();
            this.getEntityStoreRegistry().registerSystem(damageSystem);
            MoveSpeedSystem moveSpeedSystem = new MoveSpeedSystem();
            this.getEntityStoreRegistry().registerSystem(moveSpeedSystem);
            LifeRegenSystem lifeRegenSystem = new LifeRegenSystem();
            this.getEntityStoreRegistry().registerSystem(lifeRegenSystem);
            DropChanceSystem dropChanceSystem = new DropChanceSystem();
            this.getEntityStoreRegistry().registerSystem(dropChanceSystem);
            //Characteristic

            //Spell
            logger.atInfo().log("Loading spells...");
            SpellManager spellManager = new SpellManager(logger, universe, classRepository, characterCache);
            SpellCooldownScheduler spellCooldownScheduler = new SpellCooldownScheduler();
            CastSpellInteraction castSpellInteraction = new CastSpellInteraction();
            castSpellInteraction.initStatics(spellManager, playerRepository, logger);

            this.getCodecRegistry(Interaction.CODEC).register(
                    CastSpellInteraction.ROOT_INTERACTION,
                    CastSpellInteraction.class,
                    CastSpellInteraction.CODEC
            );

            this.getTaskRegistry().registerTask(spellCooldownScheduler.run(spellManager));
            logger.atInfo().log("Spells loaded!");
            //Spell

            //Level
            logger.atInfo().log("Loading Level...");
            var levelType = this.getEntityStoreRegistry().registerComponent(
                    PlayerLevelComponent.class,
                    "LevelComponent",
                    PlayerLevelComponent.CODEC
            );
            PlayerLevelComponent.setComponentType(levelType);

            this.getEventRegistry().register(GiveXPEvent.class, new GiveXPHandler(logger));
            this.getEventRegistry().register(LevelUpEvent.class, new LevelUpHandler(characterCache, logger));
            this.getEntityStoreRegistry().registerSystem(new OnDeathSystem(logger));
            this.getEntityStoreRegistry().registerSystem(new PlayerJoinSystem(playerRepository, characterCache, logger));
            logger.atInfo().log("Level loaded!");
            //Level

            //Inventory
            InventoryService inventoryService = new InventoryService();
            this.getEventRegistry().registerGlobal(
                    PlayerReadyEvent.class,
                    new OpenInventoryPacketInterceptor(inventoryService, characterCache, playerRepository, logger)::onPlayerReady
            );
            //Inventory

            //Mob
            logger.atInfo().log("Loading mobs...");
            JsonMobFileLoader jsonMobFileLoader = new JsonMobFileLoader();
            File jsonMobFile = jsonMobFileLoader.loadJsonMobsFile();
            ArrayList<Mob> jsonMobData = jsonMobFileLoader.getJsonData(jsonMobFile);
            JsonMobRepository jsonMobRepository = new JsonMobRepository(jsonMobData);

            this.getEntityStoreRegistry().registerSystem(new MobNameplateAssignSystem(jsonMobRepository));
            this.getEntityStoreRegistry().registerSystem(new MobStatsAssignSystem(jsonMobRepository));
            this.getEntityStoreRegistry().registerSystem(new fr.welltale.mob.system.DamageSystem(jsonMobRepository));
            this.getEntityStoreRegistry().registerSystem(new MobLootOnDeathSystem(inventoryService, jsonMobRepository, logger));

            var mobLevelType = this.getEntityStoreRegistry().registerComponent(
                    MobStatsComponent.class,
                    "MobLevelComponent",
                    MobStatsComponent.CODEC
            );
            MobStatsComponent.setComponentType(mobLevelType);
            logger.atInfo().log(jsonMobData.size() + " mob(s) loaded!");
            //Mob
        } catch (Exception e) {
            logger.atSevere().log("Failed to load Welltale Mod: " + e.getMessage());
        }
    }

    @Override
    protected void start() {
        this.getLogger().atInfo().log("Welltale Mod loaded!");
    }

    @Override
    protected void shutdown() {
        //Remove this if you don't use JsonPlayerRepository
        //TODO ENABLE IT
        //if (this.playerRepository != null) {
        //    this.playerRepository.saveData();
        //}

        this.getLogger().atInfo().log("Welltale Mod shutting down!");
    }
}
