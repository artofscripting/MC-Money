package com.artof.minecraftmoney.data;

import com.artof.minecraftmoney.MinecraftMoney;
import com.artof.minecraftmoney.network.NetworkHandler;
import com.artof.minecraftmoney.network.SyncCurrencyPacket;
import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class PlayerCurrencyData {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = 
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MinecraftMoney.MOD_ID);
    
    public static final Supplier<AttachmentType<Integer>> CURRENCY = ATTACHMENT_TYPES.register(
            "currency",
            () -> AttachmentType.builder(() -> 0)
                    .serialize(Codec.INT)
                    .build()
    );
    
    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
    
    public static int getCurrency(Player player) {
        return player.getData(CURRENCY);
    }
    
    public static void setCurrency(Player player, int amount) {
        player.setData(CURRENCY, Math.max(0, amount));
        syncToClient(player);
    }
    
    public static void addCurrency(Player player, int amount) {
        setCurrency(player, getCurrency(player) + amount);
    }
    
    public static boolean removeCurrency(Player player, int amount) {
        int current = getCurrency(player);
        if (current >= amount) {
            setCurrency(player, current - amount);
            return true;
        }
        return false;
    }
    
    public static boolean hasCurrency(Player player, int amount) {
        return getCurrency(player) >= amount;
    }
    
    private static void syncToClient(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.sendToPlayer(serverPlayer, new SyncCurrencyPacket(getCurrency(player)));
        }
    }
}
