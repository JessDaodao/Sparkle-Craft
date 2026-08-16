package top.csituka.sparkle_craft.integration.jade;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import top.csituka.sparkle_craft.block.entity.CrystalManaExtractorBlockEntity;
import top.csituka.sparkle_craft.block.entity.FlyBeaconBlockEntity;
import top.csituka.sparkle_craft.block.entity.ManaPipeBlockEntity;
import top.csituka.sparkle_craft.block.entity.ManaTankBlockEntity;
import top.csituka.sparkle_craft.sparkle_craft;

public enum ManaContainerComponentProvider implements IBlockComponentProvider,
        IServerDataProvider<BlockAccessor> {

    INSTANCE;

    private static final String MANA_TAG = "Mana";
    private static final String MAX_MANA_TAG = "MaxMana";
    private static final Identifier UID = new Identifier(sparkle_craft.MOD_ID, "mana_pipe_mana");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        NbtCompound data = accessor.getServerData();
        if (data.contains(MANA_TAG) && data.contains(MAX_MANA_TAG)) {
            tooltip.add(Text.translatable("jade.sparkle-craft.mana_container.mana",
                    data.getInt(MANA_TAG), data.getInt(MAX_MANA_TAG)));
        }
    }

    @Override
    public void appendServerData(NbtCompound data, BlockAccessor accessor) {
        ManaData manaData = getManaData(accessor.getBlockEntity());
        if (manaData != null) {
            data.putInt(MANA_TAG, manaData.mana());
            data.putInt(MAX_MANA_TAG, manaData.maxMana());
        }
    }

    @Override
    public Identifier getUid() {
        return UID;
    }

    private static ManaData getManaData(BlockEntity blockEntity) {
        if (blockEntity instanceof ManaPipeBlockEntity manaPipe) {
            return new ManaData(manaPipe.getMana(), ManaPipeBlockEntity.MAX_MANA);
        }
        if (blockEntity instanceof ManaTankBlockEntity manaTank) {
            return new ManaData(manaTank.getMana(), manaTank.getMaxMana());
        }
        if (blockEntity instanceof CrystalManaExtractorBlockEntity extractor) {
            return new ManaData(extractor.getMana(), CrystalManaExtractorBlockEntity.MAX_MANA);
        }
        if (blockEntity instanceof FlyBeaconBlockEntity beacon) {
            return new ManaData(beacon.getMana(), FlyBeaconBlockEntity.MAX_MANA);
        }
        return null;
    }

    private record ManaData(int mana, int maxMana) {
    }
}
