package net.maunium.bukkit.MauWars.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;

import net.maunium.bukkit.MauBukLib.MauUtils;
import net.maunium.bukkit.MauWars.MauWars;
import net.maunium.bukkit.MauWars.Util.MauArena;

public class PlayerQuitListener implements Listener {
	private MauWars plugin;
	
	public PlayerQuitListener(MauWars plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent evt) {
		if (evt.getPlayer().hasMetadata(plugin.arenaMeta)) {
			MetadataValue mv = MauUtils.getMetadata(evt.getPlayer(), plugin.arenaMeta, plugin);
			if (mv == null || mv.value() == null || !(mv.value() instanceof String)) return;
			evt.getPlayer().getInventory().setContents(new ItemStack[evt.getPlayer().getInventory().getContents().length]);
			evt.getPlayer().getInventory().setArmorContents(new ItemStack[evt.getPlayer().getInventory().getArmorContents().length]);
			MauArena ma = plugin.getArena((String) mv.value());
			if(ma != null) ma.leave(evt.getPlayer());
		}
	}
}
