package com.artof.minecraftmoney.client.screen;

import com.artof.minecraftmoney.MinecraftMoney;
import com.artof.minecraftmoney.client.ClientCurrencyData;
import com.artof.minecraftmoney.client.ClientShopData;
import com.artof.minecraftmoney.config.ShopConfig;
import com.artof.minecraftmoney.menu.ShopMenu;
import com.artof.minecraftmoney.network.ShopBuyPacket;
import com.artof.minecraftmoney.network.ShopSellPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import com.artof.minecraftmoney.util.CurrencyFormatter;

import java.util.ArrayList;
import java.util.List;

public class ShopScreen extends AbstractContainerScreen<ShopMenu> {
    private static final int ROW_HEIGHT = 12;
    private final int itemsPerPage;
    private int currentPage = 0;
    private int maxPages = 1;
    private final List<ShopConfig.ShopEntry> allShopItems = new ArrayList<>();
    private final List<ShopConfig.ShopEntry> filteredItems = new ArrayList<>();
    private final List<Integer> filteredItemIndices = new ArrayList<>();
    private final List<Button> actionButtons = new ArrayList<>();
    private EditBox searchBox;
    private String searchQuery = "";
    private final Inventory playerInventory;
    private int hoveredRowIndex = -1;
    private int lastShopRevision = -1;
    
    public ShopScreen(ShopMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.playerInventory = playerInventory;
        this.imageWidth = 256;
        // Big screen mode: 20 rows, normal: 10 rows
        boolean bigScreen = ClientShopData.isBigScreen();
        this.itemsPerPage = bigScreen ? 20 : 10;
        // Height: base 60 + (rows * ROW_HEIGHT) + footer 20
        this.imageHeight = bigScreen ? 300 : 180;
    }
    
