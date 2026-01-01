package com.artof.minecraftmoney.command;

import com.artof.minecraftmoney.data.PlayerCurrencyData;
import com.artof.minecraftmoney.item.ModItems;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class CurrencyCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("currency")
                // Check own balance
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    long balance = PlayerCurrencyData.getCurrency(player);
                    context.getSource().sendSuccess(() -> 
                            Component.translatable("command.minecraftmoney.balance", balance)
                                    .withStyle(ChatFormatting.GOLD), false);
                    return (int) Math.min(balance, Integer.MAX_VALUE);
                })
                
                // Check another player's balance
                .then(Commands.literal("balance")
                        .then(Commands.argument("player", EntityArgument.player())
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                    ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                    long balance = PlayerCurrencyData.getCurrency(target);
                                    context.getSource().sendSuccess(() -> 
                                            Component.translatable("command.minecraftmoney.balance_other", 
                                                    target.getDisplayName(), balance)
                                                    .withStyle(ChatFormatting.GOLD), false);
                                    return (int) Math.min(balance, Integer.MAX_VALUE);
                                })))
                
                // Add currency to a player
                .then(Commands.literal("add")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                        .executes(context -> {
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            long amount = LongArgumentType.getLong(context, "amount");
                                            PlayerCurrencyData.addCurrency(target, amount);
                                            context.getSource().sendSuccess(() -> 
                                                    Component.translatable("command.minecraftmoney.added", 
                                                            amount, target.getDisplayName())
                                                            .withStyle(ChatFormatting.GREEN), true);
                                            return (int) Math.min(amount, Integer.MAX_VALUE);
                                        }))))
                
                // Remove currency from a player
                .then(Commands.literal("remove")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                        .executes(context -> {
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            long amount = LongArgumentType.getLong(context, "amount");
                                            if (PlayerCurrencyData.removeCurrency(target, amount)) {
                                                context.getSource().sendSuccess(() -> 
                                                        Component.translatable("command.minecraftmoney.removed", 
                                                                amount, target.getDisplayName())
                                                                .withStyle(ChatFormatting.YELLOW), true);
                                                return (int) Math.min(amount, Integer.MAX_VALUE);
                                            } else {
                                                context.getSource().sendFailure(
                                                        Component.translatable("command.minecraftmoney.insufficient")
                                                                .withStyle(ChatFormatting.RED));
                                                return 0;
                                            }
                                        }))))
                
                // Set currency for a player
                .then(Commands.literal("set")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", LongArgumentType.longArg(0))
                                        .executes(context -> {
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            long amount = LongArgumentType.getLong(context, "amount");
                                            PlayerCurrencyData.setCurrency(target, amount);
                                            context.getSource().sendSuccess(() -> 
                                                    Component.translatable("command.minecraftmoney.set", 
                                                            target.getDisplayName(), amount)
                                                            .withStyle(ChatFormatting.GREEN), true);
                                            return (int) Math.min(amount, Integer.MAX_VALUE);
                                        }))))
                
                // Pay another player
                .then(Commands.literal("pay")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                        .executes(context -> {
                                            ServerPlayer sender = context.getSource().getPlayerOrException();
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            long amount = LongArgumentType.getLong(context, "amount");
                                            
                                            if (sender.equals(target)) {
                                                context.getSource().sendFailure(
                                                        Component.translatable("command.minecraftmoney.pay_self")
                                                                .withStyle(ChatFormatting.RED));
                                                return 0;
                                            }
                                            
                                            if (PlayerCurrencyData.removeCurrency(sender, amount)) {
                                                PlayerCurrencyData.addCurrency(target, amount);
                                                context.getSource().sendSuccess(() -> 
                                                        Component.translatable("command.minecraftmoney.paid", 
                                                                amount, target.getDisplayName())
                                                                .withStyle(ChatFormatting.GREEN), false);
                                                target.sendSystemMessage(
                                                        Component.translatable("command.minecraftmoney.received", 
                                                                amount, sender.getDisplayName())
                                                                .withStyle(ChatFormatting.GREEN));
                                                return (int) Math.min(amount, Integer.MAX_VALUE);
                                            } else {
                                                context.getSource().sendFailure(
                                                        Component.translatable("command.minecraftmoney.insufficient")
                                                                .withStyle(ChatFormatting.RED));
                                                return 0;
                                            }
                                        }))))
                
                // Withdraw currency as coins
                .then(Commands.literal("withdraw")
                        .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    long amount = LongArgumentType.getLong(context, "amount");
                                    
                                    if (!PlayerCurrencyData.removeCurrency(player, amount)) {
                                        context.getSource().sendFailure(
                                                Component.translatable("command.minecraftmoney.insufficient")
                                                        .withStyle(ChatFormatting.RED));
                                        return 0;
                                    }
                                    
                                    // Convert to coins and give to player
                                    giveCoinsToPlayer(player, amount);
                                    
                                    context.getSource().sendSuccess(() -> 
                                            Component.translatable("command.minecraftmoney.withdrawn", amount)
                                                    .withStyle(ChatFormatting.GREEN), false);
                                    return (int) Math.min(amount, Integer.MAX_VALUE);
                                })))
        );
    }
    
    private static void giveCoinsToPlayer(ServerPlayer player, long amount) {
        long remaining = amount;
        
        // Give trillion coins (1,000,000,000,000 each)
        int trillionCount = (int) (remaining / ModItems.TRILLION_VALUE);
        remaining %= ModItems.TRILLION_VALUE;
        if (trillionCount > 0) {
            giveItemToPlayer(player, new ItemStack(ModItems.TRILLION_COIN.get(), trillionCount));
        }
        
        // Give 10 billion coins (10,000,000,000 each)
        int tenBillionCount = (int) (remaining / ModItems.TEN_BILLION_VALUE);
        remaining %= ModItems.TEN_BILLION_VALUE;
        if (tenBillionCount > 0) {
            giveItemToPlayer(player, new ItemStack(ModItems.TEN_BILLION_COIN.get(), tenBillionCount));
        }
        
        // Give billion coins (1,000,000,000 each)
        int billionCount = (int) (remaining / ModItems.BILLION_VALUE);
        remaining %= ModItems.BILLION_VALUE;
        if (billionCount > 0) {
            giveItemToPlayer(player, new ItemStack(ModItems.BILLION_COIN.get(), billionCount));
        }
        
        // Give 10 million coins (10,000,000 each)
        int tenMillionCount = (int) (remaining / ModItems.TEN_MILLION_VALUE);
        remaining %= ModItems.TEN_MILLION_VALUE;
        if (tenMillionCount > 0) {
            giveItemToPlayer(player, new ItemStack(ModItems.TEN_MILLION_COIN.get(), tenMillionCount));
        }
        
        // Give million coins (1,000,000 each)
        int millionCount = (int) (remaining / ModItems.MILLION_VALUE);
        remaining %= ModItems.MILLION_VALUE;
        if (millionCount > 0) {
            giveItemToPlayer(player, new ItemStack(ModItems.MILLION_COIN.get(), millionCount));
        }
        
        // Give 10 thousand coins (10,000 each)
        int tenThousandCount = (int) (remaining / ModItems.TEN_THOUSAND_VALUE);
        remaining %= ModItems.TEN_THOUSAND_VALUE;
        if (tenThousandCount > 0) {
            giveItemToPlayer(player, new ItemStack(ModItems.TEN_THOUSAND_COIN.get(), tenThousandCount));
        }
        
        // Give platinum coins (1000 each)
        int platinumCount = (int) (remaining / ModItems.PLATINUM_VALUE);
        remaining %= ModItems.PLATINUM_VALUE;
        if (platinumCount > 0) {
            giveItemToPlayer(player, new ItemStack(ModItems.PLATINUM_COIN.get(), platinumCount));
        }
        
        // Give gold coins (100 each)
        int goldCount = (int) (remaining / ModItems.GOLD_VALUE);
        remaining %= ModItems.GOLD_VALUE;
        if (goldCount > 0) {
            giveItemToPlayer(player, new ItemStack(ModItems.GOLD_COIN.get(), goldCount));
        }
        
        // Give silver coins (10 each)
        int silverCount = (int) (remaining / ModItems.SILVER_VALUE);
        remaining %= ModItems.SILVER_VALUE;
        if (silverCount > 0) {
            giveItemToPlayer(player, new ItemStack(ModItems.SILVER_COIN.get(), silverCount));
        }
        
        // Give copper coins (1 each)
        if (remaining > 0) {
            giveItemToPlayer(player, new ItemStack(ModItems.COPPER_COIN.get(), (int) remaining));
        }
    }
    
    private static void giveItemToPlayer(ServerPlayer player, ItemStack stack) {
        while (stack.getCount() > 64) {
            ItemStack splitStack = stack.split(64);
            if (!player.getInventory().add(splitStack)) {
                player.drop(splitStack, false);
            }
        }
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
