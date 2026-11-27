package com.justreactors.registry;

import com.justreactors.JustReactors;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, JustReactors.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> REACTOR_HUM =
            SOUND_EVENTS.register("reactor_hum", () -> SoundEvent.createVariableRangeEvent(JustReactors.id("reactor_hum")));

    public static final DeferredHolder<SoundEvent, SoundEvent> TURBINE_SPIN =
            SOUND_EVENTS.register("turbine_spin", () -> SoundEvent.createVariableRangeEvent(JustReactors.id("turbine_spin")));

    private ModSounds() {
    }
}
