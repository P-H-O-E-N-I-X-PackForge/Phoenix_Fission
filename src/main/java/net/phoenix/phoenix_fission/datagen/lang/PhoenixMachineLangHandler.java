package net.phoenix.phoenix_fission.datagen.lang;

import com.tterrag.registrate.providers.RegistrateLangProvider;

public class PhoenixMachineLangHandler {

    public static void init(RegistrateLangProvider provider) {
        provider.add("emi.category.phoenix_fission.fission_fuel_burn", "Fission Fuel Burn");
        provider.add("emi.category.phoenix_fission.fission_coolant_cycle", "Fission Coolant Cycle");
        provider.add("emi.category.phoenix_fission.fission_moderator", "Fission Moderator");
        provider.add("emi.category.phoenix_fission.fission_blanket", "Breeder Blanket");
        provider.add("emi.category.phoenix_fission.fission_msr_liner", "MSR Core Liner");

        provider.add("block.phoenix_fission.fission_blanket.info_header", "Breeder Blanket Specifications:");
        provider.add("phoenix_fission.blanket.input", "Breeding Target Input");

        provider.add("phoenix_fission.tooltip.amount", "Yield Batch Size");
        provider.add("phoenix_fission.tooltip.required_fuel", "Required Driver Fuel");

        provider.add("phoenix_fission.coolant_usage_value",
                "§dConsumption Rate: §5%d mB/t");

        provider.add("block.phoenix_fission.msr_liner.info_header",
                "--- Molten Salt Core Liner Specifications ---");

        provider.add("phoenix_fission.blanket.generation_features",
                "Transmutation Loop Parameters:");

        provider.add("tech.phoenix_fission.laser.input.low", "Tesla Optical Collimator");
        provider.add("tech.phoenix_fission.laser.input.mid", "Tesla Optical Collimation Grid");
        provider.add("tech.phoenix_fission.laser.input.high", "Tesla Phased Beam Matrix");

        provider.add("tech.phoenix_fission.laser.output.low", "Tesla Photonic Coalescer");
        provider.add("tech.phoenix_fission.laser.output.mid", "Tesla Photonic Coalescence Array");
        provider.add("tech.phoenix_fission.laser.output.high", "Tesla Photonic Coalescence Matrix");

        provider.add("gui.phoenix_fission.thermoelectric_generator.heat_exchange_surface", "Exchange Columns: %d");
        provider.add("gui.phoenix_fission.thermoelectric_generator.current_efficiency",
                "Thermal Conductivity: Tier %d");
        provider.add("gui.phoenix_fission.missing_spring", "Missing Heat Exchange Spring!");

        provider.add("tooltip.phoenix.empty_component.0", "This is an empty component, provides no stats.");
        provider.add("tooltip.phoenix.empty_component.1", "Useful for filling out a Fission Reactor.");
        provider.add("phoenix_fission.not_formed", "Structure not formed!");
        provider.add("phoenix_fission.status.safe_idle", "Status: §aIDLE");
        provider.add("phoenix_fission.status.safe_working", "Status: §6ACTIVE");
        provider.add("phoenix_fission.status.danger_timer", "§cCRITICAL: Meltdown in %s seconds!");
        provider.add("phoenix_fission.status.no_coolant", "§eWARNING: Coolant Supply Exhausted");
        provider.add("phoenix_fission.nuke_radius", "Blast area: %s");

        provider.add("phoenix_fission.blanket.potential_outputs", "Potential Transmutations:");
        provider.add("phoenix_fission.blanket.bias_hint",
                "§d§oHigher instability yields are favored by a Fast Neutron Spectrum (High Heat/Bias).");
        provider.add("phoenix_fission.blanket_outputs", "§7Possible Products:");
        provider.add("phoenix_fission.blanket_input", "§7Target Material: §f%s");
        provider.add("phoenix_fission.blanket_output", "§7Breeding Product: §f%s");
        provider.add("phoenix_fission.blanket_desc", "Irradiate target materials to produce specialized isotopes.");
        provider.add("phoenix_fission.blanket_cycle", "Transmutes §f%s§7 units every §6%s§7 seconds");

        provider.add("phoenix_fission.neutron_bias", "§7Neutron Bias: §f%s");
        provider.add("phoenix_fission.spectrum_shift", "§7Spectrum Shift: §f%s");
        provider.add("phoenix_fission.current_heat", "Core Temperature: %s HU");
        provider.add("phoenix_fission.net_heat", "Net Heat Change: %s HU/t");
        provider.add("phoenix_fission.heat_production", "Heat Production: %s");
        provider.add("phoenix_fission.eu_generation", "Output: %s EU/t");
        provider.add("phoenix_fission.parallels", "Parallel Processing: %sx");

        provider.add("phoenix_fission.moderator", "Primary Moderator: %s");
        provider.add("phoenix_fission.moderator_fuel_discount", "Fuel Efficiency: +%s%%");
        provider.add("phoenix_fission.cooler", "Primary Cooling: %s");
        provider.add("phoenix_fission.coolant", "Coolant: %s");
        provider.add("phoenix_fission.coolant_rate", "Coolant Flow: %s mb/t");
        provider.add("phoenix_fission.coolant_output", "Hot Coolant Produced: %s");
        provider.add("phoenix_fission.coolant_status.ok", "§aCoolant Supply OK");
        provider.add("phoenix_fission.coolant_status.empty", "§cCoolant Supply Depleted");
        provider.add("tooltip.phoenix_fission.rtg_cell.output", "Output: %s EU/t");
        provider.add("tooltip.phoenix_fission.rtg_cell.remaining", "Remaining: %s of %s");
        provider.add("jade.phoenix_fission.rtg_output", "Output: %s EU/t");
        provider.add("jade.phoenix_fission.rtg_cells", "Cells: %s");
        provider.add("jade.phoenix_fission.rtg_shortest", "Shortest remaining: %s");
        provider.add("jade.phoenix_fission.rtg_no_cells", "No RTG cells loaded");
        provider.add("phoenix_fission.summary", "Cooling: %s / %s HU/t");
        provider.add("phoenix_fission.cooling_power", "§bCooling Capacity: §f%s HU/t");

        provider.add("phoenix_fission.fuel_cycle", "Consumes §f%s§7 units every §6%s§7 seconds");
        provider.add("phoenix_fission.depleted_fuel", "§7Depleted Fuel: §f%s");
        provider.add("phoenix_fission.fuel_usage", "Fuel Consumption: §f%s");
        provider.add("phoenix_fission.fuel_required", "§7Requires Fuel: §f%s");
        provider.add("phoenix_fission.coolant_required", "§3Required Coolant: §f%s");

        provider.add("block.phoenix_fission.fission_cooler.capacity", "§bCooling Capacity: §f%s HU/t");
        provider.add("block.phoenix_fission.fission_cooler.required_coolant", "§3Required Coolant: §f%s");
        provider.add("block.phoenix_fission.fission_moderator.multiplier", "§6Heat Multiplier: §f%sx");
        provider.add("block.phoenix_fission.fission_moderator.parallel", "§aParallel Bonus: §f+%s");
        provider.add("block.phoenix_fission.fission_moderator.shift", "Hold Shift for details");
        provider.add("block.phoenix_fission.fission_moderator.info_header", "Fission Moderator");
        provider.add("block.phoenix_fission.fission_moderator.boost", "EU Boost: %s");
        provider.add("block.phoenix_fission.fission_moderator.fuel_discount", "Fuel Discount: %s");
        provider.add("block.phoenix_fission.fission_cooler.info_header", "Fission Cooler");
        provider.add("block.phoenix_fission.fission_fuel_rod.info_header", "Fission Fuel Rod");

        provider.add("phoenix_fission.current_heat_display", "Core Temperature: %s / %s HU");
        provider.add("phoenix_fission.status.scram", "§c§lSCRAM ACTIVE");

        provider.add("block.phoenix_fission.fission_scram_hatch.desc",
                "Stops fuel consumption and heat generation when receiving a Redstone signal.");

        provider.add("phoenix_fission.machine.fission_scram_hatch.tooltip",
                "§cEmergency Reactor Brake§r: Halts the reactor on §fany§r redstone signal.");
        provider.add("phoenix_fission.machine.fission_scram_hatch.tooltip2",
                "§8No configuration. No mercy. Build your circuit carefully.");

        provider.add("phoenix_fission.machine.fission_advanced_scram_hatch.tooltip",
                "§6Precision SCRAM Control§r: Triggers only above a configured signal strength,");
        provider.add("phoenix_fission.machine.fission_advanced_scram_hatch.tooltip2",
                "§7and only after a sustained signal. Configurable via UI.");

        provider.add("phoenix_fission.machine.fission_stability_sensor.tooltip",
                "§eThermal Monitor§r: Emits a §fproportional§r redstone signal based on core heat.");

        provider.add("gui.phoenix_fission.stability_sensor.title", "Thermal Stability Configuration");
        provider.add("gui.phoenix_fission.stability_sensor.min", "Min Heat Threshold %");
        provider.add("gui.phoenix_fission.stability_sensor.max", "Max Heat Threshold %");
        provider.add("gui.phoenix_fission.stability_sensor.invert", "Invert Signal");

        provider.add("gui.phoenix_fission.advanced_stability_sensor.title", "Advanced Thermal Stability Configuration");
        provider.add("gui.phoenix_fission.advanced_stability_sensor.min", "Min Heat Threshold %");
        provider.add("gui.phoenix_fission.advanced_stability_sensor.max", "Max Heat Threshold %");
        provider.add("gui.phoenix_fission.advanced_stability_sensor.strength", "Emit Strength (1-15)");
        provider.add("gui.phoenix_fission.advanced_stability_sensor.invert", "Invert Signal");
        provider.add("gui.phoenix_fission.advanced_stability_sensor.hint1", "Emits fixed strength on back face only.");
        provider.add("gui.phoenix_fission.advanced_stability_sensor.hint2", "Pair with an Advanced SCRAM Hatch.");

        provider.add("gui.phoenix_fission.advanced_scram.title", "Advanced SCRAM Configuration");
        provider.add("gui.phoenix_fission.advanced_scram.threshold", "Min Signal Strength (1-15)");
        provider.add("gui.phoenix_fission.advanced_scram.sustain", "Sustain Duration (ticks)");
        provider.add("gui.phoenix_fission.advanced_scram.status_armed", "§c[SCRAM] Reactor HALTED");
        provider.add("gui.phoenix_fission.advanced_scram.status_arming", "§eArming: %d / %d ticks");
        provider.add("gui.phoenix_fission.advanced_scram.status_standby", "§a[OK] Standby - Reactor Permitted");
        provider.add("gui.phoenix_fission.advanced_scram.status_triggered", "§cArmed and triggered.");
        provider.add("gui.phoenix_fission.advanced_scram.status_waiting", "§7Waiting for signal...");
        provider.add("gui.phoenix_fission.advanced_scram.hint1", "Signal must meet strength threshold");
        provider.add("gui.phoenix_fission.advanced_scram.hint2", "for the full sustain duration to SCRAM.");

        provider.add("phoenix_fission.status.scram_basic", "§c§lSCRAM ACTIVE §8(Basic Hatch)");
        provider.add("phoenix_fission.status.scram_advanced", "§6§lSCRAM ACTIVE §8(Advanced Hatch)");
        provider.add("phoenix_fission.status.scram_arming", "§e§lSCRAM ARMING: §f%d / %d ticks");

        provider.add("phoenix.multiblock.pattern.info.multiple_fuel_rods",
                "Requires Fuel Rods. These generate base heat and determine recipe parallels.");
        provider.add("phoenix.multiblock.pattern.info.multiple_blankets",
                "Requires Blanket Rods. These act as targets for transmutation in Breeder cycles.");
        provider.add("phoenix.multiblock.pattern.info.multiple_moderators",
                "Moderators adjust heat generation and can provide EU or Parallel bonuses.");
        provider.add("phoenix.multiblock.pattern.info.multiple_coolers",
                "Coolers remove heat based on their tier and provided coolant fluid.");
        provider.add("phoenix.multiblock.pattern.info.multiple_liners",
                "Liners provide fuel and flow, can mix and match..");

        provider.add("gtceu.high_performance_breeder_reactor",
                "High-Performance Breeder Reactor");
        provider.add("gtceu.thermoelectric_generating",
                "Thermoelectric Generating");
        provider.add("gtceu.source_extraction",
                "Source Extraction");
        provider.add("gtceu.source_imbuement",
                "Source Imbuement");
        provider.add("gtceu.source_reactor",
                "Source Reactor");
        provider.add("gtceu.advanced_pressurized_fission_reactor",
                "Advanced Pressurized Fission Reactor");
        provider.add("gtceu.pressurized_fission_reactor", "Pressurized Fission Reactor");

        provider.add("gtceu.honey_chamber", "Honey Chamber");
        provider.add("gtceu.please", "Please Multiblock");
        provider.add("gtceu.simulated_colony", "Simulated Colony");
        provider.add("gtceu.comb_decanting", "Comb Decanter");
        provider.add("gtceu.swarm_nurturing", "Swarm Nurturing Chamber");
        provider.add("gtceu.apis_progenitor", "Apis Progenitor");

        provider.add("gtceu.tooltip.tier", "Tier: %s");

        provider.add("config.jade.plugin_phoenix_fission.source_machine_info", "Source Machine Information");
        provider.add("config.jade.plugin_phoenix_fission.plasma_furnace_info", "High-Pressure Plasma Arc Furnace Info");
        provider.add("config.jade.plugin_phoenix_fission.tesla_network_info", "Tesla Network Information");
        provider.add("config.jade.plugin_phoenix_fission.fission_machine_info", "Fission Machine Info");

        provider.add("phoenix_fission.machine.fission_advanced_stability_sensor.tooltip",
                "§bAdvanced Thermal Monitor§r: Emits a highly customizable redstone payload.");
        provider.add("phoenix_fission.machine.fission_advanced_stability_sensor.tooltip2",
                "§7Allows manual output strength adjustment. Pair with an Advanced SCRAM Hatch.");

        provider.add("jade.phoenix_fission.shield_state", "Shield State: %s");
        provider.add("jade.phoenix_fission.shield_health", "Shield Health: %d");
        provider.add("jade.phoenix_fission.shield_cooldown", "Shield Recharging: %ds");
        provider.add("jade.phoenix_fission.plasma_boost_duration", "Power Multiplier: %s");

        provider.add("jade.phoenix_fission.plasma_boost_active", "Plasma Boost: %s Active");
        provider.add("jade.phoenix_fission.no_plasma_boost", "No Plasma Catalyst");
        provider.add("jade.phoenix_fission.tesla_stored", "Stored: ");
        provider.add("jade.phoenix_fission.tesla_receiving", "Receiving: %s EU/t");
        provider.add("jade.phoenix_fission.tesla_providing", "Providing: %s EU/t");
        provider.add("block.phoenix_fission.tesla_battery.tooltip_empty", "§7A hollow casing. Provides no storage.");
        provider.add("block.phoenix_fission.tesla_battery.tooltip_filled", "§aCapacity: §f%s EU");
        provider.add("config.jade.plugin_phoenix_fission.imbuer_threads_info", "Alchemical Imbuer Threads Info");

        provider.add("jade.phoenix_fission.blanket_input", "Blanket Fuel: %s");
        provider.add("jade.phoenix_fission.blanket_output", "Breeding Product: %s");
        provider.add("jade.phoenix_fission.blanket_amount", "Base per cycle: %s");
        provider.add("jade.phoenix_fission.heat", "§cCore Heat: %s HU");
        provider.add("jade.phoenix_fission.fission_meltdown_timer", "§6MELTDOWN: %s seconds!");
        provider.add("jade.phoenix_fission.fission_safe", "§aCore Stable");
        provider.add("jade.phoenix_fission.fission_no_coolant", "§cNO COOLANT DETECTED");
        provider.add("jade.phoenix_fission.fission_heating", "§eCORE HEATING UP");

        provider.add("jade.phoenix_fission.source_giving", "Producing Source");
        provider.add("jade.phoenix_fission.source_taking", "Consuming Source");
        provider.add("jade.phoenix_fission.source_consumption", "Source Consumption:");
        provider.add("jade.phoenix_fission.source_production", "Source Production:");

        provider.add("multiblock.tooltip.machinetype", "Machine Type: %s");
        provider.add("multiblock.yellowline", "§e--------------------");
        provider.add("multiblock.underyellowline", "Hold §e§lSHIFT§r to display structure details!");
        provider.add("multiblock.structureadvtooltip", "Structure:");

        provider.add("phoenix_fission.msr.thermal_efficiency", "Thermal Efficiency: %s");
        provider.add("phoenix_fission.msr.structural_tier", "MSR Core Structural Tier: %s");
        provider.add("phoenix_fission.msr.active_liners", "Active Core Liner Blocks: %d");
        provider.add("phoenix_fission.msr.processing_rate", "Total Salt Processing Rate: %s");
        provider.add("phoenix_fission.msr.catalyst_fluorine", "Chemical Catalyst: Liquid Fluorine (3.0x Boost)");
        provider.add("phoenix_fission.msr.xenon_clean", "Xenon Status: CLEAN CORE (1.5x Power Bonus!)");
        provider.add("phoenix_fission.msr.xenon_poisoning", "Xenon Poisoning Interference: %s");

        provider.add("phoenix_fission.msr_liner.tier", "Structural Tier: MK%d");
        provider.add("phoenix_fission.msr_liner.flow_rate", "Flow Efficiency: %d mb/t per block");
        provider.add("phoenix_fission.msr_liner.thermal_dissipation", "Thermal Dissipation: %s Heat/mb");
        provider.add("phoenix_fission.msr_liner.input_salt", "> Accepts Fuel: §f%s");
        provider.add("phoenix_fission.msr_liner.output_salt", "< Yields Waste: §7%s");

        provider.add("phoenix_fission.msr_liner.info_header", "--- Molten Salt Core Liner Specifications ---");
        provider.add("phoenix_fission.msr.structural_type", "MSR Core Material: %s");
        provider.add("phoenix_fission.conductivity", "Thermal Conductivity: %s");
    }
}
