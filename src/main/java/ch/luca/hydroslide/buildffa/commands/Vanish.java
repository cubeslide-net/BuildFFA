package ch.luca.hydroslide.buildffa.commands;

import java.util.ArrayList;

import ch.luca.hydroslide.buildffa.BuildFFA;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

public class Vanish implements CommandExecutor, Listener {

	private BuildFFA buildFFA;

	public Vanish(BuildFFA buildFFA) {
		this.buildFFA = buildFFA;
	}

	public static ArrayList<Player> vanish = new ArrayList();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(buildFFA.getNoPlayer());
			return true;
		}
		Player p = (Player)sender;
		if(!p.hasPermission("buildffa.vanish")) {
			p.sendMessage(buildFFA.getNoPermission());
			return true;
		}
		if(args.length == 0) {
			if(!vanish.contains(p)) {
				vanish.add(p);
				p.setAllowFlight(true);
				p.sendMessage(buildFFA.getPrefix() + "§aYou are now invisible.");
				for(Player o : Bukkit.getOnlinePlayers()) {
					if(!o.hasPermission("buildffa.vanish")) {
						o.hidePlayer(p);
					}
				}
			} else {
				vanish.remove(p);
				if(!p.getGameMode().equals(GameMode.CREATIVE)) {
					p.setAllowFlight(false);
				}
				p.sendMessage(buildFFA.getPrefix()+ "§cYou are now visible again.");
				for(Player o : Bukkit.getOnlinePlayers()) {
					o.showPlayer(p);
				}
			}
		} else {
			Player t = Bukkit.getPlayer(args[0]);
			if(!p.hasPermission("buildffa.vanish.other")) {
				p.sendMessage(buildFFA.getNoPermission());
				return true;
			}
			if(!vanish.contains(t)) {
				vanish.add(t);
				t.setAllowFlight(true);
				t.sendMessage(buildFFA.getPrefix()+ "§aYou are now invisible.");
				p.sendMessage(buildFFA.getPrefix()+ "The player §e" + t.getName() + " §7is now invisble.");
				for(Player o : Bukkit.getOnlinePlayers()) {
					if(!o.hasPermission("buildffa.vanish")) {
						o.hidePlayer(t);
					}
				}
			} else {
				vanish.remove(p);
				p.setAllowFlight(false);
				t.sendMessage(buildFFA.getPrefix() + "§cYou are now visible again.");
				p.sendMessage(buildFFA.getPrefix() + "Der Spieler §e" + t.getName() + " §7is now visible again.");
				for(Player o : Bukkit.getOnlinePlayers()) {
					o.showPlayer(p);
				}
			}
		}
		return true;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		for(Player o : vanish) {
			if(!e.getPlayer().hasPermission("buildffa.vanish")) {
				e.getPlayer().hidePlayer(o);
			}
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		if(vanish.contains(e.getPlayer())) {
			vanish.remove(e.getPlayer());
			for(Player o : Bukkit.getOnlinePlayers()) {
				o.showPlayer(e.getPlayer());
			}
		}
	}

	@EventHandler
	public void onVanish(EntityDamageByEntityEvent e) {
		Entity p = e.getEntity();
		Entity pd = e.getDamager();
		if(((p instanceof Player)) && ((pd instanceof Player)) && (vanish.contains(pd)) && (!pd.hasPermission("buildffa.vanish.allow"))) {
			e.setCancelled(true);
			pd.sendMessage(buildFFA.getPrefix() + "§cYou can't damage player when you are in vanish.");
		}
	}
}