package top.sanscraft.swapball.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import top.sanscraft.swapball.SwapBallPlugin;
import top.sanscraft.swapball.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProjectileListener implements Listener {
    
    private final SwapBallPlugin plugin;
    private final Map<UUID, Long> cooldowns;
    
    public ProjectileListener(SwapBallPlugin plugin) {
        this.plugin = plugin;
        this.cooldowns = new HashMap<>();
    }
    
    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Snowball)) {
            return;
        }
        
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        
        Player shooter = (Player) event.getEntity().getShooter();
        ItemStack itemInHand = shooter.getInventory().getItemInMainHand();
        
        if (!ItemBuilder.isSwapBall(itemInHand)) {
            return;
        }
        
        // Check permission
        if (!shooter.hasPermission("swapball.use")) {
            shooter.sendMessage("§cYou don't have permission to use SwapBalls!");
            event.setCancelled(true);
            return;
        }
        
        // Check cooldown
        long cooldownTime = plugin.getConfig().getInt("config.swap-ball.cooldown", 5) * 1000L;
        if (cooldowns.containsKey(shooter.getUniqueId())) {
            long timeLeft = cooldowns.get(shooter.getUniqueId()) + cooldownTime - System.currentTimeMillis();
            if (timeLeft > 0) {
                shooter.sendMessage("§cSwapBall on cooldown! " + (timeLeft / 1000 + 1) + " seconds remaining.");
                event.setCancelled(true);
                return;
            }
        }
        
        // Check WorldGuard regions
        if (!canUseInLocation(shooter, shooter.getLocation())) {
            shooter.sendMessage("§cYou cannot use SwapBalls in this area!");
            event.setCancelled(true);
            return;
        }
        
        // Find closest target
        Player target = findClosestTarget(shooter);
        if (target == null) {
            shooter.sendMessage("§cNo valid targets found nearby!");
            event.setCancelled(true);
            return;
        }
        
        // Set cooldown
        cooldowns.put(shooter.getUniqueId(), System.currentTimeMillis());
        
        // Add to heat-seeking manager
        plugin.getHeatSeekingManager().addHeatSeekingProjectile(event.getEntity(), shooter, target);
        
        // Play launch sound
        String launchSound = plugin.getConfig().getString("config.swap-ball.sounds.launch", "ENTITY_SNOWBALL_THROW");
        try {
            shooter.playSound(shooter.getLocation(), Sound.valueOf(launchSound), 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            shooter.playSound(shooter.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1.0f, 1.0f);
        }
        
        shooter.sendMessage("§aSwapBall locked onto §e" + target.getName() + "§a!");
    }
    
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball)) {
            return;
        }
        
        if (!(event.getEntity().getShooter() instanceof Player)) {
            return;
        }
        
        Player shooter = (Player) event.getEntity().getShooter();
        
        // Remove from heat-seeking manager
        plugin.getHeatSeekingManager().removeProjectile(event.getEntity());
        
        // Handle player hit
        if (event.getHitEntity() instanceof Player) {
            Player target = (Player) event.getHitEntity();
            
            // Check if target can be swapped
            if (!canUseInLocation(target, target.getLocation())) {
                shooter.sendMessage("§cCannot swap with " + target.getName() + " - they are in a protected area!");
                return;
            }
            
            // Perform the swap
            performSwap(shooter, target);
            
            // Play hit sound
            String hitSound = plugin.getConfig().getString("config.swap-ball.sounds.hit", "ENTITY_ENDERMAN_TELEPORT");
            try {
                shooter.playSound(shooter.getLocation(), Sound.valueOf(hitSound), 1.0f, 1.0f);
                target.playSound(target.getLocation(), Sound.valueOf(hitSound), 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                shooter.playSound(shooter.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            }
        }
    }
    
    private Player findClosestTarget(Player shooter) {
        Player closest = null;
        double closestDistance = Double.MAX_VALUE;
        double maxDistance = plugin.getConfig().getDouble("config.heat-seeking.max-target-distance", 50.0);
        
        // Get the direction the shooter is looking
        Location shooterEyeLocation = shooter.getEyeLocation();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.equals(shooter)) {
                continue; // Don't target self
            }
            
            if (player.getWorld() != shooter.getWorld()) {
                continue; // Different world
            }
            
            double distance = player.getLocation().distance(shooter.getLocation());
            if (distance > maxDistance) {
                continue; // Too far
            }
            
            // Calculate angle between shooter's look direction and direction to player
            Location playerLocation = player.getLocation().add(0, 1, 0); // Aim for player's chest
            org.bukkit.util.Vector toPlayer = playerLocation.subtract(shooterEyeLocation).toVector();
            org.bukkit.util.Vector lookDirection = shooterEyeLocation.getDirection();
            
            double angle = Math.acos(Math.max(-1.0, Math.min(1.0, 
                lookDirection.normalize().dot(toPlayer.normalize()))));
            
            // Prefer players in the direction the shooter is looking (within 90 degrees)
            if (angle > Math.PI / 2) {
                distance *= 2; // Penalty for players behind the shooter
            }
            
            if (distance < closestDistance) {
                closest = player;
                closestDistance = distance;
            }
        }
        
        return closest;
    }
    
    private boolean canUseInLocation(Player player, Location location) {
        if (!plugin.isWorldGuardAvailable()) {
            return true; // No WorldGuard, allow everywhere
        }
        
        List<String> allowedRegions = plugin.getAllowedRegions();
        if (allowedRegions.isEmpty()) {
            return true; // No restrictions configured
        }
        
        com.sk89q.worldguard.protection.managers.RegionManager regionManager = plugin.getRegionManager(location.getWorld());
        if (regionManager == null) {
            return true; // No region manager for this world
        }
        
        LocalPlayer localPlayer = plugin.getWorldGuard().wrapPlayer(player);
        ApplicableRegionSet regions = regionManager.getApplicableRegions(
            BukkitAdapter.asBlockVector(location)
        );
        
        // Check if player is in any allowed region
        for (String allowedRegion : allowedRegions) {
            if (regions.getRegions().stream().anyMatch(region -> region.getId().equals(allowedRegion))) {
                return true;
            }
        }
        
        // If no allowed regions found and list is not empty, deny access
        return false;
    }
    
    private void performSwap(Player shooter, Player target) {
        Location shooterLocation = shooter.getLocation().clone();
        Location targetLocation = target.getLocation().clone();
        
        // Teleport players
        shooter.teleport(targetLocation);
        target.teleport(shooterLocation);
        
        // Send messages
        shooter.sendMessage("§aSwapped positions with §e" + target.getName() + "§a!");
        target.sendMessage("§aSwapped positions with §e" + shooter.getName() + "§a!");
        
        // Spawn particles at both locations
        if (plugin.getConfig().getBoolean("config.swap-ball.particles.enabled", true)) {
            String particleType = plugin.getConfig().getString("config.swap-ball.particles.type", "FLAME");
            int particleAmount = plugin.getConfig().getInt("config.swap-ball.particles.amount", 10);
            
            try {
                org.bukkit.Particle particle = org.bukkit.Particle.valueOf(particleType);
                shooter.getWorld().spawnParticle(particle, shooterLocation, particleAmount, 1.0, 1.0, 1.0, 0.1);
                target.getWorld().spawnParticle(particle, targetLocation, particleAmount, 1.0, 1.0, 1.0, 0.1);
            } catch (IllegalArgumentException e) {
                // Invalid particle type, use default
                shooter.getWorld().spawnParticle(org.bukkit.Particle.FLAME, shooterLocation, particleAmount, 1.0, 1.0, 1.0, 0.1);
                target.getWorld().spawnParticle(org.bukkit.Particle.FLAME, targetLocation, particleAmount, 1.0, 1.0, 1.0, 0.1);
            }
        }
    }
}
