package net.phoenix.phoenix_fission.api.machine.multiblock;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ForgeRegistries;
import net.phoenix.phoenix_fission.api.item.RtgCellItem;
import net.phoenix.phoenix_fission.client.gui.RtgWidget;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds RTG cells in its item inputs and trickles their EU/t into the energy output. No fuel or coolant: each cell
 * wears one durability point per second and is left in place, spent, when it runs out.
 */
public class RtgArrayMachine extends WorkableElectricMultiblockMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            RtgArrayMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    public static final int UI_WIDTH = 198;
    public static final int UI_HEIGHT = 190;

    @Getter
    @DescSynced
    private long generatedEUt = 0;
    @Getter
    @DescSynced
    private int activeCells = 0;
    /** One "item_id|remaining|max|eu" entry per loaded cell, refreshed about once a second for the screen. */
    @Getter
    @DescSynced
    private List<String> cellInfo = new ArrayList<>();

    private final ConditionalSubscriptionHandler tickHandler;

    public RtgArrayMachine(IMachineBlockEntity holder, Object... args) {
        super(holder);
        this.tickHandler = new ConditionalSubscriptionHandler(this, this::tickCells, this::isFormed);
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        tickHandler.updateSubscription();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        generatedEUt = 0;
        activeCells = 0;
        cellInfo = new ArrayList<>();
        tickHandler.updateSubscription();
    }

    private void tickCells() {
        if (getLevel() == null || getLevel().isClientSide || !isFormed()) return;
        if (!getRecipeLogic().isWorkingEnabled()) {
            generatedEUt = 0;
            activeCells = 0;
            return;
        }

        boolean wear = getOffsetTimer() % 20 == 0;
        long total = 0;
        int cells = 0;
        List<String> info = wear ? new ArrayList<>() : null;

        var lists = getCapabilitiesProxy().get(IO.IN);
        if (lists != null) {
            for (var list : lists) {
                for (var handler : list.getCapability(ItemRecipeCapability.CAP)) {
                    if (!(handler instanceof IItemHandlerModifiable items)) continue;
                    for (int slot = 0; slot < items.getSlots(); slot++) {
                        ItemStack stack = items.getStackInSlot(slot);
                        if (!(stack.getItem() instanceof RtgCellItem cell) ||
                                stack.getDamageValue() >= stack.getMaxDamage())
                            continue;
                        total += cell.getEuPerTick();
                        cells++;
                        if (wear) {
                            ItemStack worn = stack.copy();
                            worn.setDamageValue(worn.getDamageValue() + 1);
                            items.setStackInSlot(slot, worn);
                            info.add(ForgeRegistries.ITEMS.getKey(worn.getItem()) + "|" +
                                    (worn.getMaxDamage() - worn.getDamageValue()) + "|" + worn.getMaxDamage() + "|" +
                                    cell.getEuPerTick());
                        }
                    }
                }
            }
        }

        total = (long) Math.floor(total * Math.max(0, PhoenixFissionConfigs.INSTANCE.fission.rtg.euMultiplier));
        if (total > 0) getEnergyContainer().addEnergy(total);
        generatedEUt = total;
        activeCells = cells;
        if (wear) cellInfo = info;
    }

    @Override
    public @NotNull ModularUI createUI(@NotNull Player entityPlayer) {
        return new ModularUI(UI_WIDTH, UI_HEIGHT, this, entityPlayer).widget(new RtgWidget(this, UI_WIDTH,
                UI_HEIGHT));
    }
}
