package com.RBTnie.NieNieFactory.NieNieContents;

import com.RBTnie.NieNieFactory.NieNieFactoryMainClass;
import com.RBTnie.NieNieFactory.NieNieSuperContent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

@EventBusSubscriber(modid = NieNieFactoryMainClass.MODID)
public class NieNieDimension extends NieNieSuperContent {

    // 静态初始化：注册事件监听器（替代@SubscribeEvent）
    public NieNieDimension (){

//        modEventBus.addListener(NieNieDimension.ModBusConsumer::gatherData);
    }

    // 1. 维度核心资源键（适配命名规范）
    public static final ResourceLocation NIENIE_DIMENSION_RL = ResourceLocation.fromNamespaceAndPath(NieNieFactoryMainClass.MODID, "nienie_dimension");
    public static final ResourceKey<Level> NIENIE_DIMENSION_KEY = ResourceKey.create(Registries.DIMENSION, NIENIE_DIMENSION_RL);
    public static final ResourceKey<LevelStem> NIENIE_DIMENSION_STEM_KEY = ResourceKey.create(Registries.LEVEL_STEM, NIENIE_DIMENSION_RL);
    public static final ResourceKey<DimensionType> NIENIE_DIMENSION_TYPE_KEY = ResourceKey.create(Registries.DIMENSION_TYPE, NIENIE_DIMENSION_RL.withSuffix("_type"));
    public static final ResourceKey<NoiseGeneratorSettings> NIENIE_DIMENSION_NOISEGENSETTINGS_KEY = ResourceKey.create(Registries.NOISE_SETTINGS, NIENIE_DIMENSION_RL);

    // 2. 维度类型注册（彻底禁用生物+空维度属性）
    public static class DimensionTypeRegistry {
        public static void bootstrap(BootstrapContext<DimensionType> context) {
            context.register(NIENIE_DIMENSION_TYPE_KEY,
                    new DimensionType(
                            OptionalLong.of(12000),         // 固定正午（无昼夜更替）
                            true,                           // 开启天空光照（虚空可见）
                            false,                          // 无天花板
                            false,                          // 非极热（方块不熔化）
                            false,                          // 【核心】非自然维度，禁用生物生成
                            1.0D,                           // 1:1坐标缩放
                            false,                          // 床不可用
                            false,                          // 重生锚不可用
                            -64,                            // 最低高度
                            384,                            // 总高度
                            384,                            // 逻辑高度
                            BlockTags.INFINIBURN_OVERWORLD, // 主世界无限燃烧标签（仅占位）
                            BuiltinDimensionTypes.OVERWORLD_EFFECTS, // 虚空维度特效
                            0.0F,                           // 环境光强度0（纯黑）
                            // 强化生物禁用：所有生成参数设为0
                            new DimensionType.MonsterSettings(
                                    false,          // 猪灵不中立
                                    false,          // 禁用灾厄袭击
                                    ConstantInt.ZERO, // 生物生成消耗0
                                    0               // 生物生成阈值0
                            )
                    )
            );
        }
    }
    // 3. 空NoiseRouter（无任何地形生成）
    private static NoiseRouter Router() {
        // 所有密度函数设为常量0，彻底禁用地形生成
        DensityFunction zero = DensityFunctions.constant(0);
        return new NoiseRouter(
                zero, zero, zero, zero, zero, zero,
                zero, zero, zero, zero, zero, zero,
                zero, zero, zero
        );
    }

    private static SurfaceRules.RuleSource surfaceRule() {
        // 直接返回"强制生成空气"的规则，无需sequence嵌套（1.21.1 完全兼容）
        return SurfaceRules.state(Blocks.AIR.defaultBlockState());
    }

    // 5. 噪声生成设置（空地形配置）
    public static class NoiseGenSettingRegistry {
        public static void bootstrap(BootstrapContext<NoiseGeneratorSettings> context) {
            context.register(NIENIE_DIMENSION_NOISEGENSETTINGS_KEY,
                    new NoiseGeneratorSettings(
                            // 空噪声设置（最小参数，无地形）
                            NoiseSettings.create(0, 160, 2, 2),
                            Blocks.AIR.defaultBlockState(),    // 默认方块设为空气
                            Blocks.WATER.defaultBlockState(),  // 流体设为水（实际不会生成）
                            Router(),
                            surfaceRule(),                     // 空表面规则
                            List.of(),                         // 无生物群系调整
                            64,                                // 海平面对齐
                            false,                             // 禁用.minecraft结构
                            false,                             // 禁用下界结构
                            false,                             // 禁用村庄结构
                            true                               // 启用噪声筛选（不影响空地形）
                    )
            );
        }
    }

