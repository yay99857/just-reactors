package com.justreactors.client;

import com.justreactors.blockentity.ReactorControllerBlockEntity;
import com.justreactors.registry.ModSounds;
import com.justreactors.turbine.blockentity.TurbineControllerBlockEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages continuous seamless looping ambient audio for active Reactors and spinning Steam Turbines.
 * Avoids restart gaps caused by pitch-shortened clips and periodic playLocalSound re-triggering.
 */
public final class ClientMachineSounds {
    private static final Map<BlockPos, TurbineLoopSound> TURBINE_SOUNDS = new HashMap<>();
    private static final Map<BlockPos, ReactorLoopSound> REACTOR_SOUNDS = new HashMap<>();

    private ClientMachineSounds() {
    }

    public static void tickTurbineSound(TurbineControllerBlockEntity be) {
        if (!be.isClientAssembled() || !be.isClientSpinning() || be.getClientRpm() <= 1.0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        SoundManager manager = mc.getSoundManager();
        BlockPos key = be.getBlockPos();
        TurbineLoopSound existing = TURBINE_SOUNDS.get(key);
        if (existing == null || existing.isStopped() || !manager.isActive(existing)) {
            TurbineLoopSound loop = new TurbineLoopSound(be, key);
            TURBINE_SOUNDS.put(key, loop);
            manager.play(loop);
        }
    }

    public static void tickReactorSound(ReactorControllerBlockEntity be) {
        if (!be.isClientAssembled() || !be.isClientActive()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        SoundManager manager = mc.getSoundManager();
        BlockPos key = be.getBlockPos();
        ReactorLoopSound existing = REACTOR_SOUNDS.get(key);
        if (existing == null || existing.isStopped() || !manager.isActive(existing)) {
            ReactorLoopSound loop = new ReactorLoopSound(be, key);
            REACTOR_SOUNDS.put(key, loop);
            manager.play(loop);
        }
    }

    private static final class TurbineLoopSound extends AbstractTickableSoundInstance {
        private final TurbineControllerBlockEntity turbine;
        private final BlockPos key;

        TurbineLoopSound(TurbineControllerBlockEntity turbine, BlockPos key) {
            super(ModSounds.TURBINE_SPIN.get(), SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
            this.turbine = turbine;
            this.key = key;
            this.looping = true;
            this.delay = 0;
            this.attenuation = Attenuation.LINEAR;
            this.volume = 0.05F;
            this.pitch = targetPitch(turbine.getClientRpm());
            updateCenter();
        }

        private void updateCenter() {
            BlockPos min = turbine.getClientMin();
            BlockPos max = turbine.getClientMax();
            if (min != null && max != null) {
                this.x = (min.getX() + max.getX() + 1) * 0.5;
                this.y = (min.getY() + max.getY() + 1) * 0.5;
                this.z = (min.getZ() + max.getZ() + 1) * 0.5;
            } else {
                this.x = key.getX() + 0.5;
                this.y = key.getY() + 0.5;
                this.z = key.getZ() + 0.5;
            }
        }

        private static float targetPitch(double rpm) {
            return 0.55F + (float) Math.min(1.05, (rpm / 1800.0) * 0.90);
        }

        @Override
        public void tick() {
            if (turbine.isRemoved() || turbine.getLevel() == null
                    || !turbine.isClientAssembled() || !turbine.isClientSpinning() || turbine.getClientRpm() <= 1.0) {
                this.volume -= 0.02F;
                if (this.volume <= 0.01F) {
                    this.stop();
                    TURBINE_SOUNDS.remove(key, this);
                }
                return;
            }

            updateCenter();
            double rpm = turbine.getClientRpm();
            float desiredPitch = targetPitch(rpm);
            float desiredVolume = 0.12F + (float) Math.min(0.16, (rpm / 1800.0) * 0.16);
            this.pitch += (desiredPitch - this.pitch) * 0.15F;
            this.volume += (desiredVolume - this.volume) * 0.15F;
        }
    }

    private static final class ReactorLoopSound extends AbstractTickableSoundInstance {
        private final ReactorControllerBlockEntity reactor;
        private final BlockPos key;

        ReactorLoopSound(ReactorControllerBlockEntity reactor, BlockPos key) {
            super(ModSounds.REACTOR_HUM.get(), SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
            this.reactor = reactor;
            this.key = key;
            this.looping = true;
            this.delay = 0;
            this.attenuation = Attenuation.LINEAR;
            this.volume = 0.04F;
            this.pitch = 1.0F;
            updateCenter();
        }

        private void updateCenter() {
            BlockPos min = reactor.getClientMin();
            BlockPos max = reactor.getClientMax();
            if (min != null && max != null) {
                this.x = (min.getX() + max.getX() + 1) * 0.5;
                this.y = (min.getY() + max.getY() + 1) * 0.5;
                this.z = (min.getZ() + max.getZ() + 1) * 0.5;
            } else {
                this.x = key.getX() + 0.5;
                this.y = key.getY() + 0.5;
                this.z = key.getZ() + 0.5;
            }
        }

        @Override
        public void tick() {
            if (reactor.isRemoved() || reactor.getLevel() == null
                    || !reactor.isClientAssembled() || !reactor.isClientActive()) {
                this.volume -= 0.02F;
                if (this.volume <= 0.01F) {
                    this.stop();
                    REACTOR_SOUNDS.remove(key, this);
                }
                return;
            }

            updateCenter();
            this.volume += (0.18F - this.volume) * 0.15F;
        }
    }
}
