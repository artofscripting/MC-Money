package com.artof.minecraftmoney.item;

import com.artof.minecraftmoney.MinecraftMoney;
import com.artof.minecraftmoney.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MinecraftMoney.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MinecraftMoney.MOD_ID);
    
    // Currency values
    public static final int COPPER_VALUE = 1;
    public static final int SILVER_VALUE = 10;
    public static final int GOLD_VALUE = 100;
    public static final int PLATINUM_VALUE = 1000;
    public static final int MILLION_VALUE = 1000000;
    
    // Coin items
    public static final Supplier<CoinItem> COPPER_COIN = ITEMS.register("copper_coin", 
            () -> new CoinItem(new Item.Properties().stacksTo(64), COPPER_VALUE));
    
    public static final Supplier<CoinItem> SILVER_COIN = ITEMS.register("silver_coin", 
            () -> new CoinItem(new Item.Properties().stacksTo(64), SILVER_VALUE));
    
    public static final Supplier<CoinItem> GOLD_COIN = ITEMS.register("gold_coin", 
            () -> new CoinItem(new Item.Properties().stacksTo(64), GOLD_VALUE));
    
    public static final Supplier<CoinItem> PLATINUM_COIN = ITEMS.register("platinum_coin", 
            () -> new CoinItem(new Item.Properties().stacksTo(64), PLATINUM_VALUE));
    
    public static final Supplier<CoinItem> MILLION_COIN = ITEMS.register("million_coin", 
            () -> new CoinItem(new Item.Properties().stacksTo(64), MILLION_VALUE));
    
    // Creative Tab
    public static final Supplier<CreativeModeTab> CURRENCY_TAB = CREATIVE_MODE_TABS.register("currency_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(GOLD_COIN.get()))
                    .title(Component.translatable("itemGroup.minecraftmoney.currency_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(COPPER_COIN.get());
                        output.accept(SILVER_COIN.get());
                        output.accept(GOLD_COIN.get());
                        output.accept(PLATINUM_COIN.get());
                        output.accept(MILLION_COIN.get());
                        output.accept(ModBlocks.BANK_BLOCK_ITEM.get());
                        output.accept(ModBlocks.SHOP_BLOCK_ITEM.get());
                        output.accept(ModBlocks.PERSONAL_SELLER_BLOCK_ITEM.get());
                    })
                    .build());
    
    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
