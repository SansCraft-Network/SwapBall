package top.sanscraft.swapball.managers;

import top.sanscraft.swapball.SwapBallPlugin;
import top.sanscraft.swapball.models.HeatSeekingProjectile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HeatSeekingManager {
    
    private final SwapBallPlugin plugin;
    private final List<HeatSeekingProjectile> heatSeekingProjectiles;
    private BukkitTask task;
    
    public HeatSeekingManager(SwapBallPlugin plugin) {
        this.plugin = plugin;
        this.heatSeekingProjectiles = new ArrayList<>();
    }
    
    public void addHeatSeekingProjectile(Projectile projectile, Player shooter, Player target) {
        HeatSeekingProjectile hsp = new HeatSeekingProjectile(projectile, shooter, target);
        heatSeekingProjectiles.add(hsp);
    }
    
    public void startTask() {
        if (task != null) {
            task.cancel();
        }
        
        int updateFrequency = plugin.getConfig().getInt("config.heat-seeking.update-frequency", 2);
        
        task = new BukkitRunnable() {
            @Override
            public void run() {
                updateHeatSeekingProjectiles();
            }
        }.runTaskTimer(plugin, 0L, updateFrequency);
    }
    
    public void stopTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        heatSeekingProjectiles.clear();
    }
    
    private void updateHeatSeekingProjectiles() {
        Iterator<HeatSeekingProjectile> iterator = heatSeekingProjectiles.iterator();
        
        while (iterator.hasNext()) {
            HeatSeekingProjectile hsp = iterator.next();
            
            // Remove if projectile is dead or invalid
            if (!hsp.getProjectile().isValid() || hsp.getProjectile().isDead()) {
                iterator.remove();
                continue;
            }
            
            // Update target to closest player if original target is invalid
            if (!hsp.getTarget().isOnline() || hsp.getTarget().isDead()) {
                Player newTarget = findClosestPlayer(hsp);
                if (newTarget == null) {
                    iterator.remove();
                    continue;
                }
                hsp.setTarget(newTarget);
            }
            
            // Update projectile direction
            updateProjectileDirection(hsp);
        }
    }
    
    private Player findClosestPlayer(HeatSeekingProjectile hsp) {
        Player closest = null;
        double closestDistance = Double.MAX_VALUE;
        double maxDistance = plugin.getConfig().getDouble("config.heat-seeking.max-target-distance", 50.0);
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.equals(hsp.getShooter())) {
                continue; // Don't target the shooter
            }
            
            if (player.getWorld() != hsp.getProjectile().getWorld()) {
                continue; // Different world
            }
            
            double distance = player.getLocation().distance(hsp.getProjectile().getLocation());
            if (distance < closestDistance && distance <= maxDistance) {
                closest = player;
                closestDistance = distance;
            }
        }
        
        return closest;
    }
    
    private void updateProjectileDirection(HeatSeekingProjectile hsp) {
        Projectile projectile = hsp.getProjectile();
        Player target = hsp.getTarget();
        
        Vector currentVelocity = projectile.getVelocity();
        Vector targetDirection = target.getLocation().add(0, 1, 0).subtract(projectile.getLocation()).toVector();
        
        if (targetDirection.lengthSquared() == 0) {
            return; // Avoid division by zero
        }
        
        targetDirection.normalize();
        
        // Get configuration values
        double maxTurnAngle = Math.toRadians(plugin.getConfig().getDouble("config.heat-seeking.max-turn-angle", 15.0));
        double speedMultiplier = plugin.getConfig().getDouble("config.heat-seeking.speed-multiplier", 1.5);
        
        // Calculate the angle between current direction and target direction
        Vector currentDirection = currentVelocity.clone().normalize();
        double angle = Math.acos(Math.max(-1.0, Math.min(1.0, currentDirection.dot(targetDirection))));
        
        // Limit the turn angle
        if (angle > maxTurnAngle) {
            // Interpolate between current direction and target direction
            Vector cross = currentDirection.getCrossProduct(targetDirection);
            if (cross.lengthSquared() > 0) {
                cross.normalize();
                // Rotate current direction towards target by maxTurnAngle
                double cos = Math.cos(maxTurnAngle);
                double sin = Math.sin(maxTurnAngle);
                targetDirection = currentDirection.multiply(cos).add(cross.multiply(sin));
            }
        }
        
        // Apply speed and set velocity
        Vector newVelocity = targetDirection.multiply(currentVelocity.length() * speedMultiplier);
        projectile.setVelocity(newVelocity);
        
        // Add particle effects
        if (plugin.getConfig().getBoolean("config.swap-ball.particles.enabled", true)) {
            String particleType = plugin.getConfig().getString("config.swap-ball.particles.type", "FLAME");
            int particleAmount = plugin.getConfig().getInt("config.swap-ball.particles.amount", 10);
            
            try {
                org.bukkit.Particle particle = org.bukkit.Particle.valueOf(particleType);
                projectile.getWorld().spawnParticle(particle, projectile.getLocation(), particleAmount, 0.1, 0.1, 0.1, 0.01);
            } catch (IllegalArgumentException e) {
                // Invalid particle type, use default
                projectile.getWorld().spawnParticle(org.bukkit.Particle.FLAME, projectile.getLocation(), particleAmount, 0.1, 0.1, 0.1, 0.01);
            }
        }
    }
    
    public void removeProjectile(Projectile projectile) {
        heatSeekingProjectiles.removeIf(hsp -> hsp.getProjectile().equals(projectile));
    }
}
