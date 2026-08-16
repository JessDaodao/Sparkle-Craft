package top.csituka.sparkle_craft.integration.jade;

import top.csituka.sparkle_craft.block.custom.CrystalManaExtractorBlock;
import top.csituka.sparkle_craft.block.custom.FlyBeaconBlock;
import top.csituka.sparkle_craft.block.custom.LargeManaTankBlock;
import top.csituka.sparkle_craft.block.custom.ManaPipeBlock;
import top.csituka.sparkle_craft.block.custom.ManaTankBlock;
import top.csituka.sparkle_craft.block.entity.CrystalManaExtractorBlockEntity;
import top.csituka.sparkle_craft.block.entity.FlyBeaconBlockEntity;
import top.csituka.sparkle_craft.block.entity.LargeManaTankBlockEntity;
import top.csituka.sparkle_craft.block.entity.ManaPipeBlockEntity;
import top.csituka.sparkle_craft.block.entity.ManaTankBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class SparkleCraftJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(ManaContainerComponentProvider.INSTANCE,
                ManaPipeBlockEntity.class);
        registration.registerBlockDataProvider(ManaContainerComponentProvider.INSTANCE,
                ManaTankBlockEntity.class);
        registration.registerBlockDataProvider(ManaContainerComponentProvider.INSTANCE,
                LargeManaTankBlockEntity.class);
        registration.registerBlockDataProvider(ManaContainerComponentProvider.INSTANCE,
                CrystalManaExtractorBlockEntity.class);
        registration.registerBlockDataProvider(ManaContainerComponentProvider.INSTANCE,
                FlyBeaconBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(ManaContainerComponentProvider.INSTANCE,
                ManaPipeBlock.class);
        registration.registerBlockComponent(ManaContainerComponentProvider.INSTANCE,
                ManaTankBlock.class);
        registration.registerBlockComponent(ManaContainerComponentProvider.INSTANCE,
                LargeManaTankBlock.class);
        registration.registerBlockComponent(ManaContainerComponentProvider.INSTANCE,
                CrystalManaExtractorBlock.class);
        registration.registerBlockComponent(ManaContainerComponentProvider.INSTANCE,
                FlyBeaconBlock.class);
    }
}
