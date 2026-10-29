package net.phoenix.phoenix_fission.integration.jade;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.phoenix.phoenix_fission.integration.jade.provider.FissionMachineProvider;
import net.phoenix.phoenix_fission.integration.jade.provider.RtgArrayProvider;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class PhoenixJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(new FissionMachineProvider(), BlockEntity.class);
        registration.registerBlockDataProvider(new RtgArrayProvider(), BlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new FissionMachineProvider(), Block.class);
        registration.registerBlockComponent(new RtgArrayProvider(), Block.class);
    }
}
