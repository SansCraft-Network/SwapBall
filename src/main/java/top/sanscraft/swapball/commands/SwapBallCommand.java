package top.sanscraft.swapball.commands;

import top.sanscraft.swapball.SwapBallPlugin;
import top.sanscraft.swapball.utils.ItemBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SwapBallCommand implements CommandExecutor {
    
    private final SwapBallPlugin plugin;
    
    public SwapBallCommand(SwapBallPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("swapball.give")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }
        
        ItemStack swapBall = ItemBuilder.createSwapBall();
        player.getInventory().addItem(swapBall);
        player.sendMessage("§aYou have received a §f§lSwap Ball §42.0§a!");
        
        return true;
    }
}
