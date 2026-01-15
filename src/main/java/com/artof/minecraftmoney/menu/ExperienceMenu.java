package com.artof.minecraftmoney.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Menu for the experience trading screen.
 * This is a simple menu that doesn't require a block entity.
 */
public class ExperienceMenu extends AbstractContainerMenu {
    
    // Client constructor
    public ExperienceMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory);
    }
    
    // Server constructor
    public ExperienceMenu(int containerId, Inventory playerInventory) {
        super(ModMenuTypes.EXPERIENCE_MENU.get(), containerId);
    }
    
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean stillValid(Player player) {
        // Always valid as long as the player exists
        return true;
    }
}
