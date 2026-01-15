package com.artof.minecraftmoney.client.screen;

import com.artof.minecraftmoney.client.ClientCurrencyData;
import com.artof.minecraftmoney.config.ShopConfig;
import com.artof.minecraftmoney.menu.PortableShopMenu;
import com.artof.minecraftmoney.network.ShopBuyPacket;
import com.artof.minecraftmoney.network.ShopSellPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import com.artof.minecraftmoney.util.CurrencyFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A portable shop screen that works without a shop block.
 * Identical functionality to ShopScreen but for the ShopBookItem.
 */
public class PortableShopScreen extends AbstractContainerScreen<PortableShopMenu> {
    private static final int ROW_HEIGHT = 12;
    private final int itemsPerPage;
    private int currentPage = 0;
    private int maxPages = 1;
    private final List<ShopConfig.ShopEntry> allShopItems = new ArrayList<>();
    private final List<ShopConfig.ShopEntry> filteredItems = new ArrayList<>();
    private final List<Button> actionButtons = new ArrayList<>();
    private EditBox searchBox;
    private String searchQuery = "";
    private final Inventory playerInventory;
    private int hoveredRowIndex = -1;
    
    public PortableShopScreen(PortableShopMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.playerInventory = playerInventory;
        this.imageWidth = 256;
        // Big screen mode: 20 rows, normal: 10 rows
        boolean bigScreen = ShopConfig.isBigScreen();
        this.itemsPerPage = bigScreen ? 20 : 10;
        // Height: base 60 + (rows * ROW_HEIGHT) + footer 20
        this.imageHeight = bigScreen ? 300 : 180;
    }
    
    @Override
    protected void init() {
        super.init();
        
        allShopItems.clear();
        allShopItems.addAll(ShopConfig.getShopItems());
        
        int centerX = (width - imageWidth) / 2;
        int centerY = (height - imageHeight) / 2;
        
        // Search box
        searchBox = new EditBox(font, centerX + 8, centerY + 12, 140, 14, Component.literal("Search"));
        searchBox.setMaxLength(50);
        searchBox.setBordered(true);
        searchBox.setVisible(true);
        searchBox.setTextColor(0xFFFFFF);
        searchBox.setHint(Component.literal("Search... (@mod for mods)"));
        searchBox.setResponder(this::onSearchChanged);
        addRenderableWidget(searchBox);
        
        filterItems();
        rebuildButtons();
    }
    
    private void onSearchChanged(String query) {
        this.searchQuery = query.toLowerCase().trim();
        this.currentPage = 0;
        filterItems();
        rebuildButtons();
    }
    
    private void filterItems() {
        filteredItems.clear();
        if (searchQuery.isEmpty()) {
            filteredItems.addAll(allShopItems);
        } else if (searchQuery.startsWith("@")) {
            // Mod search: filter by mod ID prefix, optionally with item name filter
            String afterAt = searchQuery.substring(1);
            if (!afterAt.isEmpty()) {
                String modPrefix;
                String itemSearch;
                int spaceIndex = afterAt.indexOf(' ');
                if (spaceIndex > 0) {
                    modPrefix = afterAt.substring(0, spaceIndex).toLowerCase();
                    itemSearch = afterAt.substring(spaceIndex + 1).trim().toLowerCase();
                } else {
                    modPrefix = afterAt.toLowerCase();
                    itemSearch = "";
                }
                
                filteredItems.addAll(allShopItems.stream()
                    .filter(entry -> {
                        String itemId = entry.itemId().toLowerCase();
                        int colonIndex = itemId.indexOf(':');
                        if (colonIndex > 0) {
                            String modId = itemId.substring(0, colonIndex);
                            if (!modId.contains(modPrefix)) {
                                return false;
                            }
                            if (!itemSearch.isEmpty()) {
                                return entry.displayName().toLowerCase().contains(itemSearch) ||
                                       itemId.substring(colonIndex + 1).contains(itemSearch);
                            }
                            return true;
                        }
                        return false;
                    })
                    .collect(Collectors.toList()));
            } else {
                filteredItems.addAll(allShopItems);
            }
        } else {
            filteredItems.addAll(allShopItems.stream()
                .filter(entry -> entry.displayName().toLowerCase().contains(searchQuery) ||
                                 entry.itemId().toLowerCase().contains(searchQuery))
                .collect(Collectors.toList()));
        }
        maxPages = Math.max(1, (int) Math.ceil((double) filteredItems.size() / itemsPerPage));
        if (currentPage >= maxPages) {
            currentPage = maxPages - 1;
        }
    }
    
