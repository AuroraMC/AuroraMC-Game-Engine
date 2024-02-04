/*
 * Copyright (c) 2022-2024 Ethan P-B. All Rights Reserved.
 */

package net.auroramc.engine.api.games;

import net.auroramc.api.permissions.Rank;
import net.auroramc.api.player.Disguise;
import net.auroramc.api.player.Team;
import net.auroramc.api.utils.TextFormatter;
import net.auroramc.core.api.ServerAPI;
import net.auroramc.core.api.player.AuroraMCServerPlayer;
import net.auroramc.engine.api.backend.EngineDatabaseManager;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameSession {

    private final UUID uuid;
    private final String gameRegistryKey;
    private final String gameVariation;
    private long startTimestamp;
    private final List<GamePlayer> players;
    private final JSONArray gameLog;
    private long endTimestamp;

    public GameSession(String gameRegistryKey, GameVariationInfo gameVariation) {
        this.uuid = UUID.randomUUID();
        this.players = new ArrayList<>();
        this.gameLog = new JSONArray();
        this.gameRegistryKey = gameRegistryKey;
        this.gameVariation = ((gameVariation == null)?"None":gameVariation.getRegistryKey());
    }

    public void start() {
        this.startTimestamp = System.currentTimeMillis();
        for (AuroraMCServerPlayer gamePlayer : ServerAPI.getPlayers()) {
            this.players.add(new GamePlayer(gamePlayer));
        }
    }

    public void end(boolean isVoid) {
        endTimestamp = System.currentTimeMillis();
        JSONObject obj = new JSONObject();
        obj.put("uuid", uuid.toString());
        obj.put("game", ((gameRegistryKey == null)?"EVENT":gameRegistryKey));
        obj.put("variation", gameVariation);
        obj.put("start", startTimestamp);
        obj.put("end", endTimestamp);
        obj.put("void", isVoid);
        obj.put("log", gameLog);
        JSONArray array = new JSONArray();
        for (GamePlayer player : players) {
            array.put(player.toJSON());
        }
        obj.put("players", array);

        new BukkitRunnable(){
            @Override
            public void run() {
                EngineDatabaseManager.uploadGameSession(uuid, gameRegistryKey, obj, players);
            }
        }.runTaskAsynchronously(ServerAPI.getCore());
    }

    public void log(GameLogEntry gameLogEntry) {
        this.gameLog.put(gameLogEntry.toJSON());
    }

    public UUID getUuid() {
        return uuid;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public static class GameLogEntry {

        private final long timestamp;
        private final GameEvent event;
        private final JSONObject data;

        public GameLogEntry(GameEvent event, JSONObject data) {
            this.event = event;
            this.timestamp = System.currentTimeMillis();
            this.data = data;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public JSONObject getData() {
            return data;
        }

        public String toJSON() {
            JSONObject object = new JSONObject();
            object.put("timestamp", timestamp);
            object.put("event", event.name());
            object.put("data", data);
            return object.toString();
        }
    }

    public static enum GameEvent {
        START,
        RELEASED,
        GAME_EVENT,
        DEATH,
        END
    }

    public static class GamePlayer {

        private final UUID uuid;
        private final String name;
        private final int amcId;
        private final Rank rank;
        private final Disguise disguise;
        private final boolean vanished;
        private final Team team;
        private final Kit kit;

        public GamePlayer(AuroraMCServerPlayer player) {
            this.uuid = player.getUniqueId();
            this.name = player.getName();
            this.amcId = player.getId();
            this.rank = player.getRank();
            this.disguise = player.getActiveDisguise();
            this.vanished = player.isVanished();
            this.team = player.getTeam();
            this.kit = ((AuroraMCGamePlayer)player).getKit();
        }

        public String getName() {
            return name;
        }

        public int getAmcId() {
            return amcId;
        }

        public Rank getRank() {
            return rank;
        }

        public UUID getUuid() {
            return uuid;
        }

        public Disguise getDisguise() {
            return disguise;
        }

        public boolean isVanished() {
            return vanished;
        }

        public Team getTeam() {
            return team;
        }

        public String toJSON() {
            JSONObject object = new JSONObject();
            object.put("name", name);
            object.put("uuid", uuid.toString());
            object.put("amc_id", amcId);
            object.put("rank", rank.name());
            JSONObject disguise = new JSONObject();
            if (this.disguise != null) {
                disguise.put("name", this.disguise.getName());
                disguise.put("rank", this.disguise.getRank());
            }
            object.put("disguise", disguise);
            object.put("vanished", vanished);
            object.put("team", ((team == null)?"Spectator":team.getName()));
            object.put("kit", ChatColor.stripColor(TextFormatter.convert(((kit == null)?"None":kit.getName()))));
            return object.toString();
        }
    }

}
