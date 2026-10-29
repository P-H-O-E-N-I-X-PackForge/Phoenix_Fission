package net.phoenix.phoenix_fission.client.gui;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import net.phoenix.phoenix_fission.api.machine.multiblock.RtgArrayMachine;

import javax.annotation.Nonnull;

/** Screen for the RTG Array: output, cell count and a wear bar per loaded cell. */
public class RtgWidget extends Widget {

    private static final int BG_TOP = 0xFF_02101A;
    private static final int BG_BOT = 0xFF_010508;
    private static final int C_TEAL = 0xFF_00B8A5;
    private static final int C_CYAN = 0xFF_00E5CC;
    private static final int C_DIM = 0xFF_3A5E6A;
    private static final int C_MID = 0xFF_7FA3AE;
    private static final int C_WHITE = 0xFF_DCF0F4;
    private static final int C_GREEN = 0xFF_33FF88;
    private static final int C_GOLD = 0xFF_FFC040;
    private static final int C_RED = 0xFF_FF4444;

    public static final int TAB_W = 16;
    public static final int CONTENT_W = 198;
    public static final int CONTENT_H = 190;
    private static final int TAB_H = 16;
    private static final int GAP = 2;

    private static final int ROW_H = 26;
    private static final int TITLE_H = 16;
    private static final int HISTORY_LEN = 120;

    private final RtgArrayMachine machine;
    private int scroll = 0;
    private int tab = 0;
    private final double[] outputHistory = new double[HISTORY_LEN];
    private final double[] lifeHistory = new double[HISTORY_LEN];
    private int historyHead = 0;
    private int historyFilled = 0;
    private long lastSampleMs = 0;

    public RtgWidget(RtgArrayMachine machine, int width, int height) {
        super(0, 0, width, height);
        this.machine = machine;
        setBackground((IGuiTexture) null);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics g, int mouseX, int mouseY, float dt) {
        int fx = getPosition().x, fy = getPosition().y;
        int fw = getSize().width, fh = getSize().height;
        Font font = Minecraft.getInstance().font;

        sampleIfDue();
        DrawerHelper.drawGradientRect(g, fx, fy, fw, fh, BG_TOP, BG_BOT, false);

        // tab column on the left
        String[] labels = { "H", "~" };
        for (int i = 0; i < labels.length; i++) {
            int ty = fy + 6 + i * (TAB_H + GAP);
            boolean active = i == tab;
            boolean hover = mouseX >= fx && mouseX < fx + TAB_W && mouseY >= ty && mouseY < ty + TAB_H;
            g.fill(fx, ty, fx + TAB_W, ty + TAB_H, active ? 0xFF_002830 : hover ? 0xFF_061825 : 0xFF_010810);
            DrawerHelper.drawBorder(g, fx, ty, TAB_W, TAB_H,
                    active ? 0xFF_00AAA0 : hover ? 0xCC_00E5CC : 0x66_00E5CC, 1);
            g.drawString(font, labels[i], fx + (TAB_W - font.width(labels[i])) / 2 + 1,
                    ty + (TAB_H - font.lineHeight) / 2 + 1, active ? C_CYAN : hover ? C_WHITE : C_DIM, false);
        }

        int ox = fx + TAB_W + GAP, oy = fy;
        int w = CONTENT_W, h = CONTENT_H;
        if (tab == 1) {
            drawGraphs(g, font, ox, oy, w, h);
            drawInventorySection(g, ox, oy + h, w, fh - h);
            return;
        }
        drawHud(g, font, ox, oy, w, h);
        drawInventorySection(g, ox, oy + h, w, fh - h);
    }

