package dev.lokspel.deathswap;

import dev.lokspel.deathswap.api.DeathSwapAPI;
import dev.lokspel.deathswap.commands.deathswap.DeathSwapCmd;
import dev.lokspel.deathswap.config.DeathSwapConfig;
import dev.lokspel.deathswap.game.GameManager;
import dev.lokspel.deathswap.listener.AsyncChatListener;
import dev.lokspel.deathswap.listener.EntityDamageListener;
import dev.lokspel.deathswap.listener.PlayerAdvancementDoneListener;
import dev.lokspel.deathswap.listener.PlayerDeathListener;
import dev.lokspel.deathswap.listener.PlayerQuitListener;
import dev.lokspel.deathswap.listener.PlayerRespawnListener;
import dev.lokspel.deathswap.listener.PlayerTeleportListener;
import dev.lokspel.deathswap.listener.WorldInitListener;
import dev.lokspel.deathswap.util.PluginPermission;
import dev.lokspel.deathswap.util.SoftDependUtil;
import dev.lokspel.deathswap.util.entityhider.PlayerHider;
import dev.lokspel.deathswap.util.placeholderapi.PlayerExpansion;
import dev.lokspel.deathswap.world.WorldPool;
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import dev.faststats.Metrics;
import dev.faststats.bukkit.BukkitContext;
import lombok.Getter;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class DeathSwap extends JavaPlugin {

    @Getter
    private static DeathSwap instance;
    private WorldPool worldPool;
    private GameManager gameManager;
    private PlayerHider playerHider;

    private final BukkitContext fastStatsContext = new BukkitContext.Factory(this, "e4269c0474f634d0ab87b81ebf5cdb9f")
            .metrics(Metrics.Factory::create)
            .create();

    @Override
    public void onLoad() {
        instance = this;

        if (SoftDependUtil.PACKET_EVENTS_ENABLED) {
            PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
            PacketEvents.getAPI().load();
        }
    }

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("Loading configuration");
        DeathSwapConfig.load(getDataFolder().toPath());

        worldPool = new WorldPool(this);
        gameManager = new GameManager(this);

        if (SoftDependUtil.PACKET_EVENTS_ENABLED) {
            PacketEvents.getAPI().init();
            playerHider = new PlayerHider(this);
        }

        getLogger().info("Registering permissions");
        PluginPermission.registerAll();

        getLogger().info("Registering commands");
        new DeathSwapCmd().enable();

        getLogger().info("Registering listeners");
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new WorldInitListener(this), this);
        pm.registerEvents(new PlayerDeathListener(this), this);
        pm.registerEvents(new PlayerQuitListener(this), this);
        pm.registerEvents(new AsyncChatListener(this), this);
        pm.registerEvents(new PlayerAdvancementDoneListener(this), this);
        pm.registerEvents(new EntityDamageListener(this), this);
        pm.registerEvents(new PlayerRespawnListener(this), this);
        pm.registerEvents(new PlayerTeleportListener(this), this);

        new DeathSwapAPI(this);

        if (SoftDependUtil.PLACEHOLDER_API_ENABLED) {
            getLogger().info("Registering hooks");
            new PlayerExpansion(this, "deathswap").register();
            new PlayerExpansion(this, "ds").register();
        }

        fastStatsContext.ready();
        getLogger().info("Done.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling DeathSwap...");
        PluginPermission.unregisterAll();
        fastStatsContext.shutdown();
        if (gameManager != null) {
            gameManager.disable();
            gameManager = null;
        }
        if (worldPool != null) {
            worldPool.disable();
            worldPool = null;
        }
        if (SoftDependUtil.PACKET_EVENTS_ENABLED) {
            PacketEvents.getAPI().terminate();
        }
        getLogger().info("Disabled.");
    }

}
