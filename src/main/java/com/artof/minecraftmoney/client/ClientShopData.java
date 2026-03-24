package com.artof.minecraftmoney.client;

import com.artof.minecraftmoney.config.ShopConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side copy of the server-authoritative shop data.
 * Falls back to the local config before the first sync arrives.
 */
public final class ClientShopData {
    private static final List<ShopConfig.ShopEntry> syncedShopItems = new ArrayList<>();
    private static boolean hasServerSync = false;
    private static double sellMultiplier = 0.5;
    private static boolean bigScreen = false;
    private static int syncRevision = 0;

    private ClientShopData() {
    }

    public static void applySync(List<ShopConfig.ShopEntry> shopItems, double syncedSellMultiplier, boolean syncedBigScreen) {
        syncedShopItems.clear();
        syncedShopItems.addAll(shopItems);
        sellMultiplier = syncedSellMultiplier;
        bigScreen = syncedBigScreen;
        hasServerSync = true;
        syncRevision++;
    }

    public static List<ShopConfig.ShopEntry> getShopItems() {
        if (hasServerSync) {
            return List.copyOf(syncedShopItems);
        }
        return ShopConfig.getShopItems();
    }

    public static boolean isBigScreen() {
        return hasServerSync ? bigScreen : ShopConfig.isBigScreen();
    }

    public static long getSellPrice(long buyPrice) {
        double multiplier = hasServerSync ? sellMultiplier : ShopConfig.getSellMultiplier();
        return (long) Math.floor(buyPrice * multiplier);
    }

    public static int getSyncRevision() {
        return syncRevision;
    }
}
