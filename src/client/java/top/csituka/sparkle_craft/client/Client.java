package top.csituka.sparkle_craft.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import top.csituka.sparkle_craft.block.ModBlocks;
import top.csituka.sparkle_craft.block.entity.ModBlockEntities;
import top.csituka.sparkle_craft.client.render.FlyBeaconBarrierRenderer;
import top.csituka.sparkle_craft.client.render.ManaPipeBlockEntityRenderer;
import top.csituka.sparkle_craft.client.render.ManaTankBlockEntityRenderer;
import top.csituka.sparkle_craft.client.screen.CrystalManaExtractorScreen;
import top.csituka.sparkle_craft.client.screen.FlyBeaconScreen;
import top.csituka.sparkle_craft.screen.ModScreenHandlers;

public class Client implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.CRYSTAL_MANA_EXTRACTOR, CrystalManaExtractorScreen::new);
        HandledScreens.register(ModScreenHandlers.FLY_BEACON, FlyBeaconScreen::new);
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.MANA_PIPE, RenderLayer.getTranslucent());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.MANA_TANK, RenderLayer.getTranslucent());
        BlockEntityRendererFactories.register(ModBlockEntities.MANA_PIPE,
                ManaPipeBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(ModBlockEntities.MANA_TANK,
                ManaTankBlockEntityRenderer::new);
        FlyBeaconBarrierRenderer.register();
    }
}
