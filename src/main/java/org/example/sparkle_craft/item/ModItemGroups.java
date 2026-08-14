package org.example.sparkle_craft.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.example.sparkle_craft.block.ModBlocks;
import org.example.sparkle_craft.sparkle_craft;
import org.example.sparkle_craft.item.ModItems;

public class ModItemGroups {

    public static final ItemGroup SPARKLE_CRAFT_GROUP = FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.sparkle-craft"))
            .icon(() -> new ItemStack(ModBlocks.MAGIC_CRYSTAL_ORE))
            .entries((displayContext, entries) -> {
                entries.add(ModItems.MAGIC_CRYSTAL);
                entries.add(ModBlocks.MAGIC_CRYSTAL_ORE);
                entries.add(ModBlocks.DEEPSLATE_MAGIC_CRYSTAL_ORE);
                entries.add(ModBlocks.CRYSTAL_MANA_EXTRACTOR);
                entries.add(ModBlocks.MANA_PIPE);
            })
            .build();

    public static void registerItemGroups() {
        Registry.register(Registries.ITEM_GROUP,
                new Identifier(sparkle_craft.MOD_ID, "sparkle_craft"), SPARKLE_CRAFT_GROUP);
    }
}
