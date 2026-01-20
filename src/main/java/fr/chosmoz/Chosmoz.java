package fr.chosmoz;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import fr.chosmoz.clazz.Class;
import fr.chosmoz.clazz.JsonClassFileLoader;
import fr.chosmoz.clazz.JsonClassRepository;
import fr.chosmoz.player.*;
import fr.chosmoz.rank.JsonRankFileLoader;
import fr.chosmoz.rank.JsonRankRepository;
import fr.chosmoz.rank.Rank;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;


public class Chosmoz extends JavaPlugin {
    //Remove this if you don't use JsonPlayerRepository
    private PlayerRepository playerRepository;

    public Chosmoz(@NotNull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        super.setup();

        HytaleLogger logger = this.getLogger();
        try {
            //Rank
            logger.atInfo().log("Loading ranks...");
            JsonRankFileLoader jsonRankFileLoader = new JsonRankFileLoader();
            File jsonRankFile = jsonRankFileLoader.loadJsonRanksFile();
            List<Rank> jsonRankData = jsonRankFileLoader.getJsonData(jsonRankFile);
            JsonRankRepository jsonRankRepository = new JsonRankRepository(jsonRankData);
            logger.atInfo().log(jsonRankData.size() + " ranks loaded!");

            //Player
            logger.atInfo().log("Loading players...");
            JsonPlayerFileLoader jsonPlayerFileLoader = new JsonPlayerFileLoader();
            File jsonPlayerFile = jsonPlayerFileLoader.loadJsonPlayersFile();
            List<Player> jsonPlayerData = jsonPlayerFileLoader.getJsonData(jsonPlayerFile);
            JsonPlayerRepository jsonPlayerRepository = new JsonPlayerRepository(jsonPlayerData, jsonPlayerFile, logger);

            //Remove this if you don't use JsonPlayerRepository
            this.playerRepository = jsonPlayerRepository;
            PlayerSaveDataTask playerSaveDataTask = new PlayerSaveDataTask(jsonPlayerRepository);

            this.getTaskRegistry().registerTask(playerSaveDataTask.run());
            logger.atInfo().log(jsonPlayerData.size() + " players loaded!");

            this.getEventRegistry().registerGlobal(PlayerConnectEvent.class, new fr.chosmoz.player.event.PlayerConnectEvent(jsonPlayerRepository, logger)::onPlayerConnect);
            this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, new fr.chosmoz.player.event.PlayerReadyEvent(jsonPlayerRepository, jsonRankRepository)::onPlayerReady);

            //Class
            logger.atInfo().log("Loading classes...");
            JsonClassFileLoader jsonClsFileLoader = new JsonClassFileLoader();
            File jsonClsFile = jsonClsFileLoader.loadJsonClassesFile();
            List<Class> jsonClsData = jsonClsFileLoader.getJsonData(jsonClsFile);
            JsonClassRepository jsonClsRepository = new JsonClassRepository(jsonClsData);
            logger.atInfo().log(jsonClsData.size() + " classes loaded!");

            this.getEventRegistry().registerGlobal(PlayerConnectEvent.class, new fr.chosmoz.clazz.event.PlayerConnectEvent(jsonPlayerRepository, logger)::onPlayerConnectEvent);

            //Chat
            this.getEventRegistry().registerGlobal(PlayerChatEvent.class, new fr.chosmoz.chat.event.PlayerChatEvent(jsonPlayerRepository, jsonRankRepository, logger)::onPlayerChatEvent);
        } catch (Exception e) {
            logger.atSevere().log(e.toString());
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

        this.getLogger().atInfo().log("Chosmoz Mod shutting down!");
    }
}
