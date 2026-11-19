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

/** Side-tab page: the worked numbers behind the Tokamak. */
public class FusionMathPage implements IFancyUIProvider {

    private final FusionReactorMachine machine;

    public FusionMathPage(FusionReactorMachine machine) {
        this.machine = machine;
    }

    @Override
    public IGuiTexture getTabIcon() {
        return new GuiTextureGroup(new ColorRectTexture(0xFF04090F), new TextTexture("M", 0xFF_00E5CC));
    }

    @Override
    public Component getTitle() {
        return Component.literal("Fusion Math");
    }

    @Override
    public boolean hasPlayerInventory() {
        return false;
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        var size = widget.getPageContainer().getSize();
        return new CanvasWidget(new FusionMathPainter(machine), size.width, size.height);
    }
}
