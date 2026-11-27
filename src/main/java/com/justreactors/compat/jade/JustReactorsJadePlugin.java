package com.justreactors.compat.jade;

import com.justreactors.JustReactors;
import com.justreactors.block.CyaniteReprocessorBlock;
import com.justreactors.block.ReactorControlRodBlock;
import com.justreactors.block.ReactorControllerBlock;
import com.justreactors.block.ReactorCoolantPortBlock;
import com.justreactors.block.ReactorRedstonePortBlock;
import com.justreactors.blockentity.CyaniteReprocessorBlockEntity;
import com.justreactors.blockentity.ReactorControlRodBlockEntity;
import com.justreactors.blockentity.ReactorControllerBlockEntity;
import com.justreactors.blockentity.ReactorCoolantPortBlockEntity;
import com.justreactors.blockentity.ReactorRedstonePortBlockEntity;
import com.justreactors.reactor.ReactorMode;
import com.justreactors.reactor.RedstonePortMode;
import com.justreactors.turbine.TurbineRedstonePortMode;
import com.justreactors.turbine.TurbineVentMode;
import com.justreactors.turbine.block.TurbineControllerBlock;
import com.justreactors.turbine.block.TurbineFluidPortBlock;
import com.justreactors.turbine.block.TurbineRedstonePortBlock;
import com.justreactors.turbine.blockentity.TurbineControllerBlockEntity;
import com.justreactors.turbine.blockentity.TurbineFluidPortBlockEntity;
import com.justreactors.turbine.blockentity.TurbineRedstonePortBlockEntity;

import net.minecraft.ChatFormatting;
import com.justreactors.compat.nbt.ValueInput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

import java.util.Locale;

@WailaPlugin(JustReactors.MOD_ID)
public class JustReactorsJadePlugin implements IWailaPlugin {
    static final ResourceLocation REACTOR_INFO = JustReactors.id("reactor_info");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(ServerData.INSTANCE, ReactorControllerBlockEntity.class);
        registration.registerBlockDataProvider(ServerData.INSTANCE, ReactorControlRodBlockEntity.class);
        registration.registerBlockDataProvider(ServerData.INSTANCE, ReactorCoolantPortBlockEntity.class);
        registration.registerBlockDataProvider(ServerData.INSTANCE, ReactorRedstonePortBlockEntity.class);
        registration.registerBlockDataProvider(ServerData.INSTANCE, TurbineControllerBlockEntity.class);
        registration.registerBlockDataProvider(ServerData.INSTANCE, TurbineFluidPortBlockEntity.class);
        registration.registerBlockDataProvider(ServerData.INSTANCE, TurbineRedstonePortBlockEntity.class);
        registration.registerBlockDataProvider(ServerData.INSTANCE, CyaniteReprocessorBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(ClientInfo.INSTANCE, ReactorControllerBlock.class);
        registration.registerBlockComponent(ClientInfo.INSTANCE, ReactorControlRodBlock.class);
        registration.registerBlockComponent(ClientInfo.INSTANCE, ReactorCoolantPortBlock.class);
        registration.registerBlockComponent(ClientInfo.INSTANCE, ReactorRedstonePortBlock.class);
        registration.registerBlockComponent(ClientInfo.INSTANCE, TurbineControllerBlock.class);
        registration.registerBlockComponent(ClientInfo.INSTANCE, TurbineFluidPortBlock.class);
        registration.registerBlockComponent(ClientInfo.INSTANCE, TurbineRedstonePortBlock.class);
        registration.registerBlockComponent(ClientInfo.INSTANCE, CyaniteReprocessorBlock.class);
    }

