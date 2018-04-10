package shop.data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import shop.utils.Utils;

public class Shop {
	private UUID uuid;

	public static Map<UUID, Integer> shopsIds = Maps.newHashMap();
	
	private int shopId;
	
	private Material itemToSell;
	private Material price;
	
	private Block block;
	private Block attachedBlock;
	
	private int howMuch;
	private int forHowMuch;
	
	private List<ShopOwner> owners = Lists.newArrayList();
	
	public static List<Shop> shops = Lists.newArrayList();
	
	private ShopLogger shopLogger;
	
	public Shop(UUID uuid, Material itemToSell, int howMuch, Material price, int forHowMuch, Block block, Block attachedBlock) {
		this.itemToSell = itemToSell;
		this.price = price;
		
		this.howMuch = howMuch;
		this.forHowMuch = forHowMuch;
		
		this.block = block;
		this.attachedBlock = attachedBlock;
		
		this.uuid = uuid;
		
		if (shopsIds.get(uuid) == null) {
			shopsIds.put(uuid, 0);
		}
		
		int shopId = shopsIds.get(uuid) + 1;
		
		shopsIds.put(uuid, shopId);
		this.shopId = shopId;
				
		shops.add(this);
		owners.add(new ShopOwner(uuid, this));
		
		this.setShopLogger(new ShopLogger(this));
		
		Utils.addLog(this, "Added a shop which sells " + forHowMuch + " " + Utils.getNameByMaterial(itemToSell) + " for " + howMuch + " " + Utils.getNameByMaterial(price));
	}
	
	public static List<Location> getShopsLocationsFromPlayer(Player player) {
		List<Location> shopLocations = Lists.newArrayList();
		
		for (Shop e : getShopsFromUUID(player)) {
			shopLocations.add(e.getBlock().getLocation());
		}
		
		return shopLocations;
	}
	
	public static List<Block> getAttachedBlocks() {
		List<Block> blocksAttached = Lists.newArrayList();
		
		for (Shop e : shops) {
			blocksAttached.add(e.getBlockAttached());
		}
		
		return blocksAttached;
	}
	
	public boolean isOwner(UUID uuid) {
		for (ShopOwner owner : owners) {
			if (owner.getOwnerUUID().equals(uuid)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static List<Shop> getShopsFromUUID(Player player) {
		List<Shop> shopList = Lists.newArrayList();
		
		for (Shop e : shops) {
			if (e.getOwnerUUID().equals(player.getUniqueId())) {
				shopList.add(e);
			}
		}
		
		return shopList;
	}
	
	public static Shop getShopFromBlock(Block block) {
		for (Shop e : shops) {
			if (e.getBlock().equals(block)) {
				return e;
			}
		}
		
		return null;
	}
	
	public static Shop getShopFromID(String id, UUID uuid) {
		for (Shop e : shops) {
			if (e.getShopId().equals(id) && e.getOwnerUUID().equals(uuid)) {
				return e;
			}
		}
		
		return null;
	}

	public void setOwnerUUID(UUID uuid) {
		this.uuid = uuid;
	}

	public Material getItemToSell() {
		return itemToSell;
	}

	public void setItemToSell(Material itemToSell) {
		this.itemToSell = itemToSell;
	}

	public Material getPrice() {
		return price;
	}

	public void setPrice(Material price) {
		this.price = price;
	}

	public int getHowMuch() {
		return howMuch;
	}

	public void setHowMuch(int howMuch) {
		this.howMuch = howMuch;
	}

	public int getForHowMuch() {
		return forHowMuch;
	}

	public void setForHowMuch(int forHowMuch) {
		this.forHowMuch = forHowMuch;
	}

	public String getShopId() {
		return String.valueOf(shopId);
	}

	public void setShopId(int shopId) {
		this.shopId = shopId;
	}
	
	public Block getBlock() {
		return this.block;
	}
	
	public void setBlock(Block sign) {
		this.block = sign;
	}
	
	public static Shop getShopFromAttachedBlock(Block attachedBlock) {
		for (Shop e : shops) {
			if (e.getBlockAttached().equals(attachedBlock)) {
				return e;
			}
		}
		return null;
	}

	public Block getBlockAttached() {
		return attachedBlock;
	}

	public UUID getOwnerUUID() {
		return this.uuid;
	}

	public List<ShopOwner> getShopOwners() {
		return this.owners;
	}

	public boolean isOwner(String playerName) {
		for (ShopOwner owner : owners) {
			if (owner.getOwnerName() == playerName) {
				return true;
			}
		}
		
		return false;
	}

	public ShopLogger getShopLogger() {
		return shopLogger;
	}

	public void setShopLogger(ShopLogger shopLogger) {
		this.shopLogger = shopLogger;
	}
}
