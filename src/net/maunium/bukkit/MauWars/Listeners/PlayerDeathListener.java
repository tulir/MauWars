package net.maunium.bukkit.MauWars.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.metadata.MetadataValue;

import net.maunium.bukkit.MauBukLib.MauUtils;
import net.maunium.bukkit.MauWars.MauWars;
import net.maunium.bukkit.MauWars.Util.MauArena;

public class PlayerDeathListener implements Listener {
	private MauWars plugin;
	
	public PlayerDeathListener(MauWars plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeath(PlayerDeathEvent evt) {
		if(evt.getEntity().hasMetadata(plugin.arenaMeta)){
			MetadataValue mv = MauUtils.getMetadata(evt.getEntity(), plugin.arenaMeta, plugin);
			if(mv == null || mv.value() == null || !(mv.value() instanceof String)) return;
			MauArena ma = plugin.getArena((String) mv.value());
			if(ma != null) ma.dead(evt.getEntity(), evt.getDeathMessage());
			evt.setDeathMessage(null);
		}
	}
}
