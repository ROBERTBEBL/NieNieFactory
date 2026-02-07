package com.RBTnie.NieNieFactory.NieNieContents;

import com.RBTnie.NieNieFactory.NieNieFactoryMainClass;
import com.RBTnie.NieNieFactory.NieNieSuperContent;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = NieNieFactoryMainClass.MODID)
public class NieNiePotion extends NieNieSuperContent {



    // 注册捏捏效果
    public static final DeferredHolder<MobEffect, NieNieEffect> NIENIE_EFFECT = MOB_EFFECTS.register("nienie_effect", () -> new NieNieEffect(
            // 可以是 BENEFICIAL、NEUTRAL 或 HARMFUL。用于确定此效果的工具提示颜色。
            MobEffectCategory.BENEFICIAL,
            // 效果粒子的颜色。
            0x44FF44
    ));

    // 注册捏捏药水
    public static final Holder<Potion> NIENIE_POTION = POTIONS.register("nienie_potion", () -> new Potion(new MobEffectInstance(NIENIE_EFFECT, 3600,0)));


    // ✅ 你的核心设计：子类构造传总线，直接注册事件+写填充逻辑，无重写、无抽象方法！
    public NieNiePotion() {

//        modEventBus.addListener(this::gatherData);
    }
    // 数据生成核心方法：自动生成语言文件
    @SubscribeEvent
    private static void gatherData(GatherDataEvent event) {
        // 获取数据生成器
        var generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        // 仅在客户端数据生成时执行（语言文件属于客户端资源）
        if (event.includeClient()) {
            // 添加中文语言生成器
            generator.addProvider(event.includeClient(), new LanguageProvider(packOutput, NieNieFactoryMainClass.MODID, "zh_cn") {
                @Override
                protected void addTranslations() {
                    // 效果名称
                    add(NIENIE_EFFECT.get(), "捏捏效果");
                    // 药水效果描述（解决effect.empty问题）
                    add("item.nieniefactory.nienie_potion.effect.nienie_potion", "捏捏效果 3:00");
                }
            });
        }
    }


    //创建捏捏效果
    public static class NieNieEffect extends MobEffect {
        public NieNieEffect(MobEffectCategory category, int color) {
            super(category, color);
        }

        @Override
        public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
            // 在这里应用你的效果逻辑。
            // 示例：添加可观测的效果（回血），验证是否生效
//            if (!entity.level().isClientSide() && entity.getHealth() < entity.getMaxHealth()) {
//                entity.heal(0.5F); // 每2刻恢复0.5血（对应下方每2刻触发）
//            }
            return true; // 必须返回true，否则效果会立即消失
            // 如果此方法返回 false 且 shouldApplyEffectTickThisTick 返回 true，效果将立即被移除
        }

        // 效果是否应在当前刻应用。例如，再生效果每 x 刻应用一次，取决于刻计数和放大倍数。
        @Override
        public boolean shouldApplyEffectTickThisTick(int tickCount, int amplifier) {
            return tickCount % 2 == 0; // 替换为你想要的检查
        }

        // 当效果首次添加到实体时调用的实用方法。
        // 在从实体中移除所有此效果的实例之前，不会再次调用此方法。
        @Override
        public void onEffectAdded(@NotNull LivingEntity entity, int amplifier) {
            super.onEffectAdded(entity, amplifier);
            if (!entity.level().isClientSide()) {
                entity.sendSystemMessage(Component.literal("获得了捏捏效果！"));
            }
        }

        // 当效果添加到实体时调用的实用方法。
        // 每次将此效果添加到实体时都会调用此方法。
        @Override
        public void onEffectStarted(@NotNull LivingEntity entity, int amplifier) {
        }



    }

}


