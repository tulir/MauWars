package net.maunium.bukkit.MauWars.Util;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import net.maunium.bukkit.MauBukLib.Area;
import net.maunium.bukkit.MauWars.MauWars;

public class Backuper {
	private MauWars plugin;
	private int taskId;
	private boolean addArena;
	
	public Backuper(MauWars plugin, MauArena ma, Player p, boolean addArena) {
		this.plugin = plugin;
		this.addArena = addArena;
		p.sendMessage(plugin.stag + plugin.format("creating.backuping", ma.getName()));
		taskId = plugin.getServer().getScheduler().runTaskTimer(plugin, new BackupTask(ma, p), 1, 3).getTaskId();
	}
	
	private class BackupTask implements Runnable {
		private MauArena ma;
		private Player p;
		private int y;
		private World backup = plugin.getBackupWorld();
		private World game = plugin.getGameWorld();
		
		public BackupTask(MauArena ma, Player p) {
			this.ma = ma;
			ma.disable();
			plugin.getPhysicsListener().disablePhysics(ma.getMap(), backup);
			this.p = p;
			this.y = ma.getMap().getMaxY();
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public void run() {
			Area a = ma.getMap();
			for (int x = a.getMinX(); x <= a.getMaxX(); x++) {
				for (int z = a.getMinZ(); z <= a.getMaxZ(); z++) {
					Block g = game.getBlockAt(x, y, z);
					Block b = backup.getBlockAt(x, y, z);
					b.setType(g.getType());
					b.setData(g.getData());
				}
			}
			y--;
			if (y < a.getMinY()) {
				plugin.getServer().getScheduler().cancelTask(taskId);
				if (addArena) {
					plugin.addArena(ma);
					plugin.getLogger().info("Added arena " + ma.getName());
					if (p != null) p.sendMessage(plugin.stag + plugin.format("creating.ed", ma.getName()));
				} else {
					plugin.getLogger().info("Backed up arena " + ma.getName());
					if (p != null) p.sendMessage(plugin.stag + plugin.format("creating.backuped", ma.getName()));
				}
				plugin.getPhysicsListener().enablePhysics(ma.getMap());
				ma.enable();
			}
		}
	}
}
