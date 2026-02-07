package com.RBTnie.NieNieFactory.NieNieContents;

import com.RBTnie.NieNieFactory.NieNieFactoryMainClass;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
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
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

// 仅保留核心注解：ModID + 服务端限定
@EventBusSubscriber(modid = NieNieFactoryMainClass.MODID)
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
    private static final Path CACHE_DIR = FMLPaths.CONFIGDIR.get().resolve(NieNieFactoryMainClass.MODID + "/playerdata");
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

    // ==================== 核心：文件读写（修正版） ====================
// 修正：添加文件路径生成方法
    private static Path getFilePath(UUID uuid) {
        return CACHE_DIR.resolve(uuid.toString() + ".nbt");
    }

    public static void save(NieNiePlayerInfo info) {
        if (info == null) {
            System.err.println("[NieNieFactory] 跳过保存：玩家信息为 null");
            return;
        }

        // 1. 正确接收返回类型：DataResult<Tag>
        DataResult<Tag> encodeResult = NieNiePlayerInfo.CODEC.encodeStart(NbtOps.INSTANCE, info);

        // 2. 检查序列化错误
        if (encodeResult.error().isPresent()) {
            System.err.println("[NieNieFactory] 序列化失败: " + encodeResult.error().get().message());
            return;
        }

        // 3. 安全转换为 CompoundTag
        Tag rawTag = encodeResult.result().get();
        if (!(rawTag instanceof CompoundTag compoundTag)) {
            System.err.println("[NieNieFactory] 序列化结果类型错误: 期望 CompoundTag，实际 " + rawTag.getClass().getSimpleName());
            return;
        }

        Path filePath = getFilePath(info.playerUuid());
        try {
            Files.createDirectories(filePath.getParent());
            try (OutputStream outputStream = Files.newOutputStream(filePath)) {
                NbtIo.writeCompressed(compoundTag, outputStream);
            }
        } catch (IOException e) {
            System.err.println("[NieNieFactory] 保存 NBT 文件失败: " + filePath.getFileName());
            e.printStackTrace();
        }
    }

    // 修正：从文件加载（已移除冗余的 null 检查）
    public static Optional<NieNiePlayerInfo> load(UUID uuid) {
        return load(getFilePath(uuid));
    }

    public static Optional<NieNiePlayerInfo> load(Path path) {
        if (!Files.exists(path)) {
            return Optional.empty();
        }

        CompoundTag compoundTag;
        try (var inputStream = Files.newInputStream(path)) {
            compoundTag = NbtIo.readCompressed(inputStream, NbtAccounter.create(2 * 1024 * 1024));
        } catch (IOException e) {
            System.err.println("读取 NBT 文件失败: " + path.getFileName());
            e.printStackTrace();
            return Optional.empty();
        }

        DataResult<NieNiePlayerInfo> result = NieNiePlayerInfo.CODEC.parse(NbtOps.INSTANCE, compoundTag);
        if (result.error().isPresent()) {
            System.err.println("反序列化失败 (" + path.getFileName() + "): " + result.error().get().message());
            try {
                Files.deleteIfExists(path);
                System.out.println("已删除损坏的数据文件: " + path.getFileName());
            } catch (IOException ex) {
                System.err.println("无法删除损坏文件: " + path.getFileName());
            }
            return Optional.empty();
        }
        return Optional.of(result.result().get());
    }

    // ==================== 核心：登入/登出事件（修正版） ====================
    // 登入：加载文件到缓存
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        Optional<NieNiePlayerInfo> info = load(player.getUUID());
        if (info.isPresent()) {
            CACHE.put(player.getUUID(), info.get());
        }
    }

    // 登出：缓存写入文件+清理
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        UUID uuid = player.getUUID();
        NieNiePlayerInfo info = CACHE.remove(uuid);
        if (info != null) {
            save(info);
        }
    }

    // ==================== 极简工具方法 ====================
    public static NieNiePlayerInfo get(UUID uuid) {
        return CACHE.get(uuid);
    }

    public static void put(UUID uuid, NieNiePlayerInfo info) {
        CACHE.put(uuid, info);
    }
}