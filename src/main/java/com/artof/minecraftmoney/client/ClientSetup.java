package com.artof.minecraftmoney.client;

import com.artof.minecraftmoney.MinecraftMoney;
import com.artof.minecraftmoney.client.screen.BankScreen;
import com.artof.minecraftmoney.client.screen.ExperienceScreen;
import com.artof.minecraftmoney.client.screen.PersonalSellerScreen;
import com.artof.minecraftmoney.client.screen.PortableBankScreen;
import com.artof.minecraftmoney.client.screen.PortableShopScreen;
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
        event.register(ModMenuTypes.PERSONAL_SELLER_MENU.get(), PersonalSellerScreen::new);
        event.register(ModMenuTypes.EXPERIENCE_MENU.get(), ExperienceScreen::new);
        event.register(ModMenuTypes.PORTABLE_BANK_MENU.get(), PortableBankScreen::new);
        event.register(ModMenuTypes.PORTABLE_SHOP_MENU.get(), PortableShopScreen::new);
    }
}