    private void drawInventorySection(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y + 3, x + w, y + h - 3, 0xCC_010A12);
        DrawerHelper.drawBorder(g, x, y + 3, w, h - 6, 0x88_00B8A5, 1);
    }

    private void sampleIfDue() {
        long now = System.currentTimeMillis();
        if (now - lastSampleMs < 500) return;
        lastSampleMs = now;

        double lifeSum = 0;
        int n = 0;
        var cells = machine.getCellInfo();
        if (cells != null) {
            for (String entry : cells) {
                String[] p = entry.split("\\|");
                if (p.length < 3) continue;
                long max = parse(p[2]);
                if (max > 0) {
                    lifeSum += parse(p[1]) / (double) max;
                    n++;
                }
            }
        }
        outputHistory[historyHead] = machine.isFormed() ? machine.getGeneratedEUt() : 0;
        lifeHistory[historyHead] = n == 0 ? 0 : lifeSum / n;
        historyHead = (historyHead + 1) % HISTORY_LEN;
        historyFilled = Math.min(HISTORY_LEN, historyFilled + 1);
    }

    private int historyIndex(int i) {
        return (historyHead - historyFilled + i + HISTORY_LEN * 2) % HISTORY_LEN;
    }

    private void drawGraphs(GuiGraphics g, Font font, int ox, int oy, int w, int h) {
        g.fill(ox, oy, ox + w, oy + 16, BG_TOP);
        g.fill(ox + 3, oy + 3, ox + 6, oy + 13, C_TEAL);
        g.drawString(font, "RTG GRAPHS", ox + 10, oy + 4, C_CYAN, false);
        g.fill(ox, oy + 15, ox + w, oy + 16, 0x44_00E5CC);

        int x = ox + 8, cw = w - 16;
        int plotH = (h - 16 - 30 - 20) / 2;
        int y = oy + 24;
        double peak = 1;
        for (int i = 0; i < historyFilled; i++) peak = Math.max(peak, outputHistory[historyIndex(i)]);
        plot(g, font, x, y, cw, plotH, outputHistory, peak * 1.1, C_GOLD, "OUTPUT EU/t",
                machine.getGeneratedEUt() + " EU/t");
        y += plotH + 22;
        double latestLife = historyFilled == 0 ? 0 : lifeHistory[historyIndex(historyFilled - 1)];
        plot(g, font, x, y, cw, plotH, lifeHistory, 1.0, C_GREEN, "AVERAGE CELL LIFE",
                Math.round(latestLife * 100) + "%");
        DrawerHelper.drawBorder(g, ox, oy, w, h, C_TEAL, 1);
    }

    private void plot(GuiGraphics g, Font font, int x, int y, int w, int h, double[] data, double max, int color,
                      String label, String value) {
        g.drawString(font, label, x, y, C_MID, false);
        g.drawString(font, value, x + w - font.width(value), y, C_WHITE, false);
        int top = y + 11;
        g.fill(x, top, x + w, top + h, 0x33_000000);
        DrawerHelper.drawBorder(g, x, top, w, h, 0x66_00B8A5, 1);
        for (int q = 1; q < 4; q++) g.fill(x + 1, top + h * q / 4, x + w - 1, top + h * q / 4 + 1, 0x14_FFFFFF);

        if (historyFilled < 2) {
            g.drawString(font, "collecting...", x + 4, top + h / 2 - 4, C_DIM, false);
            return;
        }
        int inner = w - 2, innerH = h - 2;
        int prev = -1;
        for (int col = 0; col < inner; col++) {
            int i = (int) ((long) col * (historyFilled - 1) / Math.max(1, inner - 1));
            double v = Math.max(0, Math.min(1, data[historyIndex(i)] / max));
            int bar = (int) Math.round(v * (innerH - 1));
            int px = x + 1 + col;
            int base = top + h - 1;
            g.fill(px, base - bar, px + 1, base, (0x33 << 24) | (color & 0xFFFFFF));
            int lo = prev < 0 ? bar : Math.min(prev, bar), hi = prev < 0 ? bar : Math.max(prev, bar);
            g.fill(px, base - hi - 1, px + 1, base - lo, color);
            prev = bar;
        }
    }

    private void drawHud(GuiGraphics g, Font font, int ox, int oy, int w, int h) {

        DrawerHelper.drawGradientRect(g, ox, oy, w, h, BG_TOP, BG_BOT, false);

        g.fill(ox, oy, ox + w, oy + TITLE_H, BG_TOP);
        g.fill(ox + 3, oy + 3, ox + 6, oy + TITLE_H - 3, C_TEAL);
        g.drawString(font, "RTG ARRAY", ox + 10, oy + (TITLE_H - font.lineHeight) / 2, C_CYAN, false);
        g.fill(ox, oy + TITLE_H - 1, ox + w, oy + TITLE_H, 0x44_00E5CC);

        boolean formed = machine.isFormed();
        boolean online = formed && machine.getGeneratedEUt() > 0;
        String badge = !formed ? "OFFLINE" : online ? "ONLINE" : "NO CELLS";
        int badgeColor = !formed ? C_DIM : online ? C_GREEN : C_GOLD;
        int bw = font.width(badge) + 8;
        int bx = ox + w - bw - 8, by = oy + 3;
        g.fill(bx, by, bx + bw, by + 10, 0x44_000000);
        DrawerHelper.drawBorder(g, bx, by, bw, 10, (0x88 << 24) | (badgeColor & 0xFFFFFF), 1);
        g.drawString(font, badge, bx + 4, by + 1, badgeColor, false);

        int x = ox + 8, y = oy + TITLE_H + 6, cw = w - 16;
        if (!formed) {
            g.drawString(font, "Structure not complete", x, y, C_DIM, false);
            DrawerHelper.drawBorder(g, ox, oy, w, h, C_TEAL, 1);
            return;
        }

        g.fill(x, y, x + cw, y + 30, 0x33_00E5CC);
        DrawerHelper.drawBorder(g, x, y, cw, 30, 0x88_00B8A5, 1);
        g.drawString(font, machine.getGeneratedEUt() + " EU/t", x + 6, y + 5, C_WHITE, false);
        g.drawString(font, machine.getActiveCells() + " cell" + (machine.getActiveCells() == 1 ? "" : "s") +
                " active", x + 6, y + 17, C_MID, false);
        y += 38;

        g.drawString(font, "CELLS", x, y, C_MID, false);
        g.fill(x, y + 10, x + cw, y + 11, C_TEAL);
        y += 15;

        var cells = machine.getCellInfo();
        if (cells == null || cells.isEmpty()) {
            g.drawString(font, "Load RTG cells into an input bus", x, y + 2, C_DIM, false);
            DrawerHelper.drawBorder(g, ox, oy, w, h, C_TEAL, 1);
            return;
        }

        int top = y;
        int visible = Math.max(1, (oy + h - top - 6) / ROW_H);
        scroll = Math.max(0, Math.min(scroll, Math.max(0, cells.size() - visible)));
        g.enableScissor(ox, top, ox + w, oy + h - 4);
        for (int i = scroll; i < Math.min(cells.size(), scroll + visible); i++) {
            String[] p = cells.get(i).split("\\|");
            if (p.length < 4) continue;
            ResourceLocation id = ResourceLocation.tryParse(p[0]);
            var item = id == null ? null : ForgeRegistries.ITEMS.getValue(id);
            long remaining = parse(p[1]), max = Math.max(1, parse(p[2]));
            double frac = Math.min(1.0, remaining / (double) max);

            int ry = top + (i - scroll) * ROW_H;
            if (item != null) g.renderItem(new ItemStack(item), x, ry + 2);
            String name = item == null ? p[0] : item.getDescription().getString().replace(" RTG Cell", "");
            g.drawString(font, name, x + 19, ry, C_WHITE, false);
            String right = p[3] + " EU/t";
            g.drawString(font, right, x + cw - font.width(right), ry, C_GOLD, false);

            int barX = x + 19, barW = cw - 19 - 52;
            g.fill(barX, ry + 13, barX + barW, ry + 17, 0x33_FFFFFF);
            int fill = (int) (barW * frac);
            if (fill > 0) {
                DrawerHelper.drawGradientRect(g, barX, ry + 13, fill, 4, frac < 0.15 ? C_RED : C_GREEN,
                        frac < 0.15 ? C_GOLD : C_CYAN, true);
            }
            String time = duration(remaining);
            g.drawString(font, time, x + cw - font.width(time), ry + 11, C_MID, false);
        }
        g.disableScissor();
        DrawerHelper.drawBorder(g, ox, oy, w, h, C_TEAL, 1);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int fx = getPosition().x, fy = getPosition().y;
        if (button == 0 && mouseX >= fx && mouseX < fx + TAB_W) {
            for (int i = 0; i < 2; i++) {
                int ty = fy + 6 + i * (TAB_H + GAP);
                if (mouseY >= ty && mouseY < ty + TAB_H) {
                    tab = i;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (tab != 0 || !isMouseOverElement(mouseX, mouseY)) return false;
        scroll = Math.max(0, scroll - (int) Math.signum(wheelDelta));
        return true;
    }

    private static long parse(String s) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static String duration(long seconds) {
        long h = seconds / 3600, m = (seconds % 3600) / 60;
        return h > 0 ? h + "h " + m + "m" : m + "m " + (seconds % 60) + "s";
    }
}
