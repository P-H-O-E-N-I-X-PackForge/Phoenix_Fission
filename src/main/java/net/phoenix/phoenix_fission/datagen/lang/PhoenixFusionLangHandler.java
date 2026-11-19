package net.phoenix.phoenix_fission.datagen.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

/** Text for the Tokamak and the other late additions, brought over from the NeoForge branch. */
public class PhoenixFusionLangHandler {

    public static void init(RegistrateLangProvider provider) {
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
        provider.add("phoenix.multiblock.pattern.info.fusion_inner_coils", "Any Fusion Inner Plasma Coil");
        provider.add("phoenix.multiblock.pattern.info.fusion_outer_coils",
                "Any GT heating coil (cupronickel and up) or fusion outer coil");
        provider.add("phoenix.multiblock.pattern.info.fusion_compute_modules",
                "Any Fusion Compute Module (compute rack)");
        provider.add("config.jade.plugin_phoenix_fission.fusion_reactor_info", "Fusion Reactor Info");
        provider.add("jade.phoenix_fission.fusion_scrammed", "SCRAMMED");
        provider.add("jade.phoenix_fission.fusion_quenched", "QUENCHED - restarting in %ss");
        provider.add("jade.phoenix_fission.fusion_output", "Output: %s EU/t");
        provider.add("jade.phoenix_fission.fusion_standby", "Plasma off");
        provider.add("jade.phoenix_fission.fusion_field", "Field integrity: %s%%  confinement %sx");
        provider.add("jade.phoenix_fission.fusion_throttle", "Throttle: %s%%");
        provider.add("jade.phoenix_fission.fusion_no_coolant", "Coils overheating - no coolant");
        provider.add("jade.phoenix_fission.fusion_contamination", "Contamination: %s%%");
        provider.add("jade.phoenix_fission.fusion_compute", "Compute rack L%s, compute %s, recipe tier %s");
        provider.add("jade.phoenix_fission.fusion_lanes", "x%s parallel, %s threads, OC x%s");
        provider.add("jade.phoenix_fission.fusion_pressure", "Pressure: %s (needs %s)");
        provider.add("jade.phoenix_fission.fusion_no_gas", "No pressure gas");
        provider.add("jade.phoenix_fission.fusion_irradiated", "Plasma irradiated");
        provider.add("phoenix_fission.machine.fusion_reactor.tooltip1",
                "§dMagnetic-confinement fusion. Keep the plasma field steady while it burns deuterium and tritium.");
        provider.add("phoenix_fission.machine.fusion_reactor.tooltip2",
                "§7Inner coils (fusion coils) make plasma power. Outer coils (superconducting coils) hold the field and cost EU/t.");
        provider.add("phoenix_fission.machine.fusion_reactor.tooltip3",
                "§7Field ratio must stay above §f1.0x§7. Hold pressure near §fthrottle x 0.8§7 with nitrogen. Both are set on the screen.");
        provider.add("phoenix_fission.machine.fusion_reactor.tooltip4",
                "§7Each coil type has its own coolant. Waste goes to an item output hatch; air purges contamination.");
        provider.add("phoenix_fission.machine.fusion_reactor.tooltip5",
                "§bCompute: the computer room (basic modules) is part of the build. Upgrades are banks of module slots beside it, about 25-29 blocks behind the controller and 7-11 down.");
        provider.add("phoenix_fission.machine.fusion_reactor.tooltip6",
                "§7More energy hatches overclock recipes: §f2§7 hatches give one level, §f8§7 give two.");
        provider.add("phoenix_fission.machine.fusion_reactor.tooltip7",
                "§8A SCRAM hatch stops the plasma safely. A stability sensor reads field drift.");
        provider.add("phoenix_fission.machine.rtg_array.tooltip1",
                "§6Radioisotope power. Load RTG cells into an item input bus.");
        provider.add("phoenix_fission.machine.rtg_array.tooltip2",
                "§7Each cell adds its EU/t and wears one point per second. Spent cells stay in the bus.");
    }
}
