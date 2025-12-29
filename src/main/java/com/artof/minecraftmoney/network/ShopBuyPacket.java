package com.artof.minecraftmoney.network;

import com.artof.minecraftmoney.MinecraftMoney;
import com.artof.minecraftmoney.config.ShopConfig;
import com.artof.minecraftmoney.data.PlayerCurrencyData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record ShopBuyPacket(int itemIndex, int quantity) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ShopBuyPacket> TYPE = 
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MinecraftMoney.MOD_ID, "shop_buy"));
    
    public static final StreamCodec<FriendlyByteBuf, ShopBuyPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ShopBuyPacket::itemIndex,
            ByteBufCodecs.VAR_INT,
            ShopBuyPacket::quantity,
            ShopBuyPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(ShopBuyPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                List<ShopConfig.ShopEntry> shopItems = ShopConfig.getShopItems();
                
                if (packet.itemIndex() >= 0 && packet.itemIndex() < shopItems.size()) {
                    ShopConfig.ShopEntry entry = shopItems.get(packet.itemIndex());
                    int quantity = Math.max(1, Math.min(64, packet.quantity()));
                    int totalCost = entry.price() * quantity;
                    
                    // Check if player has enough currency
                    if (PlayerCurrencyData.removeCurrency(serverPlayer, totalCost)) {
                        // Give the items
                        ResourceLocation itemLoc = ResourceLocation.tryParse(entry.itemId());
                        if (itemLoc != null) {
                            var item = BuiltInRegistries.ITEM.get(itemLoc);
                            if (item != null) {
                                ItemStack stack = new ItemStack(item, quantity);
                                if (!serverPlayer.getInventory().add(stack)) {
                                    serverPlayer.drop(stack, false);
                                }
                                
                                // Force sync inventory to client
                                serverPlayer.inventoryMenu.broadcastChanges();
                                
                                String itemName = quantity > 1 ? quantity + "x " + entry.displayName() : entry.displayName();
                                serverPlayer.sendSystemMessage(
                                        Component.translatable("message.minecraftmoney.shop_purchased", itemName)
                                                .withStyle(net.minecraft.ChatFormatting.GREEN)
                                );
                            }
                        }
                    } else {
                        serverPlayer.sendSystemMessage(
                                Component.translatable("message.minecraftmoney.shop_insufficient")
                                        .withStyle(net.minecraft.ChatFormatting.RED)
                        );
                    }
                }
            }
        });
    }
}
