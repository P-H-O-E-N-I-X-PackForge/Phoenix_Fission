package net.phoenix.phoenix_fission.client.gui;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.phoenix.phoenix_fission.api.machine.multiblock.AscensionReactorMachine;

/** Side-tab page: the Ascension Reactor's three gauges and the controls that steer them. */
public class AscensionPage implements IFancyUIProvider {

    private static final int ACTION_CONTROL = 2;

    private final AscensionReactorMachine machine;

    public AscensionPage(AscensionReactorMachine machine) {
        this.machine = machine;
    }

    @Override
    public IGuiTexture getTabIcon() {
        return new GuiTextureGroup(new ColorRectTexture(0xFF04090F), new TextTexture("A", 0xFF_00E5CC));
    }

    @Override
    public Component getTitle() {
        return Component.literal("Ascension");
    }

    @Override
    public boolean hasPlayerInventory() {
        return false;
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        var size = widget.getPageContainer().getSize();
        return new Canvas(machine, size.width, size.height);
    }

    /** The HUD; its controls are sent to the server, which is where the machine is steered. */
    private static final class Canvas extends CanvasWidget {

        private final AscensionReactorMachine machine;

        Canvas(AscensionReactorMachine machine, int w, int h) {
            super(new Painter(machine), w, h);
            this.machine = machine;
            ((Painter) painter()).bind(this);
        }

        void send(int which, int value) {
            writeClientAction(ACTION_CONTROL, buf -> {
                buf.writeVarInt(which);
                buf.writeVarInt(value);
            });
        }

        @Override
        public void handleClientAction(int id, FriendlyByteBuf buf) {
            if (id == ACTION_CONTROL) {
                int which = buf.readVarInt();
                int value = buf.readVarInt();
                machine.applyControl(which, value);
            } else {
                super.handleClientAction(id, buf);
            }
        }
    }

    private static final class Painter implements CanvasPainter {

        private final AscensionHud hud;
        private Canvas canvas;

        Painter(AscensionReactorMachine machine) {
            this.hud = new AscensionHud(machine, machine.getAscensionHistory(),
                    (which, value) -> {
                        if (canvas != null) canvas.send(which, value);
                    });
        }

        void bind(Canvas canvas) {
            this.canvas = canvas;
        }

        @Override
        public void render(net.minecraft.client.gui.GuiGraphics g, int w, int h, int mx, int my) {
            hud.render(g, w, h, mx, my);
        }

        @Override
        public boolean click(double mx, double my, int button) {
            return hud.click(mx, my, button);
        }

        @Override
        public boolean scroll(double mx, double my, double amount) {
            return hud.scroll(mx, my, amount);
        }
    }
}