    @Override
    protected void init() {
        super.init();
        refreshShopItems();
        
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

    @Override
    protected void containerTick() {
        super.containerTick();
        if (lastShopRevision != ClientShopData.getSyncRevision()) {
            refreshShopItems();
            filterItems();
            rebuildButtons();
        }
    }

    private void refreshShopItems() {
        allShopItems.clear();
        allShopItems.addAll(ClientShopData.getShopItems());
        lastShopRevision = ClientShopData.getSyncRevision();
    }
    
    private void onSearchChanged(String query) {
        this.searchQuery = query.toLowerCase().trim();
        this.currentPage = 0;
        filterItems();
        rebuildButtons();
    }
    
    private void filterItems() {
        filteredItems.clear();
        filteredItemIndices.clear();
        if (searchQuery.isEmpty()) {
            for (int i = 0; i < allShopItems.size(); i++) {
                filteredItems.add(allShopItems.get(i));
                filteredItemIndices.add(i);
            }
        } else if (searchQuery.startsWith("@")) {
            // Mod search: filter by mod ID prefix, optionally with item name filter
            // Format: @modid or @modid searchterms
            String afterAt = searchQuery.substring(1); // Remove the @ prefix
            if (!afterAt.isEmpty()) {
                // Split into mod prefix and optional item search
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
                
                for (int i = 0; i < allShopItems.size(); i++) {
                    ShopConfig.ShopEntry entry = allShopItems.get(i);
                    String itemId = entry.itemId().toLowerCase();
                    int colonIndex = itemId.indexOf(':');
                    if (colonIndex <= 0) {
                        continue;
                    }

                    String modId = itemId.substring(0, colonIndex);
                    if (!modId.contains(modPrefix)) {
                        continue;
                    }

                    if (!itemSearch.isEmpty() &&
                            !entry.displayName().toLowerCase().contains(itemSearch) &&
                            !itemId.substring(colonIndex + 1).contains(itemSearch)) {
                        continue;
                    }

                    filteredItems.add(entry);
                    filteredItemIndices.add(i);
                }
            } else {
                // Just "@" with nothing after - show all items
                for (int i = 0; i < allShopItems.size(); i++) {
                    filteredItems.add(allShopItems.get(i));
                    filteredItemIndices.add(i);
                }
            }
        } else {
            // Regular search: filter by item name or full item ID
            for (int i = 0; i < allShopItems.size(); i++) {
                ShopConfig.ShopEntry entry = allShopItems.get(i);
                if (entry.displayName().toLowerCase().contains(searchQuery) ||
                        entry.itemId().toLowerCase().contains(searchQuery)) {
                    filteredItems.add(entry);
                    filteredItemIndices.add(i);
                }
            }
        }
        maxPages = Math.max(1, (int) Math.ceil((double) filteredItems.size() / itemsPerPage));
        if (currentPage >= maxPages) {
            currentPage = maxPages - 1;
        }
    }
    
    private void rebuildButtons() {
        // Remove old buttons
        for (Button button : actionButtons) {
            removeWidget(button);
        }
        actionButtons.clear();
        
        int centerX = (width - imageWidth) / 2;
        int centerY = (height - imageHeight) / 2;
        
        // Add buy and sell buttons for current page
        int startIndex = currentPage * itemsPerPage;
        for (int i = 0; i < itemsPerPage; i++) {
            int itemIndex = startIndex + i;
            if (itemIndex < filteredItems.size()) {
                ShopConfig.ShopEntry entry = filteredItems.get(itemIndex);
                final int originalIndex = filteredItemIndices.get(itemIndex);
                int rowY = centerY + 38 + i * ROW_HEIGHT;
                
                // Buy button - shift click buys a stack
                Button buyButton = Button.builder(Component.literal("B"), button -> {
                    int quantity = hasShiftDown() ? 64 : 1;
                    PacketDistributor.sendToServer(new ShopBuyPacket(originalIndex, quantity));
                }).bounds(centerX + 220, rowY, 14, ROW_HEIGHT).build();
                addRenderableWidget(buyButton);
                actionButtons.add(buyButton);
                
                // Sell button - shift click sells a stack
                Button sellButton = Button.builder(Component.literal("S"), button -> {
                    int quantity = hasShiftDown() ? 64 : 1;
                    PacketDistributor.sendToServer(new ShopSellPacket(originalIndex, quantity));
                }).bounds(centerX + 236, rowY, 14, ROW_HEIGHT).build();
                addRenderableWidget(sellButton);
                actionButtons.add(sellButton);
            }
        }
        
        // Navigation buttons
        if (maxPages > 1) {
            int navY = centerY + imageHeight - 18;
            // Previous page
            Button prevButton = Button.builder(Component.literal("<"), button -> {
                if (currentPage > 0) {
                    currentPage--;
                    rebuildButtons();
                }
            }).bounds(centerX + 10, navY, 20, 14).build();
            addRenderableWidget(prevButton);
            actionButtons.add(prevButton);
            
            // Next page
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
        
        // Draw green background
        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF1a3d1a);
        guiGraphics.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFF2d5a2d);
        
        // Border
        guiGraphics.fill(x, y, x + imageWidth, y + 2, 0xFF3d7a3d);
        guiGraphics.fill(x, y + imageHeight - 2, x + imageWidth, y + imageHeight, 0xFF3d7a3d);
        guiGraphics.fill(x, y, x + 2, y + imageHeight, 0xFF3d7a3d);
        guiGraphics.fill(x + imageWidth - 2, y, x + imageWidth, y + imageHeight, 0xFF3d7a3d);
        
        // Header row background
        guiGraphics.fill(x + 8, y + 28, x + imageWidth - 8, y + 38, 0x60000000);
        
        // Draw item rows
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
        guiGraphics.drawCenteredString(font, title, imageWidth / 2, 4, 0x55FF55);
        
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
                
                // Item name (truncate if too long)
                String name = entry.displayName();
                if (font.width(name) > 120) {
                    name = font.plainSubstrByWidth(name, 117) + "...";
                }
                guiGraphics.drawString(font, name, 12, rowY + 2, 0xFFFFFF);
                
                // Buy price
                long buyPrice = entry.price();
                int buyColor = balance >= buyPrice ? 0x55FF55 : 0xFF5555;
                guiGraphics.drawString(font, CurrencyFormatter.format(buyPrice), 140, rowY + 2, buyColor);
                
                // Sell price - check if player has item in inventory
                long sellPrice = ClientShopData.getSellPrice(entry.price());
                boolean hasItem = playerHasItem(entry);
                int sellColor = hasItem ? 0xFFAA00 : 0xFF5555;
                guiGraphics.drawString(font, CurrencyFormatter.format(sellPrice), 180, rowY + 2, sellColor);
            }
        }
        
        // Page indicator
        if (maxPages > 1) {
            String pageText = (currentPage + 1) + "/" + maxPages;
            guiGraphics.drawCenteredString(font, pageText, imageWidth / 2, imageHeight - 16, 0xAAAAAA);
        }
        
        // No results message
        if (filteredItems.isEmpty() && !searchQuery.isEmpty()) {
            guiGraphics.drawCenteredString(font, "No items found", imageWidth / 2, 90, 0xFF5555);
        }
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        // Calculate hovered row
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        hoveredRowIndex = -1;
        
        // Check if mouse is within the item row area (excluding button columns at the right)
        if (mouseX >= x + 8 && mouseX < x + 180) {
            int startIndex = currentPage * itemsPerPage;
            for (int i = 0; i < itemsPerPage; i++) {
                int itemIndex = startIndex + i;
                if (itemIndex < filteredItems.size()) {
                    int rowY = y + 38 + i * ROW_HEIGHT;
                    if (mouseY >= rowY && mouseY < rowY + ROW_HEIGHT) {
                        hoveredRowIndex = filteredItemIndices.get(itemIndex);
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
        
        // Render item tooltip if hovering over a row
        if (hoveredRowIndex >= 0 && hoveredRowIndex < allShopItems.size()) {
            ShopConfig.ShopEntry entry = allShopItems.get(hoveredRowIndex);
            renderItemTooltip(guiGraphics, entry, mouseX, mouseY);
        } else {
            renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }
    
    private void renderItemTooltip(GuiGraphics guiGraphics, ShopConfig.ShopEntry entry, int mouseX, int mouseY) {
        // Create the item stack using the entry's createItemStackWithRegistry method
        // which handles component data for enchanted books, potions, etc.
        // On the client, we can access the registry from the level
        ItemStack stack;
        if (minecraft != null && minecraft.level != null) {
            stack = entry.createItemStackWithRegistry(1, minecraft.level.registryAccess());
        } else {
            stack = entry.createItemStack(1);
        }
        if (stack.isEmpty()) return;
        
        ResourceLocation itemLoc = ResourceLocation.tryParse(entry.itemId());
        if (itemLoc == null) return;
        
        // Build tooltip lines - add spacing at start for item icon
        List<Component> tooltipLines = new ArrayList<>();
        
        // Item name - for enchanted books and potions, use the display name from config
        // as it may be more descriptive than the generic item name
        if (entry.hasComponentData()) {
            tooltipLines.add(Component.literal("       ").append(Component.literal(entry.displayName()).withStyle(ChatFormatting.WHITE)));
        } else {
            tooltipLines.add(Component.literal("       ").append(stack.getHoverName().copy().withStyle(ChatFormatting.WHITE)));
        }
        
        // Mod ID - add spacing to not overlap with icon
        String modId = itemLoc.getNamespace();
        tooltipLines.add(Component.literal("       ").append(Component.literal(modId).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC)));
        
        // Empty line
        tooltipLines.add(Component.empty());
        
        // Pricing info
        long buyPrice = entry.price();
        long sellPrice = ClientShopData.getSellPrice(entry.price());
        long balance = ClientCurrencyData.getClientCurrency();
        
        ChatFormatting buyColor = balance >= buyPrice ? ChatFormatting.GREEN : ChatFormatting.RED;
        tooltipLines.add(Component.literal("Buy Price: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(buyPrice)).withStyle(buyColor)));
        
        boolean hasItem = playerHasItem(entry);
        ChatFormatting sellColor = hasItem ? ChatFormatting.GOLD : ChatFormatting.RED;
        tooltipLines.add(Component.literal("Sell Price: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(sellPrice)).withStyle(sellColor)));
        
        // Inventory count
        int inventoryCount = getPlayerItemCount(entry);
        tooltipLines.add(Component.empty());
        tooltipLines.add(Component.literal("In Inventory: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(inventoryCount)).withStyle(ChatFormatting.AQUA)));
        
        // Shift-click hint
        tooltipLines.add(Component.empty());
        tooltipLines.add(Component.literal("Shift+Click to buy/sell 64").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        
        // Calculate tooltip width for positioning (add padding that Minecraft uses)
        int tooltipWidth = 0;
        for (Component line : tooltipLines) {
            int lineWidth = font.width(line);
            if (lineWidth > tooltipWidth) {
                tooltipWidth = lineWidth;
            }
        }
        // Minecraft adds 3 pixels padding on each side
        int fullTooltipWidth = tooltipWidth + 8;
        
        // Calculate tooltip position (matches Minecraft's internal logic more closely)
        int tooltipX = mouseX + 12;
        int tooltipY = mouseY - 12;
        int tooltipHeight = tooltipLines.size() * 10 + 8;
        
        // Adjust if tooltip would go off right edge - use same threshold as Minecraft
        if (tooltipX + fullTooltipWidth > width) {
            tooltipX = mouseX - fullTooltipWidth - 4;
        }
        // Adjust if tooltip would go off bottom
        if (tooltipY + tooltipHeight + 6 > height) {
            tooltipY = height - tooltipHeight - 6;
        }
        // Adjust if tooltip would go off top
        if (tooltipY < 4) {
            tooltipY = 4;
        }
        
        // Render the tooltip first
        guiGraphics.renderTooltip(font, tooltipLines, java.util.Optional.empty(), mouseX, mouseY);
        
        // Render item icon on top of the tooltip at high Z level
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
            if (keyCode == 256) { // Escape
                searchBox.setFocused(false);
                return true;
            }
            return searchBox.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // scrollY > 0 means scroll up (previous page), scrollY < 0 means scroll down (next page)
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
        
        // Check both main inventory and hotbar from the live player
        var inv = minecraft.player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (entry.matches(inv.getItem(i))) {
                return true;
            }
        }
        return false;
    }
}
