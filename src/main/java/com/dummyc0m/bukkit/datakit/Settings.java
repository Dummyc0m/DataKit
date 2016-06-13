package com.dummyc0m.bukkit.datakit;

/**
 * Created by Dummyc0m on 3/9/16.
 */
public class Settings {
    private int autoSaveInterval;

    private String loadingDataMessage;
    private String loadedDataMessage;

    private String host;
    private int port;

    public Settings() {
        autoSaveInterval = 20;
        loadingDataMessage = "[DataKit] Loading Your Data, Please Wait...";
        loadedDataMessage = "[DataKit] Data Loaded";
        host = "localhost";
        port = 25555;
    }

    public int getAutoSaveInterval() {
        return autoSaveInterval;
    }

    public String getLoadingDataMessage() {
        return loadingDataMessage;
    }

    public String getLoadedDataMessage() {
        return loadedDataMessage;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
