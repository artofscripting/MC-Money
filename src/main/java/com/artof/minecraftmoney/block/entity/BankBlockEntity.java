package com.artof.minecraftmoney.block.entity;

import com.artof.minecraftmoney.menu.BankMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BankBlockEntity extends BlockEntity implements MenuProvider {
    private final Map<UUID, Long> playerBalances = new HashMap<>();
    
    public BankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BANK_BLOCK_ENTITY.get(), pos, state);
    }
    
    public long getBalance(Player player) {
        return playerBalances.getOrDefault(player.getUUID(), 0L);
    }
    
    public void setBalance(Player player, long amount) {
        playerBalances.put(player.getUUID(), Math.max(0, amount));
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
    
    public void deposit(Player player, long amount) {
        setBalance(player, getBalance(player) + amount);
    }
    
    public boolean withdraw(Player player, long amount) {
        long balance = getBalance(player);
        if (balance >= amount) {
            setBalance(player, balance - amount);
            return true;
        }
        return false;
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag balancesTag = new CompoundTag();
        for (Map.Entry<UUID, Long> entry : playerBalances.entrySet()) {
            balancesTag.putLong(entry.getKey().toString(), entry.getValue());
        }
        tag.put("PlayerBalances", balancesTag);
    }
    
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        playerBalances.clear();
        if (tag.contains("PlayerBalances")) {
            CompoundTag balancesTag = tag.getCompound("PlayerBalances");
            for (String key : balancesTag.getAllKeys()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    playerBalances.put(uuid, balancesTag.getLong(key));
                } catch (IllegalArgumentException ignored) {}
            }
        }
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
        return Component.translatable("block.minecraftmoney.bank_block");
    }
    
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new BankMenu(containerId, playerInventory, this);
    }
}
