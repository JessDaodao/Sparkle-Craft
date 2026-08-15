package top.csituka.sparkle_craft.block.entity;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FlyBeaconBarrierTracker {

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

        private final double centerX;
        private final double centerZ;
        private final double minY;
        private final double maxY;
        private final double radius;
        private final long key;
        private final long lastTick;

        private Barrier(BlockPos pos, long lastTick) {
            this.centerX = pos.getX() + 0.5;
            this.centerZ = pos.getZ() + 0.5;
            this.minY = pos.getY() - FlyBeaconBlockEntity.FLIGHT_BELOW;
            this.maxY = pos.getY() + FlyBeaconBlockEntity.FLIGHT_ABOVE;
            this.radius = FlyBeaconBlockEntity.FLIGHT_RADIUS;
            this.key = pos.asLong();
            this.lastTick = lastTick;
        }

        public double getCenterX() {
            return centerX;
        }

        public double getCenterZ() {
            return centerZ;
        }

        public double getMinY() {
            return minY;
        }

        public double getMaxY() {
            return maxY;
        }

        public double getRadius() {
            return radius;
        }

        public long getKey() {
            return key;
        }
    }
}
