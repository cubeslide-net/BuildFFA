package ch.luca.hydroslide.buildffa.listener;

import ch.luca.hydroslide.buildffa.BuildFFA;
import ch.luca.hydroslide.buildffa.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryCloseListener implements Listener {

	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		if(e.getInventory() == null) return;
		Player p = (Player) e.getPlayer();

		if(e.getView().getTitle().equals("§eInventar Sortierung")) {
			User user = BuildFFA.getInstance().getUserManager().getUser(p);
			user.saveInventory();
			p.sendMessage(BuildFFA.getInstance().getPrefix() + "Dein Inventar wurde gespeichert.");
		}
	}

}
