package com.artof.minecraftmoney.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Menu for the portable shop accessed via the Shop Book item.
 * This opens the shop GUI without requiring a shop block.
 */
public class PortableShopMenu extends AbstractContainerMenu {
    
    // Client constructor
    public PortableShopMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory);
    }
    
    // Server constructor
    public PortableShopMenu(int containerId, Inventory playerInventory) {
        super(ModMenuTypes.PORTABLE_SHOP_MENU.get(), containerId);
    }
    
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
