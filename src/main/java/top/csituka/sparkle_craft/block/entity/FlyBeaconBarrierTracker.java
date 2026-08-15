package top.csituka.sparkle_craft.block.entity;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FlyBeaconBarrierTracker {

    public static final int AXIS_X = 0;
    public static final int AXIS_Y = 1;
    public static final int AXIS_Z = 2;

    private static final long STALE_TICKS = 2L;

    private static final Map<BlockPos, Barrier> BARRIERS = new HashMap<>();
    private static World trackedWorld;

    private FlyBeaconBarrierTracker() {
    }

    public static void update(World world, BlockPos pos, boolean active) {
        if (trackedWorld != world) {
            trackedWorld = world;
            BARRIERS.clear();
        }
        if (active) {
            BARRIERS.put(pos.toImmutable(), new Barrier(pos, world.getTime()));
        } else {
            BARRIERS.remove(pos);
        }
    }

    public static void remove(BlockPos pos) {
        BARRIERS.remove(pos);
    }

    public static List<Barrier> collect(World world) {
        if (trackedWorld != world) {
            trackedWorld = world;
            BARRIERS.clear();
            return List.of();
        }
        if (BARRIERS.isEmpty()) {
            return List.of();
        }
        long time = world.getTime();
        BARRIERS.values().removeIf(barrier -> time - barrier.lastTick > STALE_TICKS);
        return new ArrayList<>(BARRIERS.values());
    }

    public static final class Barrier {

        private final double[] min;
        private final double[] max;
        private final long key;
        private final long lastTick;

        private Barrier(BlockPos pos, long lastTick) {
            double centerX = pos.getX() + 0.5;
            double centerZ = pos.getZ() + 0.5;
            int radius = FlyBeaconBlockEntity.FLIGHT_RADIUS;
            this.min = new double[]{
                    centerX - radius,
                    pos.getY() - FlyBeaconBlockEntity.FLIGHT_BELOW,
                    centerZ - radius
            };
            this.max = new double[]{
                    centerX + radius,
                    pos.getY() + FlyBeaconBlockEntity.FLIGHT_ABOVE,
                    centerZ + radius
            };
            this.key = pos.asLong();
            this.lastTick = lastTick;
        }

        public double getMin(int axis) {
            return min[axis];
        }

        public double getMax(int axis) {
            return max[axis];
        }

        public long getKey() {
            return key;
        }
    }
}
