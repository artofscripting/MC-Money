package com.artof.minecraftmoney.network;

import com.artof.minecraftmoney.MinecraftMoney;
import com.artof.minecraftmoney.data.PlayerCurrencyData;
import com.artof.minecraftmoney.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet for withdrawing currency as coins from the portable bank (Magic Ledger).
 */
public record PortableBankActionPacket(long amount) implements CustomPacketPayload {
    
    public static final CustomPacketPayload.Type<PortableBankActionPacket> TYPE = 
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MinecraftMoney.MOD_ID, "portable_bank_action"));
    
    public static final StreamCodec<FriendlyByteBuf, PortableBankActionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            PortableBankActionPacket::amount,
            PortableBankActionPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(PortableBankActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                long amountToWithdraw = packet.amount();
                long currentBalance = PlayerCurrencyData.getCurrency(serverPlayer);
                
                if (amountToWithdraw <= 0) {
                    return;
                }
                
                // Limit to available balance
                amountToWithdraw = Math.min(amountToWithdraw, currentBalance);
                
                if (amountToWithdraw <= 0) {
                    serverPlayer.displayClientMessage(
                            Component.translatable("message.minecraftmoney.shop_insufficient")
                                    .withStyle(ChatFormatting.RED),
                            true
                    );
                    return;
                }
                
                // Remove currency from player
                PlayerCurrencyData.removeCurrency(serverPlayer, amountToWithdraw);
                
                // Give coins to player (starting with largest denominations)
                long remaining = amountToWithdraw;
                remaining = giveCoins(serverPlayer, remaining, ModItems.TRILLION_COIN.get(), ModItems.TRILLION_VALUE);
                remaining = giveCoins(serverPlayer, remaining, ModItems.TEN_BILLION_COIN.get(), ModItems.TEN_BILLION_VALUE);
                remaining = giveCoins(serverPlayer, remaining, ModItems.BILLION_COIN.get(), ModItems.BILLION_VALUE);
                remaining = giveCoins(serverPlayer, remaining, ModItems.TEN_MILLION_COIN.get(), ModItems.TEN_MILLION_VALUE);
                remaining = giveCoins(serverPlayer, remaining, ModItems.MILLION_COIN.get(), ModItems.MILLION_VALUE);
                remaining = giveCoins(serverPlayer, remaining, ModItems.TEN_THOUSAND_COIN.get(), ModItems.TEN_THOUSAND_VALUE);
                remaining = giveCoins(serverPlayer, remaining, ModItems.PLATINUM_COIN.get(), ModItems.PLATINUM_VALUE);
                remaining = giveCoins(serverPlayer, remaining, ModItems.GOLD_COIN.get(), ModItems.GOLD_VALUE);
                remaining = giveCoins(serverPlayer, remaining, ModItems.SILVER_COIN.get(), ModItems.SILVER_VALUE);
                remaining = giveCoins(serverPlayer, remaining, ModItems.COPPER_COIN.get(), ModItems.COPPER_VALUE);
                
                // If there's any remaining (shouldn't happen with copper = 1), refund it
                if (remaining > 0) {
                    PlayerCurrencyData.addCurrency(serverPlayer, remaining);
                }
                
                long withdrawn = amountToWithdraw - remaining;
                serverPlayer.displayClientMessage(
                        Component.translatable("command.minecraftmoney.withdrawn", String.format("%,d", withdrawn))
                                .withStyle(ChatFormatting.GREEN),
                        true
                );
            }
        });
    }
    
    private static long giveCoins(ServerPlayer player, long remaining, net.minecraft.world.item.Item coinItem, long coinValue) {
        if (remaining < coinValue) {
            return remaining;
        }
        
        long numCoins = remaining / coinValue;
        
        // Give coins in stacks of 64
        while (numCoins > 0) {
            int stackSize = (int) Math.min(numCoins, 64);
            ItemStack stack = new ItemStack(coinItem, stackSize);
            
            if (!player.getInventory().add(stack)) {
                // Inventory full, drop on ground
                player.drop(stack, false);
            }
            
            numCoins -= stackSize;
        }
        
        return remaining % coinValue;
    }
}
