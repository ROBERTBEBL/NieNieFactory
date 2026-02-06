package com.RBTnie.NieNieFactory.NieNieContents;
import com.RBTnie.NieNieFactory.NieNieSuperContent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.RBTnie.NieNieFactory.NieNieContents.NieNieDimension.*;
import static com.RBTnie.NieNieFactory.NieNieContents.NieNiePotion.NIENIE_EFFECT;

public class NieNieFactoryPrototype extends NieNieSuperContent {

    public static final ConcurrentHashMap<UUID, NieNiePlayerInfo> PLAYER_INFO_CACHE = new ConcurrentHashMap<>();

    // 【重要】玩家登出时清理缓存，避免内存泄漏
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID playerUuid = event.getEntity().getUUID();
        PLAYER_INFO_CACHE.remove(playerUuid);
        // 可选：登出时将缓存数据写入文件（延迟持久化）
        // savePlayerInfoToFile(playerUuid);
    }




    // 自定义方块类，重写onUse实现右键逻辑
    public static class NieNieFactoryPrototypeBlock extends Block {
        // 继承父类的构造方法（复用方块属性）
        public NieNieFactoryPrototypeBlock(Properties properties) {
            super(properties);
        }

        @Override
        protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, BlockHitResult hitResult) {
            // 区分客户端/服务端（避免逻辑重复执行）
            if (!level.isClientSide()) {

                // ========== 1. 核心新增：记录玩家交互信息 ==========
                NieNiePlayerInfo playerInfo = NieNiePlayerInfo.create(player, pos, state);
                // 示例：打印调试信息（实际可替换为序列化/存储逻辑）
                player.sendSystemMessage(Component.literal("已记录交互信息：玩家UUID=" + playerInfo.playerUuid() + " 方块类型=" + playerInfo.blockType()));

                // ========== 自定义右键逻辑 ==========
                if(player.hasEffect(NIENIE_EFFECT)){
                    // 示例1：给玩家发送提示

                    ServerLevel currentServerLevel = (ServerLevel) level; // 强转服务端维度
                    ServerLevel targetLevel = currentServerLevel.getServer().getLevel(NIENIE_DIMENSION_KEY);

                    if (targetLevel == currentServerLevel) { // 如果已经在捏捏维度
                        player.sendSystemMessage(Component.literal("你在捏捏维度右键了捏捏工厂原型方块！坐标：" + pos.getX() + "," + pos.getY() + "," + pos.getZ()));
                        return InteractionResult.PASS;
                    }
                    else {//如果不在捏捏维度
                        player.sendSystemMessage(Component.literal("你在其他维度右键了捏捏工厂原型方块！坐标：" + pos.getX() + "," + pos.getY() + "," + pos.getZ()));
                        Vec3 targetPos = new Vec3(pos.getX(), pos.getY(), pos.getZ()); // 目标位置
                        Vec3 speed = Vec3.ZERO; // 初始速度为0
                        float yRot = player.getYRot(); // 复用玩家当前Y旋转
                        float xRot = player.getXRot(); // 复用玩家当前X旋转
                        boolean missingRespawnBlock = false; // 非重生方块导致的切换
                        //AAA
                        player.changeDimension(new DimensionTransition(targetLevel, targetPos, speed, yRot, xRot, missingRespawnBlock, net.minecraft.world.level.portal.DimensionTransition.PLAY_PORTAL_SOUND));

                    }

                }
            }
            return InteractionResult.PASS;
        }

    }



    // 1. 注册方块 - 直接复用父类注册器
    public static final DeferredBlock<Block> NIENIE_FACTORY_PROTOTYPE_BLOCK = BLOCKS.register(
            "nieniefactory_prototype",
            () -> new NieNieFactoryPrototypeBlock( // 使用自定义方块类
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .strength(2.0F)
                            .requiresCorrectToolForDrops()
            )
    );




    // 2. 注册物品 - 直接复用父类注册器
    public static final DeferredItem<BlockItem> NIENIE_FACTORY_PROTOTYPE_ITEM = ITEMS.registerSimpleBlockItem(
            "nieniefactory_prototype",
            NIENIE_FACTORY_PROTOTYPE_BLOCK
    );

    // 3. 注册专属创造栏 - 直接复用父类注册器 (无需写displayItems)
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> NIENIE_FACTORY_TAB = CREATIVE_MODE_TABS.register(
            "nieniefactory_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.nieniefactory"))
                    .icon(() -> NIENIE_FACTORY_PROTOTYPE_ITEM.get().getDefaultInstance())
                    .build()
    );


//生成捏捏工厂原型的配方数据
    private static class ModRecipeProvider extends RecipeProvider {

        //https://docs.neoforged.net/docs/datagen/server/recipes
        public ModRecipeProvider(PackOutput pPackOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(pPackOutput, lookupProvider);
        }

        @Override
        protected void buildRecipes(@NotNull RecipeOutput pRecipeOutput) {
            // 有序合成
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, NIENIE_FACTORY_PROTOTYPE_ITEM.get())
                    .pattern("aaa")
                    .pattern(" b ")
                    .pattern(" b ")
                    .define('a',Items.STICK)
                    .define('b', Items.STICK)
                    .unlockedBy("has_stick",has(Items.STICK))
                    .save(pRecipeOutput);
        }
    }


    // ✅ 你的核心设计：子类构造传总线，直接注册事件+写填充逻辑，无重写、无抽象方法！
    public NieNieFactoryPrototype() {

        // 注册创造栏加入构造创造栏事件
        modEventBus.addListener((BuildCreativeModeTabContentsEvent event) -> {
            if(event.getTab() == NIENIE_FACTORY_TAB.get()) event.accept(NIENIE_FACTORY_PROTOTYPE_ITEM);
        });

        //把配方数据生成加入数据收集事件
        modEventBus.addListener((GatherDataEvent event) -> {
            ExistingFileHelper efh = event.getExistingFileHelper();
            var lp = event.getLookupProvider();
            // recipe
            event.getGenerator().addProvider(
                    event.includeServer(),
                    (DataProvider.Factory<ModRecipeProvider>) pOutput -> new ModRecipeProvider(pOutput, lp)
            );
        });

    }
}
