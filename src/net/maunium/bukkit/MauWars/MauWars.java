package net.maunium.bukkit.MauWars;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.maunium.bukkit.MauBukLib.I18n;
import net.maunium.bukkit.MauBukLib.MauUtils;
import net.maunium.bukkit.MauWars.Commands.CommandMauwars;
import net.maunium.bukkit.MauWars.Commands.CommandMauwarsAdmin;
import net.maunium.bukkit.MauWars.Listeners.BlockListener;
import net.maunium.bukkit.MauWars.Listeners.PhysicsListener;
import net.maunium.bukkit.MauWars.Listeners.PlayerDeathListener;
import net.maunium.bukkit.MauWars.Listeners.PlayerJoinListener;
import net.maunium.bukkit.MauWars.Listeners.PlayerQuitListener;
import net.maunium.bukkit.MauWars.Util.Backuper;
import net.maunium.bukkit.MauWars.Util.ChestContentEntry;
import net.maunium.bukkit.MauWars.Util.ChestContentHandler;
import net.maunium.bukkit.MauWars.Util.MauArena;
import net.maunium.bukkit.MauWars.Util.MauArena.ArenaFormatException;
import net.maunium.bukkit.MauWars.World.WorldManager;

public class MauWars extends JavaPlugin {
	
	public String version;
	public final String name = "MauWars", author = "Tulir293", stag = ChatColor.DARK_GREEN + "[" + ChatColor.GREEN + name + ChatColor.DARK_GREEN + "] " + ChatColor.GRAY, errtag = ChatColor.DARK_RED
			+ "[" + ChatColor.RED + name + ChatColor.DARK_RED + "] " + ChatColor.RED, arenaMeta = "MauWars_CurrentArena", pos1_meta = "MauWars_Selection_Pos1", pos2_meta = "MauWars_Selection_Pos2";
	private I18n i18n;
	private WorldManager wm;
	private ChestContentHandler cch;
	private PhysicsListener phl;
	private Map<String, MauArena> arenas;
	private File arenaDir = new File(this.getDataFolder(), "arenas");
	private Random r = new Random(System.nanoTime());
	private Location spawn;
	
	@Override
	public void onLoad() {
		try {
			Class.forName("net.maunium.bukkit.MauPortals.API.PortalHandlerRegistry");
		} catch (ClassNotFoundException e) {
			return;
		}
		net.maunium.bukkit.MauPortals.API.PortalHandlerRegistry.registerHandler(this, "randomarena", new net.maunium.bukkit.MauWars.Util.MWPortalHandler(this));
	}
	
	@Override
	public void onEnable() {
		// Start enable
		long st = System.currentTimeMillis();
		version = this.getDescription().getVersion();
		this.saveDefaultConfig();
		
		try {
			Class.forName("net.maunium.bukkit.MauBukLib.Area");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		// Set the plugin variable of all MauArenas
		MauArena.plugin = this;
		ChestContentEntry.plugin = this;
		ConfigurationSerialization.registerClass(ChestContentEntry.class);
		
		// Create the arena list and arena manager
		arenas = new HashMap<String, MauArena>();
		wm = new WorldManager(this);
		cch = new ChestContentHandler(this);
		
		if (!arenaDir.exists()) arenaDir.mkdirs();
		
		// Save default (non-overridable) language files
		this.saveResource("en_US.lang", true);
		this.saveResource("fi_FI.lang", true);
		
		// Load selected language.
		try {
			i18n = I18n.createInstance(getDataFolder(), getConfig().getString("language"));
		} catch (IOException e) {
			getLogger().severe("Failed to load language");
			e.printStackTrace();
		}
		
		// Register events
		this.getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
		this.getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
		this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
		this.getServer().getPluginManager().registerEvents(new BlockListener(this), this);
		this.getServer().getPluginManager().registerEvents(this.phl = new PhysicsListener(), this);
		
		// Register commands
		this.getCommand("mauwars").setExecutor(new CommandMauwars(this));
		this.getCommand("mauwarsadmin").setExecutor(new CommandMauwarsAdmin(this));
		
		// Load game and backup worlds.
		wm.loadWorlds();
		// Load chest contents
		cch.load();
		
		// Load maps
		for (File f : arenaDir.listFiles()) {
			if (f.isFile() && f.getName().endsWith(".yml")) {
				try {
					MauArena a = MauArena.load(f);
					if (getConfig().getBoolean("reset-maps-on-enable")) a.reset(null);
					arenas.put(a.getName().toLowerCase(Locale.ENGLISH), a);
				} catch (ArenaFormatException e) {
					getLogger().severe("Failed to load arena from file " + f.getName());
					e.printStackTrace();
				}
			}
		}
		
		spawn = MauUtils.parseLocation(getConfig().getString("server-spawn"));
		
		// End enable
		int et = (int) (System.currentTimeMillis() - st);
		getLogger().info(name + " v" + version + " by " + author + " enabled in " + et + "ms.");
	}
	
	@Override
	public void onDisable() {
		long st = System.currentTimeMillis();
		
		// Save chest contents
		cch.save();
		
		for (File f : arenaDir.listFiles())
			if (f.isFile() && f.getName().endsWith(".yml")) f.delete();
		
		for (Entry<String, MauArena> a : arenas.entrySet()) {
			File f = new File(arenaDir, a.getKey().toLowerCase(Locale.ENGLISH) + ".yml");
			try {
				a.getValue().save(f);
			} catch (IOException e) {
				getLogger().severe("Failed to save arena " + a.getKey());
				e.printStackTrace();
			}
		}
		
		int et = (int) (System.currentTimeMillis() - st);
		getLogger().info(name + " v" + version + " by " + author + " disabled in " + et + "ms.");
	}
	
	public World getGameWorld() {
		return wm.getGameWorld();
	}
	
	public World getBackupWorld() {
		return wm.getBackupWorld();
	}
	
	public ChestContentHandler getChestContentHandler(){
		return cch;
	}
	
	public MauArena getArena(String name) {
		return arenas.get(name.toLowerCase(Locale.ENGLISH));
	}
	
	public PhysicsListener getPhysicsListener(){
		return phl;
	}
	
	public boolean removeArena(String name) {
		if (arenas.containsKey(name.toLowerCase(Locale.ENGLISH))) {
			arenas.remove(name.toLowerCase(Locale.ENGLISH));
			return true;
		} else return false;
	}
	
	public void addArena(final MauArena ma, final Player p) {
		new Backuper(this, ma, p, true);
	}
	
	public void backupArena(final MauArena ma, final Player p) {
		new Backuper(this, ma, p, false);
	}
	
	public void addArena(MauArena ma) {
		arenas.put(ma.getName().toLowerCase(Locale.ENGLISH), ma);
	}
	
	public Random getRandom() {
		return r;
	}
	
	public MauArena getFullest() {
		MauArena selected = null;
		for (MauArena a : arenas.values()) {
			if (!a.getState().equals(MauArena.State.WAITING)) continue;
			if (a.getPlayerCount() > (selected == null ? 0 : selected.getPlayerCount())) selected = a;
		}
		if (selected == null) {
			List<MauArena> ma = new ArrayList<MauArena>(arenas.values());
			Collections.shuffle(ma, getRandom());
			for (MauArena a : ma)
				if (a.getState().equals(MauArena.State.WAITING) && !a.isDisabled()) selected = a;
		}
		return selected;
	}
	
	public Location getSpawn() {
		return spawn;
	}
	
	public String format(String s, Object... args) {
		return i18n.format(s, args);
	}
}