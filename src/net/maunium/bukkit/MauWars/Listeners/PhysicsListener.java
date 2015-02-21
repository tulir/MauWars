package net.maunium.bukkit.MauWars.Listeners;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

import net.maunium.bukkit.MauBukLib.Area;

public class PhysicsListener implements Listener {
	private HashMap<Area, World> disabledPhysics = new HashMap<Area, World>();
	
	public void disablePhysics(Area a, World w) {
		disabledPhysics.put(a, w);
	}
	
	public void enablePhysics(Area a) {
		disabledPhysics.remove(a);
	}
	
	@EventHandler
	public void onBlockPhysics(BlockPhysicsEvent evt) {
		for (Entry<Area, World> e : disabledPhysics.entrySet())
			if (e.getValue().equals(evt.getBlock().getWorld()) && e.getKey().isInArea(evt.getBlock().getLocation())) evt.setCancelled(true);
	}
}
