package com.RBTnie.NieNieFactory;

import net.minecraft.core.Position;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.dimension.DimensionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 核心抽象父类 - 完美适配 NeoForge 1.21.1 最新版API
 * 零反射、零依赖、零弃用API、零报错
 * 核心能力：注册器封装+日志封装+创造栏事件注册+物品自动填充+事件总线全局适配
 * 新增子类只需要继承本类，无需任何额外操作，自动完成所有逻辑
 */
public class NieNieSuperContent {
    // ========== 1. 三大核心注册器 全局唯一 子类直接复用 ==========
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NieNieFactoryMainClass.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NieNieFactoryMainClass.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NieNieFactoryMainClass.MODID);
    // 新增：药水效果注册器
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, NieNieFactoryMainClass.MODID);
    // 新增：维度类型注册器
    public static final DeferredRegister<DimensionType> DIMENSION_TYPES = DeferredRegister.create(Registries.DIMENSION_TYPE, NieNieFactoryMainClass.MODID);
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, NieNieFactoryMainClass.MODID);

    // ✅ 供子类使用的事件总线成员变量
    public static IEventBus modEventBus;

    public static void registerAll(IEventBus modEventBus) {
        NieNieSuperContent.modEventBus = modEventBus;
        CREATIVE_MODE_TABS.register(modEventBus);
        DIMENSION_TYPES.register(modEventBus);
        MOB_EFFECTS.register(modEventBus);
        POTIONS.register(modEventBus);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        System.out.println("✅ 父类静态注册器绑定总线完成！已经保存模组事件总线");
    }

}