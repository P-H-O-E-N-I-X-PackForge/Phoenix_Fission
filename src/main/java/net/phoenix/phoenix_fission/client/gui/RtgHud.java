package net.phoenix.phoenix_fission.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.phoenix.phoenix_fission.api.machine.multiblock.RtgArrayMachine;

/** Screen for the RTG Array: output, cell count and a wear bar per loaded cell. */
@OnlyIn(Dist.CLIENT)
public class RtgHud implements CanvasPainter {

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

    private static final int ROW_H = 26;
    private static final int TITLE_H = 16;

    private final RtgArrayMachine machine;
    private final RtgHistory history;
    private int scroll = 0;

    public RtgHud(RtgArrayMachine machine, RtgHistory history) {
        this.machine = machine;
        this.history = history;
    }

    @Override
    public void render(GuiGraphics g, int w, int h, int mouseX, int mouseY) {
        Font font = Minecraft.getInstance().font;
        history.sampleIfDue(machine);
        GuiDraw.drawGradientRect(g, 0, 0, w, h, BG_TOP, BG_BOT, false);

        g.fill(0, 0, w, TITLE_H, BG_TOP);
        g.fill(3, 3, 6, TITLE_H - 3, C_TEAL);
        g.drawString(font, "RTG ARRAY", 10, (TITLE_H - font.lineHeight) / 2, C_CYAN, false);
        g.fill(0, TITLE_H - 1, w, TITLE_H, 0x44_00E5CC);

        boolean formed = machine.isFormed();
        boolean online = formed && machine.getGeneratedEUt() > 0;
        String badge = !formed ? "OFFLINE" : online ? "ONLINE" : "NO CELLS";
        int badgeColor = !formed ? C_DIM : online ? C_GREEN : C_GOLD;
        int bw = font.width(badge) + 8;
        int bx = w - bw - 8, by = 3;
        g.fill(bx, by, bx + bw, by + 10, 0x44_000000);
        GuiDraw.drawBorder(g, bx, by, bw, 10, (0x88 << 24) | (badgeColor & 0xFFFFFF), 1);
        g.drawString(font, badge, bx + 4, by + 1, badgeColor, false);

        int x = 8, y = TITLE_H + 6, cw = w - 16;
        if (!formed) {
            g.drawString(font, "Structure not complete", x, y, C_DIM, false);
            border(g, w, h);
            return;
        }

        g.fill(x, y, x + cw, y + 30, 0x33_00E5CC);
        GuiDraw.drawBorder(g, x, y, cw, 30, 0x88_00B8A5, 1);
        g.drawString(font, machine.getGeneratedEUt() + " EU/t", x + 6, y + 5, C_WHITE, false);
        g.drawString(font, machine.getActiveCells() + " cell" + (machine.getActiveCells() == 1 ? "" : "s") +
                " active", x + 6, y + 17, C_MID, false);
        y += 38;

        g.drawString(font, "CELLS", x, y, C_MID, false);
        g.fill(x, y + 10, x + cw, y + 11, C_TEAL);
        y += 15;

        var cells = machine.getCellInfo();
        if (cells.isEmpty()) {
            g.drawString(font, "Load RTG cells into an input bus", x, y + 2, C_DIM, false);
            border(g, w, h);
            return;
        }

        int top = y;
        int visible = Math.max(1, (h - top - 6) / ROW_H);
        scroll = Math.max(0, Math.min(scroll, Math.max(0, cells.size() - visible)));
        GuiDraw.enableScissor(g, 0, top, w, h - 4);
        for (int i = scroll; i < Math.min(cells.size(), scroll + visible); i++) {
            String[] p = cells.get(i).split("\\|");
            if (p.length < 4) continue;
            ResourceLocation id = ResourceLocation.tryParse(p[0]);
            var item = id == null ? null : BuiltInRegistries.ITEM.get(id);
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
                GuiDraw.drawGradientRect(g, barX, ry + 13, fill, 4, frac < 0.15 ? C_RED : C_GREEN,
                        frac < 0.15 ? C_GOLD : C_CYAN, true);
            }
            String time = duration(remaining);
            g.drawString(font, time, x + cw - font.width(time), ry + 11, C_MID, false);
        }
        g.disableScissor();
        border(g, w, h);
    }

    @Override
    public boolean scroll(double mouseX, double mouseY, double amount) {
        scroll = Math.max(0, scroll - (int) Math.signum(amount));
        return true;
    }

    private static void border(GuiGraphics g, int w, int h) {
        GuiDraw.drawBorder(g, 0, 0, w, h, C_TEAL, 1);
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
