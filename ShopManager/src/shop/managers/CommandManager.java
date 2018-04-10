package shop.managers;

import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import shop.data.Shop;
import shop.data.ShopLogger;
import shop.data.ShopOwner;
import shop.utils.Utils;

public class CommandManager implements CommandExecutor {
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player))
		{
			return false;
		}
		
		Player plr = (Player) sender;
		List<Shop> myShops = Shop.getShopsFromUUID(plr);
		
		if (args.length == 0) {
			Utils.showGuide(plr);
			return true;
		}
		
		String func = args[0];
		
		if(func.equalsIgnoreCase("myshops")) {
			plr.sendMessage(ChatColor.GOLD + "------------------------>");
			plr.sendMessage(ChatColor.GOLD + "[*] " + ChatColor.BLUE + "You have " + myShops.size() + " shops!");
			plr.sendMessage(ChatColor.GOLD + "------------------------>");
			
			for (Shop e : myShops) {
				String id = e.getShopId();
				String itemToSell = Utils.getNameByMaterial(e.getItemToSell());
				String price = Utils.getNameByMaterial(e.getPrice());
				
				int howMuch = e.getHowMuch();
				int forHowMuch = e.getForHowMuch();
				
				plr.sendMessage(ChatColor.DARK_PURPLE + "[" + ChatColor.DARK_AQUA + id + ChatColor.DARK_PURPLE + "] Selling " + howMuch + " " 
				+ itemToSell + " for " + forHowMuch + " " + price);
			}
			
			plr.sendMessage(ChatColor.GOLD + "------------------------>");
			
			return true;
		}
		
		if(func.equalsIgnoreCase("mylogs")) {
			plr.sendMessage(ChatColor.GOLD + "------------------------>");
			plr.sendMessage(ChatColor.GOLD + "YOUR SHOPS' LOGS:");
			plr.sendMessage(ChatColor.GOLD + "------------------------>");
			
			for (Shop e : myShops) {
				String id = e.getShopId();
				ShopLogger logger = e.getShopLogger();
				
				for (String log : logger.getLog()) {
					plr.sendMessage(ChatColor.DARK_PURPLE + "[" + ChatColor.DARK_AQUA + id + ChatColor.DARK_PURPLE + "] " + log);
				}
			}
			
			plr.sendMessage(ChatColor.GOLD + "------------------------>");
			
			return true;
		}
		
		if(func.equalsIgnoreCase("mychests")) {
			plr.sendMessage(ChatColor.GOLD + "------------------------>");
			plr.sendMessage(ChatColor.GOLD + "YOUR CHESTS:");
			plr.sendMessage(ChatColor.GOLD + "------------------------>");
			
			for (Shop e : myShops) {
				String id = e.getShopId();
				Material itemToSell = e.getItemToSell();
				String itemToSellName = Utils.getNameByMaterial(e.getItemToSell());
				
				int howMuch = e.getHowMuch();
				
				Chest chest = (Chest) e.getBlockAttached().getState();
				
				if (!Utils.containsMaterial(chest.getInventory(), itemToSell, howMuch)) {
					plr.sendMessage(ChatColor.DARK_PURPLE + "[" + ChatColor.DARK_AQUA + id + ChatColor.DARK_PURPLE + "] Shop is out of stock.");
					
					continue;
				}
				
				int howMany = Utils.getSizeOfItem(chest.getInventory(), itemToSell);
				plr.sendMessage(ChatColor.DARK_PURPLE + "[" + ChatColor.DARK_AQUA + id + ChatColor.DARK_PURPLE + "] Shop has " + howMany + " " + itemToSellName + " available.");
			}
			
			plr.sendMessage(ChatColor.GOLD + "------------------------>");
			
			return true;
		}
		
		if(func.equalsIgnoreCase("owners")) {
			plr.sendMessage(ChatColor.GOLD + "------------------------>");
			plr.sendMessage(ChatColor.GOLD + "YOUR SHOPS OWNERS:");
			plr.sendMessage(ChatColor.GOLD + "------------------------>");
			
			for (Shop e : myShops) {
				String id = e.getShopId();
				String owners = "";
				List<ShopOwner> shopOwners = e.getShopOwners();
				
				for (ShopOwner owner : shopOwners) {
					if ((owner == shopOwners.get(shopOwners.size() - 1))) {
						owners += owner.getOwnerName() + ".";
					} else {
						owners += owner.getOwnerName() + ", ";
					}
				}
				
				plr.sendMessage(ChatColor.DARK_PURPLE + "[" + ChatColor.DARK_AQUA + id + ChatColor.DARK_PURPLE + "] Shop owners: " + ChatColor.DARK_AQUA + owners);
			}
			
			plr.sendMessage(ChatColor.GOLD + "------------------------>");
			
			return true;
		}
		
		if(func.equalsIgnoreCase("addowner") && args.length == 3) {
			Shop shop = Shop.getShopFromID(args[1], plr.getUniqueId());
			UUID uuid = Utils.getPlayerUUIDFromName(args[2]);
			
			if (shop == null) {
				plr.sendMessage(ChatColor.DARK_RED + "Invalid shop id.");
				return false;
			}
			
			if (shop.isOwner(uuid)) {
				plr.sendMessage(ChatColor.DARK_RED + args[2] + " is already the owner of this shop.");
				return false;
			}
			
			if (shop.getShopOwners().size() == 4) {
				plr.sendMessage(ChatColor.DARK_RED + "You can't add more than 4 owners in one shop.");
				return false;
			}
			
			if (uuid == null) {
				plr.sendMessage(ChatColor.DARK_RED + "Player '" + args[2] + "' is invalid.");
				return false;
			}
			
			shop.getShopOwners().add(new ShopOwner(uuid, shop));
			
			plr.sendMessage(ChatColor.DARK_GREEN + "Added " + args[2] + " as an owner of " + shop.getShopId() + " id shop");
			
			Utils.sendMessageIfOnline(args[2], plr.getName() + " has added you as an owner of his " + shop.getShopId() + " id shop.");
			return true;
		}
		
		if(func.equalsIgnoreCase("removeowner") && args.length == 3) {
			Shop shop = Shop.getShopFromID(args[1], plr.getUniqueId());
			String playerName = args[2];
			
			if (shop == null) {
				plr.sendMessage(ChatColor.DARK_RED + "Invalid shop id.");
				return false;
			}
			
			if (!shop.isOwner(playerName)) {
				plr.sendMessage(ChatColor.DARK_RED + args[2] + " owner doesn't exist in this shop.");
				return false;
			}
			
			if (Utils.getPlayerNameFromUUID(shop.getOwnerUUID()).equalsIgnoreCase(args[2])) {
				plr.sendMessage(ChatColor.DARK_RED + "You can't remove yourself.");
				return false;
			}

			shop.getShopOwners().remove(ShopOwner.getShopOwner(shop, playerName));
			
			plr.sendMessage(ChatColor.DARK_RED + "Removed " + args[2] + " as an owner of " + shop.getShopId() + " id shop");
			Utils.sendMessageIfOnline(args[2], plr.getName() + " has removed you from his " + shop.getShopId() + " id shop.");
			
			return true;
		}
		
		return false;
	}
}
