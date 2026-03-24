package com.artof.minecraftmoney.network;

import com.artof.minecraftmoney.MinecraftMoney;
import com.artof.minecraftmoney.client.ClientShopData;
import com.artof.minecraftmoney.config.ShopConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record SyncShopDataPacket(List<ShopConfig.ShopEntry> shopItems, double sellMultiplier, boolean bigScreen)
        implements CustomPacketPayload {
    public static final Type<SyncShopDataPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MinecraftMoney.MOD_ID, "sync_shop_data"));

    public static final StreamCodec<FriendlyByteBuf, SyncShopDataPacket> STREAM_CODEC = StreamCodec.of(
            SyncShopDataPacket::write,
            SyncShopDataPacket::read
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncShopDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() ->
                ClientShopData.applySync(packet.shopItems(), packet.sellMultiplier(), packet.bigScreen()));
    }

    public static SyncShopDataPacket fromCurrentConfig() {
        return new SyncShopDataPacket(ShopConfig.getShopItems(), ShopConfig.getSellMultiplier(), ShopConfig.isBigScreen());
    }

    private static void write(FriendlyByteBuf buffer, SyncShopDataPacket packet) {
        buffer.writeDouble(packet.sellMultiplier());
        buffer.writeBoolean(packet.bigScreen());
        buffer.writeVarInt(packet.shopItems().size());
        for (ShopConfig.ShopEntry entry : packet.shopItems()) {
            buffer.writeUtf(entry.itemId());
            buffer.writeLong(entry.price());
            buffer.writeUtf(entry.displayName());
            buffer.writeNullable(entry.componentString(), (buf, value) -> buf.writeUtf(value));
        }
    }

    private static SyncShopDataPacket read(FriendlyByteBuf buffer) {
        double sellMultiplier = buffer.readDouble();
        boolean bigScreen = buffer.readBoolean();
        int size = buffer.readVarInt();
        List<ShopConfig.ShopEntry> shopItems = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            String itemId = buffer.readUtf();
            long price = buffer.readLong();
            String displayName = buffer.readUtf();
            String componentString = buffer.readNullable(buf -> buf.readUtf());
            shopItems.add(new ShopConfig.ShopEntry(itemId, price, displayName, componentString));
        }
        return new SyncShopDataPacket(shopItems, sellMultiplier, bigScreen);
    }
}
