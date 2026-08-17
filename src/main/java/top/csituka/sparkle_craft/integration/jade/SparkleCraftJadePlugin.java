package top.csituka.sparkle_craft.integration.jade;

import top.csituka.sparkle_craft.block.entity.CrystalManaExtractorBlockEntity;
import top.csituka.sparkle_craft.block.entity.FlyBeaconBlockEntity;
import top.csituka.sparkle_craft.block.entity.ManaPipeBlockEntity;
import top.csituka.sparkle_craft.block.entity.ManaTankBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@WailaPlugin
public class SparkleCraftJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(ManaContainerComponentProvider.INSTANCE,
                ManaPipeBlockEntity.class);
        registration.registerBlockDataProvider(ManaContainerComponentProvider.INSTANCE,
                ManaTankBlockEntity.class);
        registration.registerBlockDataProvider(ManaContainerComponentProvider.INSTANCE,
                CrystalManaExtractorBlockEntity.class);
        registration.registerBlockDataProvider(ManaContainerComponentProvider.INSTANCE,
                FlyBeaconBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        try {
            Class<?> clientProvider = Class.forName(
                    "top.csituka.sparkle_craft.integration.jade.ManaContainerJadeClientProvider");
            Method register = clientProvider.getMethod("register", IWailaClientRegistration.class);
            register.invoke(null, registration);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException
                 | InvocationTargetException exception) {
            throw new IllegalStateException(
                    "Unable to register Sparkle Craft Jade components", exception);
        }
    }
}
