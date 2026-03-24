package com.artof.minecraftmoney;

import com.artof.minecraftmoney.block.ModBlocks;
import com.artof.minecraftmoney.block.entity.ModBlockEntities;
import com.artof.minecraftmoney.block.entity.PersonalSellerBlockEntity;
import com.artof.minecraftmoney.command.CurrencyCommand;
import com.artof.minecraftmoney.config.ShopConfig;
import com.artof.minecraftmoney.data.OfflineEarningsManager;
import com.artof.minecraftmoney.data.PlayerCurrencyData;
import com.artof.minecraftmoney.item.ModItems;
import com.artof.minecraftmoney.menu.ModMenuTypes;
import com.artof.minecraftmoney.network.NetworkHandler;
import com.artof.minecraftmoney.network.SyncShopDataPacket;
import com.artof.minecraftmoney.network.SyncCurrencyPacket;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MinecraftMoney.MOD_ID)
public class MinecraftMoney {
    public static final String MOD_ID = "minecraftmoney";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public MinecraftMoney(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing Minecraft Currency Mod");
        
        // Register config
        modContainer.registerConfig(ModConfig.Type.COMMON, ShopConfig.SPEC, "minecraftmoney-shop.toml");
        
        // Register items
        ModItems.register(modEventBus);
        
        // Register blocks
        ModBlocks.register(modEventBus);
        
        // Register block entities
        ModBlockEntities.register(modEventBus);
        
        // Register menus
        ModMenuTypes.register(modEventBus);
        
        // Register data attachments
        PlayerCurrencyData.register(modEventBus);
        
        // Register network handler
        NetworkHandler.register(modEventBus);
        
        // Register capabilities
        modEventBus.addListener(this::registerCapabilities);
        
        // Register common events
        NeoForge.EVENT_BUS.register(this);
    }
    
    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Register IItemHandler capability for PersonalSellerBlockEntity
        // This allows AE2 Export Buses, pipes, and other automation to insert items
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.PERSONAL_SELLER_BLOCK_ENTITY.get(),
                (blockEntity, side) -> new SidedInvWrapper(blockEntity, side)
        );
        
        // Register IEnergyStorage capability for PersonalSellerBlockEntity
        // This allows energy pipes/cables to send FE which is instantly converted to currency
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.PERSONAL_SELLER_BLOCK_ENTITY.get(),
                (blockEntity, side) -> blockEntity.getEnergyStorage()
        );
    }
    
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CurrencyCommand.register(event.getDispatcher());
    }
    
    @SubscribeEvent
    public void onServerStarted(net.neoforged.neoforge.event.server.ServerStartedEvent event) {
        // Load enchanted books from the data-driven registry
        // This includes modded enchantments
        ShopConfig.loadEnchantedBooksFromRegistry(event.getServer().registryAccess());
        ShopConfig.validateShopItems(event.getServer().registryAccess());
    }
    
    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            long currency = PlayerCurrencyData.getCurrency(serverPlayer);
            
            // Check for pending earnings from Personal Seller blocks
            OfflineEarningsManager manager = OfflineEarningsManager.get(serverPlayer.getServer());
            long pendingEarnings = manager.claimEarnings(serverPlayer.getUUID());
            if (pendingEarnings > 0) {
                PlayerCurrencyData.addCurrency(serverPlayer, pendingEarnings);
                currency += pendingEarnings;
                serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§a[Personal Seller] §eYou earned §6" + pendingEarnings + " coins§e while offline!"));
            }
            
            PacketDistributor.sendToPlayer(serverPlayer, new SyncCurrencyPacket(currency));
            NetworkHandler.sendToPlayer(serverPlayer, SyncShopDataPacket.fromCurrentConfig());
        }
    }
    
    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            long currency = PlayerCurrencyData.getCurrency(serverPlayer);
            PacketDistributor.sendToPlayer(serverPlayer, new SyncCurrencyPacket(currency));
        }
    }
    
    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            long currency = PlayerCurrencyData.getCurrency(serverPlayer);
            PacketDistributor.sendToPlayer(serverPlayer, new SyncCurrencyPacket(currency));
        }
    }
    
    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            long currency = PlayerCurrencyData.getCurrency(serverPlayer);
            if (currency > 0) {
                // Drop coins at player location
                dropCoinsAtLocation(serverPlayer, currency);
                // Clear wallet
                PlayerCurrencyData.setCurrency(serverPlayer, 0);
            }
        }
    }
    
    private void dropCoinsAtLocation(net.minecraft.server.level.ServerPlayer player, long amount) {
        // Convert to highest denomination first for fewest coins
        long remaining = amount;
        
        // Trillion coins (1,000,000,000,000 each)
        int trillionCount = (int) (remaining / ModItems.TRILLION_VALUE);
        remaining %= ModItems.TRILLION_VALUE;
        
        // 10 Billion coins (10,000,000,000 each)
        int tenBillionCount = (int) (remaining / ModItems.TEN_BILLION_VALUE);
        remaining %= ModItems.TEN_BILLION_VALUE;
        
        // Billion coins (1,000,000,000 each)
        int billionCount = (int) (remaining / ModItems.BILLION_VALUE);
        remaining %= ModItems.BILLION_VALUE;
        
        // 10 Million coins (10,000,000 each)
        int tenMillionCount = (int) (remaining / ModItems.TEN_MILLION_VALUE);
        remaining %= ModItems.TEN_MILLION_VALUE;
        
        // Million coins (1,000,000 each)
        int millionCount = (int) (remaining / ModItems.MILLION_VALUE);
        remaining %= ModItems.MILLION_VALUE;
        
        // 10K coins (10,000 each)
        int tenThousandCount = (int) (remaining / ModItems.TEN_THOUSAND_VALUE);
        remaining %= ModItems.TEN_THOUSAND_VALUE;
        
        // Platinum coins (1,000 each)
        int platinumCount = (int) (remaining / ModItems.PLATINUM_VALUE);
        remaining %= ModItems.PLATINUM_VALUE;
        
        // Gold coins (100 each)
        int goldCount = (int) (remaining / ModItems.GOLD_VALUE);
        remaining %= ModItems.GOLD_VALUE;
        
        // Silver coins (10 each)
        int silverCount = (int) (remaining / ModItems.SILVER_VALUE);
        remaining %= ModItems.SILVER_VALUE;
        
        // Copper coins (1 each)
        int copperCount = (int) remaining;
        
        // Drop the coins
        dropCoinStacks(player, ModItems.TRILLION_COIN.get(), trillionCount);
        dropCoinStacks(player, ModItems.TEN_BILLION_COIN.get(), tenBillionCount);
        dropCoinStacks(player, ModItems.BILLION_COIN.get(), billionCount);
        dropCoinStacks(player, ModItems.TEN_MILLION_COIN.get(), tenMillionCount);
        dropCoinStacks(player, ModItems.MILLION_COIN.get(), millionCount);
        dropCoinStacks(player, ModItems.TEN_THOUSAND_COIN.get(), tenThousandCount);
        dropCoinStacks(player, ModItems.PLATINUM_COIN.get(), platinumCount);
        dropCoinStacks(player, ModItems.GOLD_COIN.get(), goldCount);
        dropCoinStacks(player, ModItems.SILVER_COIN.get(), silverCount);
        dropCoinStacks(player, ModItems.COPPER_COIN.get(), copperCount);
    }
    
    private void dropCoinStacks(net.minecraft.server.level.ServerPlayer player, net.minecraft.world.item.Item coinItem, int count) {
        while (count > 0) {
            int stackSize = Math.min(count, 64);
            ItemStack stack = new ItemStack(coinItem, stackSize);
            ItemEntity itemEntity = new ItemEntity(
                    player.level(),
                    player.getX(), player.getY() + 0.5, player.getZ(),
                    stack
            );
            itemEntity.setPickUpDelay(40); // 2 second delay
            player.level().addFreshEntity(itemEntity);
            count -= stackSize;
        }
    }
}
