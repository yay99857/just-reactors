package com.justreactors.data;

import com.justreactors.JustReactors;
import com.justreactors.reactor.ReactorModerator;
import com.justreactors.turbine.TurbineCoil;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Loads datapack-configurable Reactor Moderators ({@code data/<ns>/justreactors/moderators/*.json})
 * and Steam Turbine Coils ({@code data/<ns>/justreactors/turbine_coils/*.json}).
 */
public final class ReactorDataReloadListeners {
    private ReactorDataReloadListeners() {
    }

    public static void register(IEventBus forgeBus) {
        forgeBus.addListener(ReactorDataReloadListeners::onAddReloadListeners);
    }

    private static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(JustReactors.id("moderators"), new ModeratorReloadListener());
        event.addListener(JustReactors.id("turbine_coils"), new TurbineCoilReloadListener());
    }

    public record ModeratorDef(
            Optional<Identifier> block,
            Optional<Identifier> tag,
            double absorption,
            double heatEfficiency,
            double moderation,
            double heatConductivity
    ) {
        public static final Codec<ModeratorDef> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.optionalFieldOf("block").forGetter(ModeratorDef::block),
                Identifier.CODEC.optionalFieldOf("tag").forGetter(ModeratorDef::tag),
                Codec.DOUBLE.fieldOf("absorption").forGetter(ModeratorDef::absorption),
                Codec.DOUBLE.fieldOf("heat_efficiency").forGetter(ModeratorDef::heatEfficiency),
                Codec.DOUBLE.fieldOf("moderation").forGetter(ModeratorDef::moderation),
                Codec.DOUBLE.fieldOf("heat_conductivity").forGetter(ModeratorDef::heatConductivity)
        ).apply(instance, ModeratorDef::new));
    }

    public record CoilDef(
            Optional<Identifier> block,
            Optional<Identifier> tag,
            double efficiency,
            double bonus,
            double drag
    ) {
        public static final Codec<CoilDef> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.optionalFieldOf("block").forGetter(CoilDef::block),
                Identifier.CODEC.optionalFieldOf("tag").forGetter(CoilDef::tag),
                Codec.DOUBLE.fieldOf("efficiency").forGetter(CoilDef::efficiency),
                Codec.DOUBLE.fieldOf("bonus").forGetter(CoilDef::bonus),
                Codec.DOUBLE.fieldOf("drag").forGetter(CoilDef::drag)
        ).apply(instance, CoilDef::new));
    }

    private static final class ModeratorReloadListener extends SimpleJsonResourceReloadListener<ModeratorDef> {
        ModeratorReloadListener() {
            super(ModeratorDef.CODEC, FileToIdConverter.json("justreactors/moderators"));
        }

        @Override
        protected void apply(Map<Identifier, ModeratorDef> entries, ResourceManager resourceManager, ProfilerFiller profiler) {
            Map<Block, ReactorModerator> blocks = new HashMap<>();
            List<ReactorModerator.TagEntry> tags = new ArrayList<>();
            int loaded = 0;

            for (Map.Entry<Identifier, ModeratorDef> e : entries.entrySet()) {
                ModeratorDef def = e.getValue();
                ReactorModerator mod = new ReactorModerator(
                        def.absorption(), def.heatEfficiency(), def.moderation(), def.heatConductivity());
                boolean matched = false;
                if (def.block().isPresent() && BuiltInRegistries.BLOCK.containsKey(def.block().get())) {
                    blocks.put(BuiltInRegistries.BLOCK.getValue(def.block().get()), mod);
                    matched = true;
                }
                if (def.tag().isPresent()) {
                    tags.add(new ReactorModerator.TagEntry(TagKey.create(Registries.BLOCK, def.tag().get()), mod));
                    matched = true;
                }
                if (matched) loaded++;
            }

            ReactorModerator.setDatapackEntries(blocks, tags, loaded);
            JustReactors.LOGGER.info("Loaded {} reactor moderator definition(s) from datapacks", loaded);
        }
    }

    private static final class TurbineCoilReloadListener extends SimpleJsonResourceReloadListener<CoilDef> {
        TurbineCoilReloadListener() {
            super(CoilDef.CODEC, FileToIdConverter.json("justreactors/turbine_coils"));
        }

        @Override
        protected void apply(Map<Identifier, CoilDef> entries, ResourceManager resourceManager, ProfilerFiller profiler) {
            Map<Block, TurbineCoil> blocks = new HashMap<>();
            List<TurbineCoil.TagEntry> tags = new ArrayList<>();
            int loaded = 0;

            for (Map.Entry<Identifier, CoilDef> e : entries.entrySet()) {
                CoilDef def = e.getValue();
                TurbineCoil coil = new TurbineCoil(def.efficiency(), def.bonus(), def.drag());
                boolean matched = false;
                if (def.block().isPresent() && BuiltInRegistries.BLOCK.containsKey(def.block().get())) {
                    blocks.put(BuiltInRegistries.BLOCK.getValue(def.block().get()), coil);
                    matched = true;
                }
                if (def.tag().isPresent()) {
                    tags.add(new TurbineCoil.TagEntry(TagKey.create(Registries.BLOCK, def.tag().get()), coil));
                    matched = true;
                }
                if (matched) loaded++;
            }

            TurbineCoil.setDatapackEntries(blocks, tags, loaded);
            JustReactors.LOGGER.info("Loaded {} turbine coil definition(s) from datapacks", loaded);
        }
    }
}
