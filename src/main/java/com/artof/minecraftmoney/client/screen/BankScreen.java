package com.artof.minecraftmoney.client.screen;

import com.artof.minecraftmoney.MinecraftMoney;
import com.artof.minecraftmoney.client.ClientCurrencyData;
import com.artof.minecraftmoney.menu.BankMenu;
import com.artof.minecraftmoney.network.BankActionPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import com.artof.minecraftmoney.util.CurrencyFormatter;

public class BankScreen extends AbstractContainerScreen<BankMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MinecraftMoney.MOD_ID, "textures/gui/bank.png");
    
    private EditBox amountField;
    private long displayedBankBalance = 0;
    private long displayedPlayerBalance = 0;
    
    public BankScreen(BankMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 120;
    }
    
    @Override
    protected void init() {
        super.init();
        
        int centerX = (width - imageWidth) / 2;
        int centerY = (height - imageHeight) / 2;
        
        // Amount input field
        amountField = new EditBox(font, centerX + 38, centerY + 50, 100, 16, Component.literal("Amount"));
        amountField.setMaxLength(15);
        amountField.setValue("100");
        amountField.setFilter(s -> s.isEmpty() || s.matches("\\d+"));
        addRenderableWidget(amountField);
        
        // Deposit button
        addRenderableWidget(Button.builder(Component.translatable("gui.minecraftmoney.deposit"), button -> {
            long amount = getAmount();
            if (amount > 0) {
                PacketDistributor.sendToServer(new BankActionPacket(menu.getBlockEntity().getBlockPos(), true, amount));
            }
        }).bounds(centerX + 10, centerY + 75, 75, 20).build());
        
        // Withdraw button
        addRenderableWidget(Button.builder(Component.translatable("gui.minecraftmoney.withdraw"), button -> {
            long amount = getAmount();
            if (amount > 0) {
                PacketDistributor.sendToServer(new BankActionPacket(menu.getBlockEntity().getBlockPos(), false, amount));
            }
        }).bounds(centerX + 91, centerY + 75, 75, 20).build());
        
        // Update balances
        updateBalances();
    }
    
    private long getAmount() {
        try {
            return Long.parseLong(amountField.getValue());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    private void updateBalances() {
        if (menu.getBlockEntity() != null && minecraft != null && minecraft.player != null) {
            displayedBankBalance = menu.getBlockEntity().getBalance(minecraft.player);
            displayedPlayerBalance = ClientCurrencyData.getClientCurrency();
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
        guiGraphics.fill(x, y, x + imageWidth, y + 2, 0xFF404040);
        guiGraphics.fill(x, y + imageHeight - 2, x + imageWidth, y + imageHeight, 0xFF404040);
        guiGraphics.fill(x, y, x + 2, y + imageHeight, 0xFF404040);
        guiGraphics.fill(x + imageWidth - 2, y, x + imageWidth, y + imageHeight, 0xFF404040);
    }
    
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Title
        guiGraphics.drawCenteredString(font, title, imageWidth / 2, 8, 0xFFD700);
        
        // Bank balance
        Component bankBalanceText = Component.translatable("gui.minecraftmoney.bank_balance", CurrencyFormatter.format(displayedBankBalance));
        guiGraphics.drawString(font, bankBalanceText, 10, 24, 0x55FF55);
        
        // Player balance (wallet)
        Component playerBalanceText = Component.translatable("gui.minecraftmoney.wallet_balance", CurrencyFormatter.format(displayedPlayerBalance));
        guiGraphics.drawString(font, playerBalanceText, 10, 36, 0xFFFF55);
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        // Check for balance tooltips
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        
        // Bank balance tooltip
        Component bankBalanceLabel = Component.translatable("gui.minecraftmoney.bank_balance", CurrencyFormatter.format(displayedBankBalance));
        int bankBalanceWidth = font.width(bankBalanceLabel);
        if (mouseX >= x + 10 && mouseX <= x + 10 + bankBalanceWidth && mouseY >= y + 24 && mouseY <= y + 24 + font.lineHeight) {
            guiGraphics.renderTooltip(font, Component.literal(String.format("%,d", displayedBankBalance)), mouseX, mouseY);
            return;
        }
        
        // Player balance tooltip
        Component playerBalanceLabel = Component.translatable("gui.minecraftmoney.wallet_balance", CurrencyFormatter.format(displayedPlayerBalance));
        int playerBalanceWidth = font.width(playerBalanceLabel);
        if (mouseX >= x + 10 && mouseX <= x + 10 + playerBalanceWidth && mouseY >= y + 36 && mouseY <= y + 36 + font.lineHeight) {
            guiGraphics.renderTooltip(font, Component.literal(String.format("%,d", displayedPlayerBalance)), mouseX, mouseY);
            return;
        }
        
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (amountField.isFocused()) {
            if (keyCode == 256) { // Escape
                amountField.setFocused(false);
                return true;
            }
            return amountField.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
