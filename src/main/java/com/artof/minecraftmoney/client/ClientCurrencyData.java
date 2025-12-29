package com.artof.minecraftmoney.client;

/**
 * Client-side storage for the player's currency balance.
 * This is synced from the server via network packets.
 */
public class ClientCurrencyData {
    private static int clientCurrency = 0;
    
    public static int getClientCurrency() {
        return clientCurrency;
    }
    
    public static void setClientCurrency(int currency) {
        clientCurrency = currency;
    }
    
    public static void reset() {
        clientCurrency = 0;
    }
}
