package ch.luca.hydroslide.buildffa.listener;

import ch.luca.hydroslide.buildffa.BuildFFA;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
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


		if(e.getAction() == Action.RIGHT_CLICK_AIR ||e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			final Player player = e.getPlayer();

			if(player.getInventory().getItemInMainHand().getType() == Material.FISHING_ROD) {
				if(player.getLocation().getBlockY() >= BuildFFA.getInstance().getFightHeight()) {
					e.setCancelled(true);
				}
			}
		}

	}
}
