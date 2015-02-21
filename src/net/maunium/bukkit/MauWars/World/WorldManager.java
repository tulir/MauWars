package net.maunium.bukkit.MauWars.World;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;

import net.maunium.bukkit.MauWars.MauWars;

public class WorldManager {
	private MauWars plugin;
	private EmptyChunkGenerator ecg = new EmptyChunkGenerator();
	
	public WorldManager(MauWars plugin) {
		this.plugin = plugin;
	}
	
	public void loadWorlds() {
		loadGameWorld();
		loadBackupWorld();
	}
	
	public void loadGameWorld() {
		WorldCreator wc = new WorldCreator("MauWars");
		wc.environment(Environment.NORMAL);
		wc.generateStructures(false);
		wc.generator(ecg);
		
		World w = wc.createWorld();
		w.setSpawnLocation(0, 200, 0);
		w.setAutoSave(true);
		w.setGameRuleValue("doDaylightCycle", "false");
		w.setTime(6000);
		w.setWeatherDuration(Integer.MAX_VALUE);
		w.setThundering(false);
		w.setStorm(false);
	}
	
	public void loadBackupWorld() {
		WorldCreator wc = new WorldCreator("MauWarsBackup");
		wc.environment(Environment.NORMAL);
		wc.generateStructures(false);
		wc.generator(ecg);
		
		World w = wc.createWorld();
		w.setSpawnLocation(0, 200, 0);
		w.setAutoSave(true);
	}
	
	public World getBackupWorld() {
		return plugin.getServer().getWorld("MauWarsBackup");
	}
	
	public World getGameWorld() {
		return plugin.getServer().getWorld("MauWars");
	}
}
