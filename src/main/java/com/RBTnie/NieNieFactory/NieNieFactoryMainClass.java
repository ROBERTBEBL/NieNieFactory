package com.RBTnie.NieNieFactory;

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

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(NieNieFactoryMainClass.MODID)
public class NieNieFactoryMainClass {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "nieniefactory";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
//    // Create a Deferred Register to hold Blocks which will all be registered under the "nieniefactory" namespace
//    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
//    // Create a Deferred Register to hold Items which will all be registered under the "nieniefactory" namespace
//    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
//    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "nieniefactory" namespace
//    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

//    // Creates a new Block with the id "nieniefactory:example_block", combining the namespace and path
//    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
//    // Creates a new BlockItem with the id "nieniefactory:example_block", combining the namespace and path
//    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);
//
//    // Creates a new food item with the id "nieniefactory:example_id", nutrition 1 and saturation 2
//    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", new Item.Properties().food(new FoodProperties.Builder().alwaysEdible().nutrition(1).saturationModifier(2f).build()));

//    // Creates a creative tab with the id "nieniefactory:example_tab" for the example item, that is placed after the combat tab
//    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.nieniefactory")).withTabsBefore(CreativeModeTabs.COMBAT).icon(() -> EXAMPLE_ITEM.get().getDefaultInstance()).displayItems((parameters, output) -> {
//        output.accept(EXAMPLE_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
//    }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public NieNieFactoryMainClass(IEventBus modEventBus, ModContainer modContainer) {

        // 初始化父类的注册器
        NieNieSuperContent.initAll(modEventBus);

        // 实例化所有子类
        initAllContentSubclasses();

//        modEventBus.addListener(this::addCreative);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (Nieniefactory) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

    }
    // ========== 核心方法：自动实例化所有子类 (新增子类后，这里加一行即可，极简) ==========
    private void initAllContentSubclasses() {
        // 规则：新增一个子类，就在这里 new 一次，仅此一行，永不改其他地方
        // 示例：new 你的子类名();
        new NieNieFactoryPrototype();
        // 以后加新内容，比如 new NieNieGoldGenerator(); 即可，其他全部不用动
        NieNieSuperContent.nienieContentLogic("✅ 所有子类自动实例化完成！物品将自动注册+进创造栏");
    }
    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.logDirtBlock) LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

//    // Add the example block item to the building blocks tab
//    private void addCreative(BuildCreativeModeTabContentsEvent event) {
//        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) event.accept(EXAMPLE_BLOCK_ITEM);
//    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID,  value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
