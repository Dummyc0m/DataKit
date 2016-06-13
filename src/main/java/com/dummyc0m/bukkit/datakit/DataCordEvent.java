package com.dummyc0m.bukkit.datakit;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * Created by Dummyc0m on 3/9/16.
 */
public abstract class DataCordEvent extends Event {
    private Player player;

    public DataCordEvent(Player player) {
        super(true);
        this.player = player;
    }

    public final Player getPlayer() {
        return this.player;
    }
}
