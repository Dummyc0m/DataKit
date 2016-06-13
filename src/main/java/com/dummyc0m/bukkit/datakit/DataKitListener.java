package com.dummyc0m.bukkit.datakit;

import com.dummyc0m.bukkit.datakit.client.DataClient;
import com.dummyc0m.bungeecord.datacord.server.DataPacket;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Dummyc0m on 3/9/16.
 */
public class DataKitListener implements Listener, PluginMessageListener {
    private final PlayerManager playerManager = new PlayerManager();
    private final Map<UUID, BukkitTask> uuidTaskMap = new HashMap<>();
    private final DataClient dataClient;
    private final PluginManager pluginManager;
    private final JavaPlugin plugin;
    private final int autoSaveInterval;
    private final String loadingDataMessage;
    private final String loadedDataMessage;

    public DataKitListener(JavaPlugin plugin, int autoSaveInterval, String loadingDataMessage, String loadedDataMessage,
                           String host, int port) {
        this.plugin = plugin;
        dataClient = new DataClient(host, port);
        dataClient.start();
        pluginManager = plugin.getServer().getPluginManager();
        this.autoSaveInterval = autoSaveInterval * 1200;
        this.loadingDataMessage = loadingDataMessage;
        this.loadedDataMessage = loadedDataMessage;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        playerManager.setLocked(event.getPlayer().getUniqueId(), true);
        event.getPlayer().sendMessage(loadingDataMessage);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if(!playerManager.getLocked(player.getUniqueId())) {
//            plugin.getLogger().info("sendingdisconnect");
//            ByteArrayDataOutput out = ByteStreams.newDataOutput();
//            out.writeUTF("Disconnect");
//            out.writeUTF(playerManager.popDataCordUUID(player.getUniqueId()).toString());
            DataCordSendEvent sendEvent = new DataCordSendEvent(player);
            pluginManager.callEvent(sendEvent);
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> dataClient.write(new DataPacket(DataPacket.Type.DISCONNECT,
                    playerManager.popDataCordUUID(player.getUniqueId()),
                    sendEvent.getSerializedData())));
//            out.writeUTF(sendEvent.getSerializedData());
//            plugin.getServer().sendPluginMessage(plugin, "DataCord", out.toByteArray());
            BukkitTask task = uuidTaskMap.remove(player.getUniqueId());
            if(task != null) {
                task.cancel();
            }
        }
    }

    @Override
    public void onPluginMessageReceived(String s, Player player, byte[] bytes) {
        if("DataCord".equals(s)) {
            ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
            String command = in.readUTF();
            UUID uuid = UUID.fromString(in.readUTF());
            if("Connect".equals(command)) {
                plugin.getLogger().info("ProxiedPlayer " + uuid + " connect with server UUID " + player.getUniqueId());
                DataCordReceiveEvent event = new DataCordReceiveEvent(player, in.readUTF());
                pluginManager.callEvent(event);
                playerManager.setLocked(player.getUniqueId(), false);
                playerManager.putUUID(player.getUniqueId(), uuid);
                player.sendMessage(loadedDataMessage);
                AutoSaveTask task = new AutoSaveTask(player);
                if(autoSaveInterval > 0) {
                    plugin.getLogger().info("Setting AutoSave " + autoSaveInterval);
                    BukkitTask bukkitTask = task.runTaskTimer(plugin, autoSaveInterval, autoSaveInterval);
                    uuidTaskMap.put(player.getUniqueId(), bukkitTask);
                }
            }
        }
    }

    public void shutdownGracefully() {
        dataClient.close();
    }

    private class AutoSaveTask extends BukkitRunnable {
        private WeakReference<Player> player;

        public AutoSaveTask(Player player) {
            this.player = new WeakReference<>(player);
        }

        @Override
        public void run() {
            Player player = this.player.get();
            if(player != null && player.isOnline()) {
//                ByteArrayDataOutput out = ByteStreams.newDataOutput();
//                out.writeUTF("Save");
//                out.writeUTF(player.getUniqueId().toString());
                DataCordSendEvent sendEvent = new DataCordSendEvent(player);
                pluginManager.callEvent(sendEvent);
                plugin.getLogger().info("[DEBUG] AutoSaving");
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () ->
                        dataClient.write(new DataPacket(DataPacket.Type.SAVE,
                        playerManager.getDataCordUUID(player.getUniqueId()),
                        sendEvent.getSerializedData())));
//                out.writeUTF(sendEvent.getSerializedData());
//                player.sendPluginMessage(plugin, "DataCord", out.toByteArray());
            } else {
                plugin.getLogger().info("[DEBUG] Canceling AutoSave");
                this.cancel();
            }
        }
    }

    //Starting Player Lock

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        handlePlayerEvent(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerAchieve(PlayerAchievementAwardedEvent event) {
        handlePlayerEvent(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerAnimate(PlayerAnimationEvent event) {
        handlePlayerEvent(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerEnterBed(PlayerBedEnterEvent event) {
        handlePlayerEvent(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerFillBucket(PlayerBucketFillEvent event) {
        handlePlayerEvent(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerEmptyBucket(PlayerBucketEmptyEvent event) {
        handlePlayerEvent(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        handlePlayerEvent(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        handlePlayerEvent(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerEditBook(PlayerEditBookEvent event) {
        handlePlayerEvent(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerFish(PlayerFishEvent event) {
        handlePlayerEvent(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerChangeGameMode(PlayerGameModeChangeEvent event) {
        handlePlayerEvent(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        handlePlayerEvent(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerInventory(InventoryInteractEvent event) {
        if(playerManager.getLocked(event.getWhoClicked().getUniqueId())){
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerInventoryOpen(InventoryOpenEvent event) {
        if(playerManager.getLocked(event.getPlayer().getUniqueId())){
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        handlePlayerEvent(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerConsumeItem(PlayerItemConsumeEvent event) {
        handlePlayerEvent(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerHoldItem(PlayerItemHeldEvent event) {
        handlePlayerEvent(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        handlePlayerEvent(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        handlePlayerEvent(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerShearEntity(PlayerShearEntityEvent event) {
        handlePlayerEvent(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerStatIncrement(PlayerStatisticIncrementEvent event) {
        handlePlayerEvent(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        handlePlayerEvent(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        handlePlayerEvent(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
        handlePlayerEvent(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerUnleashEntity(PlayerUnleashEntityEvent event) {
        if(playerManager.getLocked(event.getPlayer().getUniqueId())){
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerVelocity(PlayerVelocityEvent event) {
        handlePlayerEvent(event);
    }

    private <T extends PlayerEvent & Cancellable> void handlePlayerEvent(T event) {
        if(playerManager.getLocked(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
