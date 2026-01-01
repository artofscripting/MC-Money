package com.artof.minecraftmoney.block;

import com.artof.minecraftmoney.MinecraftMoney;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MinecraftMoney.MOD_ID);
    public static final DeferredRegister<Item> BLOCK_ITEMS = DeferredRegister.create(Registries.ITEM, MinecraftMoney.MOD_ID);
    
    public static final Supplier<BankBlock> BANK_BLOCK = BLOCKS.register("bank_block",
            () -> new BankBlock(BlockBehaviour.Properties.of()
                    .strength(2.0f, 3.0f)));
    
    public static final Supplier<BlockItem> BANK_BLOCK_ITEM = BLOCK_ITEMS.register("bank_block",
            () -> new BlockItem(BANK_BLOCK.get(), new Item.Properties()));
    
    public static final Supplier<ShopBlock> SHOP_BLOCK = BLOCKS.register("shop_block",
            () -> new ShopBlock(BlockBehaviour.Properties.of()
                    .strength(2.0f, 3.0f)));
    
    public static final Supplier<BlockItem> SHOP_BLOCK_ITEM = BLOCK_ITEMS.register("shop_block",
            () -> new BlockItem(SHOP_BLOCK.get(), new Item.Properties()));
    
    public static final Supplier<PersonalSellerBlock> PERSONAL_SELLER_BLOCK = BLOCKS.register("personal_seller_block",
            () -> new PersonalSellerBlock(BlockBehaviour.Properties.of()
                    .strength(2.0f, 3.0f)));
    
    public static final Supplier<BlockItem> PERSONAL_SELLER_BLOCK_ITEM = BLOCK_ITEMS.register("personal_seller_block",
            () -> new BlockItem(PERSONAL_SELLER_BLOCK.get(), new Item.Properties()));
    
    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        BLOCK_ITEMS.register(modEventBus);
    }
}
