package net.maunium.bukkit.MauWars.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitScheduler;

import net.maunium.bukkit.MauWars.MauWars;
import net.maunium.bukkit.Maussentials.Utils.Area;
import net.maunium.bukkit.Maussentials.Utils.SerializableLocation;

public class MauArena {
	public static MauWars plugin;
	private int copyTaskId = -1, startId;
	private int s10Id = -1, s5Id = -1, s4Id = -1, s3Id = -1, s2Id = -1, s1Id = -1;
	private final String name;
	private boolean disabled = false;
	private List<Location> spawnpoints;
	private State state;
	private UUID[] players;
	private Area map;
	
	public MauArena(String name, Area map, List<Location> spawnpoints) {
		this.name = name;
		this.map = map;
		this.spawnpoints = spawnpoints;
		players = new UUID[spawnpoints.size()];
		Arrays.fill(players, null);
		state = State.WAITING;
	}
	
	public void broadcast(String s) {
		for (int i = 0; i < players.length; i++)
			if (players[i] != null) plugin.getServer().getPlayer(players[i]).sendMessage(s);
	}
	
	/*
	 * Start joining/leaving
	 */
	
	/**
	 * Called when a player joins the MauArena
	 * 
	 * @param p The player who is joining.
	 * @return If there is space, the slot of the player. If not, -1
	 */
	public int join(Player p) {
		if (disabled) return -1;
		// Check that player is not null
		if (p == null) throw new NullPointerException("The player may not be null!");
		// Make sure that the game is in the lobby
		if (!getState().equals(State.WAITING)) return -2;
		// Loop through the player array to find a slot for the player.
		for (int i = 0; i < players.length; i++) {
			// If there's no one in the slot.
			if (players[i] == null) {
				// Place the player there.
				players[i] = p.getUniqueId();
				// Set the arena metadata.
				p.setMetadata(plugin.arenaMeta, new FixedMetadataValue(plugin, name));
				// Teleport the player to the correct spawn point.
				p.teleport(spawnpoints.get(i));
				// Broadcast player joining
				broadcast(plugin.stag + plugin.translate("match.join", p.getName(), getPlayerCount(), spawnpoints.size()));
				// If there is more than 1 player, reset and/or activate the startup countdown.
				if (getPlayerCount() > 1) activateCountdown();
				// Finally return the player slot ID.
				return i + 1;
			}
		}
		// No slots found ;(
		return -3;
	}
	
	/**
	 * Called when a player leaves the arena
	 * 
	 * @param p The player who left
	 * @return True if the player was in the game, false otherwise.
	 */
	public boolean leave(Player p) {
		if (disabled) return false;
		// Check that player is not null
		if (p == null) throw new NullPointerException("The player may not be null!");
		// Loop through the player array to find the players slot.
		for (int i = 0; i < players.length; i++)
			// If the slot is found.
			if (p.getUniqueId().equals(players[i])) {
				// Clear the slot.
				players[i] = null;
				// Remove arena metadata.
				p.removeMetadata(plugin.arenaMeta, plugin);
				// Teleport player to spawn.
				p.teleport(plugin.getSpawn());
				// Clear inventory
				p.getInventory().setContents(new ItemStack[p.getInventory().getContents().length]);
				p.getInventory().setArmorContents(new ItemStack[p.getInventory().getArmorContents().length]);
				// Broadcast player leaving.
				broadcast(plugin.stag
						+ plugin.translate("match.leave." + getState().toString().toLowerCase(Locale.ENGLISH), p.getName(), getPlayerCount(), spawnpoints.size()));
				// If the match is in PLAYING state and there is only 1 player left, end the match.
				if (getState().equals(State.PLAYING) && getPlayerCount() < 2) end();
				// If the match is in WAITING state and there is only 1 player left, deactivate the countdown.
				else if (getState().equals(State.WAITING) && getPlayerCount() < 2) deactivateCountdown();
				// Return true, because the player was successfully removed.
				return true;
			}
		
		// Another check for ending
		if (getState().equals(State.PLAYING) && getPlayerCount() < 2) end();
		// Return false because the player was not found.
		return false;
	}
	
	/**
	 * Called when a player dies without a reason.
	 * 
	 * @param p The player who died.
	 */
	public void dead(Player p) {
		if (disabled) return;
		// Make sure that the player is not null.
		if (p == null) throw new NullPointerException("The player may not be null!");
		// Call the real dead method with reason "<Player> died!"
		dead(p, p.getName() + " died!");
	}
	
