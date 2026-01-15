package com.artof.minecraftmoney.config;

import com.artof.minecraftmoney.MinecraftMoney;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = "minecraftmoney", bus = EventBusSubscriber.Bus.MOD)
public class ShopConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(MinecraftMoney.MOD_ID);
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    
    private static final ModConfigSpec.ConfigValue<List<? extends String>> SHOP_ITEMS;
    private static final ModConfigSpec.DoubleValue SELL_PRICE_MULTIPLIER;
    private static final ModConfigSpec.BooleanValue BIG_SCREEN;
    private static final ModConfigSpec.IntValue PERSONAL_SELLER_MAX_ITEMS;
    private static final ModConfigSpec.BooleanValue AUTO_ADD_ENCHANTED_BOOKS;
    private static final ModConfigSpec.BooleanValue AUTO_ADD_POTIONS;
    private static final ModConfigSpec.IntValue ENCHANTED_BOOK_PRICE;
    private static final ModConfigSpec.IntValue POTION_PRICE;
    private static final ModConfigSpec.BooleanValue FILTER_CREATIVE_ENCHANTMENTS;
    private static final ModConfigSpec.BooleanValue FILTER_CREATIVE_ITEMS;
    private static final ModConfigSpec.BooleanValue PERSONAL_SELLER_ACCEPT_FE;
    private static final ModConfigSpec.IntValue FE_PER_CURRENCY;
    
    public static final ModConfigSpec SPEC;
    
    // Parsed shop items
    private static final List<ShopEntry> parsedShopItems = new ArrayList<>();
    private static double sellMultiplier = 0.5;
    private static boolean bigScreen = false;
    private static int personalSellerMaxItems = 1;
    private static boolean autoAddEnchantedBooks = true;
    private static boolean autoAddPotions = true;
    private static int enchantedBookPrice = 10000;
    private static int potionPrice = 10000;
    private static boolean filterCreativeEnchantments = true;
    private static boolean filterCreativeItems = false;
    private static boolean personalSellerAcceptFE = true;
    private static int fePerCurrency = 1000;
    private static boolean enchantedBooksLoaded = false;
    private static RegistryAccess lastRegistryAccess = null;
    
    static {
        BUILDER.comment("Shop Configuration")
                .push("shop");
        
        SHOP_ITEMS = BUILDER
                .comment("List of items to sell in the shop.",
                        "Format: 'minecraft:item_id,price,display_name'",
                        "For enchanted books: 'enchanted_book:enchantment_id:level,price,display_name'",
                        "For potions: 'potion:potion_id,price,display_name' (also splash_potion: and lingering_potion:)",
                        "Example: 'minecraft:diamond,500,Diamond'",
                        "Example enchanted book: 'enchanted_book:sharpness:5,10000,Sharpness V'",
                        "Example potion: 'potion:strong_healing,5000,Strong Healing Potion'")
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
                        obj -> obj instanceof String s && (s.split(",").length >= 3)
                );
        
        SELL_PRICE_MULTIPLIER = BUILDER
                .comment("Multiplier for sell prices. Sell price = Buy price * this value.",
                        "Default is 0.5 (50% of buy price)")
                .defineInRange("sellPriceMultiplier", 0.5, 0.0, 1.0);
        
        BIG_SCREEN = BUILDER
                .comment("Enable big screen mode for the shop GUI.",
                        "When true, the shop displays 20 rows instead of 10.")
                .define("bigScreen", false);
        
        PERSONAL_SELLER_MAX_ITEMS = BUILDER
                .comment("Maximum number of items sold per tick by the Personal Seller block.",
                        "Higher values mean faster selling but more server load.",
                        "Default is 1 item per tick (20 items per second).")
                .defineInRange("personalSellerMaxItems", 1, 1, 64);
        
        AUTO_ADD_ENCHANTED_BOOKS = BUILDER
                .comment("Automatically add all enchanted books to the shop.",
                        "Books are added at max level for each enchantment.")
                .define("autoAddEnchantedBooks", true);
        
        AUTO_ADD_POTIONS = BUILDER
                .comment("Automatically add all potions (normal, splash, lingering) to the shop.")
                .define("autoAddPotions", true);
        
        ENCHANTED_BOOK_PRICE = BUILDER
                .comment("Price for auto-generated enchanted books.")
                .defineInRange("enchantedBookPrice", 10000, 1, Integer.MAX_VALUE);
        
        POTION_PRICE = BUILDER
                .comment("Price for auto-generated potions.")
                .defineInRange("potionPrice", 10000, 1, Integer.MAX_VALUE);
        
        FILTER_CREATIVE_ENCHANTMENTS = BUILDER
                .comment("Filter out creative-only and overpowered enchantments from auto-generated books.",
                        "This removes enchantments containing 'infinity' or 'creative' in their name.")
                .define("filterCreativeEnchantments", true);
        
        FILTER_CREATIVE_ITEMS = BUILDER
                .comment("Filter out creative-only items from the shop.",
                        "This removes items containing 'infinity' or 'creative' in their name.")
                .define("filterCreativeItems", false);
        
        PERSONAL_SELLER_ACCEPT_FE = BUILDER
                .comment("Allow Personal Seller block to accept Forge Energy (FE) from any side.",
                        "FE will be instantly converted to currency for the owner.")
                .define("personalSellerAcceptFE", true);
        
        FE_PER_CURRENCY = BUILDER
                .comment("Amount of FE required to earn 1 currency.",
                        "Default is 1000 FE = 1 currency.")
                .defineInRange("fePerCurrency", 1000, 1, Integer.MAX_VALUE);
        
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
    
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            sellMultiplier = SELL_PRICE_MULTIPLIER.get();
            bigScreen = BIG_SCREEN.get();
            personalSellerMaxItems = PERSONAL_SELLER_MAX_ITEMS.get();
            autoAddEnchantedBooks = AUTO_ADD_ENCHANTED_BOOKS.get();
            autoAddPotions = AUTO_ADD_POTIONS.get();
            enchantedBookPrice = ENCHANTED_BOOK_PRICE.get();
            potionPrice = POTION_PRICE.get();
            filterCreativeEnchantments = FILTER_CREATIVE_ENCHANTMENTS.get();
            filterCreativeItems = FILTER_CREATIVE_ITEMS.get();
            personalSellerAcceptFE = PERSONAL_SELLER_ACCEPT_FE.get();
            fePerCurrency = FE_PER_CURRENCY.get();
            parseShopItems();
        }
    }
    
    public static boolean isBigScreen() {
        return BIG_SCREEN.get();
    }
    
    public static int getPersonalSellerMaxItems() {
        return personalSellerMaxItems;
    }
    
    public static boolean isPersonalSellerAcceptFE() {
        return personalSellerAcceptFE;
    }
    
    public static int getFePerCurrency() {
        return fePerCurrency;
    }
    
    public static boolean isFilterCreativeItems() {
        return filterCreativeItems;
    }
    
    private static void parseShopItems() {
        parsedShopItems.clear();
        int validCount = 0;
        int invalidCount = 0;
        
        for (String entry : SHOP_ITEMS.get()) {
            // Parse format: itemId,price,displayName
            // Special formats:
            // - enchanted_book:enchantment_id:level,price,displayName
            // - potion:potion_id,price,displayName
            // - splash_potion:potion_id,price,displayName
            // - lingering_potion:potion_id,price,displayName
            
            int firstComma = entry.indexOf(',');
            if (firstComma == -1) {
                LOGGER.warn("Invalid shop entry format (no commas): {}", entry);
                continue;
            }
            
            int secondComma = entry.indexOf(',', firstComma + 1);
            if (secondComma == -1) {
                LOGGER.warn("Invalid shop entry format (only one comma): {}", entry);
                continue;
            }
            
            String itemId = entry.substring(0, firstComma).trim();
            String priceStr = entry.substring(firstComma + 1, secondComma).trim();
            String displayName = entry.substring(secondComma + 1).trim();
            
            try {
                long price = Long.parseLong(priceStr);
                
                // Filter creative items if enabled
                if (filterCreativeItems) {
                    String lowerItemId = itemId.toLowerCase();
                    String lowerDisplayName = displayName.toLowerCase();
                    if (lowerItemId.contains("infinity") || lowerItemId.contains("creative") ||
                        lowerDisplayName.contains("infinity") || lowerDisplayName.contains("creative")) {
                        LOGGER.debug("Filtered creative item from shop: {} ({})", displayName, itemId);
                        continue;
                    }
                }
                
                // Parse special item types
                if (itemId.startsWith("enchanted_book:")) {
                    // Format: enchanted_book:enchantment_id:level
                    String enchantData = itemId.substring("enchanted_book:".length());
                    int lastColon = enchantData.lastIndexOf(':');
                    if (lastColon > 0) {
                        String enchantmentId = enchantData.substring(0, lastColon);
                        int level = Integer.parseInt(enchantData.substring(lastColon + 1));
                        parsedShopItems.add(new ShopEntry("minecraft:enchanted_book", price, displayName, 
                                "enchanted_book:" + enchantmentId + ":" + level));
                        validCount++;
                    } else {
                        LOGGER.warn("Invalid enchanted book format: {}", entry);
                        invalidCount++;
                    }
                } else if (itemId.startsWith("potion:")) {
                    String potionId = itemId.substring("potion:".length());
                    parsedShopItems.add(new ShopEntry("minecraft:potion", price, displayName, "potion:" + potionId));
                    validCount++;
                } else if (itemId.startsWith("splash_potion:")) {
                    String potionId = itemId.substring("splash_potion:".length());
                    parsedShopItems.add(new ShopEntry("minecraft:splash_potion", price, displayName, "potion:" + potionId));
                    validCount++;
                } else if (itemId.startsWith("lingering_potion:")) {
                    String potionId = itemId.substring("lingering_potion:".length());
                    parsedShopItems.add(new ShopEntry("minecraft:lingering_potion", price, displayName, "potion:" + potionId));
                    validCount++;
                } else if (isValidItem(itemId)) {
                    // Regular item
                    parsedShopItems.add(new ShopEntry(itemId, price, displayName, null));
                    validCount++;
                } else {
                    LOGGER.warn("Shop item '{}' ({}) does not exist in the item registry and will be skipped", 
                            displayName, itemId);
                    invalidCount++;
                }
            } catch (NumberFormatException e) {
                LOGGER.warn("Invalid price or level format for shop entry: {}", entry);
            }
        }
        
        // Auto-add enchanted books if enabled
        // Note: Enchanted books from modded registries are added when server starts
        // via loadEnchantedBooksFromRegistry() since enchantments are data-driven
        if (autoAddEnchantedBooks) {
            // Add vanilla enchanted books using predefined list
            int booksAdded = addVanillaEnchantedBooks();
            LOGGER.info("Auto-added {} vanilla enchanted books to shop (modded books load on server start)", booksAdded);
            validCount += booksAdded;
        }
        
        // Auto-add potions if enabled
        if (autoAddPotions) {
            int potionsAdded = addPotions();
            LOGGER.info("Auto-added {} potions to shop", potionsAdded);
            validCount += potionsAdded;
        }
        
        if (invalidCount > 0) {
            LOGGER.info("Shop loaded with {} valid items, {} invalid items skipped", validCount, invalidCount);
        } else {
            LOGGER.info("Shop loaded with {} items", validCount);
        }
        
        // Reset the flag so modded enchantments get loaded on next server start
        enchantedBooksLoaded = false;
    }
    
    /**
     * Called when the server starts to load enchanted books from the data-driven registry.
     * This allows loading modded enchantments that aren't available during config parsing.
     */
    public static void loadEnchantedBooksFromRegistry(RegistryAccess registryAccess) {
        if (!autoAddEnchantedBooks) return;
        if (enchantedBooksLoaded && lastRegistryAccess == registryAccess) return;
        
        lastRegistryAccess = registryAccess;
        enchantedBooksLoaded = true;
        
        Registry<Enchantment> enchantmentRegistry = registryAccess.registryOrThrow(Registries.ENCHANTMENT);
        int count = 0;
        
        // Track which enchantments we've already added (from vanilla list)
        java.util.Set<String> existingEnchantments = new java.util.HashSet<>();
        for (ShopEntry entry : parsedShopItems) {
            if (entry.componentString() != null && entry.componentString().startsWith("enchanted_book:")) {
                String enchantId = entry.getEnchantmentId();
                if (enchantId != null) {
                    existingEnchantments.add(enchantId);
                }
            }
        }
        
        // Add all enchantments from the registry that we haven't added yet
        for (Holder<Enchantment> enchantmentHolder : enchantmentRegistry.holders().toList()) {
            var key = enchantmentHolder.unwrapKey();
            if (key.isEmpty()) continue;
            
            ResourceLocation enchantmentId = key.get().location();
            String enchantmentIdStr = enchantmentId.toString();
            
            // Skip if already added
            if (existingEnchantments.contains(enchantmentIdStr)) continue;
            
            // Filter out creative/infinity enchantments if enabled
            if (filterCreativeEnchantments) {
                String lowerCaseId = enchantmentIdStr.toLowerCase();
                if (lowerCaseId.contains("infinity") || lowerCaseId.contains("creative")) {
                    continue;
                }
            }
            
            Enchantment enchantment = enchantmentHolder.value();
            int maxLevel = enchantment.getMaxLevel();
            
            String enchantmentName = formatEnchantmentName(enchantmentId.getPath(), maxLevel);
            String componentString = "enchanted_book:" + enchantmentIdStr + ":" + maxLevel;
            
            parsedShopItems.add(new ShopEntry("minecraft:enchanted_book", enchantedBookPrice, enchantmentName, componentString));
            count++;
        }
        
        if (count > 0) {
            LOGGER.info("Loaded {} additional enchanted books from registry (including modded)", count);
        }
    }
    
    private static int addVanillaEnchantedBooks() {
        int count = 0;
        
        // Predefined list of vanilla enchantments with their max levels
        // In MC 1.21+, enchantments are data-driven so we use a predefined list
        String[][] vanillaEnchantments = {
                // Armor enchantments
                {"minecraft:protection", "4"},
                {"minecraft:fire_protection", "4"},
                {"minecraft:feather_falling", "4"},
                {"minecraft:blast_protection", "4"},
                {"minecraft:projectile_protection", "4"},
                {"minecraft:respiration", "3"},
                {"minecraft:aqua_affinity", "1"},
                {"minecraft:thorns", "3"},
                {"minecraft:depth_strider", "3"},
                {"minecraft:frost_walker", "2"},
                {"minecraft:binding_curse", "1"},
                {"minecraft:soul_speed", "3"},
                {"minecraft:swift_sneak", "3"},
                
                // Weapon enchantments
                {"minecraft:sharpness", "5"},
                {"minecraft:smite", "5"},
                {"minecraft:bane_of_arthropods", "5"},
                {"minecraft:knockback", "2"},
                {"minecraft:fire_aspect", "2"},
                {"minecraft:looting", "3"},
                {"minecraft:sweeping_edge", "3"},
                {"minecraft:density", "5"},
                {"minecraft:breach", "4"},
                {"minecraft:wind_burst", "3"},
                
                // Tool enchantments
                {"minecraft:efficiency", "5"},
                {"minecraft:silk_touch", "1"},
                {"minecraft:unbreaking", "3"},
                {"minecraft:fortune", "3"},
                
                // Bow enchantments
                {"minecraft:power", "5"},
                {"minecraft:punch", "2"},
                {"minecraft:flame", "1"},
                {"minecraft:infinity", "1"},
                
                // Crossbow enchantments
                {"minecraft:multishot", "1"},
                {"minecraft:quick_charge", "3"},
                {"minecraft:piercing", "4"},
                
                // Trident enchantments
                {"minecraft:loyalty", "3"},
                {"minecraft:impaling", "5"},
                {"minecraft:riptide", "3"},
                {"minecraft:channeling", "1"},
                
                // Mace enchantments
                {"minecraft:density", "5"},
                {"minecraft:breach", "4"},
                {"minecraft:wind_burst", "3"},
                
                // Fishing rod enchantments
                {"minecraft:luck_of_the_sea", "3"},
                {"minecraft:lure", "3"},
                
                // General enchantments
                {"minecraft:mending", "1"},
                {"minecraft:vanishing_curse", "1"}
        };
        
        for (String[] enchantment : vanillaEnchantments) {
            String enchantmentId = enchantment[0];
            int maxLevel = Integer.parseInt(enchantment[1]);
            
            // Filter out creative/infinity enchantments if enabled
            if (filterCreativeEnchantments) {
                String lowerCaseId = enchantmentId.toLowerCase();
                if (lowerCaseId.contains("infinity") || lowerCaseId.contains("creative")) {
                    continue;
                }
            }
            
            String enchantmentName = formatEnchantmentName(enchantmentId.substring(enchantmentId.indexOf(':') + 1), maxLevel);
            
            // Store as special component string for enchanted books
            String componentString = "enchanted_book:" + enchantmentId + ":" + maxLevel;
            
            parsedShopItems.add(new ShopEntry("minecraft:enchanted_book", enchantedBookPrice, enchantmentName, componentString));
            count++;
        }
        
        return count;
    }
    
    private static int addPotions() {
        int count = 0;
        
        for (Holder<Potion> potionHolder : BuiltInRegistries.POTION.holders().toList()) {
            Potion potion = potionHolder.value();
            ResourceLocation potionId = BuiltInRegistries.POTION.getKey(potion);
            
            if (potionId == null) continue;
            
            // Skip "empty" and "water" potions as they're not useful
            String potionPath = potionId.getPath();
            if (potionPath.equals("empty") || potionPath.equals("water") || potionPath.equals("mundane") || potionPath.equals("thick") || potionPath.equals("awkward")) {
                continue;
            }
            
            String baseName = formatPotionName(potionPath);
            String potionIdStr = potionId.toString();
            
            // Add normal potion
            parsedShopItems.add(new ShopEntry("minecraft:potion", potionPrice, "Potion of " + baseName, "potion:" + potionIdStr));
            count++;
            
            // Add splash potion
            parsedShopItems.add(new ShopEntry("minecraft:splash_potion", potionPrice, "Splash Potion of " + baseName, "potion:" + potionIdStr));
            count++;
            
            // Add lingering potion
            parsedShopItems.add(new ShopEntry("minecraft:lingering_potion", potionPrice, "Lingering Potion of " + baseName, "potion:" + potionIdStr));
            count++;
        }
        
        return count;
    }
    
    private static String formatEnchantmentName(String path, int level) {
        // Convert snake_case to Title Case
        StringBuilder name = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : path.toCharArray()) {
            if (c == '_') {
                name.append(' ');
                capitalizeNext = true;
            } else {
                name.append(capitalizeNext ? Character.toUpperCase(c) : c);
                capitalizeNext = false;
            }
        }
        
        // Add level as roman numeral if > 1
        if (level > 1) {
            name.append(' ').append(toRoman(level));
        } else if (level == 1) {
            // Only add "I" if the enchantment has multiple levels
            name.append(" I");
        }
        
        return name.toString();
    }
    
    private static String formatPotionName(String path) {
        // Handle prefixes like "strong_" and "long_"
        String prefix = "";
        String basePath = path;
        
        if (path.startsWith("strong_")) {
            prefix = "Strong ";
            basePath = path.substring(7);
        } else if (path.startsWith("long_")) {
            prefix = "Extended ";
            basePath = path.substring(5);
        }
        
        // Convert snake_case to Title Case
        StringBuilder name = new StringBuilder(prefix);
        boolean capitalizeNext = true;
        for (char c : basePath.toCharArray()) {
            if (c == '_') {
                name.append(' ');
                capitalizeNext = true;
            } else {
                name.append(capitalizeNext ? Character.toUpperCase(c) : c);
                capitalizeNext = false;
            }
        }
        
        return name.toString();
    }
    
    private static String toRoman(int num) {
        return switch (num) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> String.valueOf(num);
        };
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
    
    public static long getSellPrice(long buyPrice) {
        return (long) Math.floor(buyPrice * sellMultiplier);
    }
    
    /**
     * Represents a shop entry with optional component data for items like enchanted books and potions.
     * The componentString can be:
     * - null for regular items
     * - "enchanted_book:minecraft:enchantment_id:level" for enchanted books
     * - "potion:minecraft:potion_id" for potions
     */
    public record ShopEntry(String itemId, long price, String displayName, String componentString) {
        
        /**
         * Constructor for simple items without component data.
         */
        public ShopEntry(String itemId, long price, String displayName) {
            this(itemId, price, displayName, null);
        }
        
        public long getSellPrice() {
            return ShopConfig.getSellPrice(price);
        }
        
        /**
         * Creates an ItemStack for this shop entry with the correct component data.
         * Note: For enchanted books, this requires server context to access the enchantment registry.
         * Call this on the server side only when possible.
         */
        public ItemStack createItemStack(int count) {
            ResourceLocation itemLoc = ResourceLocation.tryParse(itemId);
            if (itemLoc == null) return ItemStack.EMPTY;
            
            var item = BuiltInRegistries.ITEM.get(itemLoc);
            if (item == null || item == Items.AIR) return ItemStack.EMPTY;
            
            ItemStack stack = new ItemStack(item, count);
            
            // Apply component data if present
            if (componentString != null && !componentString.isEmpty()) {
                if (componentString.startsWith("enchanted_book:")) {
                    // Format: enchanted_book:minecraft:enchantment_id:level
                    // Enchanted books will be created without the actual enchantment data here
                    // The enchantment will need to be applied when we have registry access
                    // For display purposes, we return the base enchanted book
                    // The actual enchantment is applied in createItemStackWithRegistry
                } else if (componentString.startsWith("potion:")) {
                    // Format: potion:minecraft:potion_id
                    String potionIdStr = componentString.substring("potion:".length());
                    ResourceLocation potionLoc = ResourceLocation.tryParse(potionIdStr);
                    if (potionLoc != null) {
                        var potionHolder = BuiltInRegistries.POTION.getHolder(potionLoc);
                        if (potionHolder.isPresent()) {
                            stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potionHolder.get()));
                        }
                    }
                }
            }
            
            return stack;
        }
        
        /**
         * Creates an ItemStack with full enchantment support using the given registry access.
         * This should be called on the server side where we have access to enchantment registries.
         */
        public ItemStack createItemStackWithRegistry(int count, net.minecraft.core.RegistryAccess registryAccess) {
            ResourceLocation itemLoc = ResourceLocation.tryParse(itemId);
            if (itemLoc == null) return ItemStack.EMPTY;
            
            var item = BuiltInRegistries.ITEM.get(itemLoc);
            if (item == null || item == Items.AIR) return ItemStack.EMPTY;
            
            ItemStack stack = new ItemStack(item, count);
            
            // Apply component data if present
            if (componentString != null && !componentString.isEmpty()) {
                if (componentString.startsWith("enchanted_book:")) {
                    // Format: enchanted_book:minecraft:enchantment_id:level
                    String data = componentString.substring("enchanted_book:".length());
                    int lastColon = data.lastIndexOf(':');
                    if (lastColon > 0) {
                        String enchantmentId = data.substring(0, lastColon);
                        int level = Integer.parseInt(data.substring(lastColon + 1));
                        
                        ResourceLocation enchantLoc = ResourceLocation.tryParse(enchantmentId);
                        if (enchantLoc != null && registryAccess != null) {
                            var enchantRegistry = registryAccess.registry(net.minecraft.core.registries.Registries.ENCHANTMENT);
                            if (enchantRegistry.isPresent()) {
                                var enchantHolder = enchantRegistry.get().getHolder(enchantLoc);
                                if (enchantHolder.isPresent()) {
                                    net.minecraft.world.item.enchantment.ItemEnchantments.Mutable mutableEnchants = 
                                            new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(
                                                    net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);
                                    mutableEnchants.set(enchantHolder.get(), level);
                                    stack.set(DataComponents.STORED_ENCHANTMENTS, mutableEnchants.toImmutable());
                                }
                            }
                        }
                    }
                } else if (componentString.startsWith("potion:")) {
                    // Format: potion:minecraft:potion_id
                    String potionIdStr = componentString.substring("potion:".length());
                    ResourceLocation potionLoc = ResourceLocation.tryParse(potionIdStr);
                    if (potionLoc != null) {
                        var potionHolder = BuiltInRegistries.POTION.getHolder(potionLoc);
                        if (potionHolder.isPresent()) {
                            stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potionHolder.get()));
                        }
                    }
                }
            }
            
            return stack;
        }
        
        /**
         * Returns the enchantment ID if this is an enchanted book entry.
         */
        public String getEnchantmentId() {
            if (componentString != null && componentString.startsWith("enchanted_book:")) {
                String data = componentString.substring("enchanted_book:".length());
                int lastColon = data.lastIndexOf(':');
                if (lastColon > 0) {
                    return data.substring(0, lastColon);
                }
            }
            return null;
        }
        
        /**
         * Returns the enchantment level if this is an enchanted book entry.
         */
        public int getEnchantmentLevel() {
            if (componentString != null && componentString.startsWith("enchanted_book:")) {
                String data = componentString.substring("enchanted_book:".length());
                int lastColon = data.lastIndexOf(':');
                if (lastColon > 0) {
                    try {
                        return Integer.parseInt(data.substring(lastColon + 1));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                }
            }
            return 0;
        }
        
        /**
         * Returns the potion ID if this is a potion entry.
         */
        public String getPotionId() {
            if (componentString != null && componentString.startsWith("potion:")) {
                return componentString.substring("potion:".length());
            }
            return null;
        }
        
        /**
         * Checks if the given ItemStack matches this shop entry.
         * For items with component data, the components must also match.
         */
        public boolean matches(ItemStack stack) {
            if (stack.isEmpty()) return false;
            
            ResourceLocation itemLoc = ResourceLocation.tryParse(itemId);
            if (itemLoc == null) return false;
            
            var expectedItem = BuiltInRegistries.ITEM.get(itemLoc);
            if (expectedItem == null || !stack.is(expectedItem)) return false;
            
            // If no component data, match any item of this type
            if (componentString == null || componentString.isEmpty()) {
                return true;
            }
            
            // Compare component data based on item type
            if (componentString.startsWith("enchanted_book:")) {
                // Compare stored enchantments by checking the enchantment ID
                String enchantmentId = getEnchantmentId();
                int expectedLevel = getEnchantmentLevel();
                
                if (enchantmentId == null) return false;
                
                var actualEnchants = stack.get(DataComponents.STORED_ENCHANTMENTS);
                if (actualEnchants == null) return false;
                
                // Check if stack has this enchantment at this level
                ResourceLocation enchantLoc = ResourceLocation.tryParse(enchantmentId);
                if (enchantLoc == null) return false;
                
                // Iterate through the enchantments on the stack
                for (var entry : actualEnchants.entrySet()) {
                    Holder<net.minecraft.world.item.enchantment.Enchantment> enchantHolder = entry.getKey();
                    int level = entry.getIntValue();
                    
                    // Get the key from the holder
                    var key = enchantHolder.unwrapKey();
                    if (key.isPresent() && key.get().location().equals(enchantLoc) && level == expectedLevel) {
                        return true;
                    }
                }
                return false;
            } else if (componentString.startsWith("potion:")) {
                // Compare potion contents
                String potionId = getPotionId();
                if (potionId == null) return false;
                
                ResourceLocation potionLoc = ResourceLocation.tryParse(potionId);
                if (potionLoc == null) return false;
                
                var actualPotion = stack.get(DataComponents.POTION_CONTENTS);
                if (actualPotion == null) return false;
                
                // Compare potion type
                Optional<Holder<Potion>> actualPotionType = actualPotion.potion();
                if (actualPotionType.isEmpty()) return false;
                
                var key = actualPotionType.get().unwrapKey();
                return key.isPresent() && key.get().location().equals(potionLoc);
            }
            
            // For other items with component data, just match by item type
            return true;
        }
        
        /**
         * Returns whether this entry has custom component data.
         */
        public boolean hasComponentData() {
            return componentString != null && !componentString.isEmpty();
        }
    }
}
