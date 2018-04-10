package shop.data;

import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import shop.utils.Utils;

public class ShopOwner {
	private String ownerName;
	private UUID ownerUUID;
	private Shop shop;
	
	public static List<ShopOwner> shopOwners = Lists.newArrayList();
	
	public ShopOwner(UUID uuid, Shop shop) {
		this.ownerName = Utils.getPlayerNameFromUUID(uuid);
		this.ownerUUID = uuid;
		
		shopOwners.add(this);
	}
	
	public ShopOwner(Player player) {
		this.ownerUUID = player.getUniqueId();
		this.ownerName = player.getName();
	}
	
	public ShopOwner(String playerName) {
		this.ownerName = playerName;
		this.ownerUUID = Utils.getPlayerUUIDFromName(playerName);
	}

	public UUID getOwnerUUID() {
		return ownerUUID;
	}

	public void setOwnerUUID(UUID ownerUUID) {
		this.ownerUUID = ownerUUID;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public Shop getShop() {
		return shop;
	}

	public void setShop(Shop shop) {
		this.shop = shop;
	}

	public static ShopOwner getShopOwner(Shop shop, String playerName) {
		for (ShopOwner e : shopOwners) {
			if (shop.equals(e.getShop()) && e.getOwnerName().equalsIgnoreCase(playerName)) {
				return e;
			}
		}
		
		return null;
	}
}
