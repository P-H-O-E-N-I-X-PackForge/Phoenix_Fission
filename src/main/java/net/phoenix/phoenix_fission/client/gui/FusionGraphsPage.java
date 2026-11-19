package net.phoenix.phoenix_fission.client.gui;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.network.chat.Component;
import net.phoenix.phoenix_fission.api.machine.multiblock.FusionReactorMachine;

/** Side-tab page: output, field integrity and pressure over the last minute. */
public class FusionGraphsPage implements IFancyUIProvider {

    private final FusionReactorMachine machine;

    public FusionGraphsPage(FusionReactorMachine machine) {
        this.machine = machine;
    }

    @Override
    public IGuiTexture getTabIcon() {
        return new GuiTextureGroup(new ColorRectTexture(0xFF04090F), new TextTexture("~", 0xFF_00E5CC));
    }

    @Override
    public Component getTitle() {
        return Component.literal("Fusion Graphs");
    }

    @Override
    public boolean hasPlayerInventory() {
        return true;
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        return new CanvasWidget(new FusionGraphsPainter(machine, machine.getHistory()), FusionReactorMachine.UI_WIDTH,
                FusionReactorMachine.UI_HEIGHT);
    }
}
