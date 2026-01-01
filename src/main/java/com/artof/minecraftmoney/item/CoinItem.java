package com.artof.minecraftmoney.item;

import com.artof.minecraftmoney.data.PlayerCurrencyData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class CoinItem extends Item {
    private final long value;
    
    public CoinItem(Properties properties, long value) {
        super(properties);
        this.value = value;
    }
    
    public long getValue() {
        return value;
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            // Shift+right-click deposits whole stack, regular right-click deposits 1
            int coinsToDeposit = player.isShiftKeyDown() ? stack.getCount() : 1;
            long totalValue = value * coinsToDeposit;
            PlayerCurrencyData.addCurrency(serverPlayer, totalValue);
            
            player.displayClientMessage(
                    Component.translatable("message.minecraftmoney.deposited", String.format("%,d", totalValue))
                            .withStyle(ChatFormatting.GREEN), 
                    true
            );
            
            stack.shrink(coinsToDeposit);
            return InteractionResultHolder.success(stack);
        }
        
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.minecraftmoney.coin_value", String.format("%,d", value))
                .withStyle(ChatFormatting.GOLD));
        tooltipComponents.add(Component.translatable("tooltip.minecraftmoney.right_click_deposit")
                .withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
