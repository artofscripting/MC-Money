package com.artof.minecraftmoney.network;

import com.artof.minecraftmoney.MinecraftMoney;
import com.artof.minecraftmoney.client.ClientCurrencyData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncCurrencyPacket(int currency) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncCurrencyPacket> TYPE = 
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MinecraftMoney.MOD_ID, "sync_currency"));
    
    public static final StreamCodec<FriendlyByteBuf, SyncCurrencyPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SyncCurrencyPacket::currency,
            SyncCurrencyPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(SyncCurrencyPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientCurrencyData.setClientCurrency(packet.currency());
        });
    }
}
