package com.kazeshukufuku.solardimensionaddon;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SolarDimensionConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DIMENSION_EFFICIENCIES_TRANSLATION = "config.solardimensionaddon.dimension_efficiencies";
    private static final String CONVERT_OUTPUT_UNIT_TRANSLATION = "config.solardimensionaddon.convert_solar_output_unit";
    private static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> DIMENSION_EFFICIENCIES;
    private static final ForgeConfigSpec.BooleanValue CONVERT_SOLAR_OUTPUT_UNIT;

    private static volatile List<String> cachedEntries = List.of();
    private static volatile ParsedRules cachedRules = new ParsedRules(1.0D, Map.of());

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        DIMENSION_EFFICIENCIES = builder
                .translation(DIMENSION_EFFICIENCIES_TRANSLATION)
                .defineList(
                        "dimension_efficiencies",
                        List.of(
                                "default=1.0",
                                "minecraft:overworld=1.0",
                                "minecraft:the_nether=0.0",
                                "minecraft:the_end=0.0"
                        ),
                        entry -> entry instanceof String
                );
        CONVERT_SOLAR_OUTPUT_UNIT = builder
                .translation(CONVERT_OUTPUT_UNIT_TRANSLATION)
                .define("convert_solar_output_unit", true);

        SPEC = builder.build();
    }

    private SolarDimensionConfig() {
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC);
    }

    public static double getMultiplier(Level level) {
        if (level == null) {
            return 1.0D;
        }

        ParsedRules rules = getRules();
        ResourceLocation dimensionId = level.dimension().location();
        return rules.dimensionMultipliers().getOrDefault(dimensionId, rules.defaultMultiplier());
    }

    public static boolean shouldConvertSolarOutputUnit() {
        return CONVERT_SOLAR_OUTPUT_UNIT.get();
    }

    private static ParsedRules getRules() {
        List<String> entries = copyEntries(DIMENSION_EFFICIENCIES.get());
        if (!entries.equals(cachedEntries)) {
            cachedRules = parse(entries);
            cachedEntries = entries;
        }
        return cachedRules;
    }

    private static List<String> copyEntries(List<? extends String> entries) {
        List<String> copy = new ArrayList<>(entries.size());
        for (String entry : entries) {
            copy.add(entry);
        }
        return List.copyOf(copy);
    }

    private static ParsedRules parse(List<String> entries) {
        double defaultMultiplier = 1.0D;
        Map<ResourceLocation, Double> dimensionMultipliers = new HashMap<>();

        for (String rawEntry : entries) {
            String entry = rawEntry.trim();
            if (entry.isEmpty()) {
                continue;
            }

            int separator = entry.indexOf('=');
            if (separator <= 0 || separator == entry.length() - 1) {
                LOGGER.warn("Ignoring invalid solar dimension efficiency entry '{}'. Expected '<dimension_id>=<multiplier>'.", rawEntry);
                continue;
            }

            String key = entry.substring(0, separator).trim();
            String value = entry.substring(separator + 1).trim();
            Double multiplier = parseMultiplier(rawEntry, value);
            if (multiplier == null) {
                continue;
            }

            if ("default".equals(key)) {
                defaultMultiplier = multiplier;
                continue;
            }

            ResourceLocation dimensionId = ResourceLocation.tryParse(key);
            if (dimensionId == null) {
                LOGGER.warn("Ignoring invalid dimension id '{}' in solar dimension efficiency entry '{}'.", key, rawEntry);
                continue;
            }

            dimensionMultipliers.put(dimensionId, multiplier);
        }

        return new ParsedRules(defaultMultiplier, Map.copyOf(dimensionMultipliers));
    }

    private static Double parseMultiplier(String rawEntry, String value) {
        try {
            double multiplier = Double.parseDouble(value);
            if (!Double.isFinite(multiplier) || multiplier < 0.0D) {
                LOGGER.warn("Ignoring invalid solar dimension efficiency multiplier in entry '{}'. Use a finite value >= 0.", rawEntry);
                return null;
            }
            return multiplier;
        } catch (NumberFormatException exception) {
            LOGGER.warn("Ignoring invalid solar dimension efficiency multiplier in entry '{}'.", rawEntry);
            return null;
        }
    }

    private record ParsedRules(double defaultMultiplier, Map<ResourceLocation, Double> dimensionMultipliers) {
    }
}
