package com.artof.minecraftmoney.block.entity;

import com.artof.minecraftmoney.config.ShopConfig;
import com.artof.minecraftmoney.data.PlayerCurrencyData;
import com.artof.minecraftmoney.menu.PersonalSellerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PersonalSellerBlockEntity extends BlockEntity implements MenuProvider, WorldlyContainer {
    private static final int INVENTORY_SIZE = 9;
    private static final int SELL_INTERVAL_TICKS = 20; // Sell every second
    private static final int[] SLOTS_FOR_UP = {0, 1, 2, 3, 4, 5, 6, 7, 8};
    
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private UUID ownerUUID;
    private String ownerName = "";
    private int tickCounter = 0;
    private int totalEarned = 0;
    
    public PersonalSellerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PERSONAL_SELLER_BLOCK_ENTITY.get(), pos, state);
    }
    
    public void setOwner(UUID uuid, String name) {
        this.ownerUUID = uuid;
        this.ownerName = name;
        setChanged();
    }
    
    public boolean isOwner(Player player) {
        return ownerUUID != null && ownerUUID.equals(player.getUUID());
    }
    
    public String getOwnerName() {
        return ownerName != null && !ownerName.isEmpty() ? ownerName : "Unknown";
    }
    
    public UUID getOwnerUUID() {
        return ownerUUID;
    }
    
    public int getTotalEarned() {
        return totalEarned;
    }
    
    public static void serverTick(Level level, BlockPos pos, BlockState state, PersonalSellerBlockEntity entity) {
        entity.tickCounter++;
        if (entity.tickCounter >= SELL_INTERVAL_TICKS) {
            entity.tickCounter = 0;
            entity.trySellItems(level);
        }
    }
    
    private void trySellItems(Level level) {
        if (ownerUUID == null || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        
        Player owner = serverLevel.getServer().getPlayerList().getPlayer(ownerUUID);
        boolean soldAny = false;
        
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.get(i);
            if (!stack.isEmpty()) {
                int sellPrice = getSellPriceForItem(stack);
                if (sellPrice > 0) {
                    // Sell one item at a time
                    int soldCount = Math.min(stack.getCount(), 1);
                    int earnings = sellPrice * soldCount;
                    
                    // Add currency to owner
                    if (owner != null) {
                        PlayerCurrencyData.addCurrency(owner, earnings);
                    } else {
                        // Owner offline - store for later? For now, just accumulate
                        // We'll need a way to pay offline players - using saved data
                        addOfflineEarnings(level, earnings);
                    }
                    
                    totalEarned += earnings;
                    stack.shrink(soldCount);
                    if (stack.isEmpty()) {
                        inventory.set(i, ItemStack.EMPTY);
                    }
                    soldAny = true;
                    break; // Sell one item per tick cycle
                }
            }
        }
        
        if (soldAny) {
            setChanged();
        }
    }
    
    private void addOfflineEarnings(Level level, int earnings) {
        // For offline earnings, we store it and pay when they log in
        // This is handled via the block entity save - we'll check on player join
        // For now, the totalEarned tracks it and can be claimed
    }
    
    private int getSellPriceForItem(ItemStack stack) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String itemIdStr = itemId.toString();
        
        for (ShopConfig.ShopEntry entry : ShopConfig.getShopItems()) {
            if (entry.itemId().equals(itemIdStr)) {
                return entry.getSellPrice();
            }
        }
        return 0; // Item not in shop, can't be sold
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, inventory, registries);
        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
        }
        if (ownerName != null) {
            tag.putString("OwnerName", ownerName);
        }
        tag.putInt("TotalEarned", totalEarned);
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, inventory, registries);
        if (tag.hasUUID("Owner")) {
            ownerUUID = tag.getUUID("Owner");
        }
        if (tag.contains("OwnerName")) {
            ownerName = tag.getString("OwnerName");
        }
        totalEarned = tag.getInt("TotalEarned");
    }
    
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.minecraftmoney.personal_seller_block");
    }
    
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new PersonalSellerMenu(containerId, playerInventory, this);
    }
    
    // WorldlyContainer implementation for hopper support
    @Override
    public int[] getSlotsForFace(Direction side) {
        // Only allow input from the top
        if (side == Direction.UP) {
            return SLOTS_FOR_UP;
        }
        return new int[0];
    }
    
    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
        // Only allow items that can be sold
        return direction == Direction.UP && getSellPriceForItem(itemStack) > 0;
    }
    
    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        // Don't allow extraction
        return false;
    }
    
    // Container implementation
    @Override
    public int getContainerSize() {
        return INVENTORY_SIZE;
    }
    
    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public ItemStack getItem(int slot) {
        return inventory.get(slot);
    }
    
    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(inventory, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }
    
    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(inventory, slot);
    }
    
    @Override
    public void setItem(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }
    
    @Override
    public boolean stillValid(Player player) {
        return isOwner(player) && level != null && 
               level.getBlockEntity(worldPosition) == this &&
               player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }
    
    @Override
    public void clearContent() {
        inventory.clear();
    }
}
