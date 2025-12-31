package com.artof.minecraftmoney.menu;

import com.artof.minecraftmoney.block.entity.PersonalSellerBlockEntity;
import com.artof.minecraftmoney.block.ModBlocks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PersonalSellerMenu extends AbstractContainerMenu {
    private final PersonalSellerBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    
    // Client constructor
    public PersonalSellerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }
    
    // Server constructor
    public PersonalSellerMenu(int containerId, Inventory playerInventory, BlockEntity blockEntity) {
        super(ModMenuTypes.PERSONAL_SELLER_MENU.get(), containerId);
        this.blockEntity = (PersonalSellerBlockEntity) blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        
        // Add seller inventory slots (3x3 grid)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new Slot(this.blockEntity, col + row * 3, 62 + col * 18, 17 + row * 18));
            }
        }
        
        // Add player inventory slots
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        
        // Add player hotbar slots
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }
    
    public PersonalSellerBlockEntity getBlockEntity() {
        return blockEntity;
    }
    
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();
            
            if (index < 9) {
                // From seller inventory to player inventory
                if (!this.moveItemStackTo(stackInSlot, 9, 45, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From player inventory to seller inventory
                if (!this.moveItemStackTo(stackInSlot, 0, 9, false)) {
                    return ItemStack.EMPTY;
                }
            }
            
            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            
            if (stackInSlot.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }
            
            slot.onTake(player, stackInSlot);
        }
        
        return result;
    }
    
    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.PERSONAL_SELLER_BLOCK.get());
    }
}
