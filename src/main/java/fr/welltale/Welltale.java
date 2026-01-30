package fr.welltale;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import fr.welltale.clazz.Class;
import fr.welltale.clazz.JsonClassFileLoader;
import fr.welltale.clazz.JsonClassRepository;
import fr.welltale.clazz.spell.SpellComponent;
import fr.welltale.clazz.spell.SpellCooldownScheduler;
import fr.welltale.clazz.spell.SpellManager;
import fr.welltale.clazz.spell.SpellScheduler;
import fr.welltale.clazz.spell.interaction.CastSpellInteraction;
import fr.welltale.player.*;
import fr.welltale.player.event.BreakBlockEvent;
import fr.welltale.player.event.DropItemEvent;
import fr.welltale.player.event.PlaceBlockEvent;
import fr.welltale.rank.JsonRankFileLoader;
import fr.welltale.rank.JsonRankRepository;
import fr.welltale.rank.Rank;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;


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
            List<Rank> jsonRankData = jsonRankFileLoader.getJsonData(jsonRankFile);
            JsonRankRepository rankRepository = new JsonRankRepository(jsonRankData);
            logger.atInfo().log(jsonRankData.size() + " rank(s) loaded!");

            //Player
            logger.atInfo().log("Loading players...");
            JsonPlayerFileLoader jsonPlayerFileLoader = new JsonPlayerFileLoader();
            File jsonPlayerFile = jsonPlayerFileLoader.loadJsonPlayersFile();
            List<Player> jsonPlayerData = jsonPlayerFileLoader.getJsonData(jsonPlayerFile);
            JsonPlayerRepository playerRepository = new JsonPlayerRepository(jsonPlayerData, jsonPlayerFile, logger);

            //Remove this if you don't use JsonPlayerRepository
            this.playerRepository = playerRepository;
            PlayerSaveDataScheduler playerSaveDataScheduler = new PlayerSaveDataScheduler(playerRepository);
            logger.atInfo().log(jsonPlayerData.size() + " player(s) loaded!");

            //TODO ENABLE IT
            //this.getTaskRegistry().registerTask(playerSaveDataScheduler.run());
            this.getEventRegistry().registerGlobal(PlayerConnectEvent.class,
                    new fr.welltale.player.event.PlayerConnectEvent(playerRepository, logger)::onPlayerConnect);
            this.getEventRegistry().registerGlobal(PlayerReadyEvent.class,
                    new fr.welltale.player.event.PlayerReadyEvent(playerRepository, rankRepository, logger, universe)::onPlayerReady);
            this.getEntityStoreRegistry().registerSystem(new BreakBlockEvent(playerRepository, rankRepository, logger));
            this.getEntityStoreRegistry().registerSystem(new PlaceBlockEvent(playerRepository, rankRepository, logger));
            this.getEntityStoreRegistry().registerSystem(new DropItemEvent(playerRepository, rankRepository, logger));

            //TODO ADD THIS EVENTS
            // Disable Secondary Hand
            // Disable Join/Leave Messages

            //Class
            logger.atInfo().log("Loading classes...");
            JsonClassFileLoader jsonClassFileLoader = new JsonClassFileLoader();
            File jsonClassFile = jsonClassFileLoader.loadJsonClassesFile();
            List<Class> jsonClassData = jsonClassFileLoader.getJsonData(jsonClassFile);
            JsonClassRepository classRepository = new JsonClassRepository(jsonClassData);
            logger.atInfo().log(jsonClassData.size() + " class(es) loaded!");

            this.getEventRegistry().registerGlobal(PlayerReadyEvent.class,
                    new fr.welltale.clazz.event.PlayerReadyEvent(playerRepository, classRepository, logger)::onPlayerReadyEvent);

            //Spell
            logger.atInfo().log("Loading spells...");
            SpellComponent.spellComponentType = getEntityStoreRegistry().registerComponent(SpellComponent.class, SpellComponent::new);

            SpellScheduler spellScheduler = new SpellScheduler();
            SpellManager spellManager = new SpellManager(logger, universe, classRepository, spellScheduler);
            CastSpellInteraction castSpellInteraction = new CastSpellInteraction();
            castSpellInteraction.initStatics(spellManager, playerRepository);
            SpellCooldownScheduler spellCooldownScheduler = new SpellCooldownScheduler();


            this.getCodecRegistry(Interaction.CODEC).register(
                    CastSpellInteraction.ROOT_INTERACTION,
                    CastSpellInteraction.class,
                    CastSpellInteraction.CODEC
            );

            this.getEntityStoreRegistry().registerSystem(spellScheduler);

            this.getTaskRegistry().registerTask(spellCooldownScheduler.run(spellManager));
            logger.atInfo().log("Spells loaded!");

            //Chat
            this.getEventRegistry().registerGlobal(PlayerChatEvent.class,
                    new fr.welltale.chat.event.PlayerChatEvent(playerRepository, rankRepository, logger)::onPlayerChatEvent);
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
        if (this.playerRepository != null) {
            this.playerRepository.saveData();
        }

        this.getLogger().atInfo().log("Welltale Mod shutting down!");
    }
}
