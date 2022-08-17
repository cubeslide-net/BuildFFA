package ch.luca.hydroslide.buildffa.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

import ch.luca.hydroslide.buildffa.BuildFFA;
import ch.luca.hydroslide.buildffa.item.ItemManager;
import ch.luca.hydroslide.buildffa.util.InventoryUtil;
import lombok.Getter;
import lombok.Setter;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class User {

	@Getter
	private Player player;
	
	@Getter
	private int kills, deaths, coins;
	
	@Getter
	@Setter
	private Player damage;
	
	@Getter
	private ItemStack[] inventorySort;
	
	private long delay = System.currentTimeMillis();
	
	public User(Player p) {
		this.player = p;
		
		BuildFFA.getInstance().getBuildFFADatabase().executeQuery("SELECT * FROM BuildFFA WHERE uuid='" + p.getUniqueId().toString() + "'", true, new Consumer<ResultSet>() {
			
			@Override
			public void accept(ResultSet rs) {
				if(rs == null) return;
				try {
					if(rs.next()) {
						kills = rs.getInt("kills");
						deaths = rs.getInt("deaths");
						coins = rs.getInt("coins");
						
						inventorySort = InventoryUtil.fromBase64(rs.getString("inventory"));
						
						//TODO: createScoreboard
						setInventory();
						
						BuildFFA.getInstance().getBuildFFADatabase().update("UPDATE BuildFFA SET name='" + p.getName() + "' WHERE uuid='" + p.getUniqueId().toString() + "'", false);
					} else {
						kills = 0;
						deaths = 0;
						coins = 0;
						inventorySort = BuildFFA.getInstance().getItemManager().getDefaultItems();

						//TODO: createScoreboard
						setInventory();
						
						BuildFFA.getInstance().getBuildFFADatabase().update("INSERT INTO BuildFFA VALUES('" + p.getUniqueId().toString() + "','" + p.getName() + "',0,0,0,'" + InventoryUtil.toBase64(inventorySort) + "')", false);
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
	}
	public void death(boolean fall) {
		this.player.setHealth(20.0D);
		if(fall) {
			this.player.teleport(BuildFFA.getInstance().getSpawnLocation());
			this.deaths++;
			this.player.playSound(this.player.getLocation(), Sound.ENTITY_ENDERMAN_DEATH, 1F, 1F);
			//TODO: update deaths line
			if(this.damage != null) {
				Player d = this.damage;
				this.damage = null;
				if(d != null) {
					this.player.sendMessage(BuildFFA.getInstance().getPrefix() + "Du wurdest von §c" + d.getName() + " §7getötet!");
					this.player.sendMessage(BuildFFA.getInstance().getPrefix() + "§c- §62 Coins");
					BuildFFA.getInstance().getUserManager().getUser(d).kill(this.player);
				} else {
					this.player.sendMessage(BuildFFA.getInstance().getPrefix() + "§cDu bist gestorben.");
					this.player.sendMessage(BuildFFA.getInstance().getPrefix() + "§c- §62 Coins");
				}
			} else {
				this.player.sendMessage(BuildFFA.getInstance().getPrefix() + "§cDu bist gestorben.");
				this.player.sendMessage(BuildFFA.getInstance().getPrefix() + "§c- §62 Coins");
			}
			setInventory();
			BuildFFA.getInstance().getBuildFFADatabase().update("UPDATE BuildFFA SET deaths=" + this.deaths + ", coins=" + this.coins + " WHERE uuid='" + this.player.getUniqueId().toString() + "'", true);
			return;
		}
		Bukkit.getScheduler().runTaskLater(BuildFFA.getInstance(), () -> {
			this.player.teleport(BuildFFA.getInstance().getSpawnLocation());
			this.deaths++;
			this.player.playSound(this.player.getLocation(), Sound.ENTITY_ENDERMAN_DEATH, 1F, 1F);

			//TODO: update deaths line
			if(this.damage != null) {
				Player d = this.damage;
				this.damage = null;
				if(d != null) {
					this.player.sendMessage(BuildFFA.getInstance().getPrefix() + "Du wurdest von §c" + d.getDisplayName() + " §7getötet!");
					this.player.sendMessage(BuildFFA.getInstance().getPrefix() + "§c- §62 Coins");
					BuildFFA.getInstance().getUserManager().getUser(d).kill(this.player);
				} else {
					this.player.sendMessage(BuildFFA.getInstance().getPrefix() + "§cDu bist gestorben.");
					this.player.sendMessage(BuildFFA.getInstance().getPrefix() + "§c- §62 Coins");
				}
			} else {
				this.player.sendMessage(BuildFFA.getInstance().getPrefix() + "§cDu bist gestorben.");
				this.player.sendMessage(BuildFFA.getInstance().getPrefix() + "§c- §62 Coins");
			}
			setInventory();
			BuildFFA.getInstance().getBuildFFADatabase().update("UPDATE BuildFFA SET deaths=" + this.deaths + ", coins=" + this.coins + " WHERE uuid='" + this.player.getUniqueId().toString() + "'", true);
		}, 2L);
	}
	public void kill(Player p) {
		this.player.setHealth(20.0D);
		this.kills++;
		if(player.hasPermission("buildffa.doublecoins")) {
			this.player.sendMessage(BuildFFA.getInstance().getPrefix() + "Du hast §e" + p.getDisplayName() + " §7getötet.");
			this.player.sendMessage(BuildFFA.getInstance().getPrefix() + "§a+ §65 Coins §8(§a+ §65 §7Coinbonus§8)");
		} else {
			this.player.sendMessage(BuildFFA.getInstance().getPrefix() + "Du hast §e" + p.getDisplayName() + " §7getötet.");
			this.player.sendMessage(BuildFFA.getInstance().getPrefix() + "§a+ §65 Coins");
		}
		//TODO: update kills line
		BuildFFA.getInstance().getBuildFFADatabase().update("UPDATE BuildFFA SET kills=" + this.kills + ", coins=" + this.coins + " WHERE uuid='" + this.player.getUniqueId().toString() + "'", true);
	}
	public void setInventory() {
		PlayerInventory inv = this.player.getInventory();
		inv.setContents(this.inventorySort);
		
		ItemManager itemManager = BuildFFA.getInstance().getItemManager();
		
		inv.setHelmet(itemManager.getHelmet());
		inv.setChestplate(itemManager.getChestplate());
		inv.setLeggings(itemManager.getLeggings());
		inv.setBoots(itemManager.getBoots());
	}
	public void saveInventory() {
		this.inventorySort = this.player.getInventory().getContents();
		BuildFFA.getInstance().getBuildFFADatabase().update("UPDATE BuildFFA SET inventory='" + InventoryUtil.toBase64(this.inventorySort) + "' WHERE uuid='" + this.player.getUniqueId().toString() + "'", true);
	}
	//TODO: Scoreboard
	public boolean checkDelay() {
		if(System.currentTimeMillis() > this.delay+500) {
			this.delay = System.currentTimeMillis();
			return true;
		}
		return false;
	}
}
