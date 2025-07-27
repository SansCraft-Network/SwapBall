package top.sanscraft.swapball.utils;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public class ItemBuilder {
    
    public static final NamespacedKey SWAP_BALL_KEY = new NamespacedKey(
        top.sanscraft.swapball.SwapBallPlugin.getInstance(), "swap_ball"
    );
    
    public static ItemStack createSwapBall() {
        ItemStack item = new ItemStack(Material.SNOWBALL, 1);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Set display name
            meta.setDisplayName("§f§lSwap Ball §42.0");
            
            // Set lore
            meta.setLore(Arrays.asList(
                "§fJust your average",
                "§feveryday swap ball",
                "§bEnhanced by the power of",
                "§4§lDetermination"
            ));
            
            // Add persistent data to identify this as a swap ball
            meta.getPersistentDataContainer().set(
                SWAP_BALL_KEY, 
                PersistentDataType.BOOLEAN, 
                true
            );
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    public static boolean isSwapBall(ItemStack item) {
        if (item == null || item.getType() != Material.SNOWBALL) {
            return false;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        
        return meta.getPersistentDataContainer().has(SWAP_BALL_KEY, PersistentDataType.BOOLEAN);
    }
}
