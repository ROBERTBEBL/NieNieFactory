package com.RBTnie.NieNieFactory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

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

    // ✅ ✅ ✅ 一行代码 物品自动加入创造栏 ✅ ✅ ✅
    @Override
    protected void fillCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (isTab(event, NIENIE_FACTORY_TAB.get())) {
            event.accept(NIENIE_FACTORY_PROTOTYPE_ITEM);
            nienieContentLogic("✅ 原型方块 已成功加入专属创造栏！");
        }
    }
}