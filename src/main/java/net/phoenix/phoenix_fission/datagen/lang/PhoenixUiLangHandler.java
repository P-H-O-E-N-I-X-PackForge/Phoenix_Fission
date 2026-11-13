package net.phoenix.phoenix_fission.datagen.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class PhoenixUiLangHandler {

    public static void init(RegistrateLangProvider provider) {
        provider.add("phoenix_fission.fission_blanket_block.inst", "Inst: %s");
        provider.add("phoenix_fission.fission_blanket_block.none", " - (none)");
        provider.add("phoenix_fission.fission_cooler_block.none", "None");
        provider.add("phoenix_fission.fission_cooler_block.k_threshold", " K threshold");
        provider.add("phoenix_fission.fission_cooler_block.flat_cooling", "Flat cooling: ");
        provider.add("phoenix_fission.fission_cooler_block.passive_no_coolant_required",
                "Passive: no coolant required");
        provider.add("phoenix_fission.fission_fuel_rod_block.each_rod_burns_rods_1",
                "Each rod burns (rods + 1) / 2 times faster in a full reactor");
        provider.add("phoenix_fission.fission_fuel_rod_block.per_item_lone_rod_up",
                "Per item, lone rod: %s, up to %s at max moderator discount (%s%%)");
        provider.add("phoenix_fission.fusion_coil_block.upkeep_eu_t", "Upkeep: %s EU/t");
        provider.add("phoenix_fission.fusion_coil_block.field_strength", "Field strength: %s");
        provider.add("phoenix_fission.fusion_coil_block.outer_confinement_coil", "Outer confinement coil");
        provider.add("phoenix_fission.fusion_coil_block.plasma_power", "Plasma power: %s");
        provider.add("phoenix_fission.fusion_coil_block.inner_plasma_coil", "Inner plasma coil");
        provider.add("phoenix_fission.fusion_compute_block.compute", "Compute: %s");
        provider.add("phoenix_fission.fusion_compute_block.fusion_compute_module", "Fusion compute module");
        provider.add("phoenix_fission.fusion_reactor_machine.needs_fusion_tier_more_compute",
                "Needs fusion tier %s (more compute modules)");
        provider.add("phoenix_fission.fission_component_manager.none", "  None");
        provider.add("phoenix_fission.nuke_block.handle_with_caution", "Handle with caution!");
        provider.add("phoenix_fission.nuke_block.large_scale_nuclear_explosive", "Large scale nuclear explosive.");
        provider.add("phoenix_fission.fission_blanket_emi_recipe.chance_instability", "%s chance, %s instability");
        provider.add("phoenix_fission.fission_machine_provider.w_inst", "  w=%s  inst=%s");
        provider.add("phoenix_fission.fission_machine_provider.max_cool_cap_hu_t", "Max Cool @ Cap: %s HU/t");
        provider.add("phoenix_fission.fission_machine_provider.rods_mods_coolers", "Rods: %s  Mods: %s  Coolers: %s");
        provider.add("phoenix_fission.fission_machine_provider.parallels", "Parallels: %s");
        provider.add("phoenix_fission.ui.advanced_fission_scram_hatch", "§6Advanced Fission SCRAM Hatch");
        provider.add("phoenix_fission.ui.min_signal_strength_1_15", "§fMin Signal Strength §7(1-15):");
        provider.add("phoenix_fission.ui.sustain_ticks_1_100", "§fSustain Ticks §7(1-100):");
        provider.add("phoenix_fission.ui.scram_halted", "§c[SCRAM] Reactor HALTED");
        provider.add("phoenix_fission.ui.scram_standby", "§a[OK] Standby - Reactor Permitted");
        provider.add("phoenix_fission.ui.scram_arming", "§eArming: %s / %s ticks");
        provider.add("phoenix_fission.ui.scram_triggered", "§cArmed and triggered.");
        provider.add("phoenix_fission.ui.scram_waiting", "§7Waiting for signal...");
        provider.add("phoenix_fission.ui.fission_scram_hatch", "§cFission SCRAM Hatch");
        provider.add("phoenix_fission.ui.how_it_works", "§eHow it works:");
        provider.add("phoenix_fission.ui.any_redstone_signal_on_any", "§7Any redstone signal on any face");
        provider.add("phoenix_fission.ui.halts_the_reactor_immediately", "§7halts the reactor immediately.");
        provider.add("phoenix_fission.ui.removing_the_signal_resumes_it", "§7Removing the signal resumes it.");
        provider.add("phoenix_fission.ui.hint_not_every_redstone_source", "Hint: not every redstone source");
        provider.add("phoenix_fission.ui.behaves_the_way_you_expect", "behaves the way you expect.");
        provider.add("phoenix_fission.ui.think_carefully_about_signal_timing", "Think carefully about signal timing.");
        provider.add("phoenix_fission.ui.advanced_fission_stability_sensor", "§bAdvanced Fission Stability Sensor");
        provider.add("phoenix_fission.ui.min_heat_percent", "§fMin Heat Percent:");
        provider.add("phoenix_fission.ui.max_heat_percent", "§fMax Heat Percent:");
        provider.add("phoenix_fission.ui.emit_strength_1_15", "§fEmit Strength §7(1-15):");
        provider.add("phoenix_fission.ui.invert_output", "§fInvert output:");
        provider.add("phoenix_fission.ui.emits_fixed_strength_on_all", "Emits fixed strength on all block faces.");
        provider.add("phoenix_fission.ui.invert_signal_output", "Invert Signal Output");
        provider.add("phoenix_fission.ui.sensor_heat_signal", "§7Heat: §f%s%%  §7Signal: %s%s");
        provider.add("phoenix_fission.ui.sensor_heat_na", "§7Heat: §8N/A  §7Signal: §80");
        provider.add("phoenix_fission.ui.fission_stability_sensor", "§6Fission Stability Sensor");
        provider.add("phoenix_fission.ui.emits_signal_on_all_block", "Emits signal on all block faces.");
    }
}
