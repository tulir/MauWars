package net.maunium.bukkit.MauWars.Listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.MetadataValue;

import net.maunium.bukkit.MauWars.MauWars;
import net.maunium.bukkit.MauWars.Util.MauArena;
import net.maunium.bukkit.Maussentials.Utils.MetadataUtils;

public class BlockListener implements Listener {
	private MauWars plugin;
	
	public BlockListener(MauWars plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockPlace(BlockPlaceEvent evt) {
		int canBuild = canBuild(evt.getBlock().getLocation(), evt.getPlayer());
		evt.setCancelled(canBuild == 0 ? true : canBuild == 1 ? false : evt.isCancelled());
		evt.setBuild(canBuild == 0 ? false : canBuild == 1 ? true : evt.canBuild());
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent evt) {
		int canBuild = canBuild(evt.getBlock().getLocation(), evt.getPlayer());
		evt.setCancelled(canBuild == 0 ? true : canBuild == 1 ? false : evt.isCancelled());
	}
	
	public int canBuild(Location l, Player p) {
		if (l.getWorld().equals(plugin.getBackupWorld())) return 0;
		else if (l.getWorld().equals(plugin.getGameWorld())) {
			if (p.hasMetadata(plugin.arenaMeta)) {
				MetadataValue mv = MetadataUtils.getMetadata(p, plugin.arenaMeta, plugin);
				if (mv == null || mv.value() == null || !(mv.value() instanceof String)) {
					if (!p.hasPermission("mauwars.admin")) return 0;
					else return 1;
				}
				
				MauArena ma = plugin.getArena((String) mv.value());
				if (ma != null) {
					if (!ma.getState().equals(MauArena.State.PLAYING) || !ma.getMap().isInArea(l)) return 0;
					else return 1;
				} else if (!p.hasPermission("mauwars.admin")) return 0;
				else return 1;
			} else if (!p.hasPermission("mauwars.admin")) return 0;
			else return 1;
		} else return -1;
	}
}
