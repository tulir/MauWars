package net.maunium.bukkit.MauWars.Util;

import org.bukkit.entity.Player;

import net.maunium.bukkit.MauPortals.API.PortalHandler;
import net.maunium.bukkit.MauWars.MauWars;

public class MWPortalHandler implements PortalHandler {
	private MauWars plugin;
	
	public MWPortalHandler(MauWars plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void enterPortal(Player p, int id) {
		MauArena ma = plugin.getFullest();
		if(ma != null) ma.join(p);
		else {
			p.sendMessage(plugin.errtag + plugin.translate("join.noarenas"));
			if(plugin.getSpawn() != null) p.teleport(plugin.getSpawn());
		}
	}

	@Override
	public String getName() {
		return "MauWars:RandomArena";
	}
	
	@Override
	public String toString(){
		return "MauWars Portal Handler: Random Arena";
	}
}
