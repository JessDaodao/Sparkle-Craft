package org.example.sparkle_craft.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import org.example.sparkle_craft.block.ModBlocks;
import org.example.sparkle_craft.block.entity.ModBlockEntities;
import org.example.sparkle_craft.client.render.ManaPipeBlockEntityRenderer;
import org.example.sparkle_craft.client.screen.CrystalManaExtractorScreen;
import org.example.sparkle_craft.client.screen.FlyBeaconScreen;
import org.example.sparkle_craft.screen.ModScreenHandlers;

public class Client implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.CRYSTAL_MANA_EXTRACTOR, CrystalManaExtractorScreen::new);
        HandledScreens.register(ModScreenHandlers.FLY_BEACON, FlyBeaconScreen::new);
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.MANA_PIPE, RenderLayer.getTranslucent());
        BlockEntityRendererFactories.register(ModBlockEntities.MANA_PIPE,
                ManaPipeBlockEntityRenderer::new);
    }
}
