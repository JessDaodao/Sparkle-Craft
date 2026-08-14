package org.example.sparkle_craft.world.gen;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.example.sparkle_craft.sparkle_craft;

public class ModPlacedFeatures {

    public static final RegistryKey<PlacedFeature> MAGIC_CRYSTAL_ORE_PLACED_KEY =
            RegistryKey.of(RegistryKeys.PLACED_FEATURE, new Identifier(sparkle_craft.MOD_ID, "magic_crystal_ore"));
}
