package com.RBTnie.NieNieFactory.NieNieContents;

import com.RBTnie.NieNieFactory.NieNieSuperContent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.Supplier;

public class NieNieFactory extends NieNieSuperContent {
    // 1. 注册方块 - 直接复用父类注册器
    public static final DeferredBlock<Block> NIENIE_FACTORY_BLOCK = BLOCKS.registerSimpleBlock(
            "nieniefactory",
            BlockBehaviour.Properties.of().
            mapColor(MapColor.STONE).
            strength(2.0F).
            requiresCorrectToolForDrops()
    );

    // 2. 注册物品 - 直接复用父类注册器
    public static final DeferredItem<BlockItem> NIENIE_FACTORY_ITEM = ITEMS.registerSimpleBlockItem(
            "nieniefactory",
            NIENIE_FACTORY_BLOCK
    );

    public static class NieNieFactoryBlockEntity extends BlockEntity {
//        private static final int MAX_TIME = 5 * 20;
//        private int timer = 0;
        public NieNieFactoryBlockEntity(BlockPos pPos, BlockState pBlockState) {
            super(NIENIE_FACTORY_PROTOTYPE_BLOCK_ENTITY.get(), pPos, pBlockState);
        }


    }
    //4 . 注册方块实体
    public static final Supplier<BlockEntityType<?>> NIENIE_FACTORY_PROTOTYPE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("hello_block_entity", () ->
                    BlockEntityType.Builder.of(NieNieFactoryBlockEntity::new,
                            NIENIE_FACTORY_BLOCK.get()).build(null));

    // ✅ 你的核心设计：子类构造传总线，直接注册事件+写填充逻辑，无重写、无抽象方法！
    public NieNieFactory() {
        // 直接注册创造栏事件
        modEventBus.addListener((BuildCreativeModeTabContentsEvent event) -> {
            if(event.getTab() == NieNieFactoryPrototype.NIENIE_FACTORY_TAB.get()) event.accept(NIENIE_FACTORY_ITEM);
        });

    }
}