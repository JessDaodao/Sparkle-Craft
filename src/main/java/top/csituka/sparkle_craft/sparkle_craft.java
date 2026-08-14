package top.csituka.sparkle_craft;

import net.fabricmc.api.ModInitializer;
import top.csituka.sparkle_craft.block.ModBlocks;
import top.csituka.sparkle_craft.block.entity.FlyBeaconFlightManager;
import top.csituka.sparkle_craft.block.entity.ModBlockEntities;
import top.csituka.sparkle_craft.item.ModItemGroups;
import top.csituka.sparkle_craft.item.ModItems;
import top.csituka.sparkle_craft.screen.ModScreenHandlers;
import top.csituka.sparkle_craft.world.gen.ModWorldGeneration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class sparkle_craft implements ModInitializer {

    public static final String MOD_ID = "sparkle-craft";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModBlocks.registerModBlocks();
        ModItems.registerModItems();
        ModBlockEntities.registerModBlockEntities();
        ModScreenHandlers.registerScreenHandlers();
        ModItemGroups.registerItemGroups();
        ModWorldGeneration.generateOres();
        FlyBeaconFlightManager.register();
    }
}
