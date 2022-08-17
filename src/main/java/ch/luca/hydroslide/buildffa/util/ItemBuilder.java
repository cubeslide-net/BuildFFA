package ch.luca.hydroslide.buildffa.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class ItemBuilder {

	private org.bukkit.inventory.ItemStack is;

	public ItemBuilder(Material mat, int amount, int data)
	{
		this.is = new org.bukkit.inventory.ItemStack(mat, amount, (short) data);
	}
	public ItemBuilder(Material mat)
	{
		this.is = new org.bukkit.inventory.ItemStack(mat);
	}

	public ItemBuilder(org.bukkit.inventory.ItemStack is)
	{
		this.is = is;
	}

	public ItemBuilder amount(int amount)
	{
		this.is.setAmount(amount);
		return this;
	}

	public ItemBuilder name(String name)
	{
		ItemMeta meta = this.is.getItemMeta();
		meta.setDisplayName(name);
		this.is.setItemMeta(meta);
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ItemBuilder lore(String name)
	{
		ItemMeta meta = this.is.getItemMeta();
		List<String> lore = meta.getLore();
		if (lore == null) {
			lore = new ArrayList();
		}
		lore.add(name);
		meta.setLore(lore);
		this.is.setItemMeta(meta);
		return this;
	}

	public ItemBuilder lore(List<String> lore)
	{
		ItemMeta meta = this.is.getItemMeta();
		meta.setLore(lore);
		this.is.setItemMeta(meta);
		return this;
	}
	public ItemBuilder addItemFlag(ItemFlag itemFlag) {
		ItemMeta meta = this.is.getItemMeta();
		meta.addItemFlags(itemFlag);
		this.is.setItemMeta(meta);
		return this;
	}
	public ItemBuilder removeItemFlag(ItemFlag itemFlag) {
		ItemMeta meta = this.is.getItemMeta();
		meta.removeItemFlags(itemFlag);
		this.is.setItemMeta(meta);
		return this;
	}
	public ItemBuilder addAllItemFlags() {
		ItemMeta meta = this.is.getItemMeta();
		for(ItemFlag flag : ItemFlag.values()) {
			meta.addItemFlags(flag);
		}
		this.is.setItemMeta(meta);
		return this;
	}

	public ItemBuilder setUnbreakable(boolean unbreakable)
	{
		ItemMeta meta = this.is.getItemMeta();
		meta.setUnbreakable(unbreakable);
		this.is.setItemMeta(meta);
		return this;
	}
	public ItemBuilder enchantedBook(Enchantment ench, int level) {
		EnchantmentStorageMeta meta = (EnchantmentStorageMeta)this.is.getItemMeta();
		meta.addStoredEnchant(ench, level, true);
		this.is.setItemMeta(meta);
		return this;
	}

	public ItemBuilder durability(int durability)
	{
		this.is.setDurability((short)durability);
		return this;
	}

	@SuppressWarnings("deprecation")
	public ItemBuilder data(int data)
	{
		byte dataa = this.is.getData().getData();
		dataa = (byte) data;
		this.is.getData().setData(dataa);
		return this;
	}

	public ItemBuilder enchantment(Enchantment enchantment, int level)
	{
		this.is.addUnsafeEnchantment(enchantment, level);
		return this;
	}

	public ItemBuilder enchantment(Enchantment enchantment)
	{
		this.is.addUnsafeEnchantment(enchantment, 1);
		return this;
	}

	public ItemBuilder type(Material material)
	{
		this.is.setType(material);
		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ItemBuilder clearLore()
	{
		ItemMeta meta = this.is.getItemMeta();
		meta.setLore(new ArrayList());
		this.is.setItemMeta(meta);
		return this;
	}

	public ItemBuilder clearEnchantments()
	{
		for (Enchantment e : this.is.getEnchantments().keySet()) {
			this.is.removeEnchantment(e);
		}
		return this;
	}

	public ItemBuilder color(Color color)
	{
		if ((this.is.getType() == Material.LEATHER_BOOTS) || (this.is.getType() == Material.LEATHER_CHESTPLATE) || (this.is.getType() == Material.LEATHER_HELMET) || 
				(this.is.getType() == Material.LEATHER_LEGGINGS))
		{
			LeatherArmorMeta meta = (LeatherArmorMeta)this.is.getItemMeta();
			meta.setColor(color);
			this.is.setItemMeta(meta);
			return this;
		}
		throw new IllegalArgumentException("color() only applicable for leather armor!");
	}
	public org.bukkit.inventory.ItemStack build()
	{
		return this.is;
	}

}
