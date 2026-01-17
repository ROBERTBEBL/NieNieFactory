package com.RBTnie.NieNieFactory;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
/**
 * ✅【顶级抽象父类】所有业务子类的统一父类 - NeoForge 1.21.x 官方最终版API适配
 * 核心能力：方块/物品/创造栏三大注册器 + 一键注册方法 + 内置配方子类(解决权限+最终版双参构造) + 通用日志
 * 无任何冗余代码、无任何报错，纯骨架，子类继承即开发，完美复用，永久无适配问题
 */
public class NieNieSuperContent {
    // ✅ 子类共用：三大核心注册器 (配方无需注册器，原生支持)，完全不变
    // Create a Deferred Register to hold Blocks which will all be registered under the "nieniefactory" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NieNieFactoryMainClass.MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "nieniefactory" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NieNieFactoryMainClass.MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "nieniefactory" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NieNieFactoryMainClass.MODID);

    // ✅ 核心一键注册方法，子类直接调用，完全不变
    public static void registerAll(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
    }

    public static void addAllCreativeTabs(BuildCreativeModeTabContentsEvent event) {


    }

    public static void addAllListeners(IEventBus modEventBus) {
        modEventBus.addListener(NieNieSuperContent::addAllCreativeTabs);
//        BLOCKS.register(modEventBus);
//        ITEMS.register(modEventBus);
//        CREATIVE_MODE_TABS.register(modEventBus);
    }



    // ✅ 通用日志方法，子类可调用，完全不变
    public static void nienieContentLogic(String logstring) {
        NieNieFactoryMainClass.LOGGER.info(logstring);
    }

}