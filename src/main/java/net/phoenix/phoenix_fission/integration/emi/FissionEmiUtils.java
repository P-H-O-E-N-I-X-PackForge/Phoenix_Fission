package net.phoenix.phoenix_fission.integration.emi;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.core.registries.BuiltInRegistries;

import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;

import java.util.Locale;

final class FissionEmiUtils {

    private FissionEmiUtils() {}

    static final int BG_FILL = 0x30_FFFFFF;
    static final int BG_BORDER = 0xFF_555555;

    static TextWidget text(WidgetHolder widgets, String text, int x, int y, int color) {
        return widgets.addText(Component.literal(text), x, y, color, true);
    }

    static String formatName(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        String name = raw.contains(":") ? raw.substring(raw.indexOf(':') + 1) : raw;
        String[] words = name.replace('_', ' ').trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1).toLowerCase(Locale.ROOT));
        }
        return sb.toString();
    }

    static void drawPanel(WidgetHolder widgets, int w, int h) {
        widgets.addDrawable(0, 0, w, h, (gg, mouseX, mouseY, delta) -> {
            gg.fill(1, 1, w - 1, h - 1, BG_FILL);
            gg.fill(0, 0, w, 1, BG_BORDER);
            gg.fill(0, h - 1, w, h, BG_BORDER);
            gg.fill(0, 0, 1, h, BG_BORDER);
            gg.fill(w - 1, 0, w, h, BG_BORDER);
        });
    }

    static int tierColor(int tier) {
        return switch (Math.max(1, Math.min(tier, 6))) {
            case 1 -> 0xFF_AAAAAA;
            case 2 -> 0xFF_55FF55;
            case 3 -> 0xFF_55FFFF;
            case 4 -> 0xFF_FF55FF;
            case 5 -> 0xFF_FFAA00;
            default -> 0xFF_FF5555;
        };
    }

    static EmiStack resolveItem(String id, int count) {
        if (id == null || id.isEmpty() || "none".equalsIgnoreCase(id)) return EmiStack.EMPTY;
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) return EmiStack.EMPTY;
        Item item = BuiltInRegistries.ITEM.getOptional(rl).orElse(null);
        if (item == null || item == Items.AIR) return EmiStack.EMPTY;
        return EmiStack.of(new ItemStack(item, Math.max(1, count)));
    }

    static EmiStack resolveFluid(String id, int mb) {
        if (id == null || id.isEmpty() || "none".equalsIgnoreCase(id)) return EmiStack.EMPTY;
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) return EmiStack.EMPTY;
        Fluid fluid = BuiltInRegistries.FLUID.getOptional(rl).orElse(null);
        if (fluid == null || fluid == Fluids.EMPTY) return EmiStack.EMPTY;
        return EmiStack.of(fluid, Math.max(1, mb));
    }
}
