package com.kazeshukufuku.solardimensionaddon.mixin;

import com.kazeshukufuku.solardimensionaddon.SolarDimensionConfig;
import com.hachirouwu.createsolar.CreateSolarConfig;
import com.hachirouwu.createsolar.SolarPanelBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Locale;

@Mixin(value = SolarPanelBlockEntity.class, remap = false)
public abstract class SolarPanelBlockEntityMixin {
    @Unique
    private int solardimensionaddon$tooltipStartIndex;

    @Shadow(remap = false)
    public abstract int getCurrentOutput();

    @Inject(method = "calculateOutput", at = @At("RETURN"), cancellable = true, remap = false)
    private void solardimensionaddon$applyDimensionEfficiency(CallbackInfoReturnable<Integer> callback) {
        int output = callback.getReturnValue();
        if (output <= 0) {
            return;
        }

        BlockEntity blockEntity = (BlockEntity) (Object) this;
        Level level = blockEntity.getLevel();
        double multiplier = SolarDimensionConfig.getMultiplier(level, blockEntity.getBlockPos());
        if (multiplier == 1.0D) {
            return;
        }

        int adjustedOutput = Math.max(0, (int) Math.floor(output * multiplier));
        callback.setReturnValue(adjustedOutput);
    }

    @Inject(method = "addToGoggleTooltip", at = @At("HEAD"), remap = false)
    private void solardimensionaddon$captureTooltipStart(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> callback) {
        solardimensionaddon$tooltipStartIndex = tooltip.size();
    }

    @Inject(method = "addToGoggleTooltip", at = @At("RETURN"), remap = false)
    private void solardimensionaddon$convertSolarOutputUnit(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> callback) {
        if (!callback.getReturnValue() || !SolarDimensionConfig.shouldConvertSolarOutputUnit()) {
            return;
        }

        int outputLineIndex = solardimensionaddon$tooltipStartIndex + 4;
        if (tooltip.size() <= outputLineIndex) {
            return;
        }

        int updateInterval = Math.max(1, CreateSolarConfig.UPDATE_INTERVAL.get());
        double fePerTick = (double) getCurrentOutput() / updateInterval;
        String formattedOutput = String.format(Locale.ROOT, "%.2f", fePerTick);

        tooltip.set(outputLineIndex, Component.literal("  ")
                .append(Component.literal(formattedOutput).withStyle(ChatFormatting.AQUA))
                .append(Component.translatable("tooltip.solardimensionaddon.fe_per_tick").withStyle(ChatFormatting.AQUA))
                .append(Component.translatable("tooltip.createsolar.at_current_sun_strength").withStyle(ChatFormatting.DARK_GRAY)));
    }
}
