package org.example.sparkle_craft.block.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.example.sparkle_craft.block.ModBlocks;
import org.example.sparkle_craft.sparkle_craft;

public class ModBlockEntities {

    public static final BlockEntityType<CrystalManaExtractorBlockEntity> CRYSTAL_MANA_EXTRACTOR =
            Registry.register(Registries.BLOCK_ENTITY_TYPE,
                    new Identifier(sparkle_craft.MOD_ID, "crystal_mana_extractor"),
                    FabricBlockEntityTypeBuilder.create(CrystalManaExtractorBlockEntity::new,
                            ModBlocks.CRYSTAL_MANA_EXTRACTOR).build());

    public static final BlockEntityType<ManaPipeBlockEntity> MANA_PIPE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE,
                    new Identifier(sparkle_craft.MOD_ID, "mana_pipe"),
                    FabricBlockEntityTypeBuilder.create(ManaPipeBlockEntity::new,
                            ModBlocks.MANA_PIPE).build());

    public static void registerModBlockEntities() {
        sparkle_craft.LOGGER.info("Registering Mod Block Entities for " + sparkle_craft.MOD_ID);
    }
}
