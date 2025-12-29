package com.artof.minecraftmoney.config;

import com.artof.minecraftmoney.MinecraftMoney;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = "minecraftmoney", bus = EventBusSubscriber.Bus.MOD)
public class ShopConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(MinecraftMoney.MOD_ID);
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    
    private static final ModConfigSpec.ConfigValue<List<? extends String>> SHOP_ITEMS;
    private static final ModConfigSpec.DoubleValue SELL_PRICE_MULTIPLIER;
    
    public static final ModConfigSpec SPEC;
    
    // Parsed shop items
    private static final List<ShopEntry> parsedShopItems = new ArrayList<>();
    private static double sellMultiplier = 0.5;
    
    static {
        BUILDER.comment("Shop Configuration")
                .push("shop");
        
        SHOP_ITEMS = BUILDER
                .comment("List of items to sell in the shop.",
                        "Format: 'minecraft:item_id,price,display_name'",
                        "Example: 'minecraft:diamond,500,Diamond'")
                .defineListAllowEmpty(
                        "shopItems",
                        List.of(
                                // Ores and Minerals
                                "minecraft:diamond,500,Diamond",
                                "minecraft:emerald,200,Emerald",
                                "minecraft:netherite_ingot,2000,Netherite Ingot",
                                "minecraft:netherite_scrap,600,Netherite Scrap",
                                "minecraft:ancient_debris,550,Ancient Debris",
                                "minecraft:gold_ingot,100,Gold Ingot",
                                "minecraft:iron_ingot,50,Iron Ingot",
                                "minecraft:copper_ingot,25,Copper Ingot",
                                "minecraft:coal,10,Coal",
                                "minecraft:charcoal,8,Charcoal",
                                "minecraft:lapis_lazuli,30,Lapis Lazuli",
                                "minecraft:redstone,20,Redstone",
                                "minecraft:quartz,15,Nether Quartz",
                                "minecraft:amethyst_shard,40,Amethyst Shard",
                                "minecraft:raw_iron,40,Raw Iron",
                                "minecraft:raw_gold,80,Raw Gold",
                                "minecraft:raw_copper,20,Raw Copper",
                                
                                // Gems and Blocks
                                "minecraft:diamond_block,4500,Diamond Block",
                                "minecraft:emerald_block,1800,Emerald Block",
                                "minecraft:gold_block,900,Gold Block",
                                "minecraft:iron_block,450,Iron Block",
                                "minecraft:copper_block,225,Copper Block",
                                "minecraft:lapis_block,270,Lapis Block",
                                "minecraft:redstone_block,180,Redstone Block",
                                "minecraft:coal_block,90,Coal Block",
                                "minecraft:netherite_block,18000,Netherite Block",
                                "minecraft:amethyst_block,160,Amethyst Block",
                                
                                // Combat Equipment
                                "minecraft:netherite_sword,3500,Netherite Sword",
                                "minecraft:diamond_sword,1200,Diamond Sword",
                                "minecraft:iron_sword,200,Iron Sword",
                                "minecraft:golden_sword,250,Golden Sword",
                                "minecraft:stone_sword,50,Stone Sword",
                                "minecraft:wooden_sword,20,Wooden Sword",
                                "minecraft:bow,150,Bow",
                                "minecraft:crossbow,300,Crossbow",
                                "minecraft:arrow,5,Arrow",
                                "minecraft:spectral_arrow,25,Spectral Arrow",
                                "minecraft:tipped_arrow,50,Tipped Arrow",
                                "minecraft:trident,2500,Trident",
                                "minecraft:shield,200,Shield",
                                "minecraft:mace,4000,Mace",
                                
                                // Armor - Netherite
                                "minecraft:netherite_helmet,3000,Netherite Helmet",
                                "minecraft:netherite_chestplate,4000,Netherite Chestplate",
                                "minecraft:netherite_leggings,3500,Netherite Leggings",
                                "minecraft:netherite_boots,2500,Netherite Boots",
                                
                                // Armor - Diamond
                                "minecraft:diamond_helmet,1000,Diamond Helmet",
                                "minecraft:diamond_chestplate,1600,Diamond Chestplate",
                                "minecraft:diamond_leggings,1400,Diamond Leggings",
                                "minecraft:diamond_boots,800,Diamond Boots",
                                
                                // Armor - Iron
                                "minecraft:iron_helmet,200,Iron Helmet",
                                "minecraft:iron_chestplate,320,Iron Chestplate",
                                "minecraft:iron_leggings,280,Iron Leggings",
                                "minecraft:iron_boots,160,Iron Boots",
                                
                                // Armor - Gold
                                "minecraft:golden_helmet,250,Golden Helmet",
                                "minecraft:golden_chestplate,400,Golden Chestplate",
                                "minecraft:golden_leggings,350,Golden Leggings",
                                "minecraft:golden_boots,200,Golden Boots",
                                
                                // Armor - Leather
                                "minecraft:leather_helmet,50,Leather Helmet",
                                "minecraft:leather_chestplate,80,Leather Chestplate",
                                "minecraft:leather_leggings,70,Leather Leggings",
                                "minecraft:leather_boots,40,Leather Boots",
                                
                                // Armor - Chainmail
                                "minecraft:chainmail_helmet,150,Chainmail Helmet",
                                "minecraft:chainmail_chestplate,240,Chainmail Chestplate",
                                "minecraft:chainmail_leggings,210,Chainmail Leggings",
                                "minecraft:chainmail_boots,120,Chainmail Boots",
                                
                                // Tools - Netherite
                                "minecraft:netherite_pickaxe,3200,Netherite Pickaxe",
                                "minecraft:netherite_axe,3000,Netherite Axe",
                                "minecraft:netherite_shovel,2800,Netherite Shovel",
                                "minecraft:netherite_hoe,2600,Netherite Hoe",
                                
                                // Tools - Diamond
                                "minecraft:diamond_pickaxe,900,Diamond Pickaxe",
                                "minecraft:diamond_axe,850,Diamond Axe",
                                "minecraft:diamond_shovel,600,Diamond Shovel",
                                "minecraft:diamond_hoe,500,Diamond Hoe",
                                
                                // Tools - Iron
                                "minecraft:iron_pickaxe,180,Iron Pickaxe",
                                "minecraft:iron_axe,170,Iron Axe",
                                "minecraft:iron_shovel,100,Iron Shovel",
                                "minecraft:iron_hoe,80,Iron Hoe",
                                
                                // Tools - Other
                                "minecraft:golden_pickaxe,220,Golden Pickaxe",
                                "minecraft:stone_pickaxe,40,Stone Pickaxe",
                                "minecraft:wooden_pickaxe,15,Wooden Pickaxe",
                                "minecraft:shears,60,Shears",
                                "minecraft:flint_and_steel,75,Flint and Steel",
                                "minecraft:fishing_rod,100,Fishing Rod",
                                "minecraft:carrot_on_a_stick,120,Carrot on a Stick",
                                "minecraft:warped_fungus_on_a_stick,150,Warped Fungus on a Stick",
                                "minecraft:spyglass,200,Spyglass",
                                "minecraft:brush,180,Brush",
                                
                                // Food
                                "minecraft:golden_apple,300,Golden Apple",
                                "minecraft:enchanted_golden_apple,5000,Enchanted Golden Apple",
                                "minecraft:golden_carrot,80,Golden Carrot",
                                "minecraft:cooked_beef,15,Steak",
                                "minecraft:cooked_porkchop,15,Cooked Porkchop",
                                "minecraft:cooked_chicken,12,Cooked Chicken",
                                "minecraft:cooked_mutton,12,Cooked Mutton",
                                "minecraft:cooked_rabbit,14,Cooked Rabbit",
                                "minecraft:cooked_cod,10,Cooked Cod",
                                "minecraft:cooked_salmon,12,Cooked Salmon",
                                "minecraft:bread,8,Bread",
                                "minecraft:apple,6,Apple",
                                "minecraft:melon_slice,4,Melon Slice",
                                "minecraft:pumpkin_pie,20,Pumpkin Pie",
                                "minecraft:cake,50,Cake",
                                "minecraft:cookie,5,Cookie",
                                "minecraft:honey_bottle,30,Honey Bottle",
                                "minecraft:sweet_berries,8,Sweet Berries",
                                "minecraft:glow_berries,15,Glow Berries",
                                "minecraft:chorus_fruit,25,Chorus Fruit",
                                "minecraft:beetroot_soup,18,Beetroot Soup",
                                "minecraft:mushroom_stew,20,Mushroom Stew",
                                "minecraft:rabbit_stew,35,Rabbit Stew",
                                "minecraft:suspicious_stew,40,Suspicious Stew",
                                
                                // Potions and Brewing
                                "minecraft:blaze_rod,100,Blaze Rod",
                                "minecraft:blaze_powder,55,Blaze Powder",
                                "minecraft:nether_wart,25,Nether Wart",
                                "minecraft:ghast_tear,200,Ghast Tear",
                                "minecraft:magma_cream,80,Magma Cream",
                                "minecraft:phantom_membrane,150,Phantom Membrane",
                                "minecraft:rabbit_foot,120,Rabbit Foot",
                                "minecraft:fermented_spider_eye,60,Fermented Spider Eye",
                                "minecraft:spider_eye,30,Spider Eye",
                                "minecraft:glistering_melon_slice,70,Glistering Melon Slice",
                                "minecraft:glass_bottle,10,Glass Bottle",
                                "minecraft:brewing_stand,250,Brewing Stand",
                                "minecraft:cauldron,140,Cauldron",
                                "minecraft:dragon_breath,500,Dragon Breath",
                                
                                // Enchanting
                                "minecraft:enchanting_table,1500,Enchanting Table",
                                "minecraft:experience_bottle,100,Bottle o' Enchanting",
                                "minecraft:bookshelf,60,Bookshelf",
                                "minecraft:book,15,Book",
                                "minecraft:anvil,600,Anvil",
                                "minecraft:grindstone,150,Grindstone",
                                
                                // Rare Items
                                "minecraft:elytra,10000,Elytra",
                                "minecraft:totem_of_undying,3000,Totem of Undying",
                                "minecraft:nether_star,8000,Nether Star",
                                "minecraft:beacon,12000,Beacon",
                                "minecraft:conduit,6000,Conduit",
                                "minecraft:heart_of_the_sea,4000,Heart of the Sea",
                                "minecraft:nautilus_shell,400,Nautilus Shell",
                                "minecraft:shulker_shell,600,Shulker Shell",
                                "minecraft:dragon_egg,50000,Dragon Egg",
                                "minecraft:wither_skeleton_skull,2000,Wither Skeleton Skull",
                                "minecraft:creeper_head,500,Creeper Head",
                                "minecraft:zombie_head,500,Zombie Head",
                                "minecraft:skeleton_skull,500,Skeleton Skull",
                                "minecraft:piglin_head,500,Piglin Head",
                                
                                // Utility Items
                                "minecraft:ender_pearl,150,Ender Pearl",
                                "minecraft:ender_eye,350,Eye of Ender",
                                "minecraft:ender_chest,800,Ender Chest",
                                "minecraft:name_tag,75,Name Tag",
                                "minecraft:saddle,150,Saddle",
                                "minecraft:lead,50,Lead",
                                "minecraft:compass,100,Compass",
                                "minecraft:recovery_compass,1000,Recovery Compass",
                                "minecraft:clock,120,Clock",
                                "minecraft:map,80,Map",
                                "minecraft:writable_book,40,Book and Quill",
                                "minecraft:fire_charge,30,Fire Charge",
                                "minecraft:firework_rocket,25,Firework Rocket",
                                "minecraft:firework_star,35,Firework Star",
                                
                                // Redstone
                                "minecraft:observer,150,Observer",
                                "minecraft:piston,100,Piston",
                                "minecraft:sticky_piston,150,Sticky Piston",
                                "minecraft:slime_ball,60,Slime Ball",
                                "minecraft:slime_block,540,Slime Block",
                                "minecraft:honey_block,120,Honey Block",
                                "minecraft:hopper,200,Hopper",
                                "minecraft:dropper,80,Dropper",
                                "minecraft:dispenser,100,Dispenser",
                                "minecraft:redstone_torch,25,Redstone Torch",
                                "minecraft:redstone_lamp,80,Redstone Lamp",
                                "minecraft:repeater,50,Redstone Repeater",
                                "minecraft:comparator,100,Redstone Comparator",
                                "minecraft:daylight_detector,120,Daylight Detector",
                                "minecraft:tripwire_hook,40,Tripwire Hook",
                                "minecraft:tnt,150,TNT",
                                "minecraft:lever,10,Lever",
                                "minecraft:stone_button,10,Stone Button",
                                "minecraft:stone_pressure_plate,20,Stone Pressure Plate",
                                "minecraft:heavy_weighted_pressure_plate,80,Heavy Weighted Pressure Plate",
                                "minecraft:light_weighted_pressure_plate,60,Light Weighted Pressure Plate",
                                "minecraft:target,120,Target Block",
                                "minecraft:sculk_sensor,300,Sculk Sensor",
                                "minecraft:calibrated_sculk_sensor,500,Calibrated Sculk Sensor",
                                
                                // Spawn Eggs
                                "minecraft:wolf_spawn_egg,500,Wolf Spawn Egg",
                                "minecraft:cat_spawn_egg,400,Cat Spawn Egg",
                                "minecraft:horse_spawn_egg,600,Horse Spawn Egg",
                                "minecraft:donkey_spawn_egg,400,Donkey Spawn Egg",
                                "minecraft:mule_spawn_egg,500,Mule Spawn Egg",
                                "minecraft:llama_spawn_egg,350,Llama Spawn Egg",
                                "minecraft:parrot_spawn_egg,450,Parrot Spawn Egg",
                                "minecraft:fox_spawn_egg,400,Fox Spawn Egg",
                                "minecraft:axolotl_spawn_egg,600,Axolotl Spawn Egg",
                                "minecraft:bee_spawn_egg,350,Bee Spawn Egg",
                                "minecraft:villager_spawn_egg,1000,Villager Spawn Egg",
                                "minecraft:iron_golem_spawn_egg,2000,Iron Golem Spawn Egg",
                                "minecraft:snow_golem_spawn_egg,500,Snow Golem Spawn Egg",
                                "minecraft:allay_spawn_egg,800,Allay Spawn Egg",
                                "minecraft:camel_spawn_egg,700,Camel Spawn Egg",
                                "minecraft:sniffer_spawn_egg,1500,Sniffer Spawn Egg",
                                "minecraft:armadillo_spawn_egg,600,Armadillo Spawn Egg",
                                
                                // Building Blocks
                                "minecraft:obsidian,100,Obsidian",
                                "minecraft:crying_obsidian,150,Crying Obsidian",
                                "minecraft:glowstone,50,Glowstone",
                                "minecraft:sea_lantern,80,Sea Lantern",
                                "minecraft:end_stone,30,End Stone",
                                "minecraft:purpur_block,40,Purpur Block",
                                "minecraft:prismarine,35,Prismarine",
                                "minecraft:dark_prismarine,45,Dark Prismarine",
                                "minecraft:prismarine_bricks,50,Prismarine Bricks",
                                "minecraft:sponge,200,Sponge",
                                "minecraft:wet_sponge,180,Wet Sponge",
                                "minecraft:tinted_glass,100,Tinted Glass",
                                "minecraft:reinforced_deepslate,300,Reinforced Deepslate",
                                "minecraft:lodestone,800,Lodestone",
                                "minecraft:respawn_anchor,1000,Respawn Anchor",
                                
                                // Music Discs
                                "minecraft:music_disc_13,300,Music Disc - 13",
                                "minecraft:music_disc_cat,300,Music Disc - Cat",
                                "minecraft:music_disc_blocks,300,Music Disc - Blocks",
                                "minecraft:music_disc_chirp,300,Music Disc - Chirp",
                                "minecraft:music_disc_far,300,Music Disc - Far",
                                "minecraft:music_disc_mall,300,Music Disc - Mall",
                                "minecraft:music_disc_mellohi,300,Music Disc - Mellohi",
                                "minecraft:music_disc_stal,300,Music Disc - Stal",
                                "minecraft:music_disc_strad,300,Music Disc - Strad",
                                "minecraft:music_disc_ward,300,Music Disc - Ward",
                                "minecraft:music_disc_11,400,Music Disc - 11",
                                "minecraft:music_disc_wait,300,Music Disc - Wait",
                                "minecraft:music_disc_otherside,500,Music Disc - Otherside",
                                "minecraft:music_disc_5,600,Music Disc - 5",
                                "minecraft:music_disc_pigstep,800,Music Disc - Pigstep",
                                "minecraft:music_disc_relic,700,Music Disc - Relic",
                                
                                // Dyes
                                "minecraft:white_dye,10,White Dye",
                                "minecraft:orange_dye,10,Orange Dye",
                                "minecraft:magenta_dye,10,Magenta Dye",
                                "minecraft:light_blue_dye,10,Light Blue Dye",
                                "minecraft:yellow_dye,10,Yellow Dye",
                                "minecraft:lime_dye,10,Lime Dye",
                                "minecraft:pink_dye,10,Pink Dye",
                                "minecraft:gray_dye,10,Gray Dye",
                                "minecraft:light_gray_dye,10,Light Gray Dye",
                                "minecraft:cyan_dye,10,Cyan Dye",
                                "minecraft:purple_dye,10,Purple Dye",
                                "minecraft:blue_dye,10,Blue Dye",
                                "minecraft:brown_dye,10,Brown Dye",
                                "minecraft:green_dye,10,Green Dye",
                                "minecraft:red_dye,10,Red Dye",
                                "minecraft:black_dye,10,Black Dye",
                                
                                // Mob Drops
                                "minecraft:leather,20,Leather",
                                "minecraft:rabbit_hide,15,Rabbit Hide",
                                "minecraft:feather,8,Feather",
                                "minecraft:bone,12,Bone",
                                "minecraft:string,10,String",
                                "minecraft:gunpowder,25,Gunpowder",
                                "minecraft:rotten_flesh,5,Rotten Flesh",
                                "minecraft:ender_pearl,150,Ender Pearl",
                                "minecraft:ink_sac,15,Ink Sac",
                                "minecraft:glow_ink_sac,40,Glow Ink Sac",
                                "minecraft:prismarine_shard,25,Prismarine Shard",
                                "minecraft:prismarine_crystals,35,Prismarine Crystals",
                                "minecraft:scute,100,Scute",
                                "minecraft:turtle_helmet,600,Turtle Shell",
                                "minecraft:armadillo_scute,80,Armadillo Scute",
                                "minecraft:wolf_armor,400,Wolf Armor",
                                
                                // Plants and Nature
                                "minecraft:oak_sapling,15,Oak Sapling",
                                "minecraft:spruce_sapling,15,Spruce Sapling",
                                "minecraft:birch_sapling,15,Birch Sapling",
                                "minecraft:jungle_sapling,20,Jungle Sapling",
                                "minecraft:acacia_sapling,15,Acacia Sapling",
                                "minecraft:dark_oak_sapling,20,Dark Oak Sapling",
                                "minecraft:cherry_sapling,25,Cherry Sapling",
                                "minecraft:mangrove_propagule,20,Mangrove Propagule",
                                "minecraft:bamboo,10,Bamboo",
                                "minecraft:cactus,12,Cactus",
                                "minecraft:sugar_cane,10,Sugar Cane",
                                "minecraft:kelp,8,Kelp",
                                "minecraft:sea_pickle,20,Sea Pickle",
                                "minecraft:lily_pad,15,Lily Pad",
                                "minecraft:vine,12,Vine",
                                "minecraft:glow_lichen,25,Glow Lichen",
                                "minecraft:sculk,50,Sculk",
                                "minecraft:sculk_catalyst,400,Sculk Catalyst",
                                "minecraft:sculk_shrieker,500,Sculk Shrieker",
                                "minecraft:sculk_vein,30,Sculk Vein",
                                
                                // Miscellaneous
                                "minecraft:bone_meal,8,Bone Meal",
                                "minecraft:bucket,75,Bucket",
                                "minecraft:water_bucket,85,Water Bucket",
                                "minecraft:lava_bucket,150,Lava Bucket",
                                "minecraft:milk_bucket,90,Milk Bucket",
                                "minecraft:powder_snow_bucket,100,Powder Snow Bucket",
                                "minecraft:axolotl_bucket,650,Axolotl Bucket",
                                "minecraft:tropical_fish_bucket,200,Tropical Fish Bucket",
                                "minecraft:pufferfish_bucket,180,Pufferfish Bucket",
                                "minecraft:tadpole_bucket,150,Tadpole Bucket",
                                "minecraft:painting,30,Painting",
                                "minecraft:item_frame,25,Item Frame",
                                "minecraft:glow_item_frame,50,Glow Item Frame",
                                "minecraft:armor_stand,80,Armor Stand",
                                "minecraft:flower_pot,20,Flower Pot",
                                "minecraft:decorated_pot,100,Decorated Pot",
                                "minecraft:bell,500,Bell",
                                "minecraft:end_crystal,2000,End Crystal",
                                "minecraft:goat_horn,150,Goat Horn",
                                "minecraft:echo_shard,400,Echo Shard",
                                "minecraft:disc_fragment_5,100,Disc Fragment"
                        ),
                        () -> "minecraft:stone,10,Stone",
                        obj -> obj instanceof String s && s.split(",").length == 3
                );
        
        SELL_PRICE_MULTIPLIER = BUILDER
                .comment("Multiplier for sell prices. Sell price = Buy price * this value.",
                        "Default is 0.5 (50% of buy price)")
                .defineInRange("sellPriceMultiplier", 0.5, 0.0, 1.0);
        
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
    
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            sellMultiplier = SELL_PRICE_MULTIPLIER.get();
            parseShopItems();
        }
    }
    
    private static void parseShopItems() {
        parsedShopItems.clear();
        int validCount = 0;
        int invalidCount = 0;
        
        for (String entry : SHOP_ITEMS.get()) {
            String[] parts = entry.split(",");
            if (parts.length == 3) {
                try {
                    String itemId = parts[0].trim();
                    int price = Integer.parseInt(parts[1].trim());
                    String displayName = parts[2].trim();
                    
                    // Validate that the item exists in the registry
                    if (isValidItem(itemId)) {
                        parsedShopItems.add(new ShopEntry(itemId, price, displayName));
                        validCount++;
                    } else {
                        LOGGER.warn("Shop item '{}' ({}) does not exist in the item registry and will be skipped", 
                                displayName, itemId);
                        invalidCount++;
                    }
                } catch (NumberFormatException e) {
                    LOGGER.warn("Invalid price format for shop entry: {}", entry);
                }
            } else {
                LOGGER.warn("Invalid shop entry format (expected 3 parts): {}", entry);
            }
        }
        
        if (invalidCount > 0) {
            LOGGER.info("Shop loaded with {} valid items, {} invalid items skipped", validCount, invalidCount);
        } else {
            LOGGER.info("Shop loaded with {} items", validCount);
        }
    }
    
    /**
     * Checks if an item ID corresponds to a valid, registered item.
     * @param itemId The item ID in "modid:item_name" format
     * @return true if the item exists and is not air, false otherwise
     */
    private static boolean isValidItem(String itemId) {
        ResourceLocation location = ResourceLocation.tryParse(itemId);
        if (location == null) {
            return false;
        }
        
        // Check if the registry contains this key
        if (!BuiltInRegistries.ITEM.containsKey(location)) {
            return false;
        }
        
        // Also verify the item is not AIR (default fallback)
        var item = BuiltInRegistries.ITEM.get(location);
        return item != null && item != Items.AIR;
    }
    
    public static List<ShopEntry> getShopItems() {
        return parsedShopItems;
    }
    
    public static double getSellMultiplier() {
        return sellMultiplier;
    }
    
    public static int getSellPrice(int buyPrice) {
        return (int) Math.floor(buyPrice * sellMultiplier);
    }
    
    public record ShopEntry(String itemId, int price, String displayName) {
        public int getSellPrice() {
            return ShopConfig.getSellPrice(price);
        }
    }
}
