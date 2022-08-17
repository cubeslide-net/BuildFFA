package ch.luca.hydroslide.buildffa.listener;

import ch.luca.hydroslide.buildffa.BuildFFA;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerInteractListener implements Listener {

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if(BuildFFA.getInstance().getAllowBuild().contains(e.getPlayer())) return;
		if(e.getClickedBlock() != null) {

			if(e.getClickedBlock().getType().isInteractable()) {
				e.setCancelled(true);
			}

		}
	}
}
