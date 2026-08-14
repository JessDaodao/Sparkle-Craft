package org.example.sparkle_craft;

import net.fabricmc.api.ModInitializer;
import org.example.sparkle_craft.block.ModBlocks;
import org.example.sparkle_craft.item.ModItemGroups;
import org.example.sparkle_craft.world.gen.ModWorldGeneration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class sparkle_craft implements ModInitializer {

    public static final String MOD_ID = "sparkle-craft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModBlocks.registerModBlocks();
        ModItemGroups.registerItemGroups();
        ModWorldGeneration.generateOres();
    }
}
