# Minecraft Money Mod

## Project Structure
- `src/main/java/` - Java source code
- `src/main/resources/` - Assets (textures, models, lang files)
- `src/main/templates/` - Mod metadata templates

## Key Classes
- `MinecraftMoney.java` - Main mod entry point
- `item/ModItems.java` - Item registration
- `item/CoinItem.java` - Coin item with deposit functionality
- `data/PlayerCurrencyData.java` - Player currency storage via attachments
- `command/CurrencyCommand.java` - Currency management commands

## Building
Run `./gradlew build` to build the mod.

## Running
Run `./gradlew runClient` to test in development.

## Textures Required
Create 16x16 PNG textures in `src/main/resources/assets/minecraftmoney/textures/item/`:
- copper_coin.png
- silver_coin.png
- gold_coin.png
- platinum_coin.png
- money_bag.png
