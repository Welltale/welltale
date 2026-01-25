package fr.chosmoz;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.io.adapter.PacketAdapters;
import com.hypixel.hytale.server.core.io.adapter.PacketFilter;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import fr.chosmoz.clazz.Class;
import fr.chosmoz.clazz.JsonClassFileLoader;
import fr.chosmoz.clazz.JsonClassRepository;
import fr.chosmoz.clazz.spell.SpellManager;
import fr.chosmoz.clazz.spell.interaction.CastSpellInteraction;
import fr.chosmoz.player.*;
import fr.chosmoz.player.event.BreakBlockEvent;
import fr.chosmoz.player.event.DropItemEvent;
import fr.chosmoz.player.event.PlaceBlockEvent;
import fr.chosmoz.rank.JsonRankFileLoader;
import fr.chosmoz.rank.JsonRankRepository;
import fr.chosmoz.rank.Rank;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;


public class Chosmoz extends JavaPlugin {
    private PacketFilter inboundFilter;

    //Remove this if you don't use JsonPlayerRepository
    private PlayerRepository playerRepository;

    public Chosmoz(@NotNull JavaPluginInit init) {
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
            PlayerSaveDataTask playerSaveDataTask = new PlayerSaveDataTask(playerRepository);
            logger.atInfo().log(jsonPlayerData.size() + " player(s) loaded!");

            //this.getTaskRegistry().registerTask(playerSaveDataTask.run());
            this.getEventRegistry().registerGlobal(PlayerConnectEvent.class,
                    new fr.chosmoz.player.event.PlayerConnectEvent(playerRepository, logger)::onPlayerConnect);
            this.getEventRegistry().registerGlobal(PlayerReadyEvent.class,
                    new fr.chosmoz.player.event.PlayerReadyEvent(playerRepository, rankRepository, logger, universe)::onPlayerReady);
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
                    new fr.chosmoz.clazz.event.PlayerReadyEvent(playerRepository, classRepository, logger)::onPlayerReadyEvent);

            //Spell
            logger.atInfo().log("Loading spells...");
            SpellManager spellManager = new SpellManager(logger, playerRepository, classRepository);
            CastSpellInteraction castSpellInteraction = new CastSpellInteraction();
            castSpellInteraction.initStatics(spellManager, playerRepository, classRepository);

            getCodecRegistry(Interaction.CODEC).register(
                    "chosmoz:cast_spell",
                    CastSpellInteraction.class,
                    CastSpellInteraction.CODEC
            );
            logger.atInfo().log("Spells loaded!");

            //Chat
            this.getEventRegistry().registerGlobal(PlayerChatEvent.class,
                    new fr.chosmoz.chat.event.PlayerChatEvent(playerRepository, rankRepository, logger)::onPlayerChatEvent);
        } catch (Exception e) {
            logger.atSevere().log("Failed to load Chosmoz Mod: " + e.getMessage());
        }
    }

    @Override
    protected void start() {
        this.getLogger().atInfo().log("Chosmoz Mod loaded!");
    }

    @Override
    protected void shutdown() {
        //Remove this if you don't use JsonPlayerRepository
        if (this.playerRepository != null) {
            this.playerRepository.saveData();
        }

        PacketAdapters.deregisterInbound(this.inboundFilter);
        this.getLogger().atInfo().log("Chosmoz Mod shutting down!");
    }
}
