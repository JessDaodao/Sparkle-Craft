package org.example.sparkle_craft.screen;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import org.example.sparkle_craft.sparkle_craft;

public class ModScreenHandlers {

    public static final ScreenHandlerType<CrystalManaExtractorScreenHandler> CRYSTAL_MANA_EXTRACTOR =
            Registry.register(Registries.SCREEN_HANDLER,
                    new Identifier(sparkle_craft.MOD_ID, "crystal_mana_extractor"),
                    new ScreenHandlerType<>(CrystalManaExtractorScreenHandler::new, FeatureFlags.VANILLA_FEATURES));

    public static void registerScreenHandlers() {
        sparkle_craft.LOGGER.info("Registering Screen Handlers for " + sparkle_craft.MOD_ID);
    }
}
