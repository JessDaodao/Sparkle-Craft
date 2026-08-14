package org.example.sparkle_craft.world.gen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.world.gen.GenerationStep;

public class ModWorldGeneration {

    public static void generateOres() {
        BiomeModifications.addFeature(BiomeSelectors.tag(BiomeTags.IS_OVERWORLD),
                GenerationStep.Feature.UNDERGROUND_ORES, ModPlacedFeatures.MAGIC_CRYSTAL_ORE_PLACED_KEY);
    }
}
