/*
 * Copyright (c) 2022 AuroraMC Ltd. All Rights Reserved.
 */

package net.auroramc.engine.api.players;

import net.auroramc.core.api.players.AuroraMCPlayer;
import net.auroramc.engine.api.games.Kit;

public class AuroraMCGamePlayer extends AuroraMCPlayer {

    private boolean spectator;
    private Kit kit;

    public AuroraMCGamePlayer(AuroraMCPlayer oldPlayer) {
        super(oldPlayer);
        spectator = false;
        kit = null;
    }

    public boolean isSpectator() {
        return spectator;
    }

    public void setSpectator(boolean spectator) {
        this.spectator = spectator;
    }

    public Kit getKit() {
        return kit;
    }

    public void setKit(Kit kit) {
        this.kit = kit;
    }
}
