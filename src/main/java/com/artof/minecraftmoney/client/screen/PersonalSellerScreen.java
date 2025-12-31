package com.artof.minecraftmoney.client.screen;

import com.artof.minecraftmoney.MinecraftMoney;
import com.artof.minecraftmoney.client.ClientCurrencyData;
import com.artof.minecraftmoney.menu.PersonalSellerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PersonalSellerScreen extends AbstractContainerScreen<PersonalSellerMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            MinecraftMoney.MOD_ID, "textures/gui/container/generic_54.png");
    
    public PersonalSellerScreen(PersonalSellerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }
    
    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
    
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        
        // Draw dark orange/brown background (seller theme)
        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF3d2a1a);
        guiGraphics.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFF5a3d2d);
        
        // Border
        guiGraphics.fill(x, y, x + imageWidth, y + 2, 0xFF7a5a3d);
        guiGraphics.fill(x, y + imageHeight - 2, x + imageWidth, y + imageHeight, 0xFF7a5a3d);
        guiGraphics.fill(x, y, x + 2, y + imageHeight, 0xFF7a5a3d);
        guiGraphics.fill(x + imageWidth - 2, y, x + imageWidth, y + imageHeight, 0xFF7a5a3d);
        
        // Seller inventory area
        guiGraphics.fill(x + 60, y + 15, x + 116, y + 71, 0x60000000);
        
        // Draw slot backgrounds
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slotX = x + 61 + col * 18;
                int slotY = y + 16 + row * 18;
                guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0x80000000);
            }
        }
        
        // Player inventory area
        guiGraphics.fill(x + 6, y + 82, x + 170, y + 160, 0x40000000);
        
        // Draw player slot backgrounds
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotX = x + 7 + col * 18;
                int slotY = y + 83 + row * 18;
                guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0x80000000);
            }
        }
        // Hotbar
        for (int col = 0; col < 9; col++) {
            int slotX = x + 7 + col * 18;
            int slotY = y + 141;
            guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0x80000000);
        }
    }
    
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Title
        guiGraphics.drawCenteredString(font, title, imageWidth / 2, 5, 0xFFAA55);
        
        // Wallet balance
        int balance = ClientCurrencyData.getClientCurrency();
        String balanceText = "Wallet: " + balance;
        guiGraphics.drawString(font, balanceText, imageWidth - font.width(balanceText) - 8, 5, 0xFFFF55);
        
        // Total earned
        int earned = menu.getBlockEntity().getTotalEarned();
        String earnedText = "Earned: " + earned;
        guiGraphics.drawString(font, earnedText, 8, 73, 0x55FF55);
        
        // Inventory label
        guiGraphics.drawString(font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xCCCCCC);
    }
}
