package com.artof.minecraftmoney.network;

import com.artof.minecraftmoney.MinecraftMoney;
import com.artof.minecraftmoney.data.PlayerCurrencyData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet for buying or selling experience points.
 */
public record ExperienceActionPacket(boolean isBuying, int amount) implements CustomPacketPayload {
    // Price per experience point
    private static final int XP_PRICE = 10;
    
    public static final CustomPacketPayload.Type<ExperienceActionPacket> TYPE = 
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MinecraftMoney.MOD_ID, "experience_action"));
    
    public static final StreamCodec<FriendlyByteBuf, ExperienceActionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ExperienceActionPacket::isBuying,
            ByteBufCodecs.VAR_INT,
            ExperienceActionPacket::amount,
            ExperienceActionPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(ExperienceActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                if (packet.isBuying()) {
                    // Buy XP: spend currency, gain experience
                    long cost = (long) packet.amount() * XP_PRICE;
                    if (PlayerCurrencyData.removeCurrency(serverPlayer, cost)) {
                        serverPlayer.giveExperiencePoints(packet.amount());
                        serverPlayer.displayClientMessage(
                                Component.translatable("message.minecraftmoney.xp_bought", 
                                        String.format("%,d", packet.amount()), 
                                        String.format("%,d", cost))
                                        .withStyle(ChatFormatting.GREEN),
                                true
                        );
                    } else {
                        serverPlayer.displayClientMessage(
                                Component.translatable("message.minecraftmoney.shop_insufficient")
                                        .withStyle(ChatFormatting.RED),
                                true
                        );
                    }
                } else {
                    // Sell XP: spend experience, gain currency
                    int playerTotalXP = getTotalPlayerXP(serverPlayer);
                    int xpToSell = Math.min(packet.amount(), playerTotalXP);
                    
                    if (xpToSell > 0) {
                        // Remove XP from player
                        removeExperience(serverPlayer, xpToSell);
                        
                        // Add currency
                        long earned = (long) xpToSell * XP_PRICE;
                        PlayerCurrencyData.addCurrency(serverPlayer, earned);
                        
                        serverPlayer.displayClientMessage(
                                Component.translatable("message.minecraftmoney.xp_sold",
                                        String.format("%,d", xpToSell),
                                        String.format("%,d", earned))
                                        .withStyle(ChatFormatting.GREEN),
                                true
                        );
                    } else {
                        serverPlayer.displayClientMessage(
                                Component.translatable("message.minecraftmoney.no_xp")
                                        .withStyle(ChatFormatting.RED),
                                true
                        );
                    }
                }
            }
        });
    }
    
    private static int getTotalPlayerXP(ServerPlayer player) {
        // Calculate total XP from level and progress
        int level = player.experienceLevel;
        float progress = player.experienceProgress;
        int xpForCurrentLevel = getXpForLevel(level);
        return getXpNeededForLevel(level) + (int) (xpForCurrentLevel * progress);
    }
    
    private static int getXpNeededForLevel(int level) {
        // Total XP needed to reach this level from 0
        if (level <= 16) {
            return level * level + 6 * level;
        } else if (level <= 31) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        }
    }
    
    private static int getXpForLevel(int level) {
        // XP needed to go from this level to the next
        if (level <= 15) {
            return 2 * level + 7;
        } else if (level <= 30) {
            return 5 * level - 38;
        } else {
            return 9 * level - 158;
        }
    }
    
    private static void removeExperience(ServerPlayer player, int amount) {
        int totalXP = getTotalPlayerXP(player);
        int newTotalXP = Math.max(0, totalXP - amount);
        
        // Reset player XP
        player.experienceLevel = 0;
        player.experienceProgress = 0;
        player.totalExperience = 0;
        
        // Re-add the remaining XP
        if (newTotalXP > 0) {
            player.giveExperiencePoints(newTotalXP);
        }
    }
}
