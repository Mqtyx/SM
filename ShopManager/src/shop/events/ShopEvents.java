package shop.events;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.block.Chest;
import org.bukkit.material.Sign;

import shop.data.Shop;
import shop.main.Main;
import shop.utils.Utils;

public class ShopEvents implements Listener {
	
	public ShopEvents(Main plugin)
	{
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void BlockBreakEvent(BlockBreakEvent e) {
		Block brokenBlock = e.getBlock();
		Shop shopBlock = Shop.getShopFromBlock(brokenBlock);
		Shop shopAttached = Shop.getShopFromAttachedBlock(brokenBlock);
		
		if (shopAttached != null) {
			if (!shopAttached.isOwner(e.getPlayer().getUniqueId())) {
				e.setCancelled(true);
				
				return;
			}
		}
		
		if (shopBlock != null) {
			if (!shopBlock.isOwner(e.getPlayer().getUniqueId())) {
				e.setCancelled(true);
				
				return;
			}
		}
		
		if (shopBlock != null) {
			Shop.shops.remove(shopBlock);
			
			Main.INSTANCE.getConfig().getConfigurationSection("Shops." + shopBlock.getOwnerUUID()).set(shopBlock.getShopId(), null);
		}
		
		if (shopAttached != null) {
			Shop.shops.remove(shopAttached);
			
			Main.INSTANCE.getConfig().getConfigurationSection("Shops." + shopAttached.getOwnerUUID()).set(shopAttached.getShopId(), null);
		}
	}
	
	@EventHandler
	public void onPlayerInteractChest(PlayerInteractEvent e) {
	    if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
	        if(e.getClickedBlock().getType().equals(Material.CHEST)) {
	        	Shop shop = Shop.getShopFromAttachedBlock(e.getClickedBlock());
	        	
	        	if (shop == null) {
	        		return;
	        	}
	        	
	        	if (!shop.isOwner(e.getPlayer().getUniqueId())) {
	        		e.getPlayer().sendMessage(ChatColor.DARK_RED + "You can't open someone's shop.");
	        		e.setCancelled(true);
	        	}
	        }
	    }
	}
	
	@EventHandler
	public void onPlayerInteractSign(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		
		if (e.getClickedBlock().getType() != Material.WALL_SIGN && e.getClickedBlock().getType() != Material.SIGN_POST) {
			return;
		}
		
		Shop shop = Shop.getShopFromBlock(e.getClickedBlock());
		
		if (shop == null) {
			return;
		}
		
		Player player = e.getPlayer();
		
		Chest chest = (Chest) shop.getBlockAttached().getState();
		
		Material itemToSell = shop.getItemToSell();
		Material price = shop.getPrice();
		
		int howMuch = shop.getHowMuch();
		int forHowMuch = shop.getForHowMuch();
		
		if (shop.isOwner(player.getUniqueId())) {
			player.sendMessage(ChatColor.DARK_RED + "You can't buy from your own shop.");
			return;
		}

		if (!Utils.containsMaterial(chest.getInventory(), itemToSell, howMuch)) {
			player.sendMessage(ChatColor.DARK_RED + "Shop is out of stock at the moment, please come back later!");
			return;
		}
		
		if (!Utils.hasAvaliableSlot(player)) {
			player.sendMessage(ChatColor.DARK_RED + "You should have available slot before buying something.");
			return;
		}
		
		if (!player.getInventory().containsAtLeast(new ItemStack(price), forHowMuch)) {
			int amount = Utils.getPlayerItemAmount(player, price);
			String difference = String.valueOf(forHowMuch - amount);
			
			player.sendMessage(ChatColor.DARK_RED + "You should have " + difference + " " + Utils.getNameByMaterial(price) + " in order to buy " + howMuch + " " + Utils.getNameByMaterial(itemToSell) + ".");
			return;
		}
		
		ItemStack itemStack = Utils.getContent(player, chest.getInventory(), itemToSell, howMuch);
		
		player.sendMessage(ChatColor.DARK_GREEN + "Successfully bought " + howMuch + " " + Utils.getNameByMaterial(itemToSell) + "!");
		
		chest.getInventory().addItem(new ItemStack(price, forHowMuch));
		player.getInventory().addItem(itemStack);
		
		Utils.sendMessageIfOnline(shop, ChatColor.GOLD + "[*] " + ChatColor.BLUE + player.getName() + " has bought " + shop.getHowMuch() + " " + Utils.getNameByMaterial(shop.getItemToSell()) + " from your shop!");
	
		if (!Utils.containsMaterial(chest.getInventory(), itemToSell, howMuch)) {
			Utils.sendMessageIfOnline(shop, ChatColor.GOLD + "[" + ChatColor.BLUE + shop.getShopId() + ChatColor.DARK_PURPLE + "] " + ChatColor.GOLD + " Shop has been out of stock.");
		}
		
		Utils.removeAmountInventory(chest.getInventory(), howMuch, itemToSell);
		Utils.removeAmountInventory(player.getInventory(), forHowMuch, price);
		
		Utils.addLog(shop, player.getName() + " has bought " + shop.getHowMuch() + " " + Utils.getNameByMaterial(shop.getItemToSell()) + " from your shop!");
	}
	
	@EventHandler
	public void onPlayerEditSign(SignChangeEvent e) {
		Sign sign = (Sign) e.getBlock().getState().getData();
        Block attached = e.getBlock().getRelative(sign.getAttachedFace());
        
        if (attached.getType() != Material.CHEST) {
        	return;
        }
		
		boolean success = Utils.addSignShop(e);
		
		if (!success) {
			return;
		}
		
		// CREATESHOP
		// AMOUNT OF THE SELLING ITEM
		// PRICE AND HOW MUCH
		// NAME OF THE SELLING ITEM
	}
}