	/**
	 * Called when a player dies with a reason.
	 * 
	 * @param p The player who died.
	 * @param reason The death message
	 */
	public void dead(Player p, String reason) {
		if (disabled) return;
		// Make sure that the player is not null.
		if (p == null) throw new NullPointerException("The player may not be null!");
		// Check if the state is not PLAYING
		if (!getState().equals(State.PLAYING)) {
			// If it's not, count it as a leave, not a death.
			leave(p);
			// Return. Nothing more to do in this method.
			return;
		}
		
		for (int i = 0; i < players.length; i++)
			if (p.getUniqueId().equals(players[i])) {
				players[i] = null;
				p.removeMetadata(plugin.arenaMeta, plugin);
				p.teleport(plugin.getSpawn());
				broadcast(plugin.stag + ChatColor.DARK_AQUA + reason);
			}
		
		if (getPlayerCount() < 2) end();
	}
	
	/*
	 * End joining/leaving - Start starting/ending
	 */
	
	/**
	 * Activates the startup countdown.
	 */
	private void activateCountdown() {
		if (disabled) return;
		BukkitScheduler bc = plugin.getServer().getScheduler();
		if (s10Id != -1) bc.cancelTask(s10Id);
		if (s5Id != -1) bc.cancelTask(s5Id);
		if (s4Id != -1) bc.cancelTask(s4Id);
		if (s3Id != -1) bc.cancelTask(s3Id);
		if (s2Id != -1) bc.cancelTask(s2Id);
		if (s1Id != -1) bc.cancelTask(s1Id);
		if (startId != -1) bc.cancelTask(startId);
		
		s10Id = bc.scheduleSyncDelayedTask(plugin, new Broadcaster(10), 100);
		s5Id = bc.scheduleSyncDelayedTask(plugin, new Broadcaster(5), 200);
		s4Id = bc.scheduleSyncDelayedTask(plugin, new Broadcaster(4), 220);
		s3Id = bc.scheduleSyncDelayedTask(plugin, new Broadcaster(3), 240);
		s2Id = bc.scheduleSyncDelayedTask(plugin, new Broadcaster(2), 260);
		s1Id = bc.scheduleSyncDelayedTask(plugin, new Broadcaster(1), 280);
		startId = bc.scheduleSyncDelayedTask(plugin, new Broadcaster(0), 300);
	}
	
	private void deactivateCountdown() {
		if (disabled) return;
		BukkitScheduler bc = plugin.getServer().getScheduler();
		if (s10Id != -1) bc.cancelTask(s10Id);
		if (s5Id != -1) bc.cancelTask(s5Id);
		if (s4Id != -1) bc.cancelTask(s4Id);
		if (s3Id != -1) bc.cancelTask(s3Id);
		if (s2Id != -1) bc.cancelTask(s2Id);
		if (s1Id != -1) bc.cancelTask(s1Id);
		if (startId != -1) bc.cancelTask(startId);
		
		s10Id = -1;
		s5Id = -1;
		s4Id = -1;
		s3Id = -1;
		s2Id = -1;
		s1Id = -1;
		startId = -1;
	}
	
	private class Broadcaster implements Runnable {
		private int seconds;
		
		public Broadcaster(int seconds) {
			this.seconds = seconds;
		}
		
		@Override
		public void run() {
			if (seconds > 0) broadcast(plugin.stag + plugin.translate("start.seconds", seconds));
			else {
				broadcast(plugin.stag + plugin.translate("start.now"));
				state = State.PLAYING;
				for (Location l : spawnpoints)
					l.getBlock().getRelative(BlockFace.DOWN).setType(Material.AIR);
				deactivateCountdown();
			}
		}
	};
	
	private void end() {
		UUID pp = null;
		for (int i = 0; i < players.length; i++)
			if (players[i] != null) {
				pp = players[i];
				players[i] = null;
				break;
			}
		
		if (pp != null) {
			Player ppp = plugin.getServer().getPlayer(pp);
			plugin.getServer().broadcastMessage(plugin.stag + plugin.translate("match.end", ppp.getName(), name));
			ppp.removeMetadata(plugin.arenaMeta, plugin);
			// Clear inventory
			ppp.getInventory().setContents(new ItemStack[ppp.getInventory().getContents().length]);
			ppp.getInventory().setArmorContents(new ItemStack[ppp.getInventory().getArmorContents().length]);
			ppp.teleport(plugin.getSpawn());
		}
		reset(null);
	}
	
	public void start() {
		if (disabled) return;
		state = State.PLAYING;
		for (Location l : spawnpoints)
			l.getBlock().getRelative(BlockFace.DOWN).setType(Material.AIR);
		broadcast(plugin.stag + "The game has started!");
	}
	
	/*
	 * Start Getters - End starting/ending
	 */
	
	public int getPlayerCount() {
		int left = 0;
		for (int i = 0; i < players.length; i++)
			if (players[i] != null) left++;
		return left;
	}
	
	public State getState() {
		if (state.equals(State.RESETTING)) {
			if (copyTaskId != -1 && !plugin.getServer().getScheduler().isQueued(copyTaskId)
					&& !plugin.getServer().getScheduler().isCurrentlyRunning(copyTaskId)) {
				state = State.WAITING;
				copyTaskId = -1;
			}
		}
		return state;
	}
	
