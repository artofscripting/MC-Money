package com.artof.minecraftmoney.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Menu for the portable bank accessed via the Magic Ledger.
 * This shows only the player's wallet balance (no block-based bank storage).
 */
public class PortableBankMenu extends AbstractContainerMenu {
    
    // Client constructor
    public PortableBankMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory);
    }
    
    // Server constructor
    public PortableBankMenu(int containerId, Inventory playerInventory) {
        super(ModMenuTypes.PORTABLE_BANK_MENU.get(), containerId);
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
