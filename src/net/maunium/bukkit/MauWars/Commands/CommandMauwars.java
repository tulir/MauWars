package net.maunium.bukkit.MauWars.Commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import net.maunium.bukkit.MauWars.MauWars;
import net.maunium.bukkit.MauWars.Util.MauArena;
import net.maunium.bukkit.Maussentials.Utils.IngameCommandExecutor;
import net.maunium.bukkit.Maussentials.Utils.MetadataUtils;

public class CommandMauwars implements IngameCommandExecutor {
	private MauWars plugin;
	
	public CommandMauwars(MauWars plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(Player p, Command cmd, String label, String[] args) {
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("join")) {
				if (args.length > 1) {
					MauArena ma = plugin.getArena(args[1]);
					if (ma != null) {
						int i = ma.join(p);
						if (i == -1) p.sendMessage(plugin.errtag + plugin.translate("join.disabled", ma.getName()));
						else if (i == -2) p.sendMessage(plugin.errtag + plugin.translate("join.started", ma.getName()));
						else if (i == -3) p.sendMessage(plugin.errtag + plugin.translate("join.nospace", ma.getName()));
					} else p.sendMessage(plugin.errtag + plugin.translate("arena.notfound", args[1]));
				} else {
					MauArena ma = plugin.getFullest();
					if (ma != null) ma.join(p);
					else p.sendMessage(plugin.errtag + plugin.translate("join.noarenas"));
				}
				return true;
			} else if (args[0].equalsIgnoreCase("leave")) {
				if (p.hasMetadata(plugin.arenaMeta)) {
					MetadataValue mv = MetadataUtils.getMetadata(p, plugin.arenaMeta, plugin);
					if (mv == null || mv.value() == null || !(mv.value() instanceof String)) {
						p.sendMessage(plugin.errtag + plugin.translate("leave.notinarena"));
						return true;
					}
					MauArena ma = plugin.getArena((String) mv.value());
					if (ma != null) ma.leave(p);
					else p.sendMessage(plugin.errtag + plugin.translate("leave.notinarena"));
				}
				return true;
			}
		}
		return false;
	}
}
