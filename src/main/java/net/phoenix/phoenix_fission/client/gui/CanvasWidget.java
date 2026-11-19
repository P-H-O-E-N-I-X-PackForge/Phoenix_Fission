package net.phoenix.phoenix_fission.client.gui;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/** An LDLib widget that hands its whole area to a {@link CanvasPainter}. */
public class CanvasWidget extends Widget {

    private final CanvasPainter painter;

    public CanvasWidget(CanvasPainter painter, int width, int height) {
        super(0, 0, width, height);
        this.painter = painter;
        setBackground((IGuiTexture) null);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics g, int mouseX, int mouseY, float dt) {
        int ox = getPosition().x, oy = getPosition().y;
        g.pose().pushPose();
        g.pose().translate(ox, oy, 0);
        painter.render(g, getSize().width, getSize().height, mouseX - ox, mouseY - oy);
        g.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isMouseOverElement(mouseX, mouseY)) return false;
        return painter.click(mouseX - getPosition().x, mouseY - getPosition().y, button);
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (!isMouseOverElement(mouseX, mouseY)) return false;
        return painter.scroll(mouseX - getPosition().x, mouseY - getPosition().y, wheelDelta);
    }
}
