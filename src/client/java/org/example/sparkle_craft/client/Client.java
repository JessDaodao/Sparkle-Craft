package org.example.sparkle_craft.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import org.example.sparkle_craft.client.screen.CrystalManaExtractorScreen;
import org.example.sparkle_craft.screen.ModScreenHandlers;

public class Client implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.CRYSTAL_MANA_EXTRACTOR, CrystalManaExtractorScreen::new);
    }
}
