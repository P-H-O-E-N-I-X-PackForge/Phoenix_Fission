package net.phoenix.phoenix_fission.integration.jade.provider;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;
import net.phoenix.phoenix_fission.PhoenixFission;
import net.phoenix.phoenix_fission.api.block.iface.IFissionBlanketType;
import net.phoenix.phoenix_fission.api.machine.multiblock.BreederWorkableElectricMultiblockMachine;
import net.phoenix.phoenix_fission.api.machine.multiblock.FissionWorkableElectricMultiblockMachine;
import net.phoenix.phoenix_fission.api.machine.multiblock.MoltenSaltReactorMultiblockMachine;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class FissionMachineProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    public static final ResourceLocation UID = PhoenixFission.id("fission_machine_info");

    private static final String NBT_HEAT = "pf_heat";
    private static final String NBT_NET_HEAT = "pf_net_heat";
    private static final String NBT_MELTDOWN_SECONDS = "pf_meltdown_seconds";
    private static final String NBT_HAS_COOLANT = "pf_has_coolant";
    private static final String NBT_IS_BREEDER = "pf_is_breeder";
    private static final String NBT_IS_MSR = "pf_is_msr";
    private static final String NBT_XENON_LEVEL = "pf_xenon_level";
    private static final String NBT_CATALYST_ACTIVE = "pf_catalyst_active";
    private static final String NBT_RUNNING = "pf_running";
    private static final String NBT_PARALLELS = "pf_parallels";
    private static final String NBT_EUT = "pf_eut";
    private static final String NBT_RODS = "pf_rods";
    private static final String NBT_COOLERS = "pf_coolers";
    private static final String NBT_MODS = "pf_mods";
    private static final String NBT_BLANKETS = "pf_blankets";
    private static final String NBT_COOLING_POWER = "pf_cooling_power";
    private static final String NBT_BLANKET_INPUT = "pf_blanket_input";
    private static final String NBT_BLANKET_OUTPUT = "pf_blanket_output";
    private static final String NBT_BLANKET_OUTPUTS = "pf_blanket_outputs";
    private static final String NBT_BLANKET_AMOUNT = "pf_blanket_amount";

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!config.get(UID)) return;

        if (!(accessor.getBlockEntity() instanceof MetaMachineBlockEntity metaBE &&
                metaBE.getMetaMachine() instanceof FissionWorkableElectricMultiblockMachine)) {
            return;
        }

        CompoundTag data = accessor.getServerData();
        if (data.isEmpty()) return;

        boolean isMSR = data.getBoolean(NBT_IS_MSR);
        double heat = data.getDouble(NBT_HEAT);
        double netHeat = data.getDouble(NBT_NET_HEAT);

        int meltdownSeconds = data.getInt(NBT_MELTDOWN_SECONDS);
        if (meltdownSeconds > 0) {
            tooltip.add(Component.translatable("jade.phoenix_fission.fission_meltdown_timer", meltdownSeconds)
                    .withStyle(s -> s.withColor(0xFFAA00)));
            tooltip.add(Component.translatable("jade.phoenix_fission.heat", (long) heat));
        } else {
            tooltip.add(Component.translatable("jade.phoenix_fission.fission_safe")
                    .withStyle(s -> s.withColor(0x33FF33)));
        }

        if (!data.getBoolean(NBT_HAS_COOLANT)) {
            tooltip.add(Component.translatable("jade.phoenix_fission.fission_no_coolant")
                    .withStyle(s -> s.withColor(0xFF3333)));
        }

        if (netHeat > 0.0001) {
            tooltip.add(Component.translatable("jade.phoenix_fission.fission_heating")
                    .withStyle(s -> s.withColor(0xFF5555)));
        }

        int parallels = data.getInt(NBT_PARALLELS);
        long eut = data.getLong(NBT_EUT);

        tooltip.add(Component.literal("Parallels: " + parallels));
        if (PhoenixFissionConfigs.INSTANCE.fission.enableDirectEUOutput) {
            tooltip.add(Component.literal("EU/t: " + eut));
        }

        if (isMSR) {
            double xenon = data.getDouble(NBT_XENON_LEVEL);
            String formattedXenon = String.format(java.util.Locale.ROOT, "%.1f", xenon * 100) + "%";
            int xenonColor = xenon < 0.05 ? 0x33FF33 : (xenon > 0.35 ? 0xFF3333 : 0xAA00AA);

            if (xenon < 0.05 && eut > 0) {
                tooltip.add(Component.translatable("phoenix_fission.msr.xenon_clean")
                        .withStyle(s -> s.withColor(0x33FF33)));
            } else {
                tooltip.add(Component.translatable("phoenix_fission.msr.xenon_poisoning", formattedXenon)
                        .withStyle(s -> s.withColor(xenonColor)));
            }

            if (data.getBoolean(NBT_CATALYST_ACTIVE)) {
                tooltip.add(Component.translatable("phoenix_fission.msr.catalyst_fluorine")
                        .withStyle(s -> s.withColor(0x55FFFF)));
            }
        } else {
            int rods = data.getInt(NBT_RODS);
            int coolers = data.getInt(NBT_COOLERS);
            int mods = data.getInt(NBT_MODS);
            tooltip.add(Component.literal("Rods: " + rods + "  Mods: " + mods + "  Coolers: " + coolers));
        }

        int coolingPower = data.getInt(NBT_COOLING_POWER);
        tooltip.add(Component.literal("Max Cool @ Cap: " + coolingPower + " HU/t")
                .withStyle(s -> s.withColor(0x55FFFF)));

        if (!isMSR && data.getBoolean(NBT_IS_BREEDER) && data.getInt(NBT_BLANKETS) > 0 &&
                data.contains(NBT_BLANKET_INPUT)) {
            String inKey = data.getString(NBT_BLANKET_INPUT);
            Component inName = resolveKeyToDisplayName(inKey);

            tooltip.add(Component.translatable("jade.phoenix_fission.blanket_input", inName)
                    .withStyle(s -> s.withColor(0xAAAAFF)));

            if (data.contains(NBT_BLANKET_OUTPUT)) {
                String outKey = data.getString(NBT_BLANKET_OUTPUT);
                if (!outKey.isEmpty()) {
                    Component outName = resolveKeyToDisplayName(outKey);
                    tooltip.add(Component.translatable("jade.phoenix_fission.blanket_output", outName)
                            .withStyle(s -> s.withColor(0xFFBBFF)));
                }
            }

            if (data.contains(NBT_BLANKET_OUTPUTS, Tag.TAG_LIST)) {
                ListTag list = data.getList(NBT_BLANKET_OUTPUTS, Tag.TAG_STRING);
                if (!list.isEmpty()) {
                    tooltip.add(Component.translatable("phoenix_fission.blanket.potential_outputs")
                            .withStyle(s -> s.withColor(0xFFDD88)));

                    int shown = 0;
                    for (int i = 0; i < list.size(); i++) {
                        if (shown++ >= 5) break;

                        String entry = list.getString(i);
                        String[] parts = entry.split("\\|");
                        String key = parts.length > 0 ? parts[0] : entry;
                        String w = parts.length > 1 ? parts[1] : "?";
                        String inst = parts.length > 2 ? parts[2] : "?";

                        Component outName = resolveKeyToDisplayName(key);
                        tooltip.add(Component.literal("- ")
                                .append(outName)
                                .append(Component.literal("  w=" + w + "  inst=" + inst)
                                        .withStyle(s -> s.withColor(0x888888)))
                                .withStyle(s -> s.withColor(0xCCCCCC)));
                    }
                }
            }

            if (data.contains(NBT_BLANKET_AMOUNT)) {
                int amt = data.getInt(NBT_BLANKET_AMOUNT);
                tooltip.add(Component.translatable("jade.phoenix_fission.blanket_amount", amt)
                        .withStyle(s -> s.withColor(0xBBBBBB)));
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof MetaMachineBlockEntity metaBE &&
                metaBE.getMetaMachine() instanceof FissionWorkableElectricMultiblockMachine machine)) {
            return;
        }

        boolean msr = machine instanceof MoltenSaltReactorMultiblockMachine;
        tag.putBoolean(NBT_IS_MSR, msr);

        tag.putInt(NBT_COOLERS, machine.getComponentManager().getActiveCoolers().size());
        tag.putDouble(NBT_HEAT, machine.getHeat());
        tag.putDouble(NBT_NET_HEAT, machine.lastHeatGainedPerTick - machine.lastHeatRemovedPerTick);
        tag.putBoolean(NBT_HAS_COOLANT, machine.lastHasCoolant);
        tag.putBoolean(NBT_RUNNING, machine.getRecipeLogic().isWorking());
        tag.putInt(NBT_PARALLELS, machine.lastParallels);
        tag.putLong(NBT_EUT, machine.lastGeneratedEUt);
        tag.putInt(NBT_COOLING_POWER, machine.lastProvidedCooling);

        int secondsRemaining = machine.meltdownTimerTicks > 0 ? (int) Math.ceil(machine.meltdownTimerTicks / 20.0) : 0;
        tag.putInt(NBT_MELTDOWN_SECONDS, secondsRemaining);

        if (msr) {
            MoltenSaltReactorMultiblockMachine msrMachine = (MoltenSaltReactorMultiblockMachine) machine;
            try {
                java.lang.reflect.Field xenonField = MoltenSaltReactorMultiblockMachine.class
                        .getDeclaredField("xenonPoisonLevel");
                xenonField.setAccessible(true);
                double xenonVal = xenonField.getDouble(msrMachine);
                tag.putDouble(NBT_XENON_LEVEL, xenonVal);

                java.lang.reflect.Field catalystField = MoltenSaltReactorMultiblockMachine.class
                        .getDeclaredField("lastCatalystActive");
                catalystField.setAccessible(true);
                tag.putBoolean(NBT_CATALYST_ACTIVE, catalystField.getBoolean(msrMachine));
            } catch (Throwable ignored) {
                tag.putDouble(NBT_XENON_LEVEL, 0.0);
                tag.putBoolean(NBT_CATALYST_ACTIVE, false);
            }

            tag.putInt(NBT_RODS, 0);
            tag.putInt(NBT_MODS, 0);
            tag.putInt(NBT_BLANKETS, 0);
            return;
        }

        tag.putInt(NBT_RODS, machine.getComponentManager().getActiveFuelRods().size());
        tag.putInt(NBT_MODS, machine.getComponentManager().getActiveModerators().size());
        tag.putInt(NBT_BLANKETS, machine.getComponentManager().getActiveBlankets().size());

        boolean breeder = machine instanceof BreederWorkableElectricMultiblockMachine;
        tag.putBoolean(NBT_IS_BREEDER, breeder);

        if (breeder) {
            BreederWorkableElectricMultiblockMachine b = (BreederWorkableElectricMultiblockMachine) machine;
            var activeBlankets = b.getComponentManager().getActiveBlankets();
            boolean hasBlankets = activeBlankets != null && !activeBlankets.isEmpty() && b.getPrimaryBlanket() != null;

            if (hasBlankets) {
                IFissionBlanketType primary = b.getPrimaryBlanket();
                tag.putInt(NBT_BLANKETS, activeBlankets.size());
                tag.putString(NBT_BLANKET_INPUT, primary.getInputKey());
                tag.putInt(NBT_BLANKET_AMOUNT, Math.max(0, primary.getAmountPerCycle()));

                String primaryOut = "";
                if (primary.getOutputs() != null && !primary.getOutputs().isEmpty() &&
                        primary.getOutputs().get(0) != null) {
                    primaryOut = primary.getOutputs().get(0).key();
                }
                tag.putString(NBT_BLANKET_OUTPUT, primaryOut);

                ListTag outs = new ListTag();
                if (primary.getOutputs() != null) {
                    for (var o : primary.getOutputs()) {
                        if (o == null) continue;
                        outs.add(StringTag.valueOf(o.key() + "|" + o.weight() + "|" + o.instability()));
                    }
                }
                tag.put(NBT_BLANKET_OUTPUTS, outs);
            } else {
                clearBreederNBT(tag);
            }
        } else {
            clearBreederNBT(tag);
        }
    }

    private void clearBreederNBT(CompoundTag tag) {
        tag.remove(NBT_BLANKET_INPUT);
        tag.remove(NBT_BLANKET_OUTPUT);
        tag.remove(NBT_BLANKET_OUTPUTS);
        tag.remove(NBT_BLANKET_AMOUNT);
        tag.putInt(NBT_BLANKETS, 0);
    }

    private static Component resolveKeyToDisplayName(String key) {
        if (key == null || key.isEmpty() || "none".equalsIgnoreCase(key)) {
            return Component.literal("None");
        }

        Material mat = GTMaterials.get(key);
        if (mat != GTMaterials.NULL) {
            // Idk what the proper method is lol.
            String transKey = mat.getDefaultTranslation();
            if (transKey != null && !transKey.isEmpty()) {
                return Component.translatable(transKey);
            }
            return Component.literal(key);
        }

        ResourceLocation rl = ResourceLocation.tryParse(key);
        if (rl != null) {
            var item = ForgeRegistries.ITEMS.getValue(rl);
            if (item != null && item != Items.AIR) {
                return new ItemStack(item, 1).getHoverName();
            }

            Fluid fluid = ForgeRegistries.FLUIDS.getValue(rl);
            if (fluid != null && fluid != Fluids.EMPTY) {
                return Component.translatable(fluid.getFluidType().getDescriptionId());
            }
        }

        return Component.literal(key);
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
