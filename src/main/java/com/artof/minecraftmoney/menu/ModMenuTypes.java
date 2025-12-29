package com.artof.minecraftmoney.menu;

import com.artof.minecraftmoney.MinecraftMoney;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = 
            DeferredRegister.create(Registries.MENU, MinecraftMoney.MOD_ID);
    
    public static final Supplier<MenuType<BankMenu>> BANK_MENU = MENUS.register("bank_menu",
            () -> IMenuTypeExtension.create(BankMenu::new));
    
    public static final Supplier<MenuType<ShopMenu>> SHOP_MENU = MENUS.register("shop_menu",
            () -> IMenuTypeExtension.create(ShopMenu::new));
    
    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
