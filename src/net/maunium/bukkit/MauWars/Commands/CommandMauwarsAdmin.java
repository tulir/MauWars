package net.maunium.bukkit.MauWars.Commands;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import net.maunium.bukkit.MauBukLib.Area;
import net.maunium.bukkit.MauBukLib.IngameCommandExecutor;
import net.maunium.bukkit.MauBukLib.MauUtils;
import net.maunium.bukkit.MauWars.MauWars;
import net.maunium.bukkit.MauWars.Util.MauArena;

public class CommandMauwarsAdmin extends IngameCommandExecutor {
	private MauWars plugin;
	
	public CommandMauwarsAdmin(MauWars plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(Player p, Command cmd, String label, String[] args) {
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("pos1")) {
				p.setMetadata(plugin.pos1_meta, new FixedMetadataValue(plugin, p.getLocation().getBlock().getLocation()));
				p.sendMessage(plugin.stag + plugin.format("creating.pos", 1));
				return true;
			} else if (args[0].equalsIgnoreCase("pos2")) {
				p.setMetadata(plugin.pos2_meta, new FixedMetadataValue(plugin, p.getLocation().getBlock().getLocation()));
				p.sendMessage(plugin.stag + plugin.format("creating.pos", 2));
				return true;
			} else if (args[0].equalsIgnoreCase("tpworld")) {
				if (p.getLocation().getWorld().equals(plugin.getGameWorld())) p.teleport(plugin.getSpawn());
				else p.teleport(new Location(plugin.getGameWorld(), 0, 200, 0));
				return true;
			} else if (args.length > 1) {
				if (args[0].equalsIgnoreCase("create")) {
					MetadataValue c1 = MauUtils.getMetadata(p, plugin.pos1_meta, plugin);
					MetadataValue c2 = MauUtils.getMetadata(p, plugin.pos2_meta, plugin);
					if (c1 == null || c2 == null || c1.value() == null || c2.value() == null || !(c1.value() instanceof Location) || !(c2.value() instanceof Location)) {
						p.sendMessage(plugin.errtag + plugin.format("creating.selection", label));
						return true;
					}
					Area a = new Area((Location) c1.value(), (Location) c2.value());
					MauArena ma = new MauArena(args[1], a, new ArrayList<Location>());
					plugin.addArena(ma, p);
					return true;
				} else if (args[0].equalsIgnoreCase("addspawn")) {
					MauArena ma = plugin.getArena(args[1]);
					if (ma != null) {
						ma.addSpawn(p.getLocation());
						p.sendMessage(plugin.stag + plugin.format("spawn.added", ma.getName()));
					} else p.sendMessage(plugin.errtag + plugin.format("arena.notfound", args[1]));
					return true;
				} else if (args[0].equalsIgnoreCase("remove")) {
					if(plugin.removeArena(args[1])) p.sendMessage(plugin.stag + plugin.format("arena.removed", args[1]));
					else p.sendMessage(plugin.errtag + plugin.format("arena.notfound", args[1]));
					return true;
				} else if (args[0].equalsIgnoreCase("disable")) {
					MauArena ma = plugin.getArena(args[1]);
					if (ma != null) {
						if (ma.disable()) p.sendMessage(plugin.stag + plugin.format("disable.success"));
						else p.sendMessage(plugin.stag + plugin.format("disable.fail"));
					}
				} else if (args[0].equalsIgnoreCase("enable")) {
					MauArena ma = plugin.getArena(args[1]);
					if (ma != null) {
						if (ma.disable()) p.sendMessage(plugin.stag + plugin.format("disable.success"));
						else p.sendMessage(plugin.stag + plugin.format("disable.fail"));
					} else p.sendMessage(plugin.errtag + plugin.format("arena.notfound"));
				} else if (args[0].equalsIgnoreCase("backup")) {
					MauArena ma = plugin.getArena(args[1]);
					if (ma != null) plugin.backupArena(ma, p);
					else p.sendMessage(plugin.errtag + plugin.format("arena.notfound"));
					return true;
				} else if (args[0].equalsIgnoreCase("reset")) {
					MauArena ma = plugin.getArena(args[1]);
					if (ma != null) ma.reset(p);
					else p.sendMessage(plugin.errtag + plugin.format("arena.notfound"));
					return true;
				} else if (args[0].equalsIgnoreCase("addcc")) {
					double d;
					try {
						d = Double.parseDouble(args[1]);
					} catch (NumberFormatException e) {
						p.sendMessage(plugin.errtag + plugin.format("cc.nfe"));
						return true;
					};
					p.sendMessage(plugin.stag + plugin.format("cc.added", plugin.getChestContentHandler().addEntry(p.getItemInHand(), d)));
					p.getInventory().setItemInHand(new ItemStack(Material.AIR));
					return true;
				} else if (args[0].equalsIgnoreCase("removecc")){
					int i = Integer.parseInt(args[1]);
					plugin.getChestContentHandler().removeEntry(i);
					return true;
				}
			}
		}
		return false;
	}
}
