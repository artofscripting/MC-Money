package com.artof.minecraftmoney.client;

/**
 * Client-side storage for the player's currency balance.
 * This is synced from the server via network packets.
 */
public class ClientCurrencyData {
    private static long clientCurrency = 0;
    
    public static long getClientCurrency() {
        return clientCurrency;
    }
    
    public static void setClientCurrency(long currency) {
        clientCurrency = currency;
    }
    
    public static void reset() {
        clientCurrency = 0;
    }
}
