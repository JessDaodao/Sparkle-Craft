package org.example.sparkle_craft.integration.jade;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.example.sparkle_craft.block.entity.ManaPipeBlockEntity;
import org.example.sparkle_craft.sparkle_craft;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum ManaPipeComponentProvider implements IBlockComponentProvider,
        IServerDataProvider<BlockAccessor> {

    INSTANCE;

    private static final String MANA_TAG = "Mana";
    private static final Identifier UID = new Identifier(sparkle_craft.MOD_ID, "mana_pipe_mana");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getServerData().contains(MANA_TAG)) {
            tooltip.add(Text.translatable("jade.sparkle-craft.mana_pipe.mana",
                    accessor.getServerData().getInt(MANA_TAG), ManaPipeBlockEntity.MAX_MANA));
        }
    }

    @Override
    public void appendServerData(NbtCompound data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof ManaPipeBlockEntity manaPipe) {
            data.putInt(MANA_TAG, manaPipe.getMana());
        }
    }

    @Override
    public Identifier getUid() {
        return UID;
    }
}
