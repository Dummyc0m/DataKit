package com.dummyc0m.bukkit.datakit;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Plugin Channel: DataCord
 *
 * Sent when the client connects to the target server.
 * Command: Connect
 * Argument: uuid, data
 * (DataCord)
 *
 * Send when you try to save the data to DataCord
 * Command: Save
 * Argument: uuid, data
 *
 * Send when the player disconnects from your server
 * Command: Disconnect
 * Argument: uuid, data
 */
public class DataKitPlugin extends JavaPlugin {
    private ConfigFile configFile;
    private DataKitListener listener;

    @Override
    public void onEnable() {
        configFile = new ConfigFile(getDataFolder(), "datakit.json", Settings.class);
        Settings settings = ((Settings) configFile.getSettings());
        listener = new DataKitListener(this,
                settings.getAutoSaveInterval(),
                settings.getLoadingDataMessage(),
                settings.getLoadedDataMessage(),
                settings.getHost(),
                settings.getPort());
        getServer().getPluginManager().registerEvents(listener, this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerOutgoingPluginChannel(this, "DataCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "DataCord", listener);
        getLogger().info("DataKit Initialized");
    }

    @Override
    public void onDisable() {
        configFile.save();
        listener.shutdownGracefully();
    }
}
