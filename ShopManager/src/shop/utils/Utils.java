package shop.utils;

import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Maps;

import shop.data.Shop;
import shop.main.Main;

public class Utils {
	public static Map<String, String> guideList = Maps.newHashMap();
	
    public static boolean isInteger(String s) {
    	try
    	{
    		Integer.parseInt(s);
    	} catch (Exception e) {
    		return false;
    	}
    	return true;
    }

    /*
    public static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }*/

	public static void setupShops() {
	    Utils.guideList.put("myshops", "Displays your running shops.");
	    Utils.guideList.put("mychests", "Displays your shops' chests.");
	    Utils.guideList.put("mylogs", "Displays your shops' logs.");
	    Utils.guideList.put("owners", "Displays your shops' owners.");
	    Utils.guideList.put("addowner <shopId> <playerName>", "Adds owner to your shop to manage it.");
	    Utils.guideList.put("removeowner <shopId> <playerName>", "Removes owner from your shop.");

	    if(Main.INSTANCE.getConfig().getConfigurationSection("Shops") != null)
	    for (String playerUUID : Main.INSTANCE.getConfig().getConfigurationSection("Shops").getKeys(false))
	    {
		    for (String shopId : Main.INSTANCE.getConfig().getConfigurationSection("Shops." + playerUUID).getKeys(false))
		    {
		    	String path = "Shops." + playerUUID + "." + shopId + ".Shop.";
		    	UUID uuid;
		    	
		    	double locX = Main.INSTANCE.getConfig().getDouble(path + "Location.x");
		    	double locY = Main.INSTANCE.getConfig().getDouble(path + "Location.y");
		    	double locZ = Main.INSTANCE.getConfig().getDouble(path + "Location.z");
		    	String world = Main.INSTANCE.getConfig().getString(path + "Location.world");
		    	
		    	if (world == null) {
		    		continue;
		    	}
		    	
		    	World w = Bukkit.getServer().getWorld(world);
		    	Location loc = new Location(w, locX, locY, locZ);
		    	
		    	Block block = loc.getBlock();
		    	
		    	if (block == null) {
		    		continue;
		    	}
		    	
		    	org.bukkit.material.Sign sign;
		    	
		    	try {
					sign = (org.bukkit.material.Sign) block.getState().getData();
					uuid = UUID.fromString(playerUUID);
					
		    	} catch(Exception ex) {
		    		Main.INSTANCE.getConfig().getConfigurationSection("Shops").set(playerUUID, null);
		    		
		    		continue;
		    	}
				
		        Block attachedBlock = block.getRelative(sign.getAttachedFace());
		        
		    	if (attachedBlock == null) {
		    		continue;
		    	}
		    	
		    	Material itemToSell = Material.valueOf(Main.INSTANCE.getConfig().getString(path + "ItemToSell").toUpperCase());
		    	int howMuch = Integer.valueOf(Main.INSTANCE.getConfig().getString(path + "HowMuch"));
		    	
		    	Material price = Material.valueOf(Main.INSTANCE.getConfig().getString(path + "Price").toUpperCase());
		    	int forHowMuch = Integer.valueOf(Main.INSTANCE.getConfig().getString(path + "ForHowMuch"));
		    	
				new Shop(uuid, itemToSell, howMuch, price, forHowMuch, block, attachedBlock);
		    }
	    }
	}
	
	public static boolean addSignShop(SignChangeEvent e) {	
		String[] lines = e.getLines();
		
		if ((!lines[0].equalsIgnoreCase("::buyshop"))) {
			return false;
		}
		
		if (!isInteger(lines[1])) {
			return false;
		}
		
		if ((!lines[3].contains("[")) || (!lines[3].contains("]"))) {
			return false;
		}
		
		String after = lines[3].substring(lines[3].indexOf('[') + 1);
		String forHowMuch = after.substring(0, after.indexOf(']'));
		
		if (!isInteger(forHowMuch)) {
			return false;
		}
		
		String sellingItem = lines[3].substring(lines[3].indexOf(']') + 1).toUpperCase();
		String priceItem  = lines[2].toUpperCase();
		int howMuch = Integer.valueOf(lines[1]);
		
		if (!isMaterialValid(sellingItem)) {
			return false;
		}
		
		if (!isMaterialValid(priceItem)) {
			return false;
		}
		
		if (Material.valueOf(sellingItem) == Material.valueOf(priceItem)) {
			return false;
		}
		
		org.bukkit.material.Sign sign = (org.bukkit.material.Sign) e.getBlock().getState().getData();
        Block attached = e.getBlock().getRelative(sign.getAttachedFace());
        
		if (isAttachedBlock(attached)) {
			return false;
		}
		
		Shop shop = new Shop(e.getPlayer().getUniqueId(), Material.valueOf(priceItem), Integer.valueOf(howMuch), Material.valueOf(sellingItem), Integer.valueOf(forHowMuch), e.getBlock(), attached);		
		Location location = e.getBlock().getLocation();
		
		UUID uuid = e.getPlayer().getUniqueId();
		
		Main.INSTANCE.getConfig().set("Shops." + uuid + "." + shop.getShopId() + ".Shop.Location.x", location.getBlockX());
		Main.INSTANCE.getConfig().set("Shops." + uuid + "." + shop.getShopId() + ".Shop.Location.y", location.getBlockY());
		Main.INSTANCE.getConfig().set("Shops." + uuid + "." + shop.getShopId() + ".Shop.Location.z", location.getBlockZ());
		Main.INSTANCE.getConfig().set("Shops." + uuid + "." + shop.getShopId() + ".Shop.Location.world", location.getWorld().getName());
		
		Main.INSTANCE.getConfig().set("Shops." + uuid + "." + shop.getShopId() + ".Shop.ItemToSell", priceItem);
		Main.INSTANCE.getConfig().set("Shops." + uuid + "." + shop.getShopId() + ".Shop.HowMuch", howMuch);
		Main.INSTANCE.getConfig().set("Shops." + uuid + "." + shop.getShopId() + ".Shop.Price", sellingItem);
		Main.INSTANCE.getConfig().set("Shops." + uuid + "." + shop.getShopId() + ".Shop.ForHowMuch", forHowMuch);
		
		Main.INSTANCE.saveConfig();
		
		e.setLine(0, ChatColor.BLUE + "[Buy] ");
		e.setLine(1, ChatColor.BLACK + "" + forHowMuch);
		e.setLine(2, ChatColor.BLACK + getNameByMaterial(Material.valueOf(sellingItem)));
		e.setLine(3, ChatColor.DARK_PURPLE + "" + howMuch + " " + getNameByMaterial(Material.valueOf(priceItem)));
		
		return true;
	}
	
