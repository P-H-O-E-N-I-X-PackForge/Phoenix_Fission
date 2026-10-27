package net.phoenix.phoenix_fission;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

@SuppressWarnings("unused")
@GTAddon(PhoenixFission.MOD_ID)
public class PhoenixGTAddon implements IGTAddon {

    @Override
    public GTRegistrate getRegistrate() {
        return PhoenixFission.PHOENIX_REGISTRATE;
    }
}
