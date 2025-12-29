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

public record ShopSellPacket(int itemIndex, int quantity) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ShopSellPacket> TYPE = 
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MinecraftMoney.MOD_ID, "shop_sell"));
    
    public static final StreamCodec<FriendlyByteBuf, ShopSellPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ShopSellPacket::itemIndex,
            ByteBufCodecs.VAR_INT,
            ShopSellPacket::quantity,
            ShopSellPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(ShopSellPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                List<ShopConfig.ShopEntry> shopItems = ShopConfig.getShopItems();
                
                if (packet.itemIndex() >= 0 && packet.itemIndex() < shopItems.size()) {
                    ShopConfig.ShopEntry entry = shopItems.get(packet.itemIndex());
                    int requestedQuantity = Math.max(1, Math.min(64, packet.quantity()));
                    
                    // Find the item in the player's inventory
                    ResourceLocation itemLoc = ResourceLocation.tryParse(entry.itemId());
                    if (itemLoc != null) {
                        var item = BuiltInRegistries.ITEM.get(itemLoc);
                        if (item != null) {
                            // Count total items in inventory
                            int totalCount = 0;
                            for (int i = 0; i < serverPlayer.getInventory().getContainerSize(); i++) {
                                ItemStack stack = serverPlayer.getInventory().getItem(i);
                                if (stack.is(item)) {
                                    totalCount += stack.getCount();
                                }
                            }
                            
                            if (totalCount <= 0) {
                                serverPlayer.sendSystemMessage(
                                        Component.translatable("message.minecraftmoney.shop_no_item")
                                                .withStyle(net.minecraft.ChatFormatting.RED)
                                );
                                return;
                            }
                            
                            // Sell up to the requested quantity
                            int toSell = Math.min(requestedQuantity, totalCount);
                            int remaining = toSell;
                            
                            for (int i = 0; i < serverPlayer.getInventory().getContainerSize() && remaining > 0; i++) {
                                ItemStack stack = serverPlayer.getInventory().getItem(i);
                                if (stack.is(item) && !stack.isEmpty()) {
                                    int removeCount = Math.min(remaining, stack.getCount());
                                    stack.shrink(removeCount);
                                    remaining -= removeCount;
                                }
                            }
                            
                            int soldCount = toSell - remaining;
                            int sellPrice = entry.getSellPrice() * soldCount;
                            PlayerCurrencyData.addCurrency(serverPlayer, sellPrice);
                            
                            // Force sync inventory to client
                            serverPlayer.inventoryMenu.broadcastChanges();
                            
                            String itemName = soldCount > 1 ? soldCount + "x " + entry.displayName() : entry.displayName();
                            serverPlayer.sendSystemMessage(
                                    Component.translatable("message.minecraftmoney.shop_sold", itemName, sellPrice)
                                            .withStyle(net.minecraft.ChatFormatting.GREEN)
                            );
                        }
                    }
                }
            }
        });
    }
}
