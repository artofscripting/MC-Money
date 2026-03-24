package com.artof.minecraftmoney.block;

import com.artof.minecraftmoney.block.entity.PersonalSellerBlockEntity;
import com.artof.minecraftmoney.block.entity.ModBlockEntities;
import com.artof.minecraftmoney.network.NetworkHandler;
import com.artof.minecraftmoney.network.SyncShopDataPacket;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class PersonalSellerBlock extends BaseEntityBlock {
    public static final MapCodec<PersonalSellerBlock> CODEC = simpleCodec(PersonalSellerBlock::new);
    
    public PersonalSellerBlock(Properties properties) {
        super(properties);
    }
    
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
    
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PersonalSellerBlockEntity(pos, state);
    }
    
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.PERSONAL_SELLER_BLOCK_ENTITY.get(), PersonalSellerBlockEntity::serverTick);
    }
    
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof Player player) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof PersonalSellerBlockEntity sellerEntity) {
                sellerEntity.setOwner(player.getUUID(), player.getName().getString());
            }
        }
    }
    
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof PersonalSellerBlockEntity sellerEntity) {
                // Only owner can access
                if (sellerEntity.isOwner(player)) {
                    NetworkHandler.sendToPlayer(serverPlayer, SyncShopDataPacket.fromCurrentConfig());
                    serverPlayer.openMenu(sellerEntity, pos);
                } else {
                    player.sendSystemMessage(Component.literal("This seller belongs to " + sellerEntity.getOwnerName()));
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof PersonalSellerBlockEntity sellerEntity) {
                Containers.dropContents(level, pos, sellerEntity);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
