package com.RBTnie.NieNieFactory.NieNieContents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

// 仅保留核心注解：ModID + 服务端限定
@EventBusSubscriber(modid = "your_mod_id", value = Dist.DEDICATED_SERVER)
public record NieNiePlayerInfo(
        UUID playerUuid,
        BlockPos blockPos,
        String blockType,
        String clickTime,
        List<ItemStack> inventoryItems
) {
    // 1. 全局缓存（仅保留核心容器）
    private static final ConcurrentHashMap<UUID, NieNiePlayerInfo> CACHE = new ConcurrentHashMap<>();
    // 2. 固定存储路径（简化路径拼接）
    private static final Path CACHE_DIR = FMLPaths.CONFIGDIR.get().resolve("your_mod_id/playerdata");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // 3. 简化版Codec（只保留核心序列化）
    public static final Codec<NieNiePlayerInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("uuid").forGetter(NieNiePlayerInfo::playerUuid),
            BlockPos.CODEC.fieldOf("pos").forGetter(NieNiePlayerInfo::blockPos),
            Codec.STRING.fieldOf("block").forGetter(NieNiePlayerInfo::blockType),
            Codec.STRING.fieldOf("time").forGetter(NieNiePlayerInfo::clickTime),
            ItemStack.CODEC.listOf().fieldOf("items").forGetter(NieNiePlayerInfo::inventoryItems)
    ).apply(instance, NieNiePlayerInfo::new));

    // 4. 简化版构造（只存缓存，不做额外日志）
    public static NieNiePlayerInfo create(Player player, BlockPos pos, BlockState state) {
        if (player.level().isClientSide()) return null;

        NieNiePlayerInfo info = new NieNiePlayerInfo(
                player.getUUID(),
                pos,
                BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString(),
                LocalDateTime.now().format(FORMATTER),
                collectItems(player)
        );
        CACHE.put(info.playerUuid(), info);
        return info;
    }

    // 5. 简化版物品收集（只保留核心逻辑）
    private static List<ItemStack> collectItems(Player player) {
        Stream<ItemStack> main = player.getInventory().items.stream().filter(s -> !s.isEmpty());
        Stream<ItemStack> offhand = Stream.of(player.getOffhandItem()).filter(s -> !s.isEmpty());
        Stream<ItemStack> armor = StreamSupport.stream(player.getArmorSlots().spliterator(), false).filter(s -> !s.isEmpty());
        return Stream.concat(Stream.concat(main, offhand), armor).collect(Collectors.toList());
    }

    // ==================== 核心：文件读写（极简版） ====================
    // 保存到文件（Path转File，简化IO）
    private static void save(UUID uuid, NieNiePlayerInfo info) {
        try {
            Files.createDirectories(CACHE_DIR); // 确保目录存在
            File file = CACHE_DIR.resolve(uuid + ".nbt").toFile();
            // 序列化+写入（简化容错，直接抛出异常）
            CompoundTag tag = (CompoundTag) CODEC.encodeStart(NbtOps.INSTANCE, info).getOrThrow();
            NbtIo.writeCompressed(tag, file);
        } catch (Exception e) {
            e.printStackTrace(); // 仅打印异常，不做复杂处理
        }
    }

    // 从文件加载（简化容错）
    private static NieNiePlayerInfo load(UUID uuid) {
        File file = CACHE_DIR.resolve(uuid + ".nbt").toFile();
        if (!file.exists()) return null;

        try {
            CompoundTag tag = NbtIo.readCompressed(file);
            return CODEC.decode(NbtOps.INSTANCE, tag).getOrThrow().getFirst();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ==================== 核心：登入/登出事件（极简版） ====================
    // 登入：加载文件到缓存
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        NieNiePlayerInfo info = load(player.getUUID());
        if (info != null) CACHE.put(player.getUUID(), info);
    }

    // 登出：缓存写入文件+清理
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        UUID uuid = player.getUUID();
        NieNiePlayerInfo info = CACHE.remove(uuid);
        if (info != null) save(uuid, info);
    }

    // ==================== 极简工具方法 ====================
    public static NieNiePlayerInfo get(UUID uuid) {
        return CACHE.get(uuid);
    }

    public static void put(UUID uuid, NieNiePlayerInfo info) {
        CACHE.put(uuid, info);
    }
}