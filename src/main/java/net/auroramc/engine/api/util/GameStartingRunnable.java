/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.api.util;

import net.auroramc.api.cosmetics.Cosmetic;
import net.auroramc.api.player.Team;
import net.auroramc.api.utils.TextFormatter;
import net.auroramc.core.api.ServerAPI;
import net.auroramc.core.api.player.AuroraMCServerPlayer;
import net.auroramc.engine.api.EngineAPI;
import net.auroramc.engine.api.players.AuroraMCGamePlayer;
import net.auroramc.engine.api.server.ServerState;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class GameStartingRunnable extends BukkitRunnable {

    private int startTime;
    private boolean forced;

    public GameStartingRunnable(int startTime, boolean forced) {
        this.startTime = startTime;
        this.forced = forced;
    }

    @Override
    public void run() {
        if (EngineAPI.getGameStartingRunnable() != this) {
            this.cancel();
        }
        for (AuroraMCServerPlayer player : ServerAPI.getPlayers()) {
            player.getScoreboard().setTitle("&3-= &b&lSTARTING IN " + startTime + "&r &3=-");
            switch(startTime) {
                case 5:
                    player.closeInventory();
                case 1:
                case 4:
                case 3:
                case 2:
                case 60:
                case 30:
                case 10:
                    player.playSound(player.getLocation(), Sound.NOTE_STICKS, 100, 1);
                    player.sendMessage(TextFormatter.pluginMessage("Game", String.format("The game is starting in **%s** second%s!", startTime, ((startTime > 1)?"s":""))));

            }
        }

        if (startTime == 0) {
            if (EngineAPI.getActiveGameInfo().hasTeamCommand()) {
                for (AuroraMCServerPlayer player : ServerAPI.getPlayers()) {
                    AuroraMCGamePlayer gp = (AuroraMCGamePlayer) player;
                    if (!gp.isSpectator()) {
                        if (player.getTeam() != null) {
                            for (Team team : EngineAPI.getActiveGame().getTeams().values()) {
                                if (team.getPlayers().size() == 1) {
                                    team.getPlayers().add(player);
                                    player.setTeam(team);
                                    player.sendMessage(TextFormatter.pluginMessage("Game Manager", String.format("You have been assigned to the %s%s§r team", team.getTeamColor(), team.getName())));
                                    for (AuroraMCServerPlayer pl : ServerAPI.getPlayers()) {
                                        pl.updateNametag(player);
                                    }
                                }
                            }
                            if (player.getTeam() == null) {
                                EngineAPI.getActiveGame().generateTeam(player);
                            }
                        }
                        if (gp.getKit() == null && !gp.isSpectator()) {
                            gp.setKit(EngineAPI.getActiveGame().getKits().get(0));
                        }
                    }
                }
            } else {
                if (EngineAPI.isTeamBalancingEnabled()) {
                    teamBalance();
                } else {
                    assignRandomly();
                }
            }

            for (AuroraMCServerPlayer player : ServerAPI.getPlayers()) {
                for (Map.Entry<Cosmetic.CosmeticType, Cosmetic> entry : player.getActiveCosmetics().entrySet()) {
                    if (entry.getKey() == Cosmetic.CosmeticType.GADGET || entry.getKey() == Cosmetic.CosmeticType.BANNER || entry.getKey() == Cosmetic.CosmeticType.HAT  || entry.getKey() == Cosmetic.CosmeticType.PARTICLE) {
                        entry.getValue().onUnequip(player);
                        player.sendMessage(TextFormatter.pluginMessage("Cosmetics", String.format("%s **%s** has been unequipped during the game.", entry.getKey().getName(), entry.getValue().getName())));
                    }
                }
            }
            EngineAPI.getActiveGame().start();
            EngineAPI.setServerState(ServerState.IN_GAME);
            EngineAPI.setGameStartingRunnable(null);
            this.cancel();
            return;
        }
        startTime--;
    }

    public int getStartTime() {
        return startTime;
    }

    public boolean isForced() {
        return forced;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public static void teamBalance() {
        Map<UUID, Integer> parties = new HashMap<>();
        for (AuroraMCServerPlayer player1 : ServerAPI.getPlayers()) {
            if (player1.getPartyUUID() != null && !((AuroraMCGamePlayer)player1).isSpectator()) {
                if (parties.containsKey(player1.getPartyUUID())) {
                    parties.put(player1.getPartyUUID(), parties.get(player1.getPartyUUID()) + 1);
                } else {
                    parties.put(player1.getPartyUUID(), 1);
                }
            }
        }
        Map<UUID, Team> provisionalTeamAssignments = new HashMap<>();
        int players = (int) ServerAPI.getPlayers().stream().filter(player1 -> !player1.isVanished() && !((AuroraMCGamePlayer)player1).isSpectator()).count();
        int numTeams = EngineAPI.getActiveGame().getTeams().size();
        int ppt = players / numTeams;
        int extras = players % numTeams;

        List<Team> teamsToAssign = new ArrayList<>(EngineAPI.getActiveGame().getTeams().values());

        Map<UUID, Integer> partiesLeft = new HashMap<>();


        //Now that we have all the parties and their sizes, assign teams based on parties, then by non parties.
        //Firstly assign teams for parties that have exactly the number of players needed on the low end.
        for (Map.Entry<UUID, Integer> entry : parties.entrySet()) {
            if (entry.getValue() == ppt) {
                provisionalTeamAssignments.put(entry.getKey(), teamsToAssign.remove(0));
            } else {
                partiesLeft.put(entry.getKey(), entry.getValue());
            }
        }

        if (teamsToAssign.size() > 0 || extras > 0 || partiesLeft.size() > 0) {
            //There are still unassigned teams/parties/players
            if (extras > 0 && teamsToAssign.size() > 0 && partiesLeft.size() > 0) {
                //THere are extra spaces with teams and parties left to assign. See if any are 1 over the min number of players per team then
                while (extras > 0) {
                    if (teamsToAssign.size() == 0 || partiesLeft.size() == 0) {
                        break;
                    }
                    Map<UUID, Integer> partiesLeft2 = new HashMap<>(partiesLeft);
                    for (Map.Entry<UUID, Integer> entry : partiesLeft2.entrySet()) {
                        if (entry.getValue() <= ppt + 1) {
                            provisionalTeamAssignments.put(entry.getKey(), teamsToAssign.remove(0));
                            partiesLeft.remove(entry.getKey());
                            extras--;
                        }
                    }
                }
            }

            if (teamsToAssign.size() == 0 && partiesLeft.size() == 0 && extras == 0) {
                assignTeams(provisionalTeamAssignments);
                return;
            }

            //At this point either extras, teamsToAssign or partiesLeft will be 0. Assign based on which of them are 0.
            if (teamsToAssign.size() > 0 && partiesLeft.size() > 0) {
                //There are teams left and parties left to assign but no extra spaces, the parties should fit exactly into the exact number of teams left to assign. Start with the biggest parties then split the rest to fill the teams.
                while (teamsToAssign.size() > 0 && partiesLeft.size() > 0) {
                    UUID biggestParty = null;
                    for (Map.Entry<UUID, Integer> entry : partiesLeft.entrySet()) {
                        if (biggestParty == null) {
                            biggestParty = entry.getKey();
                            continue;
                        }
                        if (entry.getValue() > partiesLeft.get(biggestParty)) {
                            biggestParty = entry.getKey();
                        }
                    }
                    provisionalTeamAssignments.put(biggestParty, teamsToAssign.remove(0));
                    partiesLeft.remove(biggestParty);
                }

                //The biggest/all parties are now assigned. Assign the rest randomly.
            } else if (partiesLeft.size() > 0 && extras > 0) {
                //There are still parties left to assign with extra spaces in teams. See if the parties will be able to fit into a team without going over. Start with the biggest parties.
                outer:
                for (Map.Entry<UUID, Integer> toAssign : partiesLeft.entrySet()) {
                    Map<Team, Integer> teamNumbers = new HashMap<>();
                    for (Map.Entry<UUID, Team> assigned : provisionalTeamAssignments.entrySet()) {
                        if (teamNumbers.containsKey(assigned.getValue())) {
                            teamNumbers.put(assigned.getValue(), teamNumbers.get(assigned.getValue()) + parties.get(assigned.getKey()));
                        } else {
                            teamNumbers.put(assigned.getValue(), parties.get(assigned.getKey()));
                        }
                    }
                    for (Map.Entry<Team, Integer> assigned : teamNumbers.entrySet()) {
                        if (assigned.getValue() + toAssign.getValue() <= ppt || (assigned.getValue() + toAssign.getValue() <= ppt + 1 && extras > 0)) {
                            if (assigned.getValue() + toAssign.getValue() <= ppt + 1) {
                                extras--;
                            }
                            provisionalTeamAssignments.put(toAssign.getKey(), assigned.getKey());
                            continue outer;
                        }
                    }
                }

                //Those parties that fit in another team have been added to a team. Assign teams and assign others randomly.
            }

            //Parties can no longer be distributed to fill a team or there are only non-party players left. Distribute randomly.
            assignTeams(provisionalTeamAssignments);
            assignRandomly();
        } else {
            //There are no extra spaces, and no teams left to assign, assign all teams. Also means everyone in the server is on a team and all teams are even.
            assignTeams(provisionalTeamAssignments);
        }
    }

    public static void assignRandomly() {
        for (AuroraMCServerPlayer player1 : ServerAPI.getPlayers()) {
            AuroraMCGamePlayer gp = (AuroraMCGamePlayer) player1;
            if (player1.getTeam() == null && !gp.isSpectator()) {
                Team leastPlayers = null;
                for (Team team : EngineAPI.getActiveGame().getTeams().values()) {
                    if (leastPlayers == null) {
                        leastPlayers = team;
                        continue;
                    }
                    if (leastPlayers.getPlayers().size() > team.getPlayers().size()) {
                        leastPlayers = team;
                    }
                }
                if (leastPlayers != null) {
                    leastPlayers.getPlayers().add(player1);
                    player1.setTeam(leastPlayers);
                    player1.sendMessage(TextFormatter.pluginMessage("Game Manager", String.format("You have been assigned to the %s%s§r team", leastPlayers.getTeamColor(), leastPlayers.getName())));
                    for (AuroraMCServerPlayer pl : ServerAPI.getPlayers()) {
                        pl.updateNametag(player1);
                    }
                }
            }
            if (gp.getKit() == null && !gp.isSpectator()) {
                gp.setKit(EngineAPI.getActiveGame().getKits().get(0));
            }
        }
    }

    public static void assignTeams(Map<UUID, Team> provisionalTeamAssignments) {
        for (AuroraMCServerPlayer player1 : ServerAPI.getPlayers()) {
            AuroraMCGamePlayer gp = (AuroraMCGamePlayer) player1;
            if (gp.getPartyUUID() != null) {
                if (!gp.isSpectator()) {
                    if (provisionalTeamAssignments.containsKey(gp.getPartyUUID())) {
                        Team team = provisionalTeamAssignments.get(gp.getPartyUUID());
                        team.getPlayers().add(player1);
                        player1.setTeam(team);
                        player1.sendMessage(TextFormatter.pluginMessage("Game Manager", String.format("You have been assigned to the %s%s§r team", team.getTeamColor(), team.getName())));
                        for (AuroraMCServerPlayer pl : ServerAPI.getPlayers()) {
                            pl.updateNametag(player1);
                        }
                    }
                }
                if (gp.getKit() == null && !gp.isSpectator()) {
                    gp.setKit(EngineAPI.getActiveGame().getKits().get(0));
                }
            }
        }
    }
}
