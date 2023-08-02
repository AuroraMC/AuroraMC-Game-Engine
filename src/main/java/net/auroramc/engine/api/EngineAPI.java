/*
 * Copyright (c) 2022-2023 AuroraMC Ltd. All Rights Reserved.
 *
 * PRIVATE AND CONFIDENTIAL - Distribution and usage outside the scope of your job description is explicitly forbidden except in circumstances where a company director has expressly given written permission to do so.
 */

package net.auroramc.engine.api;

import net.auroramc.api.AuroraMCAPI;
import net.auroramc.api.backend.info.ServerInfo;
import net.auroramc.api.utils.TextFormatter;
import net.auroramc.core.api.ServerAPI;
import net.auroramc.core.api.player.AuroraMCServerPlayer;
import net.auroramc.core.api.utils.ZipUtil;
import net.auroramc.core.api.utils.gui.GUIItem;
import net.auroramc.engine.AuroraMCGameEngine;
import net.auroramc.engine.api.backend.EngineDatabaseManager;
import net.auroramc.engine.api.events.ServerStateChangeEvent;
import net.auroramc.engine.api.games.*;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.auroramc.engine.api.players.Reward;
import net.auroramc.engine.api.server.ServerState;
import net.auroramc.engine.api.util.GameStartingRunnable;
import net.auroramc.engine.api.util.TitleBarRunnable;
import net.auroramc.engine.api.util.VoidGenerator;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class EngineAPI {

    private static UUID reloadCode;

    private static AuroraMCGameEngine gameEngine;
    private static final Map<String, GameInfo> games;
    private static final Map<String, String> versionNumbers;
    private static GameMap waitingLobbyMap;
    private static ServerState serverState;
    private static Game activeGame;
    private static GameInfo activeGameInfo;
    private static GameMap activeMap;
    private static final Map<String, MapRegistry> maps;
    private static final Map<GameInfo, GameVariationInfo> gameRotation;
    private static World mapWorld;
    private static GameStartingRunnable gameStartingRunnable;

    private static boolean teamBalancingEnabled;

    private final static GUIItem lobbyItem;
    private final static GUIItem prefsItem;
    private final static GUIItem cosmeticsItem;
    private final static GUIItem kitItem;
    private final static GUIItem teamItem;

    private static final Random random;

    private static GameInfo nextGame;
    private static GameMap nextMap;
    private static GameVariationInfo nextVariation;

    private static boolean awaitingRestart;
    private static String restartType;
    private static boolean awaitingMapReload;

    private final static Map<Integer, Reward> kitLevelRewards;

    private static String xpBoostMessage;
    private static float xpBoostMultiplier;

    static {
        games = new HashMap<>();
        maps = new HashMap<>();
        kitLevelRewards = new HashMap<>();
        gameRotation = new HashMap<>();
        serverState = ServerState.STARTING_UP;

        versionNumbers = EngineDatabaseManager.getVersionNumbers();

        teamBalancingEnabled = true;

        random = new Random();

        activeGame = null;
        activeGameInfo = null;
        activeMap = null;

        lobbyItem = new GUIItem(Material.WOOD_DOOR, "&a&lReturn to Lobby");
        prefsItem = new GUIItem(Material.REDSTONE_COMPARATOR, "&a&lView Preferences");
        cosmeticsItem = new GUIItem(Material.EMERALD, "&a&lView Cosmetics");
        kitItem = new GUIItem(Material.CHEST, "&a&lSelect Kit");
        teamItem = new GUIItem(Material.BOOK, "&a&lSelect Team");

       xpBoostMessage = EngineDatabaseManager.getXpMessage();
       xpBoostMultiplier = EngineDatabaseManager.getXpMultiplier();

       reloadCode = UUID.randomUUID();
    }

    public static void init(AuroraMCGameEngine gameEngine) {
        EngineAPI.gameEngine = gameEngine;
    }

    public static Map<String, GameInfo> getGames() {
        return games;
    }

    public static void registerGame(GameInfo game) {
        if (game.getId() == 6) {
            EngineAPI.games.put("EVENT", game);
        } else {
            EngineAPI.games.put(game.getRegistryKey(), game);
        }
    }

    public static GameMap getWaitingLobbyMap() {
        return waitingLobbyMap;
    }

    public static ServerState getServerState() {
        return serverState;
    }

    public static void setMapWorld(World mapWorld) {
        EngineAPI.mapWorld = mapWorld;
    }

    public static World getMapWorld() {
        return mapWorld;
    }

    public static void setServerState(ServerState serverState) {
        EngineAPI.serverState = serverState;
        Bukkit.getPluginManager().callEvent(new ServerStateChangeEvent(serverState));
    }

    public static AuroraMCGameEngine getGameEngine() {
        return gameEngine;
    }

    public static Game getActiveGame() {
        return activeGame;
    }

    public static GameMap getActiveMap() {
        return activeMap;
    }

    public static void setActiveGame(Game activeGame) {
        EngineAPI.activeGame = activeGame;
    }

    public static void setActiveMap(GameMap activeGameMap) {
        EngineAPI.activeMap = activeGameMap;
    }

    public static Map<String, MapRegistry> getMaps() {
        return maps;
    }

    public static void setWaitingLobbyMap(GameMap waitingLobbyMap) {
        EngineAPI.waitingLobbyMap = waitingLobbyMap;
    }

    public static Map<GameInfo, GameVariationInfo> getGameRotation() {
        return gameRotation;
    }

    public static int randomNumber(int start, int end) {
        return random.nextInt((end - start) + 1) + start;
    }

    public static int randomNumber(int end) {
        return random.nextInt(end);
    }

    public static GameInfo getActiveGameInfo() {
        return activeGameInfo;
    }

    public static void setActiveGameInfo(GameInfo activeGameInfo) {
        EngineAPI.activeGameInfo = activeGameInfo;
    }

    public static void loadRotation() {
        gameEngine.getLogger().info("Loading map world...");
        File file = new File(Bukkit.getWorldContainer(), "map_world");
        if (file.exists()) {
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException e) {
                AuroraMCAPI.getLogger().log(Level.WARNING, "An exception has occurred. Stack trace: ", e);
            }
        }

        World world = Bukkit.createWorld(new WorldCreator("map_world").generator(new VoidGenerator(gameEngine)));
        Bukkit.unloadWorld(world, false);

        gameEngine.getLogger().info("Map world loaded. Loading rotation...");

        for (Object object : ((ServerInfo)AuroraMCAPI.getInfo()).getServerType().getJSONArray("rotation")) {
            String string = (String) object;
            if (string.contains(":")) {
                //this is a variation rotation.
                String[] args = string.split(":");
                GameInfo game = games.get(args[0]);
                gameRotation.put(game, game.getVariations().get(args[1]));
            } else {
                gameRotation.put(games.get(string), null);
            }
        }
        gameEngine.getLogger().info(EngineAPI.getGameRotation().size() + " games loaded into rotation.");
        if (EngineAPI.getGameRotation().size() > 0) {
            gameEngine.getLogger().info("Loading a random game...");
            GameUtils.loadNextGame();
        } else {
            gameEngine.getLogger().info("Game rotation is empty, entering idle state.");
            EngineAPI.setServerState(ServerState.IDLE);
        }

        TitleBarRunnable runnable = new TitleBarRunnable();
        runnable.runTaskTimerAsynchronously(EngineAPI.getGameEngine(), 0, 20);
        gameEngine.getLogger().info("Loading complete.");
    }

    public static GameStartingRunnable getGameStartingRunnable() {
        return gameStartingRunnable;
    }

    public static void setGameStartingRunnable(GameStartingRunnable gameStartingRunnable) {
        EngineAPI.gameStartingRunnable = gameStartingRunnable;
    }

    public static GUIItem getCosmeticsItem() {
        return cosmeticsItem;
    }

    public static GUIItem getKitItem() {
        return kitItem;
    }

    public static GUIItem getLobbyItem() {
        return lobbyItem;
    }

    public static GUIItem getPrefsItem() {
        return prefsItem;
    }

    public static GUIItem getTeamItem() {
        return teamItem;
    }

    public static void setNextGame(GameInfo nextGame) {
        EngineAPI.nextGame = nextGame;
    }

    public static GameInfo getNextGame() {
        return nextGame;
    }

    public static GameMap getNextMap() {
        return nextMap;
    }

    public static GameVariationInfo getNextVariation() {
        return nextVariation;
    }

    public static void setNextMap(GameMap nextMap) {
        EngineAPI.nextMap = nextMap;
    }

    public static void setNextVariation(GameVariationInfo nextVariation) {
        EngineAPI.nextVariation = nextVariation;
    }

    public static boolean isAwaitingRestart() {
        return awaitingRestart;
    }

    public static void setAwaitingRestart(boolean awaitingRestart) {
        EngineAPI.awaitingRestart = awaitingRestart;
    }

    public static boolean isAwaitingMapReload() {
        return awaitingMapReload;
    }

    public static void setAwaitingMapReload(boolean awaitingMapReload) {
        EngineAPI.awaitingMapReload = awaitingMapReload;
    }

    public static void reloadMaps() {
        for (AuroraMCServerPlayer pl : ServerAPI.getPlayers()) {
            pl.sendMessage(TextFormatter.pluginMessage("Game Manager", "The server is currently updating its map register. Please wait..."));
        }
        setServerState(ServerState.RELOADING_MAPS);
        EngineAPI.maps.clear();
        gameEngine.getLogger().info("Downloading all live maps...");
        List<Integer> ints = EngineDatabaseManager.downloadMaps();
        File zips = new File(gameEngine.getDataFolder(), "zips");

        gameEngine.getLogger().info(ints.size() + " zips downloaded. Extracting maps...");
        File mapsFolder = new File(gameEngine.getDataFolder(), "maps");
        if (mapsFolder.exists()) {
            try {
                FileUtils.deleteDirectory(mapsFolder);
            } catch (IOException e) {
                AuroraMCAPI.getLogger().log(Level.WARNING, "An exception has occurred. Stack trace: ", e);
            }
        }
        mapsFolder.mkdirs();
        for (int zip : ints) {
            File file = new File(zips, zip + ".zip");
            try {
                ZipUtil.unzip(file.toPath().toAbsolutePath().toString(), mapsFolder.toPath().toAbsolutePath() + "/" + zip);
            } catch (IOException e) {
                AuroraMCAPI.getLogger().log(Level.WARNING, "An exception has occurred. Stack trace: ", e);
            }
        }
        File[] maps = mapsFolder.listFiles();
        assert maps != null;

        gameEngine.getLogger().info(ints.size() + " maps extracted. Removing old maps...");

        int i = 0;
        for (File map : maps) {
            String mapId = map.getName();
            if (AuroraMCGameEngine.getMaps().contains(mapId + ".load-code")) {
                if (!UUID.fromString(AuroraMCGameEngine.getMaps().getString(mapId + ".load-code")).equals(EngineAPI.getReloadCode())) {
                    map.delete();
                    i++;
                }
            } else {
                map.delete();
                i++;
            }
        }

        maps = mapsFolder.listFiles();

        gameEngine.getLogger().info(i + " maps removed. Loading map registry...");
        for (File map : maps) {
            File data = new File(map, "map.json");
            JSONParser parser = new JSONParser();
            Object object;
            JSONObject jsonObject;
            try {
                FileReader fileReader = new FileReader(data);
                object = parser.parse(fileReader);
                jsonObject = new JSONObject(((org.json.simple.JSONObject)  object).toJSONString());
            } catch (IOException | ParseException e) {
                AuroraMCAPI.getLogger().log(Level.WARNING, "An exception has occurred. Stack trace: ", e);
                gameEngine.getLogger().info("Map loading for a map failed, skipping...");
                continue;
            }

            String gameType = jsonObject.getString("game_type");
            int id = Integer.parseInt(map.getName().split("\\.")[0]);
            String name = jsonObject.getString("name");
            String author = jsonObject.getString("author");
            String game = jsonObject.getString("game_type");
            if (EngineAPI.getMaps().containsKey(gameType)) {
                EngineAPI.getMaps().get(gameType).getMaps().add(new GameMap(map, id, name, author, game, jsonObject));
            } else {
                MapRegistry registry = new MapRegistry(gameType);
                registry.getMaps().add(new GameMap(map, id, name, author, game, jsonObject));
                EngineAPI.getMaps().put(gameType, registry);
            }
        }
        new BukkitRunnable(){
            @Override
            public void run() {
                if (EngineAPI.getNextGame() != null) {
                    if (EngineAPI.getNextMap() != null) {
                        GameUtils.loadGame(EngineAPI.getNextGame(), EngineAPI.getNextMap(), EngineAPI.getNextVariation());
                    } else {
                        GameUtils.loadGame(EngineAPI.getNextGame(), EngineAPI.getNextVariation());
                    }

                    EngineAPI.setNextMap(null);
                    EngineAPI.setNextGame(null);
                    EngineAPI.setNextVariation(null);
                } else if (EngineAPI.getGameRotation().size() > 0) {
                    GameUtils.loadNextGame();
                } else {
                    EngineAPI.setActiveGameInfo(null);
                    EngineAPI.setActiveGame(null);
                    EngineAPI.setActiveMap(null);
                    EngineAPI.setServerState(ServerState.IDLE);
                }
                if (EngineAPI.getServerState() != ServerState.STARTING && EngineAPI.getActiveGame() != null && EngineAPI.getGameStartingRunnable() == null) {
                    if (ServerAPI.getPlayers().stream().filter(player1 -> !player1.isVanished() && !((AuroraMCGamePlayer)player1).isOptedSpec()).count() >= ((ServerInfo)AuroraMCAPI.getInfo()).getServerType().getInt("min_players")) {
                        EngineAPI.setGameStartingRunnable(new GameStartingRunnable(30, false));
                        EngineAPI.getGameStartingRunnable().runTaskTimer(ServerAPI.getCore(), 0, 20);
                    }
                }

                for (AuroraMCServerPlayer pl : ServerAPI.getPlayers()) {
                    pl.sendMessage(TextFormatter.pluginMessage("Game Manager", "Maps have finished loading! Play will now continue!"));
                    AuroraMCGamePlayer player = (AuroraMCGamePlayer) pl;
                    if (EngineAPI.getActiveGame() != null) {
                        player.getInventory().setItem(0, EngineAPI.getKitItem().getItemStack());
                        if (EngineAPI.getActiveGame().getTeams().size() > 1 && !EngineAPI.getActiveGameInfo().hasTeamCommand() && !EngineAPI.isTeamBalancingEnabled()) {
                            player.getInventory().setItem(1, EngineAPI.getTeamItem().getItemStack());
                        }
                    }
                }
            }
        }.runTask(ServerAPI.getCore());
    }

    public static void setRestartType(String restartType) {
        EngineAPI.restartType = restartType;
    }

    public static String getRestartType() {
        return restartType;
    }

    public static boolean isTeamBalancingEnabled() {
        return teamBalancingEnabled;
    }

    public static void setTeamBalancingEnabled(boolean teamBalancingEnabled) {
        EngineAPI.teamBalancingEnabled = teamBalancingEnabled;
    }

    public static Map<Integer, Reward> getKitLevelRewards() {
        return kitLevelRewards;
    }

    public static float getXpBoostMultiplier() {
        return xpBoostMultiplier;
    }

    public static String getXpBoostMessage() {
        return xpBoostMessage;
    }

    public static void setXpBoostMessage(String xpBoostMessage) {
        EngineAPI.xpBoostMessage = xpBoostMessage;
    }

    public static void setXpBoostMultiplier(float xpBoostMultiplier) {
        EngineAPI.xpBoostMultiplier = xpBoostMultiplier;
    }

    public static Map<String, String> getVersionNumbers() {
        return versionNumbers;
    }

    public static UUID getReloadCode() {
        return reloadCode;
    }
}
