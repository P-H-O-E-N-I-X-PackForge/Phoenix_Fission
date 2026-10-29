package net.phoenix.phoenix_fission.api.machine.multiblock;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.mui.MachineUIPanelBuilder;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.phoenix.phoenix_fission.api.item.RtgCellItem;
import net.phoenix.phoenix_fission.client.gui.CanvasWidget;
import net.phoenix.phoenix_fission.client.gui.FissionPanelUi;
import net.phoenix.phoenix_fission.client.gui.RtgHud;
import net.phoenix.phoenix_fission.configs.PhoenixFissionConfigs;

import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.value.sync.PanelSyncManager;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds RTG cells in its item inputs and trickles their EU/t into the energy output. No fuel or coolant: each cell
 * wears one durability point per second and is left in place, spent, when it runs out.
 */
public class RtgArrayMachine extends WorkableElectricMultiblockMachine {

    @Getter
    @SyncToClient
    private long generatedEUt = 0;
    @Getter
    @SyncToClient
    private int activeCells = 0;

    /** One "item_id|remaining|max|eu" entry per loaded cell, refreshed about once a second for the screen. */
    @Getter
    @SyncToClient
    private List<String> cellInfo = new ArrayList<>();

    public static final int UI_WIDTH = 198;
    public static final int UI_HEIGHT = 190;

    private final ConditionalSubscriptionHandler tickHandler;

    public RtgArrayMachine(BlockEntityCreationInfo info, Object... args) {
        super(info);
        this.tickHandler = new ConditionalSubscriptionHandler(this, this::tickCells, this::isFormed);
    }

    @Override
    public void formStructure(String substructureName) {
        super.formStructure(substructureName);
        tickHandler.updateSubscription();
    }

    @Override
    public void invalidateStructure(String substructureName) {
        super.invalidateStructure(substructureName);
        generatedEUt = 0;
        activeCells = 0;
        cellInfo = new ArrayList<>();
        tickHandler.updateSubscription();
    }

    private void tickCells() {
        if (getLevel() == null || getLevel().isClientSide || !isFormed()) return;
        if (!isWorkingEnabled()) {
            generatedEUt = 0;
            activeCells = 0;
            return;
        }

        boolean wear = getOffsetTimer() % 20 == 0;
        long total = 0;
        int cells = 0;
        List<String> info = wear ? new ArrayList<>() : null;

        for (var handler : getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP)) {
            if (!(handler instanceof IItemHandlerModifiable items)) continue;
            for (int slot = 0; slot < items.getSlots(); slot++) {
                ItemStack stack = items.getStackInSlot(slot);
                if (!(stack.getItem() instanceof RtgCellItem cell) || stack.getDamageValue() >= stack.getMaxDamage())
                    continue;
                total += cell.getEuPerTick();
                cells++;
                if (wear) {
                    ItemStack worn = stack.copy();
                    worn.setDamageValue(worn.getDamageValue() + 1);
                    items.setStackInSlot(slot, worn);
                    info.add(BuiltInRegistries.ITEM.getKey(worn.getItem()) + "|" +
                            (worn.getMaxDamage() - worn.getDamageValue()) + "|" + worn.getMaxDamage() + "|" +
                            cell.getEuPerTick());
                }
            }
        }

        total = (long) Math.floor(total * Math.max(0, PhoenixFissionConfigs.INSTANCE.fission.rtg.euMultiplier));
        if (total > 0) getEnergyContainer().addEnergy(total);
        generatedEUt = total;
        activeCells = cells;
        if (wear) {
            cellInfo = info;
            getSyncDataHolder().markClientSyncFieldDirty("cellInfo");
        }
        getSyncDataHolder().markClientSyncFieldDirty("generatedEUt");
        getSyncDataHolder().markClientSyncFieldDirty("activeCells");
    }

    @Override
    public ModularPanel<?> buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        boolean client = getLevel() != null && getLevel().isClientSide;
        var panel = MachineUIPanelBuilder.panelBuilder(this).attachInventory(false).addTitleBar(false)
                .drawGTLogo(false)
                .mainContents(main -> {
                    if (client) main.child(new CanvasWidget(new RtgHud(this)).size(UI_WIDTH, UI_HEIGHT));
                    FissionPanelUi.addInventory(main, UI_WIDTH, UI_HEIGHT);
                }).build(syncManager, settings);
        if (client) panel.background(FissionPanelUi.BACKGROUND);
        return panel;
    }
}
