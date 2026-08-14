package top.csituka.sparkle_craft.block;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import top.csituka.sparkle_craft.sparkle_craft;
import top.csituka.sparkle_craft.block.custom.CrystalManaExtractorBlock;
import top.csituka.sparkle_craft.block.custom.FlyBeaconBlock;
import top.csituka.sparkle_craft.block.custom.ManaPipeBlock;

public class ModBlocks {

    public static final Block MAGIC_CRYSTAL_ORE = registerBlock("magic_crystal_ore",
            new Block(FabricBlockSettings.copyOf(Blocks.DIAMOND_ORE)));

    public static final Block DEEPSLATE_MAGIC_CRYSTAL_ORE = registerBlock("deepslate_magic_crystal_ore",
            new Block(FabricBlockSettings.copyOf(Blocks.DEEPSLATE_DIAMOND_ORE)));

    public static final Block CRYSTAL_MANA_EXTRACTOR = registerBlock("crystal_mana_extractor",
            new CrystalManaExtractorBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK)
                    .strength(3.5f)
                    .requiresTool()));

    public static final Block MANA_PIPE = registerBlock("mana_pipe",
            new ManaPipeBlock(FabricBlockSettings.copyOf(Blocks.GLASS)
                    .strength(0.5f)
                    .nonOpaque()));

    public static final Block FLY_BEACON = registerBlock("fly_beacon",
            new FlyBeaconBlock(FabricBlockSettings.copyOf(Blocks.BEACON)
                    .strength(3.0f)
                    .requiresTool()));

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(sparkle_craft.MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block) {
        return Registry.register(Registries.ITEM, new Identifier(sparkle_craft.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    }

    public static void registerModBlocks() {
        sparkle_craft.LOGGER.info("Registering Mod Blocks for " + sparkle_craft.MOD_ID);
    }
}
