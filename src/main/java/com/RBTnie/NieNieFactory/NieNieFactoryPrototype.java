package com.RBTnie.NieNieFactory;
//import com.nienie.nieniefactory.OriginContent;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public class NieNieFactoryPrototype extends NieNieSuperContent {
    // ✅ 添加捏捏工厂原型的方块物品注册
    public static final DeferredBlock<Block> NIENIE_FACTORY_PROTOTYPE_BLOCK = BLOCKS.registerSimpleBlock(
            "nieniefactory_prototype",
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0F).requiresCorrectToolForDrops()
    );

    public static final DeferredItem<BlockItem> NIENIE_FACTORY_PROTOTYPE_ITEM = ITEMS.registerSimpleBlockItem(
            "nieniefactory_prototype",
            NIENIE_FACTORY_PROTOTYPE_BLOCK
    );

    // ✅ 用刚刚的方块建一个新的创造栏
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> NIENIE_FACTORY_TAB = CREATIVE_MODE_TABS.register("nieniefactory_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.nieniefactory"))
            .icon(() -> NIENIE_FACTORY_PROTOTYPE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> output.accept(NIENIE_FACTORY_PROTOTYPE_ITEM.get())).build()
    );




}