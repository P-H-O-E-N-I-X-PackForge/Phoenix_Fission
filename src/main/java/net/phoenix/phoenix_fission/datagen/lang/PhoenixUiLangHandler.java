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
        provider.add("phoenix_fission.breeder_workable_electric_multiblock_machine.breeding_active", "Breeding Active");
        provider.add("phoenix_fission.molten_salt_reactor_multiblock_machine.xe_purge", "  Xe Purge: ");
        provider.add("phoenix_fission.fission_component_manager.none", "  None");
        provider.add("phoenix_fission.nuke_block.handle_with_caution", "Handle with caution!");
        provider.add("phoenix_fission.nuke_block.large_scale_nuclear_explosive", "Large scale nuclear explosive.");
        provider.add("phoenix_fission.fission_graphs_page.heat_curves", "Heat Curves");
        provider.add("phoenix_fission.fission_math_page.fission_math", "Fission Math");
        provider.add("phoenix_fission.rtg_graphs_page.rtg_graphs", "RTG Graphs");
        provider.add("phoenix_fission.thermoelectric_generator_math_page.thermo_math", "Thermo Math");
        provider.add("phoenix_fission.fission_blanket_emi_recipe.chance_instability", "%s chance, %s instability");
        provider.add("phoenix_fission.fission_machine_provider.w_inst", "  w=%s  inst=%s");
        provider.add("phoenix_fission.fission_machine_provider.max_cool_cap_hu_t", "Max Cool @ Cap: %s HU/t");
        provider.add("phoenix_fission.fission_machine_provider.rods_mods_coolers", "Rods: %s  Mods: %s  Coolers: %s");
        provider.add("phoenix_fission.fission_machine_provider.parallels", "Parallels: %s");
        provider.add("phoenix_fission.ui.fission_graphs_page.reactor_not_formed", "Reactor not formed.");
        provider.add("phoenix_fission.ui.fission_graphs_page.no_liner_installed", "No liner installed.");
        provider.add("phoenix_fission.ui.fission_graphs_page.no_fuel_rods_installed", "No fuel rods installed.");
        provider.add("phoenix_fission.ui.fission_graphs_page.production", "production");
        provider.add("phoenix_fission.ui.fission_graphs_page.cooling", "cooling");
        provider.add("phoenix_fission.ui.fission_graphs_page.warming", "warming");
        provider.add("phoenix_fission.ui.fission_graphs_page.no_stable_temperature", "!! NO STABLE TEMPERATURE");
        provider.add("phoenix_fission.ui.fission_graphs_page.reactor_will_always_trend_to",
                "Reactor will always trend to meltdown.");
        provider.add("phoenix_fission.ui.fission_graphs_page.stable_at_minimum_heat_0",
                "  Stable at minimum heat (0 K)");
        provider.add("phoenix_fission.ui.fission_graphs_page.100_margin", "100% margin");
        provider.add("phoenix_fission.ui.fission_graphs_page.stable_above_meltdown_threshold",
                "  STABLE ABOVE MELTDOWN THRESHOLD");
        provider.add("phoenix_fission.ui.fission_graphs_page.equilibrium", "  Equilibrium: %s");
        provider.add("phoenix_fission.ui.fission_graphs_page.reactor_melts_before_stabilising",
                "  Reactor melts before stabilising.");
        provider.add("phoenix_fission.ui.fission_graphs_page.stable_temperature", "  Stable temperature: %s");
        provider.add("phoenix_fission.ui.fission_graphs_page.threshold", "  Threshold:  %s");
        provider.add("phoenix_fission.ui.fission_graphs_page.meltdown", "  Meltdown: %s");
        provider.add("phoenix_fission.ui.fission_graphs_page.assumes_reactivity_1_0_coolant",
                "Assumes: reactivity=1.0, coolant present");
        provider.add("phoenix_fission.ui.fission_graphs_page.threshold_2", "threshold");
        provider.add("phoenix_fission.ui.fission_graphs_page.meltdown_2", "meltdown");
        provider.add("phoenix_fission.ui.fission_math_page.meltdown_threshold", "Meltdown Threshold");
        provider.add("phoenix_fission.ui.fission_math_page.passive_conductivity", "Passive Conductivity");
        provider.add("phoenix_fission.ui.fission_math_page.current", "Current");
        provider.add("phoenix_fission.ui.fission_math_page.of_threshold", "%s of threshold");
        provider.add("phoenix_fission.ui.fission_math_page.base", "base");
        provider.add("phoenix_fission.ui.fission_math_page.min", "min");
        provider.add("phoenix_fission.ui.fission_math_page.severity", "severity");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.structure_not_complete",
                "Structure not complete");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.heat", "HEAT");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.net_heat_coolant", "NET HEAT / COOLANT");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.collecting_data", "Collecting data...");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.per_tick", "PER TICK");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.over_cooled_90_coolant_refund",
                "  OVER-COOLED  (90% coolant refund, no EU)");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.components", "COMPONENTS");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.no_blanket_installed",
                "  No blanket installed");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.nominal", "  Nominal");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.no_moderators_installed",
                "No moderators installed.");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.no_cooler_blocks_installed",
                "No cooler blocks installed.");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.coolers_reduce_reactor_temperature",
                "Coolers reduce reactor temperature.");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.always_active_no_coolant_needed",
                "Always active - no coolant needed");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.no_blanket_blocks_detected",
                "No blanket blocks detected.");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.place_blanket_blocks_inside_the",
                "Place blanket blocks inside the reactor");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.casing_to_begin_breeding_materials",
                "casing to begin breeding materials.");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.input", "  Input: %s");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.no_outputs_configured",
                "  No outputs configured");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.outputs", "  Outputs:");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.heat_net", "heat (net)");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.cool_hu_t", "cool (HU/t)");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.countdown", "COUNTDOWN");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.net_rate", "Net rate");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.coolant", "Coolant");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.breeder_fission_reactor",
                "BREEDER FISSION REACTOR");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.fission_reactor", "FISSION REACTOR");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.breeder_reactor", "BREEDER REACTOR");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.offline", "OFFLINE");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.scrammed", "SCRAMMED");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.running", "RUNNING");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.standby", "STANDBY");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.stable", "~ stable");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.details", "details >");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.x_par", "x%s par");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.coolant_ok", "  Coolant OK");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.no_coolant", "  NO COOLANT");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.x_parallels", "x%s parallels");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.stab", "STAB");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.sens", "SENS");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.meltdown", "MELTDOWN");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.meltdown_s", "MELTDOWN  %ss");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.paused_scram", "PAUSED - SCRAM");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.hu_rod", "%s HU/rod");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.x_per_cycle", "x%s per cycle");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.parallel", "+%s parallel");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.no_par_bonus", "no par bonus");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.passive_x", "PASSIVE  x%s");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.k_threshold", "%s K threshold");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.active_now", "Active now");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.idle_heat_threshold",
                "Idle (heat < threshold)");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.x_tick", "x%s/tick");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.req_fuel_none", "  Req. fuel: None");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.req_fuel", "  Req. fuel: %s");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.back", "[< BACK]");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.unknown", "Unknown");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.stable_2", "(stable)");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.low", "(low)");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.moderate", "(moderate)");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.high", "(high)");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.meltdown_scram_engaged",
                "MELTDOWN  -  SCRAM ENGAGED");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.meltdown_imminent",
                "!! MELTDOWN IMMINENT !!");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.core_heat", "CORE HEAT");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.absent", "ABSENT");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.scram_has_paused_the_countdown",
                "SCRAM has paused the countdown.");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.cool_the_core_below_critical",
                "Cool the core below critical threshold.");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.engage_scram_to_pause_the",
                "Engage SCRAM to pause the countdown.");
        provider.add("phoenix_fission.ui.fission_reactor_fancy_u_i_widget.structural_failure_imminent",
                "Structural failure imminent.");
        provider.add("phoenix_fission.ui.m_s_r_fancy_u_i_widget.salt_core", "SALT CORE");
        provider.add("phoenix_fission.ui.m_s_r_fancy_u_i_widget.xenon_poison", "XENON POISON");
        provider.add("phoenix_fission.ui.m_s_r_fancy_u_i_widget.salt_loop", "SALT LOOP");
        provider.add("phoenix_fission.ui.m_s_r_fancy_u_i_widget.chemistry", "CHEMISTRY");
        provider.add("phoenix_fission.ui.m_s_r_fancy_u_i_widget.no_catalyst", "  No catalyst");
        provider.add("phoenix_fission.ui.m_s_r_fancy_u_i_widget.scram_engaged", "  SCRAM ENGAGED");
        provider.add("phoenix_fission.ui.m_s_r_fancy_u_i_widget.molten_salt_reactor", "MOLTEN SALT REACTOR");
        provider.add("phoenix_fission.ui.m_s_r_fancy_u_i_widget.scram", "SCRAM");
        provider.add("phoenix_fission.ui.m_s_r_fancy_u_i_widget.critical", "CRITICAL");
        provider.add("phoenix_fission.ui.m_s_r_fancy_u_i_widget.no_liner", "No liner");
        provider.add("phoenix_fission.ui.m_s_r_fancy_u_i_widget.offline", "offline");
        provider.add("phoenix_fission.ui.rtg_fancy_u_i_widget.rtg_array", "RTG ARRAY");
        provider.add("phoenix_fission.ui.rtg_fancy_u_i_widget.cell_active", "%s cell%s active");
        provider.add("phoenix_fission.ui.rtg_fancy_u_i_widget.cells", "CELLS");
        provider.add("phoenix_fission.ui.rtg_fancy_u_i_widget.load_rtg_cells_into_an",
                "Load RTG cells into an input bus");
        provider.add("phoenix_fission.ui.rtg_fancy_u_i_widget.online", "ONLINE");
        provider.add("phoenix_fission.ui.rtg_fancy_u_i_widget.no_cells", "NO CELLS");
        provider.add("phoenix_fission.ui.rtg_graphs_page.rtg_graphs", "RTG GRAPHS");
        provider.add("phoenix_fission.ui.rtg_graphs_page.output_eu_t", "OUTPUT EU/t");
        provider.add("phoenix_fission.ui.rtg_graphs_page.average_cell_life", "AVERAGE CELL LIFE");
        provider.add("phoenix_fission.ui.rtg_graphs_page.collecting", "collecting...");
        provider.add("phoenix_fission.ui.thermoelectric_generator_fancy_u_i_widget.danger", "danger");
        provider.add("phoenix_fission.ui.thermoelectric_generator_fancy_u_i_widget.exchange_stats", "EXCHANGE STATS");
        provider.add("phoenix_fission.ui.thermoelectric_generator_fancy_u_i_widget.idle_awaiting_recipe",
                "  Idle - awaiting recipe");
        provider.add("phoenix_fission.ui.thermoelectric_generator_fancy_u_i_widget.cryo_boost_active_helium_cooling",
                "  CRYO-BOOST ACTIVE - helium cooling");
        provider.add("phoenix_fission.ui.thermoelectric_generator_fancy_u_i_widget.standard_loop_configured_coolant",
                "  Standard loop - configured coolant");
        provider.add("phoenix_fission.ui.thermoelectric_generator_fancy_u_i_widget.burnt_out", "BURNT OUT");
        provider.add("phoenix_fission.ui.thermoelectric_generator_fancy_u_i_widget.cryo_boost", "CRYO-BOOST");
        provider.add("phoenix_fission.ui.thermoelectric_generator_fancy_u_i_widget.active", "ACTIVE");
        provider.add("phoenix_fission.ui.thermoelectric_generator_fancy_u_i_widget.core_heat_burnt_out",
                "CORE HEAT - BURNT OUT");
        provider.add("phoenix_fission.ui.thermoelectric_generator_fancy_u_i_widget.cooldown", "COOLDOWN");
        provider.add("phoenix_fission.ui.thermoelectric_generator_fancy_u_i_widget.s_remaining", "%ss remaining");
        provider.add("phoenix_fission.ui.thermoelectric_generator_fancy_u_i_widget.mb_s_helium_mb_s",
                "%s mB/s helium  +  %s mB/s coolant");
        provider.add("phoenix_fission.ui.thermoelectric_generator_fancy_u_i_widget.mb_s_coolant", "%s mB/s coolant");
        provider.add("phoenix_fission.ui.thermoelectric_generator_fancy_u_i_widget.no_dynamo_hatches",
                "No dynamo hatches");
        provider.add("phoenix_fission.ui.thermoelectric_generator_math_page.columns_len", "Columns (len)");
        provider.add("phoenix_fission.ui.thermoelectric_generator_math_page.linear_multiplier", "Linear multiplier");
        provider.add("phoenix_fission.ui.thermoelectric_generator_math_page.live_eut_multiplier",
                "Live EUt multiplier");
        provider.add("phoenix_fission.ui.thermoelectric_generator_math_page.helium", "Helium");
        provider.add("phoenix_fission.ui.thermoelectric_generator_math_page.detected_tier", "Detected tier");
        provider.add("phoenix_fission.ui.thermoelectric_generator_math_page.max_hatch_output", "Max hatch output");
        provider.add("phoenix_fission.ui.thermoelectric_generator_math_page.no_dynamo_hatches", "no dynamo hatches");
        provider.add("phoenix_fission.ui.thermoelectric_generator_math_page.helium_2", "helium");
        provider.add("phoenix_fission.ui.thermoelectric_generator_math_page.none", "none");
        provider.add("phoenix_fission.ui.scram_halted", "§c[SCRAM] Reactor HALTED");
        provider.add("phoenix_fission.ui.scram_standby", "§a[OK] Standby - Reactor Permitted");
        provider.add("phoenix_fission.ui.sensor_heat_signal", "§7Heat: §f%s%%  §7Signal: %s%s");
        provider.add("phoenix_fission.ui.sensor_heat_na", "§7Heat: §8N/A  §7Signal: §80");
        provider.add("phoenix_fission.ui.fission_scram_hatch", "§cFission SCRAM Hatch");
        provider.add("phoenix_fission.ui.how_it_works", "§eHow it works:");
        provider.add("phoenix_fission.ui.any_redstone_signal_on_any", "§7Any redstone signal on any face");
        provider.add("phoenix_fission.ui.halts_the_reactor_immediately", "§7halts the reactor immediately.");
        provider.add("phoenix_fission.ui.removing_the_signal_resumes_it", "§7Removing the signal resumes it.");
        provider.add("phoenix_fission.ui.hint_not_every_redstone_source", "Hint: not every redstone source");
        provider.add("phoenix_fission.ui.behaves_the_way_you_expect", "behaves the way you expect.");
        provider.add("phoenix_fission.ui.think_carefully_about_signal_timing", "Think carefully about signal timing.");
        provider.add("phoenix_fission.ui.advanced_fission_scram_hatch", "§6Advanced Fission SCRAM Hatch");
        provider.add("phoenix_fission.ui.min_signal_strength_1_15", "§fMin Signal Strength §7(1-15):");
        provider.add("phoenix_fission.ui.sustain_ticks_1_100", "§fSustain Ticks §7(1-100):");
        provider.add("phoenix_fission.ui.scram_arming", "§eArming: %s / %s ticks");
        provider.add("phoenix_fission.ui.scram_triggered", "§cArmed and triggered.");
        provider.add("phoenix_fission.ui.scram_waiting", "§7Waiting for signal...");
        provider.add("phoenix_fission.ui.advanced_fission_stability_sensor", "§bAdvanced Fission Stability Sensor");
        provider.add("phoenix_fission.ui.min_heat_percent", "§fMin Heat Percent:");
        provider.add("phoenix_fission.ui.max_heat_percent", "§fMax Heat Percent:");
        provider.add("phoenix_fission.ui.emit_strength_1_15", "§fEmit Strength §7(1-15):");
        provider.add("phoenix_fission.ui.invert_output", "§fInvert output:");
        provider.add("phoenix_fission.ui.emits_fixed_strength_on_all", "Emits fixed strength on all block faces.");
        provider.add("phoenix_fission.ui.invert_signal_output", "Invert Signal Output");
        provider.add("phoenix_fission.ui.fission_stability_sensor", "§6Fission Stability Sensor");
        provider.add("phoenix_fission.ui.heat_signal", "§7Heat: --   §7Signal: --");
        provider.add("phoenix_fission.ui.emits_signal_on_all_block", "Emits signal on all block faces.");
    }
}
