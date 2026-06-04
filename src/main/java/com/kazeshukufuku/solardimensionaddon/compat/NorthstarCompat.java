package com.kazeshukufuku.solardimensionaddon.compat;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;
import java.util.Optional;

public final class NorthstarCompat {
    private static final String MOD_ID = "northstar";
    private static final String PLANET_REGISTRY_CLASS = "com.lightning.northstar.api.planet.PlanetRegistry";
    private static final String LEGACY_PLANETS_CLASS = "com.lightning.northstar.world.dimension.NorthstarPlanets";

    private static volatile boolean registryLookupAttempted;
    private static volatile Method byDimensionMethod;
    private static volatile Method definitionSunMultiplierMethod;
    private static volatile Class<?> definitionClass;

    private static volatile boolean legacyLookupAttempted;
    private static volatile Method legacySunMultiplierMethod;

    private NorthstarCompat() {
    }

    public static double getSunMultiplier(Level level) {
        if (level == null || !ModList.get().isLoaded(MOD_ID)) {
            return 1.0D;
        }

        Double registryMultiplier = getRegistrySunMultiplier(level);
        if (registryMultiplier != null) {
            return registryMultiplier;
        }

        Double legacyMultiplier = getLegacySunMultiplier(level);
        return legacyMultiplier != null ? legacyMultiplier : 1.0D;
    }

    private static Double getRegistrySunMultiplier(Level level) {
        Method byDimension = getByDimensionMethod();
        if (byDimension == null) {
            return null;
        }

        try {
            Object value = byDimension.invoke(null, level.dimension());
            if (!(value instanceof Optional<?> optional) || optional.isEmpty()) {
                return 1.0D;
            }

            Object definition = optional.get();
            Method sunMultiplier = getDefinitionSunMultiplierMethod(definition.getClass());
            if (sunMultiplier == null) {
                return null;
            }
            return sanitizeMultiplier(sunMultiplier.invoke(definition));
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            return null;
        }
    }

    private static Method getByDimensionMethod() {
        if (registryLookupAttempted) {
            return byDimensionMethod;
        }

        registryLookupAttempted = true;
        try {
            Class<?> registryClass = Class.forName(PLANET_REGISTRY_CLASS);
            byDimensionMethod = registryClass.getMethod("byDimension", ResourceKey.class);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            byDimensionMethod = null;
        }
        return byDimensionMethod;
    }

    private static Method getDefinitionSunMultiplierMethod(Class<?> currentDefinitionClass) {
        if (definitionSunMultiplierMethod != null && definitionClass == currentDefinitionClass) {
            return definitionSunMultiplierMethod;
        }

        try {
            Method method = currentDefinitionClass.getMethod("sunMultiplier");
            definitionClass = currentDefinitionClass;
            definitionSunMultiplierMethod = method;
            return method;
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            definitionClass = currentDefinitionClass;
            definitionSunMultiplierMethod = null;
            return null;
        }
    }

    private static Double getLegacySunMultiplier(Level level) {
        Method method = getLegacySunMultiplierMethod();
        if (method == null) {
            return null;
        }

        try {
            return sanitizeMultiplier(method.invoke(null, level.dimension()));
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            return null;
        }
    }

    private static Method getLegacySunMultiplierMethod() {
        if (legacyLookupAttempted) {
            return legacySunMultiplierMethod;
        }

        legacyLookupAttempted = true;
        try {
            Class<?> planetsClass = Class.forName(LEGACY_PLANETS_CLASS);
            legacySunMultiplierMethod = planetsClass.getMethod("getSunMultiplier", ResourceKey.class);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            legacySunMultiplierMethod = null;
        }
        return legacySunMultiplierMethod;
    }

    private static Double sanitizeMultiplier(Object value) {
        if (!(value instanceof Number number)) {
            return null;
        }

        double multiplier = number.doubleValue();
        if (!Double.isFinite(multiplier) || multiplier < 0.0D) {
            return null;
        }
        return multiplier;
    }
}
