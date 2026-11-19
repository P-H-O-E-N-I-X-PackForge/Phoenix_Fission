package net.phoenix.phoenix_fission.client.gui;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.phoenix.phoenix_fission.api.machine.multiblock.FusionReactorMachine;

import javax.annotation.Nonnull;

/**
 * The Tokamak screen on GregTech's fancy UI, so it keeps GT's side tabs, configurators and part pages, but drawn on
 * one dark panel as on the GregTech 8 version: the page fills the top of it and the player's inventory sits in a
 * section underneath.
 */
public class FusionFancyUIWidget extends FancyMachineUIWidget {

    private static final int BG_TOP = 0xFF_02101A;
    private static final int BG_BOT = 0xFF_010508;
    private static final int C_TEAL = 0xFF_00B8A5;

    private final FusionReactorMachine machine;

    public FusionFancyUIWidget(IFancyUIProvider provider, int width, int height) {
        super(provider, width, height);
        this.machine = (FusionReactorMachine) provider;
        // the pages are the full size of the panel, with no frame of GT's round them
        setBorder(0);
        setBackground((IGuiTexture) null);
    }

    @Override
    public void initWidget() {
        super.initWidget();
        FissionReactorFancyUIWidget.applyTheme(sideTabsWidget);
        sideTabsWidget.addSelfPosition(-3, 0);
        FissionReactorFancyUIWidget.applySlotTheme(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics g, int mouseX, int mouseY, float dt) {
        int x = getPosition().x, y = getPosition().y;
        int w = getSize().width, h = getSize().height;
        int pageH = pageContainer.getSize().height;

        machine.getHistory().sampleIfDue(machine);

        // the panel
        GuiDraw.drawGradientRect(g, x, y, w, h, BG_TOP, BG_BOT, false);
        for (int gx = x + 4; gx < x + w; gx += 8) g.fill(gx, y, gx + 1, y + h, 0x0C_00E5CC);
        for (int gy = y + 4; gy < y + h; gy += 8) g.fill(x, gy, x + w, gy + 1, 0x0C_00E5CC);
        GuiDraw.drawBorder(g, x, y, w, h, C_TEAL, 1);
        GuiDraw.drawBorder(g, x + 2, y + 2, w - 4, h - 4, 0x22_00E5CC, 1);

        // the inventory section, when the page has the inventory under it
        if (playerInventory != null && playerInventory.isVisible()) {
            int sy = y + pageH + 3;
            int sh = h - pageH - 3;
            g.fill(x, sy, x + w, sy + sh, 0xCC_010A12);
            GuiDraw.drawBorder(g, x, sy, w, sh, 0x88_00B8A5, 1);
            g.fill(x, sy, x + 3, sy + 8, 0xFF_00B8A5);
        }

        super.drawInBackground(g, mouseX, mouseY, dt);
    }

    /** The home page: the HUD, with the throttle and pressure fields along its bottom edge. */
    public static WidgetGroup hudPage(FusionReactorMachine machine, int width, int height) {
        var page = new WidgetGroup(0, 0, width, height);
        page.setBackground((IGuiTexture) null);
        page.addWidget(new CanvasWidget(new FusionHud(machine, machine.getHistory()), width, height));

        int fieldY = height - 22;
        int half = (width - 16) / 2;
        page.addWidget(new TextFieldWidget(8 + 66, fieldY, 44, 14,
                () -> String.valueOf(machine.getThrottle()),
                s -> machine.setThrottle(parse(s, machine.getThrottle())))
                .setNumbersOnly(10, 100));
        page.addWidget(new TextFieldWidget(8 + half + 4 + 66, fieldY, 44, 14,
                () -> String.valueOf(machine.getPressureTarget()),
                s -> machine.setPressureTarget(parse(s, machine.getPressureTarget())))
                .setNumbersOnly(0, 100));
        return page;
    }

    private static int parse(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
