package shop.data;

import java.util.List;

import com.google.common.collect.Lists;

public class ShopLogger {
	private Shop shop;
	
	private List<String> logs = Lists.newArrayList();
	
	public ShopLogger(Shop shop) {
		this.shop = shop;
	}

	public List<String> getLog() {
		return logs;
	}

	public Shop getShop() {
		return shop;
	}

	public void setShop(Shop shop) {
		this.shop = shop;
	}
}
