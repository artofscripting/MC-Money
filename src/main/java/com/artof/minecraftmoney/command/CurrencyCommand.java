package com.artof.minecraftmoney.command;

import com.artof.minecraftmoney.data.PlayerCurrencyData;
import com.artof.minecraftmoney.item.ModItems;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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
                    int balance = PlayerCurrencyData.getCurrency(player);
                    context.getSource().sendSuccess(() -> 
                            Component.translatable("command.minecraftmoney.balance", balance)
                                    .withStyle(ChatFormatting.GOLD), false);
                    return balance;
                })
                
                // Check another player's balance
                .then(Commands.literal("balance")
                        .then(Commands.argument("player", EntityArgument.player())
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                    ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                    int balance = PlayerCurrencyData.getCurrency(target);
                                    context.getSource().sendSuccess(() -> 
                                            Component.translatable("command.minecraftmoney.balance_other", 
                                                    target.getDisplayName(), balance)
                                                    .withStyle(ChatFormatting.GOLD), false);
                                    return balance;
                                })))
                
                // Add currency to a player
                .then(Commands.literal("add")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            int amount = IntegerArgumentType.getInteger(context, "amount");
                                            PlayerCurrencyData.addCurrency(target, amount);
                                            context.getSource().sendSuccess(() -> 
                                                    Component.translatable("command.minecraftmoney.added", 
                                                            amount, target.getDisplayName())
                                                            .withStyle(ChatFormatting.GREEN), true);
                                            return amount;
                                        }))))
                
                // Remove currency from a player
                .then(Commands.literal("remove")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            int amount = IntegerArgumentType.getInteger(context, "amount");
                                            if (PlayerCurrencyData.removeCurrency(target, amount)) {
                                                context.getSource().sendSuccess(() -> 
                                                        Component.translatable("command.minecraftmoney.removed", 
                                                                amount, target.getDisplayName())
                                                                .withStyle(ChatFormatting.YELLOW), true);
                                                return amount;
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
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(context -> {
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            int amount = IntegerArgumentType.getInteger(context, "amount");
                                            PlayerCurrencyData.setCurrency(target, amount);
                                            context.getSource().sendSuccess(() -> 
                                                    Component.translatable("command.minecraftmoney.set", 
                                                            target.getDisplayName(), amount)
                                                            .withStyle(ChatFormatting.GREEN), true);
                                            return amount;
                                        }))))
                
                // Pay another player
                .then(Commands.literal("pay")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            ServerPlayer sender = context.getSource().getPlayerOrException();
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            int amount = IntegerArgumentType.getInteger(context, "amount");
                                            
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
                                                return amount;
                                            } else {
                                                context.getSource().sendFailure(
                                                        Component.translatable("command.minecraftmoney.insufficient")
                                                                .withStyle(ChatFormatting.RED));
                                                return 0;
                                            }
                                        }))))
                
                // Withdraw currency as coins
                .then(Commands.literal("withdraw")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    int amount = IntegerArgumentType.getInteger(context, "amount");
                                    
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
                                    return amount;
                                })))
        );
    }
    
    private static void giveCoinsToPlayer(ServerPlayer player, int amount) {
        int remaining = amount;
        
        // Give platinum coins
        int platinum = remaining / ModItems.PLATINUM_VALUE;
        remaining %= ModItems.PLATINUM_VALUE;
        if (platinum > 0) {
            giveItemToPlayer(player, new ItemStack(ModItems.PLATINUM_COIN.get(), platinum));
        }
        
        // Give gold coins
        int gold = remaining / ModItems.GOLD_VALUE;
        remaining %= ModItems.GOLD_VALUE;
        if (gold > 0) {
            giveItemToPlayer(player, new ItemStack(ModItems.GOLD_COIN.get(), gold));
        }
        
        // Give silver coins
        int silver = remaining / ModItems.SILVER_VALUE;
        remaining %= ModItems.SILVER_VALUE;
        if (silver > 0) {
            giveItemToPlayer(player, new ItemStack(ModItems.SILVER_COIN.get(), silver));
        }
        
        // Give copper coins
        if (remaining > 0) {
            giveItemToPlayer(player, new ItemStack(ModItems.COPPER_COIN.get(), remaining));
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
