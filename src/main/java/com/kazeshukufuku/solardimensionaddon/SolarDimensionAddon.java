package com.kazeshukufuku.solardimensionaddon;

import net.minecraftforge.fml.common.Mod;

@Mod(SolarDimensionAddon.MOD_ID)
public final class SolarDimensionAddon {
    public static final String MOD_ID = "solardimensionaddon";

    public SolarDimensionAddon() {
        SolarDimensionConfig.register();
    }
}
