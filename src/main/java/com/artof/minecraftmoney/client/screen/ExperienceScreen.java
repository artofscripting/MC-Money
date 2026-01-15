package com.artof.minecraftmoney.client.screen;

import com.artof.minecraftmoney.MinecraftMoney;
import com.artof.minecraftmoney.client.ClientCurrencyData;
import com.artof.minecraftmoney.menu.ExperienceMenu;
import com.artof.minecraftmoney.network.ExperienceActionPacket;
import com.artof.minecraftmoney.util.CurrencyFormatter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Screen for buying and selling experience with currency.
 */
public class ExperienceScreen extends AbstractContainerScreen<ExperienceMenu> {
    // Price per experience point
    private static final int XP_PRICE = 10; // 10 currency per XP point
    
    private EditBox amountField;
    private long displayedPlayerBalance = 0;
    private int displayedPlayerXP = 0;
    
    public ExperienceScreen(ExperienceMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 200;
        this.imageHeight = 150;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = (width - imageWidth) / 2;
        int centerY = (height - imageHeight) / 2;
        
        // Amount input field
        amountField = new EditBox(font, centerX + 50, centerY + 60, 100, 16, Component.literal("Amount"));
        amountField.setMaxLength(10);
        amountField.setValue("100");
        amountField.setFilter(s -> s.isEmpty() || s.matches("\\d+"));
        addRenderableWidget(amountField);
        
        // Buy XP button (spend currency to gain XP)
        addRenderableWidget(Button.builder(Component.translatable("gui.minecraftmoney.buy_xp"), button -> {
            int amount = getAmount();
            if (amount > 0) {
                PacketDistributor.sendToServer(new ExperienceActionPacket(true, amount));
            }
        }).bounds(centerX + 15, centerY + 90, 80, 20).build());
        
        // Sell XP button (spend XP to gain currency)
        addRenderableWidget(Button.builder(Component.translatable("gui.minecraftmoney.sell_xp"), button -> {
            int amount = getAmount();
            if (amount > 0) {
                PacketDistributor.sendToServer(new ExperienceActionPacket(false, amount));
            }
        }).bounds(centerX + 105, centerY + 90, 80, 20).build());
        
        // Buy All button (buy as much XP as possible)
        addRenderableWidget(Button.builder(Component.literal("Buy All"), button -> {
            long balance = ClientCurrencyData.getClientCurrency();
            int maxXP = (int) Math.min(balance / XP_PRICE, Integer.MAX_VALUE);
            if (maxXP > 0) {
                PacketDistributor.sendToServer(new ExperienceActionPacket(true, maxXP));
            }
        }).bounds(centerX + 15, centerY + 115, 80, 20).build());
        
        // Sell All button (sell all XP)
        addRenderableWidget(Button.builder(Component.literal("Sell All"), button -> {
            if (minecraft != null && minecraft.player != null) {
                int totalXP = getTotalPlayerXP();
                if (totalXP > 0) {
                    PacketDistributor.sendToServer(new ExperienceActionPacket(false, totalXP));
                }
            }
        }).bounds(centerX + 105, centerY + 115, 80, 20).build());
        
        // Update balances
        updateBalances();
    }
    
    private int getAmount() {
        try {
            return Integer.parseInt(amountField.getValue());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    private int getTotalPlayerXP() {
        if (minecraft != null && minecraft.player != null) {
            // Calculate total XP from level and progress
            int level = minecraft.player.experienceLevel;
            float progress = minecraft.player.experienceProgress;
            int xpForLevel = getXpForLevel(level);
            return (int) (getXpNeededForLevel(level) + (xpForLevel * progress));
        }
        return 0;
    }
    
    private int getXpNeededForLevel(int level) {
        // Total XP needed to reach this level from 0
        if (level <= 16) {
            return level * level + 6 * level;
        } else if (level <= 31) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        }
    }
    
    private int getXpForLevel(int level) {
        // XP needed to go from this level to the next
        if (level <= 15) {
            return 2 * level + 7;
        } else if (level <= 30) {
            return 5 * level - 38;
        } else {
            return 9 * level - 158;
        }
    }
    
    private void updateBalances() {
        if (minecraft != null && minecraft.player != null) {
            displayedPlayerBalance = ClientCurrencyData.getClientCurrency();
            displayedPlayerXP = getTotalPlayerXP();
        }
    }
    
    @Override
    public void containerTick() {
        super.containerTick();
        updateBalances();
    }
    
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        
        // Draw dark background
        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF1a1a1a);
        guiGraphics.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFF2d2d2d);
        
        // Border
        guiGraphics.fill(x, y, x + imageWidth, y + 2, 0xFF50C878); // Green border
        guiGraphics.fill(x, y + imageHeight - 2, x + imageWidth, y + imageHeight, 0xFF50C878);
        guiGraphics.fill(x, y, x + 2, y + imageHeight, 0xFF50C878);
        guiGraphics.fill(x + imageWidth - 2, y, x + imageWidth, y + imageHeight, 0xFF50C878);
    }
    
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Title
        guiGraphics.drawCenteredString(font, title, imageWidth / 2, 8, 0x50C878);
        
        // Player currency balance
        Component balanceText = Component.translatable("gui.minecraftmoney.wallet_balance", CurrencyFormatter.format(displayedPlayerBalance));
        guiGraphics.drawString(font, balanceText, 10, 24, 0xFFFF55);
        
        // Player XP
        Component xpText = Component.translatable("gui.minecraftmoney.xp_amount", String.format("%,d", displayedPlayerXP));
        guiGraphics.drawString(font, xpText, 10, 36, 0x55FF55);
        
        // Price info
        Component priceText = Component.translatable("gui.minecraftmoney.xp_price", XP_PRICE);
        guiGraphics.drawString(font, priceText, 10, 48, 0xAAAAAA);
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        // Tooltips for balance display
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        
        // Balance tooltip
        Component balanceLabel = Component.translatable("gui.minecraftmoney.wallet_balance", CurrencyFormatter.format(displayedPlayerBalance));
        int balanceWidth = font.width(balanceLabel);
        if (mouseX >= x + 10 && mouseX <= x + 10 + balanceWidth && mouseY >= y + 24 && mouseY <= y + 24 + font.lineHeight) {
            guiGraphics.renderTooltip(font, Component.literal(String.format("%,d", displayedPlayerBalance)), mouseX, mouseY);
        }
    }
}
