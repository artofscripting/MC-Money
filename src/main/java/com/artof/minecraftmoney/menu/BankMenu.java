package com.artof.minecraftmoney.menu;

import com.artof.minecraftmoney.block.entity.BankBlockEntity;
import com.artof.minecraftmoney.block.ModBlocks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BankMenu extends AbstractContainerMenu {
    private final BankBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    
    // Client constructor
    public BankMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }
    
    // Server constructor
    public BankMenu(int containerId, Inventory playerInventory, BlockEntity blockEntity) {
        super(ModMenuTypes.BANK_MENU.get(), containerId);
        this.blockEntity = (BankBlockEntity) blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
    }
    
    public BankBlockEntity getBlockEntity() {
        return blockEntity;
    }
    
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.BANK_BLOCK.get());
    }
}