	public static String getNameByMaterial(Material m) {
		String strMaterial = m.toString();
		if (strMaterial.contains("_")) {
			String[] split_ = strMaterial.split("_");
			String first = split_[0].toLowerCase();
			String second = split_[1].toLowerCase();
			String total = WordUtils.capitalize(first) + " " + WordUtils.capitalize(second);
			return total;
 		} else {
 			return WordUtils.capitalize(strMaterial.toLowerCase());
 		}
	}
	
	public static boolean isAttachedBlock(Block block) {
		for (Block e : Shop.getAttachedBlocks()) {	
			if (e.equals(block)) {
				return true;
			}
		}
		
		return false;
	}
	
    public static Block getBlockSignAttachedTo(Block block) {
        if (block.getType().equals(Material.WALL_SIGN))
            switch (block.getData()) {
                case 2:
                    return block.getRelative(BlockFace.WEST);
                case 3:
                    return block.getRelative(BlockFace.EAST);
                case 4:
                    return block.getRelative(BlockFace.SOUTH);
                case 5:
                    return block.getRelative(BlockFace.NORTH);
            }
        return null;
    }
	
	public static boolean isMaterialValid(String materialName) {
		try {
			Material.valueOf(materialName.toUpperCase());
			return true;
		} catch(Exception ex) {
			return false;
		}
	}

	public static int getPlayerItemAmount(Player player, Material material) {
		for (ItemStack item : player.getInventory()) {
			if (item == null) {
				continue;
			}
			
			if (item.getType().equals(material)) {
				return item.getAmount();
			}
		}
		return 0;
	}
	
	public static void removeAmountInventory(Inventory inventory, int amount, Material material) {
		for (ItemStack item : inventory) {
			if (item == null) {
				continue;
			}
			
			if (item.getType().equals(material)) {
				item.setAmount(item.getAmount() - amount);
				break;
			}
		}
	}
	
	public static boolean hasAvaliableSlot(Player player){
	    Inventory inv = player.getInventory();
	    
	    for (ItemStack item: inv.getContents()) {
	         if(item == null) {
	        	 return true;
	         }
	    }
	    
	    return false;
	}
	
	public static int getSizeOfItem(Inventory inv, Material material){
		int size = 0;
		
	    for (ItemStack item : inv.getContents()) {
	         if(item == null) {
	        	 continue;
	         }
	         
	         if (item.getType().equals(material)) {
	        	 size += item.getAmount();
	         }   
	    }
	    
	    return size;
	}

	public static void sendMessageIfOnline(Shop shop, String msg) {
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			if (p.getUniqueId().equals(shop.getOwnerUUID())) {
				p.sendMessage(msg);
				
				break;
			}
		}
	}
	
	public static void sendMessageIfOnline(String playerName, String msg) {
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			if (p.getName().equalsIgnoreCase(playerName)) {
				p.sendMessage(msg);
				
				break;
			}
		}
	}
	
	public static void showGuide(Player plr) {
		plr.sendMessage(ChatColor.GOLD + "------------------------>");
		
		for (String cmdName : guideList.keySet()) {
			plr.sendMessage(ChatColor.GRAY + "/ts " + cmdName + ChatColor.DARK_PURPLE + " ---> " + guideList.get(cmdName));
		}
		
		plr.sendMessage(ChatColor.GOLD + "------------------------>");
	}

	public static String getPlayerNameFromUUID(UUID uuid) {
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			if (p.getUniqueId().equals(uuid)) {
				return p.getName();
			}
		}
		return null;
	}

	public static UUID getPlayerUUIDFromName(String playerName) {
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			if (p.getName().equals(playerName)) {
				return p.getUniqueId();
			}
		}
		return null;
	}
	
	public static boolean containsMaterial(Inventory inv, Material material, int amount) {
		int size = getSizeOfItem(inv, material);
		
		if (size <= 0 || size < amount) {
			return false;
		}
		
		return true;
	}

	public static ItemStack getContent(Player player, Inventory inv, Material material, int amount) {
		for (ItemStack item : inv.getContents()) {
			if (item == null) {
				continue;
			}
			
			if (item.getType().equals(material)) {
				ItemStack modifiedItemStack = item.clone();
				
				modifiedItemStack.setAmount(amount);
				
				return modifiedItemStack;
			}
		}
		
		return null;
	}
	
	public static void addLog(Shop shop, String log) {
		shop.getShopLogger().getLog().add(log);
	}
}
