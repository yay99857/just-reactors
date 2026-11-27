package com.justreactors.data;

import com.justreactors.JustReactors;
import com.justreactors.reactor.ReactorModerator;
import com.justreactors.turbine.TurbineCoil;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

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

    private static final Gson GSON = new Gson();

    private static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new ModeratorReloadListener());
        event.addListener(new TurbineCoilReloadListener());
    }

    /** 1.21.1's SimpleJsonResourceReloadListener hands over raw JSON; decode it with the same codecs as 26.3. */
    private static <T> Map<ResourceLocation, T> decode(Map<ResourceLocation, JsonElement> raw, Codec<T> codec, String kind) {
        Map<ResourceLocation, T> out = new HashMap<>();
        raw.forEach((id, json) -> codec.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(err -> JustReactors.LOGGER.error("Invalid {} definition {}: {}", kind, id, err))
                .ifPresent(def -> out.put(id, def)));
        return out;
    }

    public record ModeratorDef(
            Optional<ResourceLocation> block,
            Optional<ResourceLocation> tag,
            double absorption,
            double heatEfficiency,
            double moderation,
            double heatConductivity
    ) {
        public static final Codec<ModeratorDef> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.optionalFieldOf("block").forGetter(ModeratorDef::block),
                ResourceLocation.CODEC.optionalFieldOf("tag").forGetter(ModeratorDef::tag),
                Codec.DOUBLE.fieldOf("absorption").forGetter(ModeratorDef::absorption),
                Codec.DOUBLE.fieldOf("heat_efficiency").forGetter(ModeratorDef::heatEfficiency),
                Codec.DOUBLE.fieldOf("moderation").forGetter(ModeratorDef::moderation),
                Codec.DOUBLE.fieldOf("heat_conductivity").forGetter(ModeratorDef::heatConductivity)
        ).apply(instance, ModeratorDef::new));
    }

    public record CoilDef(
            Optional<ResourceLocation> block,
            Optional<ResourceLocation> tag,
            double efficiency,
            double bonus,
            double drag
    ) {
        public static final Codec<CoilDef> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.optionalFieldOf("block").forGetter(CoilDef::block),
                ResourceLocation.CODEC.optionalFieldOf("tag").forGetter(CoilDef::tag),
                Codec.DOUBLE.fieldOf("efficiency").forGetter(CoilDef::efficiency),
                Codec.DOUBLE.fieldOf("bonus").forGetter(CoilDef::bonus),
                Codec.DOUBLE.fieldOf("drag").forGetter(CoilDef::drag)
        ).apply(instance, CoilDef::new));
    }

    private static final class ModeratorReloadListener extends SimpleJsonResourceReloadListener {
        ModeratorReloadListener() {
            super(GSON, "justreactors/moderators");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> raw, ResourceManager resourceManager, ProfilerFiller profiler) {
            Map<ResourceLocation, ModeratorDef> entries = decode(raw, ModeratorDef.CODEC, "moderator");
            Map<Block, ReactorModerator> blocks = new HashMap<>();
            List<ReactorModerator.TagEntry> tags = new ArrayList<>();
            int loaded = 0;

            for (Map.Entry<ResourceLocation, ModeratorDef> e : entries.entrySet()) {
                ModeratorDef def = e.getValue();
                ReactorModerator mod = new ReactorModerator(
                        def.absorption(), def.heatEfficiency(), def.moderation(), def.heatConductivity());
                boolean matched = false;
                if (def.block().isPresent() && BuiltInRegistries.BLOCK.containsKey(def.block().get())) {
                    blocks.put(BuiltInRegistries.BLOCK.get(def.block().get()), mod);
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

    private static final class TurbineCoilReloadListener extends SimpleJsonResourceReloadListener {
        TurbineCoilReloadListener() {
            super(GSON, "justreactors/turbine_coils");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> raw, ResourceManager resourceManager, ProfilerFiller profiler) {
            Map<ResourceLocation, CoilDef> entries = decode(raw, CoilDef.CODEC, "turbine coil");
            Map<Block, TurbineCoil> blocks = new HashMap<>();
            List<TurbineCoil.TagEntry> tags = new ArrayList<>();
            int loaded = 0;

            for (Map.Entry<ResourceLocation, CoilDef> e : entries.entrySet()) {
                CoilDef def = e.getValue();
                TurbineCoil coil = new TurbineCoil(def.efficiency(), def.bonus(), def.drag());
                boolean matched = false;
                if (def.block().isPresent() && BuiltInRegistries.BLOCK.containsKey(def.block().get())) {
                    blocks.put(BuiltInRegistries.BLOCK.get(def.block().get()), coil);
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
