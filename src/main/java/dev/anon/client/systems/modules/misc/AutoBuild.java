package dev.anon.client.systems.modules.misc;

import dev.anon.client.events.world.TickEvent;
import dev.anon.client.settings.*;
import dev.anon.client.systems.modules.Categories;
import dev.anon.client.systems.modules.Module;
import dev.anon.client.utils.player.InvUtils;
import dev.anon.client.utils.player.Rotations;
import dev.anon.client.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class AutoBuild extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> schematicPath = sgGeneral.add(new StringSetting.Builder()
        .name("schematic")
        .description("Path to the .litematica schematic file.")
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotate towards build position.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay between placements in ticks.")
        .defaultValue(1)
        .min(0)
        .sliderMax(10)
        .build()
    );

    private final Setting<Boolean> print = sgGeneral.add(new BoolSetting.Builder()
        .name("print")
        .description("Print blocks being placed in chat.")
        .defaultValue(false)
        .build()
    );

    private Queue<BlockPos> buildQueue;
    private boolean loaded, building;
    private int timer;

    public AutoBuild() {
        super(Categories.Misc, "auto-build", "Automatically builds from a .litematica schematic.");
    }

    @Override
    public void onActivate() {
        buildQueue = new LinkedList<>();
        loaded = false;
        building = false;
        timer = 0;

        String path = schematicPath.get();
        if (path.isEmpty()) {
            error("No schematic path set.");
            toggle();
            return;
        }

        loadSchematic(path);
    }

    @Override
    public void onDeactivate() {
        buildQueue = null;
        loaded = false;
        building = false;
    }

    private void loadSchematic(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                error("Schematic file not found: " + path);
                toggle();
                return;
            }

            CompoundTag tag = NbtIo.readCompressed(new FileInputStream(file), NbtAccounter.unlimitedHeap());

            int sizeX = tag.getInt("SizeX").orElse(0);
            int sizeY = tag.getInt("SizeY").orElse(0);
            int sizeZ = tag.getInt("SizeZ").orElse(0);

            long[] blockStateArray = tag.getLongArray("BlockStates").orElse(new long[0]);

            BlockPos origin = BlockPos.containing(mc.player.position()).below();
            int index = 0;
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    for (int x = 0; x < sizeX; x++) {
                        int blockIndex = getBlockIndex(blockStateArray, index++);
                        if (blockIndex > 0) {
                            BlockPos pos = origin.offset(x, y, z);
                            buildQueue.add(pos);
                        }
                    }
                }
            }

            info("Loaded schematic: " + buildQueue.size() + " blocks to place.");
            loaded = true;
            building = true;
        } catch (Exception e) {
            error("Failed to load schematic: " + e.getMessage());
            toggle();
        }
    }

    private int getBlockIndex(long[] data, int index) {
        if (data.length == 0) return 0;
        int bitsPerEntry = (int) Math.ceil(Math.log(data.length * 64) / Math.log(2));
        if (bitsPerEntry <= 0) bitsPerEntry = 1;
        int entriesPerLong = 64 / bitsPerEntry;
        int longIndex = index / entriesPerLong;
        int bitOffset = (index % entriesPerLong) * bitsPerEntry;
        if (longIndex >= data.length) return 0;
        return (int) ((data[longIndex] >> bitOffset) & ((1L << bitsPerEntry) - 1));
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!building || !loaded || buildQueue == null || buildQueue.isEmpty()) {
            if (building && buildQueue != null && buildQueue.isEmpty()) {
                info("Build complete!");
                building = false;
                toggle();
            }
            return;
        }

        if (timer > 0) { timer--; return; }

        BlockPos pos = buildQueue.peek();
        if (pos == null) return;

        BlockState state = mc.level.getBlockState(pos);
        if (!state.isAir()) {
            buildQueue.poll();
            return;
        }

        ItemStack hand = mc.player.getMainHandItem();
        if (!(hand.getItem() instanceof BlockItem)) {
            var result = InvUtils.findInHotbar(item -> item.getItem() instanceof BlockItem);
            if (!result.found()) {
                error("No blocks in hotbar.");
                toggle();
                return;
            }
            InvUtils.swap(result.slot(), true);
        }

        if (rotate.get()) {
            Rotations.rotate(Rotations.getYaw(pos), Rotations.getPitch(pos));
        }

        var blockResult = InvUtils.findInHotbar(item -> item.getItem() instanceof BlockItem);
        if (BlockUtils.place(pos, blockResult, true, 0, true)) {
            buildQueue.poll();
            if (print.get()) info("Placed: " + pos.toShortString());
        }

        timer = delay.get();
    }
}
