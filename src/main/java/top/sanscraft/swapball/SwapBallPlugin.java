package top.sanscraft.swapball;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import top.sanscraft.swapball.commands.SwapBallCommand;
import top.sanscraft.swapball.listeners.ProjectileListener;
import top.sanscraft.swapball.managers.HeatSeekingManager;
import top.sanscraft.swapball.utils.ItemBuilder;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class SwapBallPlugin extends JavaPlugin {
    
    private static SwapBallPlugin instance;
    private HeatSeekingManager heatSeekingManager;
    private WorldGuardPlugin worldGuard;
    private List<String> allowedRegions;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config
        saveDefaultConfig();
        
        // Load configuration
        loadConfiguration();
        
        // Initialize WorldGuard
        initializeWorldGuard();
        
        // Initialize managers
        heatSeekingManager = new HeatSeekingManager(this);
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new ProjectileListener(this), this);
        
        // Register commands
        getCommand("swapball").setExecutor(new SwapBallCommand(this));
        
        // Start heat-seeking task
        heatSeekingManager.startTask();
        
        getLogger().info("SwapBall 2.0 has been enabled!");
    }
    
    @Override
    public void onDisable() {
        if (heatSeekingManager != null) {
            heatSeekingManager.stopTask();
        }
        getLogger().info("SwapBall 2.0 has been disabled!");
    }
    
    private void loadConfiguration() {
        allowedRegions = getConfig().getStringList("config.allowed-regions");
    }
    
    private void initializeWorldGuard() {
        if (getServer().getPluginManager().getPlugin("WorldGuard") == null) {
            getLogger().warning("WorldGuard not found! Region restrictions will not work.");
            return;
        }
        
        worldGuard = WorldGuardPlugin.inst();
        getLogger().info("WorldGuard integration enabled!");
    }
    
    public static SwapBallPlugin getInstance() {
        return instance;
    }
    
    public HeatSeekingManager getHeatSeekingManager() {
        return heatSeekingManager;
    }
    
    public WorldGuardPlugin getWorldGuard() {
        return worldGuard;
    }
    
    public List<String> getAllowedRegions() {
        return allowedRegions;
    }
    
    public boolean isWorldGuardAvailable() {
        return worldGuard != null;
    }
    
    public com.sk89q.worldguard.protection.managers.RegionManager getRegionManager(org.bukkit.World world) {
        if (!isWorldGuardAvailable()) {
            return null;
        }
        
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        return container.get(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(world));
    }
}
