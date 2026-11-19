package net.phoenix.phoenix_fission.client.gui;

import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.phoenix.phoenix_fission.api.machine.multiblock.FusionReactorMachine;

import javax.annotation.Nonnull;

/**
 * The Tokamak screen: the reactors' fancy-UI side tabs, with the HUD over the home page and the throttle and
 * pressure fields along its bottom edge (they only show while the home page does).
 */
public class FusionFancyUIWidget extends FancyMachineUIWidget {

    private static final int BG_TOP = 0xFF_02101A;
    private static final int BG_BOT = 0xFF_010508;
    private static final int C_TEAL = 0xFF_00B8A5;

    private final FusionReactorMachine machine;
    private final FusionHud hud;
    private TextFieldWidget throttleField;
    private TextFieldWidget pressureField;

    public FusionFancyUIWidget(IFancyUIProvider provider, int width, int height) {
        super(provider, width, height);
        this.machine = (FusionReactorMachine) provider;
        this.hud = new FusionHud(machine, machine.getHistory());
        setBackground((IGuiTexture) null);
    }

    @Override
    public void initWidget() {
        super.initWidget();
        FissionReactorFancyUIWidget.applyTheme(sideTabsWidget);

        var origin = pageContainer.getSelfPosition();
        int pw = pageContainer.getSize().width, ph = pageContainer.getSize().height;
        int fieldY = origin.y + ph - 22;
        int half = (pw - 16) / 2;

        throttleField = new TextFieldWidget(origin.x + 8 + 66, fieldY, 44, 14,
                () -> String.valueOf(machine.getThrottle()),
                s -> machine.setThrottle(parse(s, machine.getThrottle())))
                .setNumbersOnly(10, 100);
        pressureField = new TextFieldWidget(origin.x + 8 + half + 4 + 66, fieldY, 44, 14,
                () -> String.valueOf(machine.getPressureTarget()),
                s -> machine.setPressureTarget(parse(s, machine.getPressureTarget())))
                .setNumbersOnly(0, 100);
        addWidget(throttleField);
        addWidget(pressureField);
    }

    private static int parse(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics g, int mouseX, int mouseY, float dt) {
        int x = getPosition().x, y = getPosition().y;
        int w = getSize().width, h = getSize().height;

        machine.getHistory().sampleIfDue(machine);
        boolean home = currentPage == mainPage;
        if (throttleField != null) {
            throttleField.setVisible(home);
            pressureField.setVisible(home);
        }

        DrawerHelper.drawGradientRect(g, x, y, w, h, BG_TOP, BG_BOT, false);

        super.drawInBackground(g, mouseX, mouseY, dt);

        if (home && pageContainer != null && !pageContainer.widgets.isEmpty()) {
            var page = pageContainer.widgets.get(0);
            int px = page.getPosition().x, py = page.getPosition().y;
            int pw = page.getSize().width, ph = page.getSize().height;
            g.pose().pushPose();
            g.pose().translate(px, py, 0);
            hud.render(g, pw, ph, mouseX - px, mouseY - py);
            g.pose().popPose();
            // the text fields draw on top of the HUD
            throttleField.drawInBackground(g, mouseX, mouseY, dt);
            pressureField.drawInBackground(g, mouseX, mouseY, dt);
        }

        DrawerHelper.drawBorder(g, x, y, w, h, C_TEAL, 1);
    }
}
