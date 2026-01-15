package com.artof.minecraftmoney.network;

import com.artof.minecraftmoney.MinecraftMoney;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {
    public static final String PROTOCOL_VERSION = "1";
    
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(NetworkHandler::registerPackets);
    }
    
    private static void registerPackets(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MinecraftMoney.MOD_ID)
                .versioned(PROTOCOL_VERSION);
        
        registrar.playToClient(
                SyncCurrencyPacket.TYPE,
                SyncCurrencyPacket.STREAM_CODEC,
                SyncCurrencyPacket::handle
        );
        
        registrar.playToServer(
                BankActionPacket.TYPE,
                BankActionPacket.STREAM_CODEC,
                BankActionPacket::handle
        );
        
        registrar.playToServer(
                ShopBuyPacket.TYPE,
                ShopBuyPacket.STREAM_CODEC,
                ShopBuyPacket::handle
        );
        
        registrar.playToServer(
                ShopSellPacket.TYPE,
                ShopSellPacket.STREAM_CODEC,
                ShopSellPacket::handle
        );
        
        registrar.playToServer(
                ExperienceActionPacket.TYPE,
                ExperienceActionPacket.STREAM_CODEC,
                ExperienceActionPacket::handle
        );
        
        registrar.playToServer(
                PortableBankActionPacket.TYPE,
                PortableBankActionPacket.STREAM_CODEC,
                PortableBankActionPacket::handle
        );
    }
    
    public static void sendToPlayer(ServerPlayer player, SyncCurrencyPacket packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }
    
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MinecraftMoney.MOD_ID, path);
    }
}
