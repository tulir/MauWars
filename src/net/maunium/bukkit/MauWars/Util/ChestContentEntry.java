package net.maunium.bukkit.MauWars.Util;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;

import net.maunium.bukkit.MauWars.MauWars;

@SerializableAs(value = "MauChestContentEntry")
public class ChestContentEntry implements ConfigurationSerializable {
	public static MauWars plugin;
	private ItemStack item;
	private double percentChance;
	
	public ChestContentEntry(ItemStack item, double percentChance) {
		this.item = item;
		this.percentChance = percentChance;
	}
	
	public ChestContentEntry(Map<String, Object> serialized) {
		Object i = serialized.get("item");
		if (i instanceof ItemStack) this.item = (ItemStack) i;
		else throw new IllegalArgumentException("Serialized map doesn't contain the item.");
		
		Object p = serialized.get("chance");
		try {
			this.percentChance = Double.parseDouble(p.toString());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Serialized map doesn't contain the chance.");
		}
	}
	
	public boolean addItem() {
		return plugin.getRandom().nextGaussian() * 100 < percentChance;
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> serialized = new HashMap<String, Object>();
		
		serialized.put("item", item);
		serialized.put("chance", percentChance);
		
		return serialized;
	}
	
	public ChestContentEntry valueOf(Map<String, Object> serialized) {
		return new ChestContentEntry(serialized);
	}
	
	public ChestContentEntry deserialize(Map<String, Object> serialized) {
		return new ChestContentEntry(serialized);
	}
}