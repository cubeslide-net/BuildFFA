package ch.luca.hydroslide.buildffa.listener;

import ch.luca.hydroslide.buildffa.BuildFFA;
import com.comphenix.protocol.PacketType.Play;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerDeathListener implements Listener {
	
	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		e.setDroppedExp(0);
		e.getDrops().clear();
		e.setKeepLevel(true);
		e.setDeathMessage(null);
		BuildFFA.getInstance().getUserManager().getUser(p).death(false);

		new BukkitRunnable() {
			@Override
			public void run() {
				p.spigot().respawn();
			}
		}.runTaskLater(BuildFFA.getInstance(), 20);
	}

	@EventHandler
	public void on(PlayerRespawnEvent event) {
		Player player = event.getPlayer();

		event.setRespawnLocation(BuildFFA.getInstance().getSpawnLocation());
	}

}
