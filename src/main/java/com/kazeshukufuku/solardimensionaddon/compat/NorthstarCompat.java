package com.kazeshukufuku.solardimensionaddon.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;

public final class NorthstarCompat {
    private static final String MOD_ID = "northstar";
    private static final String LEVEL_ACCESSOR_CLASS = "com.lightning.northstar.accessor.NorthstarLevel";

    private static volatile boolean modernLookupAttempted;
    private static volatile Method dimensionMethod;
    private static volatile Method dimensionSunMethod;
    private static volatile Method levelFunctionGetMethod;

    private NorthstarCompat() {
    }

    public static double getSunFactor(Level level, BlockPos pos) {
        if (level == null || !ModList.get().isLoaded(MOD_ID)) {
            return 1.0D;
        }

        if (!ensureModernMethods()) {
            return 1.0D;
        }

        try {
            Object dimension = dimensionMethod.invoke(level);
            if (dimension == null) {
                return 1.0D;
            }

            Object sunFunction = dimensionSunMethod.invoke(dimension);
            if (sunFunction == null) {
                return 1.0D;
            }

            Object value = levelFunctionGetMethod.invoke(sunFunction, level, pos == null ? BlockPos.ZERO : pos);
            if (!(value instanceof Number number)) {
                return 1.0D;
            }
            return sanitizeMultiplier(number.floatValue());
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            return 1.0D;
        }
    }

    private static boolean ensureModernMethods() {
        if (modernLookupAttempted) {
            return dimensionMethod != null && dimensionSunMethod != null && levelFunctionGetMethod != null;
        }

        modernLookupAttempted = true;
        try {
            Class<?> levelAccessorClass = Class.forName(LEVEL_ACCESSOR_CLASS);
            Class<?> planetDimensionClass = Class.forName("com.lightning.northstar.planet.data.PlanetDimension");
            Class<?> levelFunctionClass = Class.forName("com.lightning.northstar.planet.data.func.LevelFunction");

            dimensionMethod = levelAccessorClass.getMethod("northstar$dimension");
            dimensionSunMethod = planetDimensionClass.getMethod("sun");
            levelFunctionGetMethod = levelFunctionClass.getMethod("get", Level.class, BlockPos.class);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            dimensionMethod = null;
            dimensionSunMethod = null;
            levelFunctionGetMethod = null;
        }
        return dimensionMethod != null && dimensionSunMethod != null && levelFunctionGetMethod != null;
    }

    private static double sanitizeMultiplier(float multiplier) {
        if (!Float.isFinite(multiplier) || multiplier < 0.0F) {
            return 1.0D;
        }
        return multiplier;
    }
}
