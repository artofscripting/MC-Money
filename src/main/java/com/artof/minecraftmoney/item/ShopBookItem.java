package com.artof.minecraftmoney.item;

import com.artof.minecraftmoney.menu.PortableShopMenu;
import com.artof.minecraftmoney.network.NetworkHandler;
import com.artof.minecraftmoney.network.SyncShopDataPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A dark tome that opens the shop when right-clicked.
 */
public class ShopBookItem extends Item {
    
    public ShopBookItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            // Open the portable shop screen
            NetworkHandler.sendToPlayer(serverPlayer, SyncShopDataPacket.fromCurrentConfig());
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, p) -> new PortableShopMenu(containerId, playerInventory),
                    Component.translatable("gui.minecraftmoney.shop_book")
            ));
            return InteractionResultHolder.success(stack);
        }
        
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.minecraftmoney.shop_book_1")
                .withStyle(ChatFormatting.DARK_PURPLE));
        tooltipComponents.add(Component.translatable("tooltip.minecraftmoney.shop_book_2")
                .withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
