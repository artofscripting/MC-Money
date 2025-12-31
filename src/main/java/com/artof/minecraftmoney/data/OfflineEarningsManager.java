package com.artof.minecraftmoney.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages offline earnings for players from Personal Seller blocks.
 * This data is saved per-world and persists across server restarts.
 */
public class OfflineEarningsManager extends SavedData {
    private static final String DATA_NAME = "minecraftmoney_offline_earnings";
    
    private final Map<UUID, Integer> pendingEarnings = new HashMap<>();
    
    public OfflineEarningsManager() {
    }
    
    public static OfflineEarningsManager get(MinecraftServer server) {
        DimensionDataStorage storage = server.overworld().getDataStorage();
        return storage.computeIfAbsent(
                new Factory<>(OfflineEarningsManager::new, OfflineEarningsManager::load),
                DATA_NAME
        );
    }
    
    public static OfflineEarningsManager load(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        OfflineEarningsManager manager = new OfflineEarningsManager();
        
        if (tag.contains("Earnings")) {
            ListTag earningsList = tag.getList("Earnings", 10); // 10 = CompoundTag
            for (int i = 0; i < earningsList.size(); i++) {
                CompoundTag entry = earningsList.getCompound(i);
                UUID uuid = entry.getUUID("Player");
                int amount = entry.getInt("Amount");
                manager.pendingEarnings.put(uuid, amount);
            }
        }
        
        return manager;
    }
    
    @Override
    public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        ListTag earningsList = new ListTag();
        
        for (Map.Entry<UUID, Integer> entry : pendingEarnings.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("Player", entry.getKey());
            entryTag.putInt("Amount", entry.getValue());
            earningsList.add(entryTag);
        }
        
        tag.put("Earnings", earningsList);
        return tag;
    }
    
    /**
     * Add earnings for an offline player.
     */
    public void addEarnings(UUID playerUUID, int amount) {
        pendingEarnings.merge(playerUUID, amount, Integer::sum);
        setDirty();
    }
    
    /**
     * Claim all pending earnings for a player.
     * @return The amount claimed
     */
    public int claimEarnings(UUID playerUUID) {
        Integer amount = pendingEarnings.remove(playerUUID);
        if (amount != null && amount > 0) {
            setDirty();
            return amount;
        }
        return 0;
    }
    
    /**
     * Get pending earnings without claiming them.
     */
    public int getPendingEarnings(UUID playerUUID) {
        return pendingEarnings.getOrDefault(playerUUID, 0);
    }
}
