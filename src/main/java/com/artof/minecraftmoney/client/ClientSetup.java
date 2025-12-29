package com.artof.minecraftmoney.client;

import com.artof.minecraftmoney.MinecraftMoney;
import com.artof.minecraftmoney.client.screen.BankScreen;
import com.artof.minecraftmoney.client.screen.ShopScreen;
import com.artof.minecraftmoney.menu.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = MinecraftMoney.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.BANK_MENU.get(), BankScreen::new);
        event.register(ModMenuTypes.SHOP_MENU.get(), ShopScreen::new);
    }
}
