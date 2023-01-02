/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.api.games;

import net.auroramc.core.api.AuroraMCAPI;
import net.auroramc.core.api.permissions.Rank;
import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.core.api.players.Disguise;
import net.auroramc.engine.api.backend.EngineDatabaseManager;
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

    public GameSession(String gameRegistryKey, GameVariation gameVariation) {
        this.uuid = UUID.randomUUID();
        this.players = new ArrayList<>();
        this.gameLog = new JSONArray();
        this.gameRegistryKey = gameRegistryKey;
        this.gameVariation = ((gameVariation == null)?"None":gameVariation.getRegistryKey());
    }

    public void start() {
        this.startTimestamp = System.currentTimeMillis();
        for (AuroraMCPlayer gamePlayer : AuroraMCAPI.getPlayers()) {
            this.players.add(new GamePlayer(gamePlayer));
        }
    }

    public void end(boolean isVoid) {
        endTimestamp = System.currentTimeMillis();
        JSONObject obj = new JSONObject();
        obj.put("uuid", uuid.toString());
        obj.put("game", gameRegistryKey);
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
                EngineDatabaseManager.uploadGameSession(uuid, gameRegistryKey, obj);
            }
        }.runTaskAsynchronously(AuroraMCAPI.getCore());
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

        public GamePlayer(AuroraMCPlayer player) {
            this.uuid = player.getPlayer().getUniqueId();
            this.name = player.getName();
            this.amcId = player.getId();
            this.rank = player.getRank();
            this.disguise = player.getActiveDisguise();
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
            return object.toString();
        }
    }

}
