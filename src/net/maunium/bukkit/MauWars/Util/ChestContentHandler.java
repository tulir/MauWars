package net.maunium.bukkit.MauWars.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import net.maunium.bukkit.MauWars.MauWars;

public class ChestContentHandler {
	private MauWars plugin;
	private Map<Integer, ChestContentEntry> contents = new HashMap<Integer, ChestContentEntry>();
	private File f;
	
	public ChestContentHandler(MauWars plugin) {
		this.plugin = plugin;
		this.f = new File(plugin.getDataFolder(), "chest-contents.yml");
		if (!f.exists()) try {
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private List<ChestContentEntry> shuffled() {
		List<ChestContentEntry> e = new ArrayList<ChestContentEntry>(contents.values());
		Collections.shuffle(e);
		return e;
	}
	
	public ItemStack[] getRandomContents() {
		ItemStack[] cs = new ItemStack[27];
		int slot = addition() - 1;
		
		for (ChestContentEntry cce : shuffled()) {
			if (slot >= cs.length) break;
			if (cce.addItem()) cs[slot] = cce.getItem();
			slot += addition();
		}
		return cs;
	}
	
	private int addition() {
		int i = plugin.getRandom().nextInt(4);
		switch (i) {
			case 1:
				return plugin.getRandom().nextInt(2) + 1;
			case 2:
				return plugin.getRandom().nextInt(3) + 1;
			default:
				return 1;
		}
	}
	
	public int addEntry(ItemStack is, double chance) {
		int i = contents.size();
		contents.put(i, new ChestContentEntry(is, chance));
		return i;
	}
	
	public void removeEntry(int id) {
		contents.remove(id);
	}
	
	public void save() {
		YamlConfiguration conf = new YamlConfiguration();
		conf.set("size", contents.size());
		for (Entry<Integer, ChestContentEntry> e : contents.entrySet())
			conf.set("i" + e.getKey().intValue(), e.getValue());
		
		try {
			conf.save(f);
		} catch (IOException e) {
			plugin.getLogger().severe("Failed to save chest contents.");
			e.printStackTrace();
		}
	}
	
	public void load() {
		YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
		int size = conf.getInt("size");
		for (int i = 0; i < size; i++) {
			Object o = conf.get("i" + i);
			if (o instanceof ChestContentEntry) contents.put(i, (ChestContentEntry) o);
		}
	}
}