    enum ServerData implements IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public ResourceLocation getUid() {
            return REACTOR_INFO;
        }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            switch (accessor.getBlockEntity()) {
                case ReactorControllerBlockEntity controller -> {
                    data.putString("Kind", "Reactor");
                    data.putBoolean("Assembled", controller.isAssembled());
                    if (controller.isAssembled()) {
                        data.putBoolean("Active", controller.getSimulation().isActive());
                        data.putBoolean("ActiveCooled", controller.getSimulation().getMode() == ReactorMode.ACTIVE);
                        data.putInt("Rate", controller.getSimulation().getLastGeneratedFe());
                        data.putInt("SteamRate", controller.getSimulation().getLastSteamProducedMb());
                        data.putInt("Fuel", (int) Math.round(controller.getSimulation().getFuelMb()));
                        data.putInt("MaxFuel", controller.getTotalFuelCapacityMb());
                        data.putInt("CoreTemp", (int) Math.round(controller.getSimulation().getFuelHeat()));
                        data.putInt("CasingTemp", (int) Math.round(controller.getSimulation().getCasingHeat()));
                    } else if (controller.getLastValidationError() != null) {
                        ComponentSerialization.CODEC.encodeStart(NbtOps.INSTANCE, controller.getLastValidationError())
                                .result().ifPresent(tag -> data.put("Error", tag));
                    }
                }
                case ReactorControlRodBlockEntity rod -> {
                    data.putString("Kind", "Rod");
                    data.putInt("Insertion", rod.getInsertion());
                }
                case ReactorCoolantPortBlockEntity port -> {
                    data.putString("Kind", "ReactorPort");
                    data.putBoolean("Outlet", port.isOutlet());
                }
                case ReactorRedstonePortBlockEntity port -> {
                    data.putString("Kind", "RedstonePort");
                    data.putInt("Mode", port.getMode().ordinal());
                    data.putInt("Threshold", port.getThresholdPercent());
                    data.putInt("Signal", port.getCurrentDisplaySignal());
                }
                case TurbineControllerBlockEntity turbine -> {
                    data.putString("Kind", "Turbine");
                    data.putBoolean("Assembled", turbine.isAssembled());
                    if (turbine.isAssembled()) {
                        data.putBoolean("Active", turbine.getSimulation().isActive());
                        data.putInt("Rate", turbine.getSimulation().getLastGeneratedFe());
                        data.putInt("Rpm", (int) Math.round(turbine.getSimulation().getRpm()));
                        data.putInt("VentMode", turbine.getVentMode().ordinal());
                    } else if (turbine.getLastValidationError() != null) {
                        ComponentSerialization.CODEC.encodeStart(NbtOps.INSTANCE, turbine.getLastValidationError())
                                .result().ifPresent(tag -> data.put("Error", tag));
                    }
                }
                case TurbineFluidPortBlockEntity port -> {
                    data.putString("Kind", "TurbinePort");
                    data.putBoolean("Outlet", port.isOutlet());
                }
                case TurbineRedstonePortBlockEntity port -> {
                    data.putString("Kind", "TurbineRedstonePort");
                    data.putInt("Mode", port.getMode().ordinal());
                    data.putInt("Threshold", port.getThresholdPercent());
                    data.putInt("Signal", port.getCurrentDisplaySignal());
                }
                case CyaniteReprocessorBlockEntity rep -> {
                    data.putString("Kind", "Reprocessor");
                    data.putInt("Progress", rep.getProgress());
                    data.putInt("MaxProgress", rep.getMaxProgress());
                }
                default -> {
                }
            }
        }
    }

    enum ClientInfo implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public ResourceLocation getUid() {
            return REACTOR_INFO;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            ValueInput in = new ValueInput(data, accessor.getLevel().registryAccess());
            String kind = in.getStringOr("Kind", "");
            switch (kind) {
                case "Reactor" -> {
                    if (!in.getBooleanOr("Assembled", false)) {
                        tooltip.add(Component.translatable("jade.justreactors.not_assembled").withStyle(ChatFormatting.RED));
                        addErrorTooltip(tooltip, data);
                        return;
                    }
                    boolean active = in.getBooleanOr("Active", false);
                    tooltip.add(Component.translatable(active ? "gui.justreactors.status.online" : "gui.justreactors.status.offline")
                            .withStyle(active ? ChatFormatting.GREEN : ChatFormatting.RED));
                    if (in.getBooleanOr("ActiveCooled", false)) {
                        tooltip.add(Component.translatable("jade.justreactors.steam_output",
                                String.format(Locale.ROOT, "%,d", in.getIntOr("SteamRate", 0))).withStyle(ChatFormatting.WHITE));
                    } else {
                        tooltip.add(Component.translatable("jade.justreactors.output",
                                String.format(Locale.ROOT, "%,d", in.getIntOr("Rate", 0))).withStyle(ChatFormatting.AQUA));
                    }
                    tooltip.add(Component.translatable("jade.justreactors.fuel",
                            String.format(Locale.ROOT, "%,d", in.getIntOr("Fuel", 0)),
                            String.format(Locale.ROOT, "%,d", in.getIntOr("MaxFuel", 0))).withStyle(ChatFormatting.YELLOW));
                    tooltip.add(Component.translatable("jade.justreactors.temp",
                            in.getIntOr("CoreTemp", 20), in.getIntOr("CasingTemp", 20)).withStyle(ChatFormatting.GOLD));
                }
                case "Rod" -> tooltip.add(Component.translatable("jade.justreactors.insertion", in.getIntOr("Insertion", 0))
                        .withStyle(ChatFormatting.AQUA));
                case "ReactorPort" -> {
                    boolean outlet = in.getBooleanOr("Outlet", false);
                    tooltip.add(Component.translatable(outlet ? "jade.justreactors.port.outlet_steam" : "jade.justreactors.port.inlet_water")
                            .withStyle(outlet ? ChatFormatting.AQUA : ChatFormatting.BLUE));
                }
                case "RedstonePort" -> {
                    RedstonePortMode mode = RedstonePortMode.byOrdinal(in.getIntOr("Mode", 0));
                    tooltip.add(Component.translatable(mode.translationKey())
                            .withStyle(mode.isOutput() ? ChatFormatting.RED : ChatFormatting.AQUA));
                    if (mode.isOutput()) {
                        tooltip.add(Component.translatable("jade.justreactors.redstone.threshold_signal",
                                in.getIntOr("Threshold", 50), in.getIntOr("Signal", 0)).withStyle(ChatFormatting.GOLD));
                    }
                }
                case "Turbine" -> {
                    if (!in.getBooleanOr("Assembled", false)) {
                        tooltip.add(Component.translatable("jade.justreactors.not_assembled").withStyle(ChatFormatting.RED));
                        addErrorTooltip(tooltip, data);
                        return;
                    }
                    boolean active = in.getBooleanOr("Active", false);
                    tooltip.add(Component.translatable(active ? "gui.justreactors.status.online" : "gui.justreactors.status.offline")
                            .withStyle(active ? ChatFormatting.GREEN : ChatFormatting.RED));
                    tooltip.add(Component.translatable("jade.justreactors.output",
                            String.format(Locale.ROOT, "%,d", in.getIntOr("Rate", 0))).withStyle(ChatFormatting.AQUA));
                    tooltip.add(Component.translatable("jade.justreactors.rpm",
                            String.format(Locale.ROOT, "%,d", in.getIntOr("Rpm", 0))).withStyle(ChatFormatting.GOLD));
                    tooltip.add(Component.translatable(
                            TurbineVentMode.byOrdinal(in.getIntOr("VentMode", 0)).translationKey()).withStyle(ChatFormatting.GRAY));
                }
                case "TurbinePort" -> {
                    boolean outlet = in.getBooleanOr("Outlet", false);
                    tooltip.add(Component.translatable(outlet ? "jade.justreactors.port.outlet_water" : "jade.justreactors.port.inlet_steam")
                            .withStyle(outlet ? ChatFormatting.BLUE : ChatFormatting.AQUA));
                }
                case "TurbineRedstonePort" -> {
                    TurbineRedstonePortMode mode = TurbineRedstonePortMode.byOrdinal(in.getIntOr("Mode", 0));
                    tooltip.add(Component.translatable(mode.translationKey())
                            .withStyle(mode.isOutput() ? ChatFormatting.RED : ChatFormatting.AQUA));
                    if (mode.isOutput()) {
                        int threshold = in.getIntOr("Threshold", 50);
                        int signal = in.getIntOr("Signal", 0);
                        if (mode.isRpmThreshold()) {
                            tooltip.add(Component.translatable("jade.justreactors.turbine_redstone.threshold_rpm_signal",
                                    threshold * 30, threshold, signal).withStyle(ChatFormatting.GOLD));
                        } else {
                            tooltip.add(Component.translatable("jade.justreactors.redstone.threshold_signal",
                                    threshold, signal).withStyle(ChatFormatting.GOLD));
                        }
                    }
                }
                case "Reprocessor" -> {
                    int max = Math.max(1, in.getIntOr("MaxProgress", 100));
                    int pct = in.getIntOr("Progress", 0) * 100 / max;
                    if (pct > 0) {
                        tooltip.add(Component.translatable("jade.justreactors.progress", pct).withStyle(ChatFormatting.AQUA));
                    }
                }
                default -> {
                }
            }
        }

        private static void addErrorTooltip(ITooltip tooltip, CompoundTag data) {
            if (!data.contains("Error")) return;
            ComponentSerialization.CODEC.parse(NbtOps.INSTANCE, data.get("Error")).result()
                    .ifPresent(err -> tooltip.add(err.copy().withStyle(ChatFormatting.YELLOW)));
        }
    }
}
