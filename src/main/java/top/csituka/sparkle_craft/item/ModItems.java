package top.csituka.sparkle_craft.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import top.csituka.sparkle_craft.sparkle_craft;

public class ModItems {

    public static final Item MAGIC_CRYSTAL = registerItem("magic_crystal", new Item(new FabricItemSettings()));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(sparkle_craft.MOD_ID, name), item);
    }

    public static void registerModItems() {
        sparkle_craft.LOGGER.info("Registering Mod Items for " + sparkle_craft.MOD_ID);
    }
}
