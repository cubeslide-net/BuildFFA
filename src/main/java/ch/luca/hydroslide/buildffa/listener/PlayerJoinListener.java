package ch.luca.hydroslide.buildffa.listener;

import ch.luca.hydroslide.buildffa.BuildFFA;
import ch.luca.hydroslide.buildffa.user.User;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
	
	@EventHandler(priority = EventPriority.HIGHEST )
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		e.setJoinMessage(null);
		p.sendMessage(BuildFFA.getInstance().getPrefix() + "Use §e/inv §7to sort your inventory.");
		p.sendMessage(BuildFFA.getInstance().getPrefix() + "§cTeaming with other players is not allowed!");
		User user = BuildFFA.getInstance().getUserManager().getUser(p);
		Location loc = BuildFFA.getInstance().getSpawnLocation();
		p.teleport(loc != null ? loc : Bukkit.getWorlds().get(0).getSpawnLocation());
		p.setHealth(20.0D);
		p.setFoodLevel(20);
		p.setGameMode(GameMode.SURVIVAL);
	}
}
