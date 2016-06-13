package com.dummyc0m.bukkit.datakit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Dummyc0m on 3/9/16.
 */
public class PlayerManager {
    private final Map<UUID, PlayerState> playerStateMap;
    private final Map<UUID, UUID> dataCordUUIDMap;

    public PlayerManager() {
        playerStateMap = new HashMap<>();
        dataCordUUIDMap = new HashMap<>();
    }

    public boolean getLocked(UUID uuid) {
        return playerStateMap.get(uuid) != null;
    }

    public void setLocked(UUID uuid, boolean locked) {
        if(locked) {
            playerStateMap.put(uuid, PlayerState.LOCKED);
        } else {
            playerStateMap.remove(uuid);
        }
    }

    public void putUUID(UUID datakit, UUID datacord) {
        if(datacord == null) {
            dataCordUUIDMap.remove(datakit);
            return;
        }
        dataCordUUIDMap.put(datakit, datacord);
    }

    public UUID popDataCordUUID(UUID datakit) {
        return dataCordUUIDMap.remove(datakit);
    }

    public UUID getDataCordUUID(UUID datakit) {
        return dataCordUUIDMap.get(datakit);
    }

    public enum PlayerState {
        LOCKED
    }
}
