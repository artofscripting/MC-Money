package com.artof.minecraftmoney.menu;

import com.artof.minecraftmoney.block.ModBlocks;
import com.artof.minecraftmoney.block.entity.ShopBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ShopMenu extends AbstractContainerMenu {
    private final ShopBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    
    // Client constructor
    public ShopMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }
    
    // Server constructor
    public ShopMenu(int containerId, Inventory playerInventory, BlockEntity blockEntity) {
        super(ModMenuTypes.SHOP_MENU.get(), containerId);
        this.blockEntity = (ShopBlockEntity) blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
    }
    
    public ShopBlockEntity getBlockEntity() {
        return blockEntity;
    }
    
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.SHOP_BLOCK.get());
    }
}
