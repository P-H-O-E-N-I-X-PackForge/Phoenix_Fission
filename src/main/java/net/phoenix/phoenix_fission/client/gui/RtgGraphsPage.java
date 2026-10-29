package net.phoenix.phoenix_fission.client.gui;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.phoenix.phoenix_fission.api.machine.multiblock.RtgArrayMachine;

import javax.annotation.Nonnull;

/** Side-tab page: output and average cell life over the last minute. */
public class RtgGraphsPage implements IFancyUIProvider {

    private final RtgArrayMachine machine;

    public RtgGraphsPage(RtgArrayMachine machine) {
        this.machine = machine;
    }

    @Override
    public IGuiTexture getTabIcon() {
        return new GuiTextureGroup(new ColorRectTexture(0xFF04090F), new TextTexture("~", 0xFF_00E5CC));
    }

    @Override
    public Component getTitle() {
        return Component.literal("RTG Graphs");
    }

    @Override
    public boolean hasPlayerInventory() {
        return false;
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        var size = widget.getPageContainer().getSize();
        return new GraphWidget(machine, size.width, size.height);
    }

    private static final class GraphWidget extends Widget {

        private static final int C_TEAL = 0xFF_00B8A5;
        private static final int C_CYAN = 0xFF_00E5CC;
        private static final int C_MID = 0xFF_7FA3AE;
        private static final int C_DIM = 0xFF_3A5E6A;
        private static final int C_WHITE = 0xFF_DCF0F4;
        private static final int C_GREEN = 0xFF_33FF88;
        private static final int C_GOLD = 0xFF_FFC040;

        private final RtgArrayMachine machine;

        GraphWidget(RtgArrayMachine machine, int w, int h) {
            super(0, 0, w, h);
            this.machine = machine;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void drawInBackground(@Nonnull GuiGraphics g, int mouseX, int mouseY, float dt) {
            Font font = Minecraft.getInstance().font;
            var history = machine.getHistory();
            history.sampleIfDue(machine);
            int ox = getPosition().x, oy = getPosition().y, w = getSize().width, h = getSize().height;

            g.drawString(font, "RTG GRAPHS", ox + 6, oy + 4, C_CYAN, false);
            g.fill(ox, oy + 15, ox + w, oy + 16, 0x44_00E5CC);

            int x = ox + 8, cw = w - 16;
            int plotH = Math.max(20, (h - 16 - 30 - 20) / 2);
            int y = oy + 24;
            double peak = 1;
            for (int i = 0; i < history.filled; i++) peak = Math.max(peak, history.output[history.index(i)]);
            plot(g, font, history, x, y, cw, plotH, history.output, peak * 1.1, C_GOLD, "OUTPUT EU/t",
                    machine.getGeneratedEUt() + " EU/t");
            y += plotH + 22;
            double latest = history.filled == 0 ? 0 : history.life[history.index(history.filled - 1)];
            plot(g, font, history, x, y, cw, plotH, history.life, 1.0, C_GREEN, "AVERAGE CELL LIFE",
                    Math.round(latest * 100) + "%");
        }

        private void plot(GuiGraphics g, Font font, RtgHistory history, int x, int y, int w, int h, double[] data,
                          double max, int color, String label, String value) {
            g.drawString(font, label, x, y, C_MID, false);
            g.drawString(font, value, x + w - font.width(value), y, C_WHITE, false);
            int top = y + 11;
            g.fill(x, top, x + w, top + h, 0x33_000000);
            DrawerHelper.drawBorder(g, x, top, w, h, 0x66_00B8A5, 1);
            for (int q = 1; q < 4; q++) g.fill(x + 1, top + h * q / 4, x + w - 1, top + h * q / 4 + 1, 0x14_FFFFFF);

            if (history.filled < 2) {
                g.drawString(font, "collecting...", x + 4, top + h / 2 - 4, C_DIM, false);
                return;
            }
            int inner = w - 2, innerH = h - 2;
            int prev = -1;
            for (int col = 0; col < inner; col++) {
                int i = (int) ((long) col * (history.filled - 1) / Math.max(1, inner - 1));
                double v = Math.max(0, Math.min(1, data[history.index(i)] / max));
                int bar = (int) Math.round(v * (innerH - 1));
                int px = x + 1 + col;
                int base = top + h - 1;
                g.fill(px, base - bar, px + 1, base, (0x33 << 24) | (color & 0xFFFFFF));
                int lo = prev < 0 ? bar : Math.min(prev, bar), hi = prev < 0 ? bar : Math.max(prev, bar);
                g.fill(px, base - hi - 1, px + 1, base - lo, color);
                prev = bar;
            }
        }
    }
}
