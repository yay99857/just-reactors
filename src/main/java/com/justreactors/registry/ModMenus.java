package com.justreactors.registry;

import com.justreactors.JustReactors;
import com.justreactors.menu.CyaniteReprocessorMenu;
import com.justreactors.menu.ReactorAccessPortMenu;
import com.justreactors.menu.ReactorControlRodMenu;
import com.justreactors.menu.ReactorControllerMenu;
import com.justreactors.menu.ReactorRedstonePortMenu;
import com.justreactors.turbine.menu.TurbineControllerMenu;
import com.justreactors.turbine.menu.TurbineRedstonePortMenu;

import com.justreactors.battery.BatteryMenu;
import com.justreactors.battery.BatterySensorMenu;
import com.justreactors.exchanger.HeatExchangerMenu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, JustReactors.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ReactorControllerMenu>> REACTOR_CONTROLLER =
            MENUS.register("reactor_controller", () -> IMenuTypeExtension.create(ReactorControllerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ReactorControlRodMenu>> REACTOR_CONTROL_ROD =
            MENUS.register("reactor_control_rod", () -> IMenuTypeExtension.create(ReactorControlRodMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ReactorAccessPortMenu>> REACTOR_ACCESS_PORT =
            MENUS.register("reactor_access_port", () -> IMenuTypeExtension.create(ReactorAccessPortMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<TurbineControllerMenu>> TURBINE_CONTROLLER =
            MENUS.register("turbine_controller", () -> IMenuTypeExtension.create(TurbineControllerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<CyaniteReprocessorMenu>> CYANITE_REPROCESSOR =
            MENUS.register("cyanite_reprocessor", () -> IMenuTypeExtension.create(CyaniteReprocessorMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<ReactorRedstonePortMenu>> REACTOR_REDSTONE_PORT =
            MENUS.register("reactor_redstone_port", () -> IMenuTypeExtension.create(ReactorRedstonePortMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<TurbineRedstonePortMenu>> TURBINE_REDSTONE_PORT =
            MENUS.register("turbine_redstone_port", () -> IMenuTypeExtension.create(TurbineRedstonePortMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<HeatExchangerMenu>> HEAT_EXCHANGER =
            MENUS.register("heat_exchanger", () -> IMenuTypeExtension.create(HeatExchangerMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<BatteryMenu>> BATTERY =
            MENUS.register("battery", () -> IMenuTypeExtension.create(BatteryMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<BatterySensorMenu>> BATTERY_SENSOR =
            MENUS.register("battery_sensor", () -> IMenuTypeExtension.create(BatterySensorMenu::new));

    private ModMenus() {
    }
}
