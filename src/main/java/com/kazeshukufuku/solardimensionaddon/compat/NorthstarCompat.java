package com.kazeshukufuku.solardimensionaddon.compat;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;

public final class NorthstarCompat {
    private static final String MOD_ID = "northstar";
    private static final String PLANETS_CLASS = "com.lightning.northstar.world.dimension.NorthstarPlanets";

    private static volatile boolean methodLookupAttempted;
    private static volatile Method sunMultiplierMethod;

    private NorthstarCompat() {
    }

    public static double getSunMultiplier(Level level) {
        if (level == null || !ModList.get().isLoaded(MOD_ID)) {
            return 1.0D;
        }

        Method method = getSunMultiplierMethod();
        if (method == null) {
            return 1.0D;
        }

        float multiplier;
        try {
            Object value = method.invoke(null, level.dimension());
            if (!(value instanceof Number number)) {
                return 1.0D;
            }
            multiplier = number.floatValue();
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            return 1.0D;
        }

        if (!Float.isFinite(multiplier) || multiplier < 0.0F) {
            return 1.0D;
        }
        return multiplier;
    }

    private static Method getSunMultiplierMethod() {
        if (methodLookupAttempted) {
            return sunMultiplierMethod;
        }

        methodLookupAttempted = true;
        try {
            Class<?> planetsClass = Class.forName(PLANETS_CLASS);
            sunMultiplierMethod = planetsClass.getMethod("getSunMultiplier", ResourceKey.class);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            sunMultiplierMethod = null;
        }
        return sunMultiplierMethod;
    }
}
