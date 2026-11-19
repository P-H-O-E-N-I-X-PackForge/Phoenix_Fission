package net.phoenix.phoenix_fission.gtm8.datagen.lang;

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
        provider.add("phoenix_fission.ascension_reactor_machines.the_core_s_output_the",
                "§7the core's output. The core and the ring both need to be built well.");
        provider.add("phoenix_fission.ascension_reactor_machines.let_the_field_fail_and",
                "§7Let the field fail and the whole climb is lost. Ascended, the ring adds to");
        provider.add("phoenix_fission.ascension_reactor_machines.hold_all_three_in_their",
                "§7Hold all three in their bands to climb: ignition, coupling, sustain, ascend.");
        provider.add("phoenix_fission.ascension_reactor_machines.the_coils_and_the_gas",
                "§7the coils and the gas injection set the plasma; the plasma feeds back.");
        provider.add("phoenix_fission.ascension_reactor_machines.disturbances_you_can_see_coming",
                "§7disturbances you can see coming. The rods set how long the core burns;");
        provider.add("phoenix_fission.ascension_reactor_machines.flux_heat_and_plasma_are",
                "§7Flux, heat and plasma are read from it, and each is pushed about by");
        provider.add("phoenix_fission.ascension_reactor_machines.with_the_usual_heat_model",
                "§7with the usual heat model, coolant, fuel recipes, SCRAM hatches and sensors.");
        provider.add("phoenix_fission.ascension_reactor_machines.the_core_is_a_full",
                "§7The core is a full fission core: fuel rods, coolers, moderators and blankets,");
        provider.add("phoenix_fission.ascension_reactor_machines.the_real_fission_reactor_inside",
                "§bThe real fission reactor, inside a fusion ring, and neither will sit still.");
        provider.add("phoenix_fission.ui.ascension_graphs_page.ascension_graphs", "ASCENSION GRAPHS");
        provider.add("phoenix_fission.ui.ascension_graphs_page.flux", "FLUX");
        provider.add("phoenix_fission.ui.ascension_graphs_page.heat", "HEAT");
        provider.add("phoenix_fission.ui.ascension_graphs_page.plasma", "PLASMA");
        provider.add("phoenix_fission.ui.ascension_graphs_page.field_stability", "FIELD STABILITY");
        provider.add("phoenix_fission.ui.ascension_graphs_page.collecting", "collecting...");
        provider.add("phoenix_fission.ui.ascension_hud.ascension_reactor", "ASCENSION REACTOR");
        provider.add("phoenix_fission.ui.ascension_hud.structure_not_complete", "Structure not complete");
        provider.add("phoenix_fission.ui.ascension_hud.scram", "SCRAM");
        provider.add("phoenix_fission.ui.ascension_hud.offline", "OFFLINE");
        provider.add("phoenix_fission.ui.ascension_hud.scrammed_s", "SCRAMMED %ss");
        provider.add("phoenix_fission.ui.ascension_hud.standby", "STANDBY");
        provider.add("phoenix_fission.ui.ascension_hud.ascended", "ASCENDED");
        provider.add("phoenix_fission.ui.ascension_hud.scrammed_the_core_is_held",
                "SCRAMMED. The core is held while it cools off.");
        provider.add("phoenix_fission.ui.ascension_hud.meltdown_in_s_cool_the",
                "!! MELTDOWN IN %ss. Cool the core or SCRAM.");
        provider.add("phoenix_fission.ui.ascension_hud.scram_hatch_signal_clear_it",
                "SCRAM hatch signal. Clear it to resume.");
        provider.add("phoenix_fission.ui.ascension_hud.for_s", "!! %s for %ss");
        provider.add("phoenix_fission.ui.ascension_hud.warning_in_s", "Warning: %s in %ss");
        provider.add("phoenix_fission.ui.ascension_hud.no_coolant_reaching_the_coolers",
                "No coolant reaching the coolers. The core will heat.");
        provider.add("phoenix_fission.ui.ascension_hud.waiting_for_fuel_and_active",
                "Waiting for fuel and active rods in the core.");
        provider.add("phoenix_fission.ui.ascension_hud.no_injection_gas_in_the",
                "No injection gas in the hatches. The plasma is thin.");
        provider.add("phoenix_fission.ui.ascension_hud.not_enough_power_for_the", "Not enough power for the coils.");
        provider.add("phoenix_fission.ui.ascension_hud.ascended_the_ring_adds_to",
                "ASCENDED: the ring adds to the core's output. Keep it steady.");
        provider.add("phoenix_fission.ui.ascension_hud.hold_all_gauges_in_their", "Hold all gauges in their bands.");
        provider.add("phoenix_fission.ui.ascension_hud.cooling", "cooling %s/%s");
        provider.add("phoenix_fission.ui.ascension_hud.no_coolant", "NO COOLANT");
        provider.add("phoenix_fission.ui.ascension_hud.x_rods_ring_coils", "x%s rods  ring %s coils");
        provider.add("phoenix_fission.ui.ascension_hud.runs_lost_ascended_x", "runs lost %s  ascended %sx");
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
        provider.add("phoenix_fission.ui.fission_reactor_hud.net_heat_coolant", "NET HEAT / COOLANT");
        provider.add("phoenix_fission.ui.fission_reactor_hud.collecting_data", "Collecting data...");
        provider.add("phoenix_fission.ui.fission_reactor_hud.per_tick", "PER TICK");
        provider.add("phoenix_fission.ui.fission_reactor_hud.over_cooled_90_coolant_refund",
                "  OVER-COOLED  (90% coolant refund, no EU)");
        provider.add("phoenix_fission.ui.fission_reactor_hud.components", "COMPONENTS");
        provider.add("phoenix_fission.ui.fission_reactor_hud.no_blanket_installed", "  No blanket installed");
        provider.add("phoenix_fission.ui.fission_reactor_hud.nominal", "  Nominal");
        provider.add("phoenix_fission.ui.fission_reactor_hud.no_moderators_installed", "No moderators installed.");
        provider.add("phoenix_fission.ui.fission_reactor_hud.no_cooler_blocks_installed",
                "No cooler blocks installed.");
        provider.add("phoenix_fission.ui.fission_reactor_hud.coolers_reduce_reactor_temperature",
                "Coolers reduce reactor temperature.");
        provider.add("phoenix_fission.ui.fission_reactor_hud.always_active_no_coolant_needed",
                "Always active - no coolant needed");
        provider.add("phoenix_fission.ui.fission_reactor_hud.no_blanket_blocks_detected",
                "No blanket blocks detected.");
        provider.add("phoenix_fission.ui.fission_reactor_hud.place_blanket_blocks_inside_the",
                "Place blanket blocks inside the reactor");
        provider.add("phoenix_fission.ui.fission_reactor_hud.casing_to_begin_breeding_materials",
                "casing to begin breeding materials.");
        provider.add("phoenix_fission.ui.fission_reactor_hud.input", "  Input: %s");
        provider.add("phoenix_fission.ui.fission_reactor_hud.no_outputs_configured", "  No outputs configured");
        provider.add("phoenix_fission.ui.fission_reactor_hud.outputs", "  Outputs:");
        provider.add("phoenix_fission.ui.fission_reactor_hud.heat_net", "heat (net)");
        provider.add("phoenix_fission.ui.fission_reactor_hud.cool_hu_t", "cool (HU/t)");
        provider.add("phoenix_fission.ui.fission_reactor_hud.countdown", "COUNTDOWN");
        provider.add("phoenix_fission.ui.fission_reactor_hud.net_rate", "Net rate");
        provider.add("phoenix_fission.ui.fission_reactor_hud.coolant", "Coolant");
        provider.add("phoenix_fission.ui.fission_reactor_hud.breeder_fission_reactor", "BREEDER FISSION REACTOR");
        provider.add("phoenix_fission.ui.fission_reactor_hud.fission_reactor", "FISSION REACTOR");
        provider.add("phoenix_fission.ui.fission_reactor_hud.breeder_reactor", "BREEDER REACTOR");
        provider.add("phoenix_fission.ui.fission_reactor_hud.running", "RUNNING");
        provider.add("phoenix_fission.ui.fission_reactor_hud.stable", "~ stable");
        provider.add("phoenix_fission.ui.fission_reactor_hud.details", "details >");
        provider.add("phoenix_fission.ui.fission_reactor_hud.x_par", "x%s par");
        provider.add("phoenix_fission.ui.fission_reactor_hud.coolant_ok", "  Coolant OK");
        provider.add("phoenix_fission.ui.fission_reactor_hud.no_coolant", "  NO COOLANT");
        provider.add("phoenix_fission.ui.fission_reactor_hud.x_parallels", "x%s parallels");
        provider.add("phoenix_fission.ui.fission_reactor_hud.stab", "STAB");
        provider.add("phoenix_fission.ui.fission_reactor_hud.sens", "SENS");
        provider.add("phoenix_fission.ui.fission_reactor_hud.meltdown", "MELTDOWN");
        provider.add("phoenix_fission.ui.fission_reactor_hud.meltdown_s", "MELTDOWN  %ss");
        provider.add("phoenix_fission.ui.fission_reactor_hud.paused_scram", "PAUSED - SCRAM");
        provider.add("phoenix_fission.ui.fission_reactor_hud.hu_rod", "%s HU/rod");
        provider.add("phoenix_fission.ui.fission_reactor_hud.x_per_cycle", "x%s per cycle");
        provider.add("phoenix_fission.ui.fission_reactor_hud.burns_1_per", "burns 1 per %s");
        provider.add("phoenix_fission.ui.fission_reactor_hud.burns_nothing", "burns nothing");
        provider.add("phoenix_fission.ui.fission_reactor_hud.no_discount", "no discount");
        provider.add("phoenix_fission.ui.fission_reactor_hud.parallel", "+%s parallel");
        provider.add("phoenix_fission.ui.fission_reactor_hud.no_par_bonus", "no par bonus");
        provider.add("phoenix_fission.ui.fission_reactor_hud.passive_x", "PASSIVE  x%s");
        provider.add("phoenix_fission.ui.fission_reactor_hud.k_threshold", "%s K threshold");
        provider.add("phoenix_fission.ui.fission_reactor_hud.active_now", "Active now");
        provider.add("phoenix_fission.ui.fission_reactor_hud.idle_heat_threshold", "Idle (heat < threshold)");
        provider.add("phoenix_fission.ui.fission_reactor_hud.x_tick", "x%s/tick");
        provider.add("phoenix_fission.ui.fission_reactor_hud.req_fuel_none", "  Req. fuel: None");
        provider.add("phoenix_fission.ui.fission_reactor_hud.req_fuel", "  Req. fuel: %s");
        provider.add("phoenix_fission.ui.fission_reactor_hud.back", "[< BACK]");
        provider.add("phoenix_fission.ui.fission_reactor_hud.unknown", "Unknown");
        provider.add("phoenix_fission.ui.fission_reactor_hud.stable_2", "(stable)");
        provider.add("phoenix_fission.ui.fission_reactor_hud.low", "(low)");
        provider.add("phoenix_fission.ui.fission_reactor_hud.moderate", "(moderate)");
        provider.add("phoenix_fission.ui.fission_reactor_hud.high", "(high)");
        provider.add("phoenix_fission.ui.fission_reactor_hud.meltdown_scram_engaged", "MELTDOWN  -  SCRAM ENGAGED");
        provider.add("phoenix_fission.ui.fission_reactor_hud.meltdown_imminent", "!! MELTDOWN IMMINENT !!");
        provider.add("phoenix_fission.ui.fission_reactor_hud.core_heat", "CORE HEAT");
        provider.add("phoenix_fission.ui.fission_reactor_hud.absent", "ABSENT");
        provider.add("phoenix_fission.ui.fission_reactor_hud.scram_has_paused_the_countdown",
                "SCRAM has paused the countdown.");
        provider.add("phoenix_fission.ui.fission_reactor_hud.cool_the_core_below_critical",
                "Cool the core below critical threshold.");
        provider.add("phoenix_fission.ui.fission_reactor_hud.engage_scram_to_pause_the",
                "Engage SCRAM to pause the countdown.");
        provider.add("phoenix_fission.ui.fission_reactor_hud.structural_failure_imminent",
                "Structural failure imminent.");
        provider.add("phoenix_fission.ui.fusion_graphs_page.fusion_graphs", "FUSION GRAPHS");
        provider.add("phoenix_fission.ui.fusion_graphs_page.output_eu_t", "OUTPUT EU/t");
        provider.add("phoenix_fission.ui.fusion_graphs_page.field_integrity", "FIELD INTEGRITY");
        provider.add("phoenix_fission.ui.fusion_graphs_page.pressure", "PRESSURE");
        provider.add("phoenix_fission.ui.fusion_hud.tokamak_fusion_reactor", "TOKAMAK FUSION REACTOR");
        provider.add("phoenix_fission.ui.fusion_hud.confinement", "CONFINEMENT");
        provider.add("phoenix_fission.ui.fusion_hud.contamination", "CONTAMINATION");
        provider.add("phoenix_fission.ui.fusion_hud.throttle", "THROTTLE %");
        provider.add("phoenix_fission.ui.fusion_hud.pressure", "PRESSURE %");
        provider.add("phoenix_fission.ui.fusion_hud.quenched", "QUENCHED");
        provider.add("phoenix_fission.ui.fusion_hud.burning", "BURNING");
        provider.add("phoenix_fission.ui.fusion_hud.scrammed_clear_the_scram_hatch",
                "SCRAMMED -- clear the SCRAM hatch signal to resume");
        provider.add("phoenix_fission.ui.fusion_hud.quenched_restarting_in_s", "!! QUENCHED -- restarting in %ss");
        provider.add("phoenix_fission.ui.fusion_hud.field_unpowered_supply_energy_to",
                "!! FIELD UNPOWERED -- supply energy to the coils");
        provider.add("phoenix_fission.ui.fusion_hud.no_fuel_deuterium_and_tritium",
                "No fuel -- deuterium and tritium needed");
        provider.add("phoenix_fission.ui.fusion_hud.no_pressure_gas_the_chamber",
                "!! No pressure gas -- the chamber is losing pressure");
        provider.add("phoenix_fission.ui.fusion_hud.coils_overheating_supply_coolant",
                "!! Coils overheating -- supply coolant");
        provider.add("phoenix_fission.ui.fusion_hud.waste_backing_up_empty_the",
                "!! Waste backing up -- empty the output hatch");
        provider.add("phoenix_fission.ui.fusion_hud.field_too_weak_lower_the",
                "!! Field too weak -- lower the throttle or add coils");
        provider.add("phoenix_fission.ui.fusion_hud.plasma_stable", "Plasma stable");
        provider.add("phoenix_fission.ui.fusion_math_page.fusion_math", "FUSION MATH");
        provider.add("phoenix_fission.ui.msr_hud.molten_salt_reactor", "MOLTEN SALT REACTOR");
        provider.add("phoenix_fission.ui.msr_hud.salt_core", "SALT CORE");
        provider.add("phoenix_fission.ui.msr_hud.xenon_poison", "XENON POISON");
        provider.add("phoenix_fission.ui.msr_hud.salt_loop", "SALT LOOP");
        provider.add("phoenix_fission.ui.msr_hud.fuel_salt", "Fuel salt");
        provider.add("phoenix_fission.ui.msr_hud.chemistry", "CHEMISTRY");
        provider.add("phoenix_fission.ui.msr_hud.no_catalyst", "  No catalyst");
        provider.add("phoenix_fission.ui.msr_hud.scram_engaged", "  SCRAM ENGAGED");
        provider.add("phoenix_fission.ui.msr_hud.critical", "CRITICAL");
        provider.add("phoenix_fission.ui.msr_hud.no_liner", "No liner");
        provider.add("phoenix_fission.ui.msr_hud.offline", "offline");
        provider.add("phoenix_fission.ui.rtg_graphs_page.rtg_graphs", "RTG GRAPHS");
        provider.add("phoenix_fission.ui.rtg_graphs_page.average_cell_life", "AVERAGE CELL LIFE");
        provider.add("phoenix_fission.ui.rtg_hud.rtg_array", "RTG ARRAY");
        provider.add("phoenix_fission.ui.rtg_hud.cell_active", "%s cell%s active");
        provider.add("phoenix_fission.ui.rtg_hud.cells", "CELLS");
        provider.add("phoenix_fission.ui.rtg_hud.load_rtg_cells_into_an", "Load RTG cells into an input bus");
        provider.add("phoenix_fission.ui.rtg_hud.online", "ONLINE");
        provider.add("phoenix_fission.ui.rtg_hud.no_cells", "NO CELLS");
        provider.add("phoenix_fission.ui.teg_hud.danger", "danger");
        provider.add("phoenix_fission.ui.teg_hud.exchange_stats", "EXCHANGE STATS");
        provider.add("phoenix_fission.ui.teg_hud.idle_awaiting_recipe", "  Idle - awaiting recipe");
        provider.add("phoenix_fission.ui.teg_hud.cryo_boost_active_helium_cooling",
                "  CRYO-BOOST ACTIVE - helium cooling");
        provider.add("phoenix_fission.ui.teg_hud.standard_loop_configured_coolant",
                "  Standard loop - configured coolant");
        provider.add("phoenix_fission.ui.teg_hud.burnt_out", "BURNT OUT");
        provider.add("phoenix_fission.ui.teg_hud.cryo_boost", "CRYO-BOOST");
        provider.add("phoenix_fission.ui.teg_hud.active", "ACTIVE");
        provider.add("phoenix_fission.ui.teg_hud.core_heat_burnt_out", "CORE HEAT - BURNT OUT");
        provider.add("phoenix_fission.ui.teg_hud.cooldown", "COOLDOWN");
        provider.add("phoenix_fission.ui.teg_hud.s_remaining", "%ss remaining");
        provider.add("phoenix_fission.ui.teg_hud.mb_s_helium_mb_s", "%s mB/s helium  +  %s mB/s coolant");
        provider.add("phoenix_fission.ui.teg_hud.mb_s_coolant", "%s mB/s coolant");
        provider.add("phoenix_fission.ui.teg_hud.no_dynamo_hatches", "No dynamo hatches");
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
    }
}
