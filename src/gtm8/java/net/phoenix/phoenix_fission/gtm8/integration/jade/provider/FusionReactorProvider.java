package net.phoenix.phoenix_fission.gtm8.integration.jade.provider;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.phoenix.phoenix_fission.gtm8.PhoenixFission;
import net.phoenix.phoenix_fission.gtm8.api.machine.multiblock.FusionReactorMachine;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class FusionReactorProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    public static final ResourceLocation UID = PhoenixFission.id("fusion_reactor_info");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof FusionReactorMachine)) return;
        CompoundTag d = accessor.getServerData();
        if (d.isEmpty() || !d.getBoolean("formed")) return;

        if (d.getBoolean("scram")) {
            tooltip.add(Component.translatable("jade.phoenix_fission.fusion_scrammed")
                    .withStyle(s -> s.withColor(0xFF3333)));
        } else if (d.getInt("quench") > 0) {
            tooltip.add(Component.translatable("jade.phoenix_fission.fusion_quenched", d.getInt("quench") / 20 + 1)
                    .withStyle(s -> s.withColor(0xFF3333)));
        } else if (d.getBoolean("plasma")) {
            tooltip.add(Component.translatable("jade.phoenix_fission.fusion_output", d.getLong("eut"))
                    .withStyle(s -> s.withColor(0x33FF88)));
        } else {
            tooltip.add(Component.translatable("jade.phoenix_fission.fusion_standby")
                    .withStyle(s -> s.withColor(0xFFC040)));
        }
        tooltip.add(Component.translatable("jade.phoenix_fission.fusion_field",
                Math.round(100 - d.getDouble("drift")), String.format("%.2f", d.getDouble("ratio"))));
        tooltip.add(Component.translatable("jade.phoenix_fission.fusion_throttle", d.getInt("throttle")));
        if (!d.getBoolean("coolant")) {
            tooltip.add(Component.translatable("jade.phoenix_fission.fusion_no_coolant")
                    .withStyle(s -> s.withColor(0xFF8833)));
        }
        if (d.getDouble("contamination") > 0) {
            tooltip.add(Component.translatable("jade.phoenix_fission.fusion_contamination",
                    Math.round(d.getDouble("contamination"))));
        }
        tooltip.add(Component.translatable("jade.phoenix_fission.fusion_compute", d.getInt("rack"),
                d.getInt("compute"), d.getInt("recipe_tier")));
        tooltip.add(Component.translatable("jade.phoenix_fission.fusion_lanes", d.getInt("parallels"),
                d.getInt("running_threads") + "/" + d.getInt("threads"), d.getInt("oc")));
        tooltip.add(Component.translatable("jade.phoenix_fission.fusion_pressure", Math.round(d.getDouble("pressure")),
                Math.round(d.getDouble("required_pressure"))));
        if (!d.getBoolean("gas")) {
            tooltip.add(Component.translatable("jade.phoenix_fission.fusion_no_gas")
                    .withStyle(s -> s.withColor(0xFF8833)));
        }
        if (d.getBoolean("radiation")) {
            tooltip.add(Component.translatable("jade.phoenix_fission.fusion_irradiated")
                    .withStyle(s -> s.withColor(0x88FF66)));
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof FusionReactorMachine m)) return;
        tag.putBoolean("formed", m.isFormed());
        tag.putBoolean("scram", m.isScrammed());
        tag.putInt("quench", m.getQuenchTicks());
        tag.putBoolean("plasma", m.isPlasmaOn());
        tag.putLong("eut", m.getGeneratedEUt());
        tag.putDouble("drift", m.getDrift());
        tag.putDouble("ratio", m.getFieldRatio());
        tag.putInt("throttle", m.getThrottle());
        tag.putBoolean("coolant", m.isCoolantOk());
        tag.putDouble("contamination", m.getContamination());
        tag.putInt("rack", m.getRackLayers());
        tag.putInt("compute", m.getCompute());
        tag.putInt("recipe_tier", m.getRecipeTier());
        tag.putInt("parallels", m.getParallels());
        tag.putInt("threads", m.getThreads());
        tag.putInt("oc", m.getOcLevels());
        tag.putInt("running_threads", m.getRunningThreads());
        tag.putDouble("pressure", m.getPressure());
        tag.putDouble("required_pressure", m.getRequiredPressure());
        tag.putBoolean("gas", m.isGasOk());
        tag.putBoolean("radiation", m.isRadiationActive());
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
