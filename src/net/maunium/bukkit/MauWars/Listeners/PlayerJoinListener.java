package net.maunium.bukkit.MauWars.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import net.maunium.bukkit.MauWars.MauWars;

public class PlayerJoinListener implements Listener {
	private MauWars plugin;
	
	public PlayerJoinListener(MauWars plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent evt) {
		evt.getPlayer().getInventory().setContents(new ItemStack[evt.getPlayer().getInventory().getContents().length]);
		evt.getPlayer().getInventory().setArmorContents(new ItemStack[evt.getPlayer().getInventory().getArmorContents().length]);
		if (plugin.getSpawn() != null) evt.getPlayer().teleport(plugin.getSpawn());
	}
}
