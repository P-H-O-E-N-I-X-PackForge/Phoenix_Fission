package net.phoenix.phoenix_fission.integration.jade.provider;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.phoenix.phoenix_fission.PhoenixFission;
import net.phoenix.phoenix_fission.api.machine.multiblock.RtgArrayMachine;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class RtgArrayProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    public static final ResourceLocation UID = PhoenixFission.id("rtg_array_info");

    private static final String NBT_EUT = "pf_rtg_eut";
    private static final String NBT_CELLS = "pf_rtg_cells";
    private static final String NBT_SHORTEST = "pf_rtg_shortest";

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity be) ||
                !(be.getMetaMachine() instanceof RtgArrayMachine))
            return;
        CompoundTag data = accessor.getServerData();
        if (data.isEmpty()) return;

        int cells = data.getInt(NBT_CELLS);
        if (cells <= 0) {
            tooltip.add(Component.translatable("jade.phoenix_fission.rtg_no_cells")
                    .withStyle(s -> s.withColor(0xFFAA00)));
            return;
        }
        tooltip.add(Component.translatable("jade.phoenix_fission.rtg_output", data.getLong(NBT_EUT))
                .withStyle(s -> s.withColor(0x33FF88)));
        tooltip.add(Component.translatable("jade.phoenix_fission.rtg_cells", cells));
        long shortest = data.getLong(NBT_SHORTEST);
        tooltip.add(Component.translatable("jade.phoenix_fission.rtg_shortest",
                String.format("%dh %02dm", shortest / 3600, (shortest % 3600) / 60)));
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity be) ||
                !(be.getMetaMachine() instanceof RtgArrayMachine machine) || !machine.isFormed())
            return;
        tag.putLong(NBT_EUT, machine.getGeneratedEUt());
        tag.putInt(NBT_CELLS, machine.getActiveCells());
        long shortest = Long.MAX_VALUE;
        for (String entry : machine.getCellInfo()) {
            String[] p = entry.split("\\|");
            if (p.length >= 2) {
                try {
                    shortest = Math.min(shortest, Long.parseLong(p[1]));
                } catch (NumberFormatException ignored) {}
            }
        }
        tag.putLong(NBT_SHORTEST, shortest == Long.MAX_VALUE ? 0 : shortest);
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