    private int getOriginalIndex(ShopConfig.ShopEntry entry) {
        return allShopItems.indexOf(entry);
    }
    
    private void rebuildButtons() {
        for (Button button : actionButtons) {
            removeWidget(button);
        }
        actionButtons.clear();
        
        int centerX = (width - imageWidth) / 2;
        int centerY = (height - imageHeight) / 2;
        
        int startIndex = currentPage * itemsPerPage;
        for (int i = 0; i < itemsPerPage; i++) {
            int itemIndex = startIndex + i;
            if (itemIndex < filteredItems.size()) {
                ShopConfig.ShopEntry entry = filteredItems.get(itemIndex);
                final int originalIndex = getOriginalIndex(entry);
                int rowY = centerY + 38 + i * ROW_HEIGHT;
                
                Button buyButton = Button.builder(Component.literal("B"), button -> {
                    int quantity = hasShiftDown() ? 64 : 1;
                    PacketDistributor.sendToServer(new ShopBuyPacket(originalIndex, quantity));
                }).bounds(centerX + 220, rowY, 14, ROW_HEIGHT).build();
                addRenderableWidget(buyButton);
                actionButtons.add(buyButton);
                
                Button sellButton = Button.builder(Component.literal("S"), button -> {
                    int quantity = hasShiftDown() ? 64 : 1;
                    PacketDistributor.sendToServer(new ShopSellPacket(originalIndex, quantity));
                }).bounds(centerX + 236, rowY, 14, ROW_HEIGHT).build();
                addRenderableWidget(sellButton);
                actionButtons.add(sellButton);
            }
        }
        
        if (maxPages > 1) {
            int navY = centerY + imageHeight - 18;
            Button prevButton = Button.builder(Component.literal("<"), button -> {
                if (currentPage > 0) {
                    currentPage--;
                    rebuildButtons();
                }
            }).bounds(centerX + 10, navY, 20, 14).build();
            addRenderableWidget(prevButton);
            actionButtons.add(prevButton);
            
            Button nextButton = Button.builder(Component.literal(">"), button -> {
                if (currentPage < maxPages - 1) {
                    currentPage++;
                    rebuildButtons();
                }
            }).bounds(centerX + 226, navY, 20, 14).build();
            addRenderableWidget(nextButton);
            actionButtons.add(nextButton);
        }
    }
    
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        
        // Draw dark purple/black background for the shop book theme
        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF1a1a2e);
        guiGraphics.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFF2d2d44);
        
        // Border
        guiGraphics.fill(x, y, x + imageWidth, y + 2, 0xFF4a4a6a);
        guiGraphics.fill(x, y + imageHeight - 2, x + imageWidth, y + imageHeight, 0xFF4a4a6a);
        guiGraphics.fill(x, y, x + 2, y + imageHeight, 0xFF4a4a6a);
        guiGraphics.fill(x + imageWidth - 2, y, x + imageWidth, y + imageHeight, 0xFF4a4a6a);
        
        // Header row background
        guiGraphics.fill(x + 8, y + 28, x + imageWidth - 8, y + 38, 0x60000000);
        
        int startIndex = currentPage * itemsPerPage;
        for (int i = 0; i < itemsPerPage; i++) {
            int itemIndex = startIndex + i;
            if (itemIndex < filteredItems.size()) {
                int rowY = y + 38 + i * ROW_HEIGHT;
                int bgColor = (i % 2 == 0) ? 0x30000000 : 0x40000000;
                guiGraphics.fill(x + 8, rowY, x + imageWidth - 8, rowY + ROW_HEIGHT, bgColor);
            }
        }
    }
    
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Title
        guiGraphics.drawCenteredString(font, title, imageWidth / 2, 4, 0xAA55FF);
        
        // Balance
        long balance = ClientCurrencyData.getClientCurrency();
        String balanceText = "Balance: " + CurrencyFormatter.format(balance);
        guiGraphics.drawString(font, balanceText, imageWidth - font.width(balanceText) - 10, 18, 0xFFFF55);
        
        // Column headers
        guiGraphics.drawString(font, "Item", 12, 30, 0xCCCCCC);
        guiGraphics.drawString(font, "Buy", 140, 30, 0x55FF55);
        guiGraphics.drawString(font, "Sell", 180, 30, 0xFFAA00);
        
        // Draw shop items
        int startIndex = currentPage * itemsPerPage;
        for (int i = 0; i < itemsPerPage; i++) {
            int itemIndex = startIndex + i;
            if (itemIndex < filteredItems.size()) {
                ShopConfig.ShopEntry entry = filteredItems.get(itemIndex);
                int rowY = 39 + i * ROW_HEIGHT;
                
                String name = entry.displayName();
                if (font.width(name) > 120) {
                    name = font.plainSubstrByWidth(name, 117) + "...";
                }
                guiGraphics.drawString(font, name, 12, rowY + 2, 0xFFFFFF);
                
                long buyPrice = entry.price();
                int buyColor = balance >= buyPrice ? 0x55FF55 : 0xFF5555;
                guiGraphics.drawString(font, CurrencyFormatter.format(buyPrice), 140, rowY + 2, buyColor);
                
                long sellPrice = entry.getSellPrice();
                boolean hasItem = playerHasItem(entry);
                int sellColor = hasItem ? 0xFFAA00 : 0xFF5555;
                guiGraphics.drawString(font, CurrencyFormatter.format(sellPrice), 180, rowY + 2, sellColor);
            }
        }
        
        if (maxPages > 1) {
            String pageText = (currentPage + 1) + "/" + maxPages;
            guiGraphics.drawCenteredString(font, pageText, imageWidth / 2, imageHeight - 16, 0xAAAAAA);
        }
        
        if (filteredItems.isEmpty() && !searchQuery.isEmpty()) {
            guiGraphics.drawCenteredString(font, "No items found", imageWidth / 2, 90, 0xFF5555);
        }
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        hoveredRowIndex = -1;
        
        if (mouseX >= x + 8 && mouseX < x + 180) {
            int startIndex = currentPage * itemsPerPage;
            for (int i = 0; i < itemsPerPage; i++) {
                int itemIndex = startIndex + i;
                if (itemIndex < filteredItems.size()) {
                    int rowY = y + 38 + i * ROW_HEIGHT;
                    if (mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
                        hoveredRowIndex = itemIndex;
                        break;
                    }
                }
            }
        }
        
        // Check for balance tooltip
        long balance = ClientCurrencyData.getClientCurrency();
        String balanceText = "Balance: " + CurrencyFormatter.format(balance);
        int balanceX = x + imageWidth - font.width(balanceText) - 10;
        int balanceY = y + 18;
        if (mouseX >= balanceX && mouseX <= balanceX + font.width(balanceText) && mouseY >= balanceY && mouseY <= balanceY + font.lineHeight) {
            guiGraphics.renderTooltip(font, Component.literal(String.format("%,d", balance)), mouseX, mouseY);
            return;
        }
        
        if (hoveredRowIndex >= 0 && hoveredRowIndex < filteredItems.size()) {
            ShopConfig.ShopEntry entry = filteredItems.get(hoveredRowIndex);
            renderItemTooltip(guiGraphics, entry, mouseX, mouseY);
        } else {
            renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }
    
    private void renderItemTooltip(GuiGraphics guiGraphics, ShopConfig.ShopEntry entry, int mouseX, int mouseY) {
        ItemStack stack;
        if (minecraft != null && minecraft.level != null) {
            stack = entry.createItemStackWithRegistry(1, minecraft.level.registryAccess());
        } else {
            stack = entry.createItemStack(1);
        }
        if (stack.isEmpty()) return;
        
        ResourceLocation itemLoc = ResourceLocation.tryParse(entry.itemId());
        if (itemLoc == null) return;
        
        List<Component> tooltipLines = new ArrayList<>();
        
        if (entry.hasComponentData()) {
            tooltipLines.add(Component.literal("       ").append(Component.literal(entry.displayName()).withStyle(ChatFormatting.WHITE)));
        } else {
            tooltipLines.add(Component.literal("       ").append(stack.getHoverName().copy().withStyle(ChatFormatting.WHITE)));
        }
        
        String modId = itemLoc.getNamespace();
        tooltipLines.add(Component.literal("       ").append(Component.literal(modId).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC)));
        
        tooltipLines.add(Component.empty());
        
        long buyPrice = entry.price();
        long sellPrice = entry.getSellPrice();
        long balance = ClientCurrencyData.getClientCurrency();
        
        ChatFormatting buyColor = balance >= buyPrice ? ChatFormatting.GREEN : ChatFormatting.RED;
        tooltipLines.add(Component.literal("Buy Price: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(buyPrice)).withStyle(buyColor)));
        
        boolean hasItem = playerHasItem(entry);
        ChatFormatting sellColor = hasItem ? ChatFormatting.GOLD : ChatFormatting.RED;
        tooltipLines.add(Component.literal("Sell Price: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(sellPrice)).withStyle(sellColor)));
        
        int inventoryCount = getPlayerItemCount(entry);
        tooltipLines.add(Component.empty());
        tooltipLines.add(Component.literal("In Inventory: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(inventoryCount)).withStyle(ChatFormatting.AQUA)));
        
        tooltipLines.add(Component.empty());
        tooltipLines.add(Component.literal("Shift+Click to buy/sell 64").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        
        int tooltipWidth = 0;
        for (Component line : tooltipLines) {
            int lineWidth = font.width(line);
            if (lineWidth > tooltipWidth) {
                tooltipWidth = lineWidth;
            }
        }
        int fullTooltipWidth = tooltipWidth + 8;
        
        int tooltipX = mouseX + 12;
        int tooltipY = mouseY - 12;
        int tooltipHeight = tooltipLines.size() * 10 + 8;
        
        if (tooltipX + fullTooltipWidth > width) {
            tooltipX = mouseX - fullTooltipWidth - 4;
        }
        if (tooltipY + tooltipHeight + 6 > height) {
            tooltipY = height - tooltipHeight - 6;
        }
        if (tooltipY < 4) {
            tooltipY = 4;
        }
        
        guiGraphics.renderTooltip(font, tooltipLines, java.util.Optional.empty(), mouseX, mouseY);
        
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 400);
        guiGraphics.renderFakeItem(stack, tooltipX + 5, tooltipY + 5);
        guiGraphics.pose().popPose();
    }
    
    private int getPlayerItemCount(ShopConfig.ShopEntry entry) {
        if (minecraft == null || minecraft.player == null) return 0;
        
        int count = 0;
        var inv = minecraft.player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack slotStack = inv.getItem(i);
            if (entry.matches(slotStack)) {
                count += slotStack.getCount();
            }
        }
        return count;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchBox.isFocused()) {
            if (keyCode == 256) {
                searchBox.setFocused(false);
                return true;
            }
            return searchBox.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0 && currentPage > 0) {
            currentPage--;
            rebuildButtons();
            return true;
        } else if (scrollY < 0 && currentPage < maxPages - 1) {
            currentPage++;
            rebuildButtons();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    
    private boolean playerHasItem(ShopConfig.ShopEntry entry) {
        if (minecraft == null || minecraft.player == null) return false;
        
        var inv = minecraft.player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (entry.matches(inv.getItem(i))) {
                return true;
            }
        }
        return false;
    }
}
