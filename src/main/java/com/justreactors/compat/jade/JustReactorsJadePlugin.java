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

import com.justreactors.battery.BatteryControllerBlockEntity;
import com.justreactors.battery.BatteryPowerPortBlock;
import com.justreactors.battery.BatteryRedstoneSensorBlock;
import com.justreactors.battery.BatteryRedstoneSensorBlockEntity;
import com.justreactors.battery.BatterySensorMode;
import com.justreactors.exchanger.HeatExchangerControllerBlockEntity;
import com.justreactors.exchanger.HeatExchangerFluidPortBlock;
import com.justreactors.multiblock.BoxControllerBlock;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
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
    static final Identifier REACTOR_INFO = JustReactors.id("reactor_info");

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
        registration.registerBlockDataProvider(ServerData.INSTANCE, HeatExchangerControllerBlockEntity.class);
        registration.registerBlockDataProvider(ServerData.INSTANCE, BatteryControllerBlockEntity.class);
        registration.registerBlockDataProvider(ServerData.INSTANCE, BatteryRedstoneSensorBlockEntity.class);
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
        registration.registerBlockComponent(ClientInfo.INSTANCE, BoxControllerBlock.class);
        registration.registerBlockComponent(ClientInfo.INSTANCE, HeatExchangerFluidPortBlock.class);
        registration.registerBlockComponent(ClientInfo.INSTANCE, BatteryPowerPortBlock.class);
        registration.registerBlockComponent(ClientInfo.INSTANCE, BatteryRedstoneSensorBlock.class);
    }

    enum ServerData implements IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public Identifier getUid() {
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
                case HeatExchangerControllerBlockEntity ex -> {
                    data.putString("Kind", "Exchanger");
                    data.putBoolean("Assembled", ex.isAssembled());
                    if (ex.isAssembled()) {
                        data.putBoolean("Active", ex.isActive());
                        data.putInt("Cooled", ex.getLastCooledMb());
                        data.putInt("MaxCooling", ex.getMaxCoolingMb());
                        data.putInt("Steam", ex.getLastSteamMb());
                    } else if (ex.getLastValidationError() != null) {
                        ComponentSerialization.CODEC.encodeStart(NbtOps.INSTANCE, ex.getLastValidationError())
                                .result().ifPresent(tag -> data.put("Error", tag));
                    }
                }
                case BatteryControllerBlockEntity bat -> {
                    data.putString("Kind", "Battery");
                    data.putBoolean("Assembled", bat.isAssembled());
                    if (bat.isAssembled()) {
                        data.putInt("Energy", bat.getEnergyStorage().getEnergyStored());
                        data.putInt("Capacity", bat.getEnergyStorage().getMaxEnergyStored());
                        data.putInt("In", bat.getLastInput());
                        data.putInt("Out", bat.getLastOutput());
                    } else if (bat.getLastValidationError() != null) {
                        ComponentSerialization.CODEC.encodeStart(NbtOps.INSTANCE, bat.getLastValidationError())
                                .result().ifPresent(tag -> data.put("Error", tag));
                    }
                }
                case BatteryRedstoneSensorBlockEntity sensor -> {
                    data.putString("Kind", "BatterySensor");
                    data.putInt("Mode", sensor.getMode().ordinal());
                    data.putInt("Threshold", sensor.getThresholdPercent());
                    data.putInt("Signal", sensor.getSignal());
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
        public Identifier getUid() {
            return REACTOR_INFO;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag data = accessor.getServerData();
            String kind = data.getStringOr("Kind", "");
            switch (kind) {
                case "Reactor" -> {
                    if (!data.getBooleanOr("Assembled", false)) {
                        tooltip.add(Component.translatable("jade.justreactors.not_assembled").withStyle(ChatFormatting.RED));
                        addErrorTooltip(tooltip, data);
                        return;
                    }
                    boolean active = data.getBooleanOr("Active", false);
                    tooltip.add(Component.translatable(active ? "gui.justreactors.status.online" : "gui.justreactors.status.offline")
                            .withStyle(active ? ChatFormatting.GREEN : ChatFormatting.RED));
                    if (data.getBooleanOr("ActiveCooled", false)) {
                        tooltip.add(Component.translatable("jade.justreactors.steam_output",
                                String.format(Locale.ROOT, "%,d", data.getIntOr("SteamRate", 0))).withStyle(ChatFormatting.WHITE));
                    } else {
                        tooltip.add(Component.translatable("jade.justreactors.output",
                                String.format(Locale.ROOT, "%,d", data.getIntOr("Rate", 0))).withStyle(ChatFormatting.AQUA));
                    }
                    tooltip.add(Component.translatable("jade.justreactors.fuel",
                            String.format(Locale.ROOT, "%,d", data.getIntOr("Fuel", 0)),
                            String.format(Locale.ROOT, "%,d", data.getIntOr("MaxFuel", 0))).withStyle(ChatFormatting.YELLOW));
                    tooltip.add(Component.translatable("jade.justreactors.temp",
                            data.getIntOr("CoreTemp", 20), data.getIntOr("CasingTemp", 20)).withStyle(ChatFormatting.GOLD));
                }
                case "Rod" -> tooltip.add(Component.translatable("jade.justreactors.insertion", data.getIntOr("Insertion", 0))
                        .withStyle(ChatFormatting.AQUA));
                case "ReactorPort" -> {
                    boolean outlet = data.getBooleanOr("Outlet", false);
                    tooltip.add(Component.translatable(outlet ? "jade.justreactors.port.outlet_steam" : "jade.justreactors.port.inlet_water")
                            .withStyle(outlet ? ChatFormatting.AQUA : ChatFormatting.BLUE));
                }
                case "RedstonePort" -> {
                    RedstonePortMode mode = RedstonePortMode.byOrdinal(data.getIntOr("Mode", 0));
                    tooltip.add(Component.translatable(mode.translationKey())
                            .withStyle(mode.isOutput() ? ChatFormatting.RED : ChatFormatting.AQUA));
                    if (mode.isOutput()) {
                        tooltip.add(Component.translatable("jade.justreactors.redstone.threshold_signal",
                                data.getIntOr("Threshold", 50), data.getIntOr("Signal", 0)).withStyle(ChatFormatting.GOLD));
                    }
                }
                case "Turbine" -> {
                    if (!data.getBooleanOr("Assembled", false)) {
                        tooltip.add(Component.translatable("jade.justreactors.not_assembled").withStyle(ChatFormatting.RED));
                        addErrorTooltip(tooltip, data);
                        return;
                    }
                    boolean active = data.getBooleanOr("Active", false);
                    tooltip.add(Component.translatable(active ? "gui.justreactors.status.online" : "gui.justreactors.status.offline")
                            .withStyle(active ? ChatFormatting.GREEN : ChatFormatting.RED));
                    tooltip.add(Component.translatable("jade.justreactors.output",
                            String.format(Locale.ROOT, "%,d", data.getIntOr("Rate", 0))).withStyle(ChatFormatting.AQUA));
                    tooltip.add(Component.translatable("jade.justreactors.rpm",
                            String.format(Locale.ROOT, "%,d", data.getIntOr("Rpm", 0))).withStyle(ChatFormatting.GOLD));
                    tooltip.add(Component.translatable(
                            TurbineVentMode.byOrdinal(data.getIntOr("VentMode", 0)).translationKey()).withStyle(ChatFormatting.GRAY));
                }
                case "TurbinePort" -> {
                    boolean outlet = data.getBooleanOr("Outlet", false);
                    tooltip.add(Component.translatable(outlet ? "jade.justreactors.port.outlet_water" : "jade.justreactors.port.inlet_steam")
                            .withStyle(outlet ? ChatFormatting.BLUE : ChatFormatting.AQUA));
                }
                case "TurbineRedstonePort" -> {
                    TurbineRedstonePortMode mode = TurbineRedstonePortMode.byOrdinal(data.getIntOr("Mode", 0));
                    tooltip.add(Component.translatable(mode.translationKey())
                            .withStyle(mode.isOutput() ? ChatFormatting.RED : ChatFormatting.AQUA));
                    if (mode.isOutput()) {
                        int threshold = data.getIntOr("Threshold", 50);
                        int signal = data.getIntOr("Signal", 0);
                        if (mode.isRpmThreshold()) {
                            tooltip.add(Component.translatable("jade.justreactors.turbine_redstone.threshold_rpm_signal",
                                    threshold * 30, threshold, signal).withStyle(ChatFormatting.GOLD));
                        } else {
                            tooltip.add(Component.translatable("jade.justreactors.redstone.threshold_signal",
                                    threshold, signal).withStyle(ChatFormatting.GOLD));
                        }
                    }
                }
                case "Exchanger" -> {
                    if (!data.getBooleanOr("Assembled", false)) {
                        tooltip.add(Component.translatable("jade.justreactors.not_assembled").withStyle(ChatFormatting.RED));
                        addErrorTooltip(tooltip, data);
                        return;
                    }
                    boolean active = data.getBooleanOr("Active", false);
                    tooltip.add(Component.translatable(active ? "gui.justreactors.status.online" : "gui.justreactors.status.offline")
                            .withStyle(active ? ChatFormatting.GREEN : ChatFormatting.RED));
                    tooltip.add(Component.translatable("gui.justreactors.heat_exchanger.cooling",
                            String.format(Locale.ROOT, "%,d", data.getIntOr("Cooled", 0)),
                            String.format(Locale.ROOT, "%,d", data.getIntOr("MaxCooling", 0))).withStyle(ChatFormatting.AQUA));
                    tooltip.add(Component.translatable("jade.justreactors.steam_output",
                            String.format(Locale.ROOT, "%,d", data.getIntOr("Steam", 0))).withStyle(ChatFormatting.WHITE));
                }
                case "Battery" -> {
                    if (!data.getBooleanOr("Assembled", false)) {
                        tooltip.add(Component.translatable("jade.justreactors.not_assembled").withStyle(ChatFormatting.RED));
                        addErrorTooltip(tooltip, data);
                        return;
                    }
                    int energy = data.getIntOr("Energy", 0);
                    int cap = Math.max(1, data.getIntOr("Capacity", 1));
                    tooltip.add(Component.translatable("jade.justreactors.battery.charge",
                            String.format(Locale.ROOT, "%,d", energy), String.format(Locale.ROOT, "%,d", cap),
                            String.format(Locale.ROOT, "%.1f", energy * 100.0 / cap)).withStyle(ChatFormatting.GREEN));
                    tooltip.add(Component.translatable("jade.justreactors.battery.flow",
                            String.format(Locale.ROOT, "%,d", data.getIntOr("In", 0)),
                            String.format(Locale.ROOT, "%,d", data.getIntOr("Out", 0))).withStyle(ChatFormatting.GOLD));
                }
                case "BatterySensor" -> {
                    BatterySensorMode mode = BatterySensorMode.byOrdinal(data.getIntOr("Mode", 0));
                    tooltip.add(Component.translatable(mode.translationKey()).withStyle(ChatFormatting.RED));
                    if (mode.usesThreshold()) {
                        tooltip.add(Component.translatable("jade.justreactors.redstone.threshold_signal",
                                data.getIntOr("Threshold", 90), data.getIntOr("Signal", 0)).withStyle(ChatFormatting.GOLD));
                    }
                }
                case "Reprocessor" -> {
                    int max = Math.max(1, data.getIntOr("MaxProgress", 100));
                    int pct = data.getIntOr("Progress", 0) * 100 / max;
                    if (pct > 0) {
                        tooltip.add(Component.translatable("jade.justreactors.progress", pct).withStyle(ChatFormatting.AQUA));
                    }
                }
                default -> appendStateTooltip(tooltip, accessor);
            }
        }

        /** Port modes come straight from the block state, no server data needed. */
        private static void appendStateTooltip(ITooltip tooltip, BlockAccessor accessor) {
            var state = accessor.getBlockState();
            if (state.getBlock() instanceof HeatExchangerFluidPortBlock) {
                tooltip.add(Component.translatable(state.getValue(HeatExchangerFluidPortBlock.MODE).translationKey())
                        .withStyle(ChatFormatting.AQUA));
            } else if (state.getBlock() instanceof BatteryPowerPortBlock) {
                boolean out = state.getValue(BatteryPowerPortBlock.OUTLET);
                tooltip.add(Component.translatable(out ? "message.justreactors.battery_port.output"
                        : "message.justreactors.battery_port.input").withStyle(out ? ChatFormatting.GOLD : ChatFormatting.GREEN));
            }
        }

        private static void addErrorTooltip(ITooltip tooltip, CompoundTag data) {
            if (!data.contains("Error")) return;
            ComponentSerialization.CODEC.parse(NbtOps.INSTANCE, data.get("Error")).result()
                    .ifPresent(err -> tooltip.add(err.copy().withStyle(ChatFormatting.YELLOW)));
        }
    }
}
