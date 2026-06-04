package com.kazeshukufuku.solardimensionaddon;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(SolarDimensionAddon.MOD_ID)
public final class SolarDimensionAddon {
    public static final String MOD_ID = "solardimensionaddon";

    public SolarDimensionAddon(ModContainer modContainer) {
        SolarDimensionConfig.register(modContainer);
    }
}
