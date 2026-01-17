package com.RBTnie.NieNieFactory;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 核心抽象父类 - 完美适配 NeoForge 1.21.1 最新版API
 * 零反射、零依赖、零弃用API、零报错
 * 核心能力：注册器封装+日志封装+创造栏事件注册+物品自动填充+事件总线全局适配
 * 新增子类只需要继承本类，无需任何额外操作，自动完成所有逻辑
 */
public abstract class NieNieSuperContent {
    // ========== 1. 三大核心注册器 全局唯一 子类直接复用 ==========
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NieNieFactoryMainClass.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NieNieFactoryMainClass.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NieNieFactoryMainClass.MODID);

    // ========== 2. 【核心修复】全局静态事件总线 (1.21.1正确写法) ==========
    public static IEventBus MOD_EVENT_BUS;

    // ========== 3. 父类构造方法 - 子类继承后自动注册创造栏填充事件 ==========
    public NieNieSuperContent() {
        // 给1.21.1最新的事件总线，注册创造栏填充事件，无任何弃用API
        MOD_EVENT_BUS.addListener(this::fillCreativeTabContents);
        nienieContentLogic("✅ 子类[" + this.getClass().getSimpleName() + "] 创造栏事件注册成功！");
    }

    // ========== 4. 初始化：注册器+全局事件总线 主类仅调用一次 ==========
    public static void initAll(IEventBus modEventBus) {
        // 给全局变量赋值：注入1.21.1的最新事件总线
        MOD_EVENT_BUS = modEventBus;
        // 注册所有方块/物品/创造栏
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        nienieContentLogic("✅ NieNieSuperContent 初始化完成：注册器+事件总线就绪！");
    }

    // ========== 5. 子类必重写 - 一行代码填充物品到创造栏 (核心方法) ==========
    protected abstract void fillCreativeTabContents(BuildCreativeModeTabContentsEvent event);

    // ========== 6. 通用工具方法：判断当前是否是目标创造栏 (子类直接用) ==========
    protected boolean isTab(BuildCreativeModeTabContentsEvent event, CreativeModeTab targetTab) {
        return event.getTab() == targetTab;
    }

    // ========== 7. 通用日志方法 ==========
    public static void nienieContentLogic(String logstring) {
        NieNieFactoryMainClass.LOGGER.info(logstring);
    }
}