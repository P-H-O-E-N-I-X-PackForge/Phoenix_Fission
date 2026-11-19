package net.phoenix.phoenix_fission.gtm8.api.machine;

import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;

public class PhoenixPartAbility extends PartAbility {

    private PhoenixPartAbility() {
        super("");
    }

    public static final PartAbility FISSION_SCRAM = new PartAbility("fission_scram");
    public static final PartAbility FISSION_SENSOR = new PartAbility("fission_sensor");
}
