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
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, JustReactors.MOD_ID);

    public static final RegistryObject<MenuType<ReactorControllerMenu>> REACTOR_CONTROLLER =
            MENUS.register("reactor_controller", () -> IForgeMenuType.create(ReactorControllerMenu::new));

    public static final RegistryObject<MenuType<ReactorControlRodMenu>> REACTOR_CONTROL_ROD =
            MENUS.register("reactor_control_rod", () -> IForgeMenuType.create(ReactorControlRodMenu::new));

    public static final RegistryObject<MenuType<ReactorAccessPortMenu>> REACTOR_ACCESS_PORT =
            MENUS.register("reactor_access_port", () -> IForgeMenuType.create(ReactorAccessPortMenu::new));

    public static final RegistryObject<MenuType<TurbineControllerMenu>> TURBINE_CONTROLLER =
            MENUS.register("turbine_controller", () -> IForgeMenuType.create(TurbineControllerMenu::new));

    public static final RegistryObject<MenuType<CyaniteReprocessorMenu>> CYANITE_REPROCESSOR =
            MENUS.register("cyanite_reprocessor", () -> IForgeMenuType.create(CyaniteReprocessorMenu::new));

    public static final RegistryObject<MenuType<ReactorRedstonePortMenu>> REACTOR_REDSTONE_PORT =
            MENUS.register("reactor_redstone_port", () -> IForgeMenuType.create(ReactorRedstonePortMenu::new));

    public static final RegistryObject<MenuType<TurbineRedstonePortMenu>> TURBINE_REDSTONE_PORT =
            MENUS.register("turbine_redstone_port", () -> IForgeMenuType.create(TurbineRedstonePortMenu::new));

    public static final RegistryObject<MenuType<HeatExchangerMenu>> HEAT_EXCHANGER =
            MENUS.register("heat_exchanger", () -> IForgeMenuType.create(HeatExchangerMenu::new));

    public static final RegistryObject<MenuType<BatteryMenu>> BATTERY =
            MENUS.register("battery", () -> IForgeMenuType.create(BatteryMenu::new));

    public static final RegistryObject<MenuType<BatterySensorMenu>> BATTERY_SENSOR =
            MENUS.register("battery_sensor", () -> IForgeMenuType.create(BatterySensorMenu::new));

    private ModMenus() {
    }
}
