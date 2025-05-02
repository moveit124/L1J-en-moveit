package l1j.server.server.templates;

import l1j.server.Config;
import l1j.server.server.datatables.ItemTable;

public class L1ShopItem {
    private final int _itemId;
    private final L1Item _item;
    private final int _price;
    private final int _packCount;
    private final int _enchantLevel;
    
    public L1ShopItem(int itemId, int price, int packCount, int enchantLevel) {
        _itemId = itemId;
        _item = ItemTable.getInstance().getTemplate(itemId);
        _price = price;
        _packCount = packCount;
        _enchantLevel = enchantLevel;
    }
    
    public L1ShopItem(int itemId, int price, int packCount) {
        _itemId = itemId;
        _item = ItemTable.getInstance().getTemplate(itemId);
        _price = price;
        _packCount = packCount;
        _enchantLevel = 0;
    }
    
    public int getItemId() {
        return _itemId;
    }
    
    // Modified: Make sure we always return a valid item (if possible)
    public L1Item getItem() {
        if (_item == null) {
        	System.err.println("Error: L1ShopItem.getItem() returned null for itemId: " + _itemId);
            return ItemTable.getInstance().getTemplate(_itemId);
        }
        return _item;
    }
    
    public int getPrice() {
        return _price;
    }
    
    public int getPackCount() {
        return _packCount;
    }
    
    public int getEnchantLevel() {
        return _enchantLevel;
    }
    
    // New method that encapsulates the assessed price calculation
    public int getAssessedPrice() {
        return (int) (getPrice() * Config.RATE_SHOP_PURCHASING_PRICE / getPackCount());
    }
}
