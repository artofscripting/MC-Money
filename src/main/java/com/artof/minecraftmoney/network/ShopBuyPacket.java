package com.artof.minecraftmoney.network;

import com.artof.minecraftmoney.MinecraftMoney;
import com.artof.minecraftmoney.config.ShopConfig;
import com.artof.minecraftmoney.data.PlayerCurrencyData;
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
                    long totalCost = entry.price() * quantity;
                    
                    // Create the item stack first to check if it fits in inventory
                    ItemStack stack = entry.createItemStackWithRegistry(quantity, 
                            serverPlayer.level().registryAccess());
                    
                    if (stack.isEmpty()) {
                        serverPlayer.sendSystemMessage(
                                Component.literal("Failed to create item: " + entry.displayName())
                                        .withStyle(net.minecraft.ChatFormatting.RED)
                        );
                        return;
                    }
                    
                    // Check if player has space in inventory
                    if (!canFitInInventory(serverPlayer, stack)) {
                        serverPlayer.sendSystemMessage(
                                Component.translatable("message.minecraftmoney.shop_inventory_full")
                                        .withStyle(net.minecraft.ChatFormatting.RED)
                        );
                        return;
                    }
                    
                    // Check if player has enough currency
                    if (PlayerCurrencyData.removeCurrency(serverPlayer, totalCost)) {
                        // Re-create the stack since we need a fresh one to add
                        ItemStack purchaseStack = entry.createItemStackWithRegistry(quantity, 
                                serverPlayer.level().registryAccess());
                        
                        serverPlayer.getInventory().add(purchaseStack);
                        forceSyncInventory(serverPlayer);
                        
                        String itemName = quantity > 1 ? quantity + "x " + entry.displayName() : entry.displayName();
                        serverPlayer.sendSystemMessage(
                                Component.translatable("message.minecraftmoney.shop_purchased", itemName)
                                        .withStyle(net.minecraft.ChatFormatting.GREEN)
                        );
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
    
    /**
     * Check if the item stack can fit in the player's inventory
     */
    private static boolean canFitInInventory(ServerPlayer player, ItemStack stack) {
        // Check if there's an empty slot
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack existing = player.getInventory().items.get(i);
            if (existing.isEmpty()) {
                return true;
            }
            // Check if we can stack with an existing item
            if (ItemStack.isSameItemSameComponents(existing, stack) && 
                existing.getCount() + stack.getCount() <= existing.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    private static void forceSyncInventory(ServerPlayer serverPlayer) {
        // The player is usually viewing a shop menu, not the inventory menu, so we need to
        // explicitly refresh both. This fixes stacked purchases not visually updating until
        // another inventory action happens client-side.
        serverPlayer.getInventory().setChanged();
        serverPlayer.containerMenu.broadcastChanges();
        serverPlayer.containerMenu.sendAllDataToRemote();
        serverPlayer.inventoryMenu.broadcastChanges();
        serverPlayer.inventoryMenu.sendAllDataToRemote();
    }
}
