# Minecraft Money

A NeoForge mod for Minecraft 1.21.1 that adds a complete currency system with coins, a personal bank, and a configurable shop.

## Screens

![Shop Interface](https://github.com/artofscripting/MC-Money/blob/main/screens/Screenshot%202025-12-29%20132154.png?raw=true)

![Bank Interface](https://github.com/artofscripting/MC-Money/blob/main/screens/Screenshot%202025-12-29%20132223.png?raw=true)

## Features

### 💰 Coins
Ten tiers of coins with different values that can be picked up, deposited, and traded:

| Coin | Value | Description |
|------|-------|-------------|
| Copper Coin | 1 | Basic currency unit |
| Silver Coin | 10 | Worth 10 copper coins |
| Gold Coin | 100 | Worth 100 copper coins |
| Platinum Coin | 1,000 | Worth 1,000 copper coins |
| Ten Thousand Coin | 10,000 | For larger transactions |
| Million Coin | 1,000,000 | High-value currency |
| Ten Million Coin | 10,000,000 | Very high-value currency |
| Billion Coin | 1,000,000,000 | Massive value |
| Ten Billion Coin | 10,000,000,000 | Extreme value |
| Trillion Coin | 1,000,000,000,000 | Maximum denomination |

- **Right-click** any coin to deposit it into your wallet
- Coins stack up to 64
- On death, your wallet balance drops as coins (using optimal denominations)
- Currency supports values up to trillions

### 🏦 Personal Bank
A secure block where you can store currency separately from your wallet.

- Deposit from wallet to bank for safekeeping
- Withdraw from bank back to wallet
- Bank balance persists and is separate from your wallet
- Shift-click for bulk transfers
- Supports up to 15-digit amounts

### 🏪 Shop
A configurable shop block for buying and selling items.

- **350+ items** available by default (ATM10 modpack template included)
- **Buy items** using your wallet balance
- **Sell items** back at 50% of buy price
- **Search bar** to quickly find items
- **@mod search** - Type `@modid` to filter by mod (e.g., `@mekanism`)
- **Advanced @mod search** - Type `@modid keywords` to filter by mod AND item name (e.g., `@mek energy cube`)
- **Scroll wheel navigation** - Use mouse scroll to navigate between pages
- **Item tooltips** - Hover over any item row to see item icon, prices, and inventory count
- **Big screen mode** - Optional config to show 20 items per page instead of 10
- **Item validation** - Invalid items are automatically skipped with warnings
- **Shift-click** to buy/sell 64 at once
- Prices shown in green (affordable) or red (too expensive)
- Sell prices shown in orange (have item) or red (don't have item)
- 10 items per page with navigation (20 in big screen mode)

### 🤖 Personal Seller
An automation-friendly block for passive income.

- Place items in the 9 inventory slots
- Items are automatically sold at shop prices when the block ticks
- Earnings are stored and credited to your wallet when you log in
- **Works offline** - Earnings accumulate even when you're not online
- **Automation compatible** - Supports hoppers, pipes, and mod item handlers (AE2 Export Bus, etc.)
- Owner-locked - Only you can access your Personal Seller

## Commands

### Player Commands
| Command | Description |
|---------|-------------|
| /currency | Check your current wallet balance |
| /currency pay <player> <amount> | Send currency to another player |
| /currency withdraw <amount> | Convert wallet balance to coins |

### Admin Commands (Permission Level 2+)
| Command | Description |
|---------|-------------|
| /currency balance <player> | Check another player's balance |
| /currency add <player> <amount> | Add currency to a player |
| /currency remove <player> <amount> | Remove currency from a player |
| /currency set <player> <amount> | Set a player's balance to a specific amount |

## Crafting Recipes

### Personal Bank
```
┌─────────┬─────────┬─────────┐
│  Iron   │  Gold   │  Iron   │
│  Ingot  │  Ingot  │  Ingot  │
├─────────┼─────────┼─────────┤
│  Iron   │         │  Iron   │
│  Ingot  │  Chest  │  Ingot  │
├─────────┼─────────┼─────────┤
│  Iron   │  Iron   │  Iron   │
│  Ingot  │  Ingot  │  Ingot  │
└─────────┴─────────┴─────────┘
```
- 7 Iron Ingots + 1 Gold Ingot + 1 Chest

### Shop
```
┌─────────┬─────────┬─────────┐
│         │         │         │
│ Emerald │ Emerald │ Emerald │
├─────────┼─────────┼─────────┤
│         │         │         │
│ Planks  │  Chest  │ Planks  │
├─────────┼─────────┼─────────┤
│         │         │         │
│ Planks  │ Planks  │ Planks  │
└─────────┴─────────┴─────────┘
```
- 3 Emeralds + 5 Planks (any wood type) + 1 Chest

### Personal Seller
```
┌─────────┬─────────┬─────────┐
│         │         │         │
│ Hopper  │ Hopper  │ Hopper  │
├─────────┼─────────┼─────────┤
│  Iron   │         │  Iron   │
│  Ingot  │  Chest  │  Ingot  │
├─────────┼─────────┼─────────┤
│  Iron   │  Iron   │  Iron   │
│  Ingot  │  Ingot  │  Ingot  │
└─────────┴─────────┴─────────┘
```
- 3 Hoppers + 5 Iron Ingots + 1 Chest

## Configuration

The shop is fully configurable via `config/minecraftmoney-shop.toml`:

```toml
# Sell price multiplier (0.5 = 50% of buy price)
sellPriceMultiplier = 0.5

# Enable big screen mode (20 rows instead of 10)
bigScreen = false

# Shop items format: "modid:item_id,price,Display Name"
shopItems = [
    "minecraft:diamond,500,Diamond",
    "minecraft:netherite_ingot,2000,Netherite Ingot",
    # ... more items
]
```

### Adding Custom Items
Add entries to the `shopItems` array:
```toml
shopItems = [
    "minecraft:diamond,500,Diamond",
    "mymod:custom_item,100,Custom Item"
]
```

### ATM10 Modpack Template
An [`atm10-shop-template.toml`](https://raw.githubusercontent.com/artofscripting/MC-Money/refs/heads/main/atm10-shop-template.toml) file is included with 350+ items from popular mods including:
- Mekanism, Applied Energistics 2, Thermal Series
- Ars Nouveau, Botania, Create
- And many more!

Copy the contents to your shop config to use it.

## Installation

1. Install [NeoForge](https://neoforged.net/) for Minecraft 1.21.1
2. Download the mod JAR file
3. Place it in your `mods` folder
4. Launch Minecraft

## Building from Source

```bash
./gradlew build
```

The built JAR will be in `build/libs/`.

## Development

```bash
# Run the client
./gradlew runClient

# Run the server
./gradlew runServer
```

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.77+

## License

MIT License

## Credits

Created by ArtOf
