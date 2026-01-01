package com.artof.minecraftmoney.network;

import com.artof.minecraftmoney.MinecraftMoney;
import com.artof.minecraftmoney.block.entity.BankBlockEntity;
import com.artof.minecraftmoney.data.PlayerCurrencyData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record BankActionPacket(BlockPos pos, boolean isDeposit, long amount) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<BankActionPacket> TYPE = 
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MinecraftMoney.MOD_ID, "bank_action"));
    
    public static final StreamCodec<FriendlyByteBuf, BankActionPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            BankActionPacket::pos,
            ByteBufCodecs.BOOL,
            BankActionPacket::isDeposit,
            ByteBufCodecs.VAR_LONG,
            BankActionPacket::amount,
            BankActionPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handle(BankActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                BlockEntity blockEntity = serverPlayer.level().getBlockEntity(packet.pos());
                if (blockEntity instanceof BankBlockEntity bankBlockEntity) {
                    if (packet.isDeposit()) {
                        // Deposit: take from player wallet, add to bank
                        if (PlayerCurrencyData.removeCurrency(serverPlayer, packet.amount())) {
                            bankBlockEntity.deposit(serverPlayer, packet.amount());
                        }
                    } else {
                        // Withdraw: take from bank, add to player wallet
                        if (bankBlockEntity.withdraw(serverPlayer, packet.amount())) {
                            PlayerCurrencyData.addCurrency(serverPlayer, packet.amount());
                        }
                    }
                }
            }
        });
    }
}
