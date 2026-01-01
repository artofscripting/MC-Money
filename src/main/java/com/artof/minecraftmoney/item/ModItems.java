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
    public static final long COPPER_VALUE = 1L;
    public static final long SILVER_VALUE = 10L;
    public static final long GOLD_VALUE = 100L;
    public static final long PLATINUM_VALUE = 1000L;
    public static final long TEN_THOUSAND_VALUE = 10_000L;
    public static final long MILLION_VALUE = 1_000_000L;
    public static final long TEN_MILLION_VALUE = 10_000_000L;
    public static final long BILLION_VALUE = 1_000_000_000L;
    public static final long TEN_BILLION_VALUE = 10_000_000_000L;
    public static final long TRILLION_VALUE = 1_000_000_000_000L;
    
    // Coin items
    public static final Supplier<CoinItem> COPPER_COIN = ITEMS.register("copper_coin", 
            () -> new CoinItem(new Item.Properties().stacksTo(64), COPPER_VALUE));
    
    public static final Supplier<CoinItem> SILVER_COIN = ITEMS.register("silver_coin", 
            () -> new CoinItem(new Item.Properties().stacksTo(64), SILVER_VALUE));
    
    public static final Supplier<CoinItem> GOLD_COIN = ITEMS.register("gold_coin", 
            () -> new CoinItem(new Item.Properties().stacksTo(64), GOLD_VALUE));
    
    public static final Supplier<CoinItem> PLATINUM_COIN = ITEMS.register("platinum_coin", 
            () -> new CoinItem(new Item.Properties().stacksTo(64), PLATINUM_VALUE));
    
    public static final Supplier<CoinItem> TEN_THOUSAND_COIN = ITEMS.register("ten_thousand_coin", 
            () -> new CoinItem(new Item.Properties().stacksTo(64), TEN_THOUSAND_VALUE));
    
    public static final Supplier<CoinItem> MILLION_COIN = ITEMS.register("million_coin", 
            () -> new CoinItem(new Item.Properties().stacksTo(64), MILLION_VALUE));
    
    public static final Supplier<CoinItem> TEN_MILLION_COIN = ITEMS.register("ten_million_coin", 
            () -> new CoinItem(new Item.Properties().stacksTo(64), TEN_MILLION_VALUE));
    
    public static final Supplier<CoinItem> BILLION_COIN = ITEMS.register("billion_coin", 
            () -> new CoinItem(new Item.Properties().stacksTo(64), BILLION_VALUE));
    
    public static final Supplier<CoinItem> TEN_BILLION_COIN = ITEMS.register("ten_billion_coin", 
            () -> new CoinItem(new Item.Properties().stacksTo(64), TEN_BILLION_VALUE));
    
    public static final Supplier<CoinItem> TRILLION_COIN = ITEMS.register("trillion_coin", 
            () -> new CoinItem(new Item.Properties().stacksTo(64), TRILLION_VALUE));
    
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
                        output.accept(TEN_THOUSAND_COIN.get());
                        output.accept(MILLION_COIN.get());
                        output.accept(TEN_MILLION_COIN.get());
                        output.accept(BILLION_COIN.get());
                        output.accept(TEN_BILLION_COIN.get());
                        output.accept(TRILLION_COIN.get());
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
