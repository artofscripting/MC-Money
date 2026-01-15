package com.artof.minecraftmoney.item;

import com.artof.minecraftmoney.menu.ExperienceMenu;
import com.artof.minecraftmoney.menu.PortableBankMenu;
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
 * A magic ledger item that looks like an enchanted book.
 * - Shift + Right-click: Opens the bank (currency management)
 * - Right-click: Opens the experience trading screen
 */
public class MagicLedgerItem extends Item {
    
    public MagicLedgerItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public boolean isFoil(ItemStack stack) {
        // Makes the item have the enchantment glint effect
        return true;
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (player.isShiftKeyDown()) {
                // Shift+right-click: Open portable bank screen (withdraw coins)
                serverPlayer.openMenu(new SimpleMenuProvider(
                        (containerId, playerInventory, p) -> new PortableBankMenu(containerId, playerInventory),
                        Component.translatable("gui.minecraftmoney.magic_ledger_bank")
                ));
            } else {
                // Normal right-click: Open experience trading screen
                serverPlayer.openMenu(new SimpleMenuProvider(
                        (containerId, playerInventory, p) -> new ExperienceMenu(containerId, playerInventory),
                        Component.translatable("gui.minecraftmoney.experience_trade")
                ));
            }
            return InteractionResultHolder.success(stack);
        }
        
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
    
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.minecraftmoney.magic_ledger_1")
                .withStyle(ChatFormatting.GOLD));
        tooltipComponents.add(Component.translatable("tooltip.minecraftmoney.magic_ledger_2")
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("tooltip.minecraftmoney.magic_ledger_3")
                .withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