	public String getName() {
		return name;
	}
	
	public Area getMap() {
		return map;
	}
	
	public void addSpawn(Location l) {
		spawnpoints.add(l);
		if (state.equals(State.WAITING) && getPlayerCount() == 0) {
			players = new UUID[spawnpoints.size()];
			Arrays.fill(players, null);
			Collections.shuffle(spawnpoints, plugin.getRandom());
		}
	}
	
	public boolean disable() {
		if (state == State.WAITING) {
			for (int i = 0; i < players.length; i++) {
				if (players[i] != null) {
					Player p = plugin.getServer().getPlayer(players[i]);
					leave(p);
					players[i] = null;
				}
			}
		} else return false;
		disabled = true;
		return true;
	}
	
	public void enable() {
		disabled = false;
	}
	
	public boolean isDisabled() {
		return disabled;
	}
	
	/*
	 * End Getters - Start IO
	 */
	
	public void save(File f) throws IOException {
		YamlConfiguration conf = new YamlConfiguration();
		conf.set("name", name);
		List<String> list = new ArrayList<String>(spawnpoints.size());
		for (Location l : spawnpoints)
			list.add(new SerializableLocation(l).toString());
		conf.set("spawns", list);
		conf.set("area", map);
		conf.set("disabled", disabled);
		try {
			conf.save(f);
		} catch (IOException e) {
			throw new IOException("Failed to save arena.");
		}
	}
	
	public static MauArena load(File f) throws ArenaFormatException {
		YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
		
		Object n = conf.get("name");
		String name;
		if (n instanceof String) name = (String) n;
		else throw new ArenaFormatException("Could not find a valid name object in the configuration.");
		
		Object a = conf.get("area");
		Area area;
		if (a instanceof Area) area = (Area) a;
		else throw new ArenaFormatException("Could not find a valid area object in the configuration.");
		
		List<String> spawns = conf.getStringList("spawns");
		if (spawns == null || spawns.isEmpty()) throw new ArenaFormatException("Could not find a valid spawns object in the configuration.");
		List<Location> spawnpoints = new ArrayList<Location>();
		for (String s : spawns)
			spawnpoints.add(SerializableLocation.fromString(s).toLocation());
		
		MauArena ma = new MauArena(name, area, spawnpoints);
		
		if (conf.getBoolean("disabled")) ma.disable();
		
		return ma;
	}
	
	/*
	 * End IO - Start Resetting
	 */
	
	public void reset(Player p) {
		state = State.RESETTING;
		copyTaskId = plugin.getServer().getScheduler().runTaskTimer(plugin, new Reset(p), 1, 3).getTaskId();
		
		players = new UUID[spawnpoints.size()];
		Arrays.fill(players, null);
		Collections.shuffle(spawnpoints, plugin.getRandom());
		
		for (Entity e : plugin.getGameWorld().getEntities())
			if (map.isInArea(e.getLocation())) e.remove();
	}
	
	private class Reset implements Runnable {
		private int y = map.getMaxY();
		private World backup = plugin.getBackupWorld();
		private World game = plugin.getGameWorld();
		private Player p;
		
		public Reset(Player p) {
			this.p = p;
			if (p != null) p.sendMessage(plugin.stag + plugin.translate("creating.resetting", getName()));
			plugin.getPhysicsListener().disablePhysics(getMap(), game);
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public void run() {
			for (int x = map.getMinX(); x <= map.getMaxX(); x++) {
				for (int z = map.getMinZ(); z <= map.getMaxZ(); z++) {
					Block g = game.getBlockAt(x, y, z);
					Block b = backup.getBlockAt(x, y, z);
					g.setType(b.getType());
					g.setData(b.getData());
					if (g.getType().equals(Material.CHEST) || g.getType().equals(Material.TRAPPED_CHEST)) {
						Chest c = (Chest) g.getState();
						c.getInventory().setContents(plugin.getChestContentHandler().getRandomContents());
					}
				}
			}
			y--;
			if (y < map.getMinY()) {
				plugin.getServer().getScheduler().cancelTask(copyTaskId);
				if (p != null) p.sendMessage(plugin.stag + plugin.translate("creating.resetted", getName()));
				plugin.getPhysicsListener().enablePhysics(getMap());
			}
		}
	}
	
	/*
	 * End Resetting
	 */
	
	public static class ArenaFormatException extends Exception {
		private static final long serialVersionUID = 3654654745318526597L;
		
		public ArenaFormatException() {}
		
		public ArenaFormatException(String msg) {
			super(msg);
		}
		
		public ArenaFormatException(Throwable cause) {
			super(cause);
		}
		
		public ArenaFormatException(String msg, Throwable cause) {
			super(msg, cause);
		}
	}
	
	public static enum State {
		WAITING, PLAYING, RESETTING;
	}
}
