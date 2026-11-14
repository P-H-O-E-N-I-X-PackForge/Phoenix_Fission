package net.phoenix.phoenix_fission.client;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.phoenix.phoenix_fission.client.render.TokamakRender;
import net.phoenix.phoenix_fission.common.data.PhoenixRecipeTypes;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import java.util.Locale;

/**
 * {@code /tokamak}: a client command to set how the Tokamak's plasma is drawn, to tune its look without running a
 * reactor. It changes only what this client draws, for every Tokamak it can see, and {@code /tokamak reset} puts it
 * back.
 *
 * <pre>
 * /tokamak recipe &lt;id&gt;        colour it as the plasma of a fusion recipe makes it
 * /tokamak colour &lt;hex&gt;       or any colour, such as ff44aa
 * /tokamak speed &lt;0 to 10&gt;    how fast it animates
 * /tokamak size &lt;0.2 to 12&gt;    how thick the plasma is; 0.6 is the most the reactor itself uses
 * /tokamak theme &lt;name|none&gt;  one of the pretty looks: miku, teto, pft, rin, luka, kaito, meiko, gumi, ocean, toxic, ice, void, blood, gold,
 * ember, sunset, aurora, rainbow, trans
 * /tokamak power &lt;0 to 1&gt;      how bright, and on even if the reactor is off
 * /tokamak drift &lt;0 to 1&gt;      how unstable: kinks, flares, shades to red
 * /tokamak field &lt;0 to 2&gt;      how well the field holds it
 * /tokamak scram &lt;true|false&gt;
 * /tokamak reset | status
 * </pre>
 */
public final class TokamakCommand {

    private TokamakCommand() {}

    public static void register(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(Commands.literal("tokamak")
                .then(Commands.literal("recipe")
                        .then(Commands.argument("id", StringArgumentType.greedyString())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(recipeIds(), b))
                                .executes(c -> recipe(c, StringArgumentType.getString(c, "id")))))
                .then(Commands.literal("colour")
                        .then(Commands.argument("hex", StringArgumentType.word())
                                .executes(c -> colour(c, StringArgumentType.getString(c, "hex")))))
                .then(number("speed", 0f, 10f, v -> TokamakRender.Tune.speed = v))
                .then(number("size", 0.2f, 12f, v -> TokamakRender.Tune.size = v))
                .then(Commands.literal("theme")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((c, b) -> {
                                    var names = TokamakRender.Theme.keys();
                                    names.add("none");
                                    return SharedSuggestionProvider.suggest(names, b);
                                })
                                .executes(c -> theme(c, StringArgumentType.getString(c, "name")))))
                .then(number("power", 0f, 1f, v -> TokamakRender.Tune.power = v))
                .then(number("drift", 0f, 1f, v -> TokamakRender.Tune.drift = v))
                .then(number("field", 0f, 2f, v -> TokamakRender.Tune.field = v))
                .then(Commands.literal("scram")
                        .then(Commands.argument("value", BoolArgumentType.bool()).executes(c -> {
                            TokamakRender.Tune.scram = BoolArgumentType.getBool(c, "value");
                            return say(c, "scram " + TokamakRender.Tune.scram);
                        })))
                .then(Commands.literal("reset").executes(c -> {
                    TokamakRender.Tune.reset();
                    return say(c, "back to what the reactor says");
                }))
                .then(Commands.literal("status").executes(TokamakCommand::status)));
    }

    private interface Setter {

        void set(Float value);
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> number(String name,
                                                                                                  float min,
                                                                                                  float max,
                                                                                                  Setter setter) {
        return Commands.literal(name)
                .then(Commands.argument("value", FloatArgumentType.floatArg(min, max)).executes(c -> {
                    float v = FloatArgumentType.getFloat(c, "value");
                    setter.set(v);
                    return say(c, name + " " + v);
                }))
                .then(Commands.literal("auto").executes(c -> {
                    setter.set(null);
                    return say(c, name + " follows the reactor");
                }));
    }

    private static java.util.List<String> recipeIds() {
        var level = Minecraft.getInstance().level;
        if (level == null) return java.util.List.of();
        var out = new java.util.ArrayList<String>();
        for (var holder : level.getRecipeManager().getAllRecipesFor(PhoenixRecipeTypes.FUSION_REACTOR_RECIPES.get())) {
            out.add(holder.id().toString());
        }
        return out;
    }

    private static int recipe(CommandContext<CommandSourceStack> c, String id) {
        var level = Minecraft.getInstance().level;
        ResourceLocation rl = ResourceLocation.tryParse(id.trim());
        if (level == null || rl == null) return say(c, "no such recipe");
        var holder = level.getRecipeManager().byKey(rl).orElse(null);
        if (holder == null || !(holder.value() instanceof com.gregtechceu.gtceu.api.recipe.GTRecipe gt)) {
            return say(c, "no such recipe: " + id);
        }
        float[] tint = TokamakRender.tintOf(gt);
        if (tint == null) return say(c, "that recipe makes no plasma to take a colour from");
        if (TokamakRender.Theme.of(gt) != TokamakRender.Theme.NONE) tint = new float[] { 1f, 1f, 1f };
        TokamakRender.Tune.tint = tint;
        TokamakRender.Tune.theme = TokamakRender.Theme.of(gt) == TokamakRender.Theme.NONE ? null :
                TokamakRender.Theme.of(gt);
        return say(c, String.format(Locale.ROOT, "coloured as %s (%02x%02x%02x)", rl.getPath(), (int) (tint[0] * 255),
                (int) (tint[1] * 255), (int) (tint[2] * 255)));
    }

    private static int theme(CommandContext<CommandSourceStack> c, String name) {
        if (name.equals("none")) {
            TokamakRender.Tune.theme = null;
            return say(c, "no theme: follows the reactor");
        }
        TokamakRender.Theme theme = TokamakRender.Theme.byKey(name);
        if (theme == null)
            return say(c, "no such theme: " + name + " (" + String.join(", ", TokamakRender.Theme.keys()) + ")");
        TokamakRender.Tune.theme = theme;
        return say(c, "theme " + name);
    }

    private static int colour(CommandContext<CommandSourceStack> c, String hex) {
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        try {
            int rgb = Integer.parseInt(h, 16);
            TokamakRender.Tune.tint = new float[] { (rgb >> 16 & 255) / 255f, (rgb >> 8 & 255) / 255f,
                    (rgb & 255) / 255f };
            return say(c, "colour " + h);
        } catch (NumberFormatException e) {
            return say(c, "not a hex colour: " + hex);
        }
    }

    private static int status(CommandContext<CommandSourceStack> c) {
        return say(c, String.format(Locale.ROOT,
                "speed %s, size %s, power %s, drift %s, field %s, scram %s, colour %s, theme %s",
                fmt(TokamakRender.Tune.speed),
                fmt(TokamakRender.Tune.size), fmt(TokamakRender.Tune.power), fmt(TokamakRender.Tune.drift),
                fmt(TokamakRender.Tune.field), TokamakRender.Tune.scram == null ? "auto" : TokamakRender.Tune.scram,
                TokamakRender.Tune.tint == null ? "auto" : "set",
                TokamakRender.Tune.theme == null ? "auto" : TokamakRender.Tune.theme.key));
    }

    private static String fmt(Float v) {
        return v == null ? "auto" : String.format(Locale.ROOT, "%.2f", v);
    }

    private static int say(CommandContext<CommandSourceStack> c, String text) {
        c.getSource().sendSuccess(() -> Component.literal("[tokamak] " + text), false);
        return 1;
    }
}
