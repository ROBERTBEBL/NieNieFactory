package com.RBTnie.NieNieFactory.NieNieContents;

import com.RBTnie.NieNieFactory.NieNieSuperContent;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;


public class NieNieFactoryPrototype extends NieNieSuperContent {
    // 1. 注册方块 - 直接复用父类注册器
    public static final DeferredBlock<Block> NIENIE_FACTORY_PROTOTYPE_BLOCK = BLOCKS.registerSimpleBlock(
            "nieniefactory_prototype",
            BlockBehaviour.Properties.of().
            mapColor(MapColor.STONE).
            strength(2.0F).
            requiresCorrectToolForDrops()
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