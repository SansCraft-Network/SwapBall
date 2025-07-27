package top.sanscraft.swapball.models;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

public class HeatSeekingProjectile {
    
    private final Projectile projectile;
    private final Player shooter;
    private Player target;
    
    public HeatSeekingProjectile(Projectile projectile, Player shooter, Player target) {
        this.projectile = projectile;
        this.shooter = shooter;
        this.target = target;
    }
    
    public Projectile getProjectile() {
        return projectile;
    }
    
    public Player getShooter() {
        return shooter;
    }
    
    public Player getTarget() {
        return target;
    }
    
    public void setTarget(Player target) {
        this.target = target;
    }
}
