package com.dummyc0m.bukkit.datakit;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by Dummyc0m on 3/9/16.
 */
public final class DataCordReceiveEvent extends DataCordEvent {
    private static final HandlerList handlers = new HandlerList();
    private static final Gson gson = new Gson();
    private static final Type type = new TypeToken<Map<String, String>>(){}.getType();
    private final Map<String, String> dataMap;

    public DataCordReceiveEvent(Player who, String serializedData) {
        super(who);
        dataMap = gson.fromJson(serializedData, type);
    }

    public String getData(String pluginId) {
        return dataMap.get(pluginId);
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
