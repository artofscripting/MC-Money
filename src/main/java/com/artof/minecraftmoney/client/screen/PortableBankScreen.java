package com.artof.minecraftmoney.client.screen;

import com.artof.minecraftmoney.MinecraftMoney;
import com.artof.minecraftmoney.client.ClientCurrencyData;
import com.artof.minecraftmoney.item.ModItems;
import com.artof.minecraftmoney.menu.PortableBankMenu;
import com.artof.minecraftmoney.network.PortableBankActionPacket;
import com.artof.minecraftmoney.util.CurrencyFormatter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Screen for the portable bank accessed via the Magic Ledger.
 * Allows withdrawing currency as coins.
 */
public class PortableBankScreen extends AbstractContainerScreen<PortableBankMenu> {
    
    private EditBox amountField;
    private long displayedBalance = 0;
    
    public PortableBankScreen(PortableBankMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 200;
        this.imageHeight = 130;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = (width - imageWidth) / 2;
        int centerY = (height - imageHeight) / 2;
        
        // Amount input field
        amountField = new EditBox(font, centerX + 50, centerY + 50, 100, 16, Component.literal("Amount"));
        amountField.setMaxLength(15);
        amountField.setValue("100");
        amountField.setFilter(s -> s.isEmpty() || s.matches("\\d+"));
        addRenderableWidget(amountField);
        
        // Withdraw button (converts currency to coins in inventory)
        addRenderableWidget(Button.builder(Component.translatable("gui.minecraftmoney.withdraw_coins"), button -> {
            long amount = getAmount();
            if (amount > 0) {
                PacketDistributor.sendToServer(new PortableBankActionPacket(amount));
            }
        }).bounds(centerX + 35, centerY + 75, 130, 20).build());
        
        // Withdraw All button
        addRenderableWidget(Button.builder(Component.literal("Withdraw All"), button -> {
            long balance = ClientCurrencyData.getClientCurrency();
            if (balance > 0) {
                PacketDistributor.sendToServer(new PortableBankActionPacket(balance));
            }
        }).bounds(centerX + 35, centerY + 100, 130, 20).build());
        
        updateBalance();
    }
    
    private long getAmount() {
        try {
            return Long.parseLong(amountField.getValue());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    private void updateBalance() {
        displayedBalance = ClientCurrencyData.getClientCurrency();
    }
    
    @Override
    public void containerTick() {
        super.containerTick();
        updateBalance();
    }
    
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        
        // Draw dark background
        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF1a1a1a);
        guiGraphics.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFF2d2d2d);
        
        // Gold border (like the magic ledger theme)
        guiGraphics.fill(x, y, x + imageWidth, y + 2, 0xFFFFD700);
        guiGraphics.fill(x, y + imageHeight - 2, x + imageWidth, y + imageHeight, 0xFFFFD700);
        guiGraphics.fill(x, y, x + 2, y + imageHeight, 0xFFFFD700);
        guiGraphics.fill(x + imageWidth - 2, y, x + imageWidth, y + imageHeight, 0xFFFFD700);
    }
    
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Title
        guiGraphics.drawCenteredString(font, title, imageWidth / 2, 8, 0xFFD700);
        
        // Current balance
        Component balanceText = Component.translatable("gui.minecraftmoney.wallet_balance", CurrencyFormatter.format(displayedBalance));
        guiGraphics.drawCenteredString(font, balanceText, imageWidth / 2, 26, 0xFFFF55);
        
        // Instructions
        Component infoText = Component.translatable("gui.minecraftmoney.withdraw_info");
        guiGraphics.drawCenteredString(font, infoText, imageWidth / 2, 38, 0xAAAAAA);
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        // Balance tooltip
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        
        Component balanceLabel = Component.translatable("gui.minecraftmoney.wallet_balance", CurrencyFormatter.format(displayedBalance));
        int balanceWidth = font.width(balanceLabel);
        int labelX = (imageWidth - balanceWidth) / 2 + x;
        if (mouseX >= labelX && mouseX <= labelX + balanceWidth && mouseY >= y + 26 && mouseY <= y + 26 + font.lineHeight) {
            guiGraphics.renderTooltip(font, Component.literal(String.format("%,d", displayedBalance)), mouseX, mouseY);
        }
    }
}