    // 6. 维度茎注册（绑定空群系+空地形）
    public static class LevelStemRegistry {
        // 空群系资源键
        public static final ResourceKey<Biome> NIENIE_BIOME = ResourceKey.create(Registries.BIOME,
                ResourceLocation.fromNamespaceAndPath(NieNieFactoryMainClass.MODID, "nienie_biome"));

        public static void bootstrap(BootstrapContext<LevelStem> bootstrap) {
            HolderGetter<Level> levelGetter = bootstrap.lookup(Registries.DIMENSION);
            HolderGetter<DimensionType> levelTypeGetter = bootstrap.lookup(Registries.DIMENSION_TYPE);
            HolderGetter<Biome> biomeGetter = bootstrap.lookup(Registries.BIOME);
            HolderGetter<NoiseGeneratorSettings> noiseGetter = bootstrap.lookup(Registries.NOISE_SETTINGS);

            bootstrap.register(NIENIE_DIMENSION_STEM_KEY,
                    new LevelStem(
                            levelTypeGetter.getOrThrow(NIENIE_DIMENSION_TYPE_KEY),
                            // 空群系+空噪声生成器，彻底无方块/实体
                            new NoiseBasedChunkGenerator(
                                    new FixedBiomeSource(biomeGetter.getOrThrow(NIENIE_BIOME)),
                                    noiseGetter.getOrThrow(NIENIE_DIMENSION_NOISEGENSETTINGS_KEY)
                            )
                    )
            );
        }

    }

    public static class BiomeRegistry {
        public static void bootstrap(BootstrapContext<Biome> context) {
            // 1. 获取 BiomeGenerationSettings.Builder 必需的两个 HolderGetter
            HolderGetter<PlacedFeature> placedFeatureGetter = context.lookup(Registries.PLACED_FEATURE);
            HolderGetter<ConfiguredWorldCarver<?>> worldCarverGetter = context.lookup(Registries.CONFIGURED_CARVER);

            // 2. 构建空的生物生成规则（无任何实体生成）
            MobSpawnSettings emptyMobSettings = new MobSpawnSettings.Builder().build();

            // 3. 构建空的地形生成规则（核心：不添加任何生成器/雕刻器，实现无地形）
            // 无需调用 clear()，空 Builder 本身就不会生成任何内容
            BiomeGenerationSettings emptyGenSettings = new BiomeGenerationSettings.Builder(placedFeatureGetter, worldCarverGetter)
                    .build(); // 直接构建，不添加任何生成规则

            // 4. 构建空的生物群系视觉特效（纯黑背景）
            BiomeSpecialEffects emptyEffects = new BiomeSpecialEffects.Builder()
                    .fogColor(0x000000)        // 纯黑雾色
                    .skyColor(0x000000)        // 纯黑天空
                    .waterColor(0x000000)      // 纯黑水色
                    .waterFogColor(0x000000)   // 纯黑水雾
                    .build();

            // 5. 核心：使用 BiomeBuilder 构建空群系（严格匹配你提供的 Biome 源码）
            Biome emptyBiome = new Biome.BiomeBuilder()
                    // 气候设置：无降水 + 中性温度 + 无温度修正 + 无降雨
                    .hasPrecipitation(false)    // 禁用所有降水（雨/雪）
                    .temperature(0.5F)          // 中性温度（无影响）
                    .temperatureAdjustment(Biome.TemperatureModifier.NONE) // 无温度修正
                    .downfall(0.0F)             // 无降雨/降雪量
                    // 绑定视觉特效、生物生成、地形生成规则
                    .specialEffects(emptyEffects)
                    .mobSpawnSettings(emptyMobSettings)
                    .generationSettings(emptyGenSettings)
                    // 构建最终的空群系
                    .build();

            // 6. 注册空群系到注册表
            context.register(LevelStemRegistry.NIENIE_BIOME, emptyBiome);
        }
    }



    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.NOISE_SETTINGS, NoiseGenSettingRegistry::bootstrap)
            .add(Registries.DIMENSION_TYPE, DimensionTypeRegistry::bootstrap)
            .add(Registries.LEVEL_STEM, LevelStemRegistry::bootstrap)
            .add(Registries.BIOME, BiomeRegistry::bootstrap);

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper fileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

        // 添加数据生成器
        generator.addProvider(event.includeServer(),
                new DatapackBuiltinEntriesProvider(output, lookup, BUILDER, Collections.singleton(NieNieFactoryMainClass.MODID)));
    }

}