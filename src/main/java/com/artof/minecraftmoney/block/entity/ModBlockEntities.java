package com.artof.minecraftmoney.block.entity;

import com.artof.minecraftmoney.MinecraftMoney;
import com.artof.minecraftmoney.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = 
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MinecraftMoney.MOD_ID);
    
    public static final Supplier<BlockEntityType<BankBlockEntity>> BANK_BLOCK_ENTITY = BLOCK_ENTITIES.register("bank_block_entity",
            () -> BlockEntityType.Builder.of(BankBlockEntity::new, ModBlocks.BANK_BLOCK.get()).build(null));
    
    public static final Supplier<BlockEntityType<ShopBlockEntity>> SHOP_BLOCK_ENTITY = BLOCK_ENTITIES.register("shop_block_entity",
            () -> BlockEntityType.Builder.of(ShopBlockEntity::new, ModBlocks.SHOP_BLOCK.get()).build(null));
    
    public static final Supplier<BlockEntityType<PersonalSellerBlockEntity>> PERSONAL_SELLER_BLOCK_ENTITY = BLOCK_ENTITIES.register("personal_seller_block_entity",
            () -> BlockEntityType.Builder.of(PersonalSellerBlockEntity::new, ModBlocks.PERSONAL_SELLER_BLOCK.get()).build(null));
    
    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
