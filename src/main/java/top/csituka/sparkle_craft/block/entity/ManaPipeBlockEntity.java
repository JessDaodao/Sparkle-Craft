package top.csituka.sparkle_craft.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import top.csituka.sparkle_craft.block.ModBlocks;
import top.csituka.sparkle_craft.block.custom.CrystalManaExtractorBlock;
import top.csituka.sparkle_craft.block.custom.ManaPipeBlock;

import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

public class ManaPipeBlockEntity extends BlockEntity {

    public static final int MAX_MANA = 100;
    public static final int TRANSFER_RATE = 10;
    private static final int MAX_NETWORK_SEARCH = 256;
    private static final int TANK_CONNECTION_DIRECTION_DELAY_TICKS = 20;

    private int mana;
    private final EnumMap<Direction, TankConnectionFlow> tankConnectionFlows =
            new EnumMap<>(Direction.class);

    public int getMana() {
        return mana;
    }

    public ManaPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_PIPE, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state,
                            ManaPipeBlockEntity blockEntity) {
        BlockState currentState = world.getBlockState(pos);
        if (!currentState.isOf(ModBlocks.MANA_PIPE)) {
            return;
        }

        blockEntity.tickTankConnectionFlows(world, currentState);
        int manaBeforeTick = blockEntity.mana;
        boolean connectedToTank = hasAdjacentManaTank(world, pos, currentState);
        int pullBudget = Math.min(TRANSFER_RATE, MAX_MANA - blockEntity.mana);
        if (pullBudget > 0) {
            for (Direction direction : Direction.values()) {
                if (!currentState.get(ManaPipeBlock.getConnectionProperty(direction))) {
                    continue;
                }
                BlockPos neighborPos = pos.offset(direction);
                BlockState neighborState = world.getBlockState(neighborPos);
                BlockEntity neighbor = world.getBlockEntity(neighborPos);
                if (neighborState.isOf(ModBlocks.CRYSTAL_MANA_EXTRACTOR)
                        && neighbor instanceof CrystalManaExtractorBlockEntity extractor
                        && CrystalManaExtractorBlock.hasManaOutputOn(neighborState,
                        direction.getOpposite())) {
                    int extracted = extractor.extractMana(pullBudget);
                    blockEntity.mana += extracted;
                    pullBudget -= extracted;
                    if (pullBudget == 0) {
                        break;
                    }
                }
            }
        }

        int tankPullBudget = 0;
        if (connectedToTank && pullBudget > 0) {
            tankPullBudget = Math.min(pullBudget, getManaDemand(world, pos));
        }
        if (tankPullBudget > 0) {
            for (Direction direction : Direction.values()) {
                if (!currentState.get(ManaPipeBlock.getConnectionProperty(direction))) {
                    continue;
                }
                BlockEntity neighbor = world.getBlockEntity(pos.offset(direction));
                if (neighbor instanceof ManaTankBlockEntity tank
                        && blockEntity.canPullFromTank(direction)) {
                    int extracted = tank.extractMana(tankPullBudget);
                    blockEntity.mana += extracted;
                    if (extracted > 0) {
                        blockEntity.markTankConnectionFlow(
                                direction, TankConnectionDirection.OUTPUT);
                    }
                    tankPullBudget -= extracted;
                    if (tankPullBudget == 0) {
                        break;
                    }
                }
            }
        }

        for (Direction direction : Direction.values()) {
            if (!currentState.get(ManaPipeBlock.getConnectionProperty(direction))) {
                continue;
            }
            BlockEntity neighbor = world.getBlockEntity(pos.offset(direction));
            if (neighbor instanceof ManaPipeBlockEntity pipe) {
                int amount = Math.min(TRANSFER_RATE, (blockEntity.mana - pipe.mana) / 2);
                if (amount > 0) {
                    int inserted = pipe.receiveMana(amount);
                    blockEntity.mana -= inserted;
                }
            }
        }

        int pushBudget = Math.min(TRANSFER_RATE, blockEntity.mana);
        for (Direction direction : Direction.values()) {
            if (pushBudget == 0
                    || !currentState.get(ManaPipeBlock.getConnectionProperty(direction))) {
                continue;
            }
            BlockEntity neighbor = world.getBlockEntity(pos.offset(direction));
            if (neighbor instanceof FlyBeaconBlockEntity beacon) {
                int inserted = beacon.receiveMana(pushBudget);
                blockEntity.mana -= inserted;
                pushBudget -= inserted;
            }
        }

        if (connectedToTank && pushBudget > 0 && getManaDemand(world, pos) == 0) {
            for (Direction direction : Direction.values()) {
                if (pushBudget == 0
                        || !currentState.get(ManaPipeBlock.getConnectionProperty(direction))
                        || !blockEntity.canPushToTank(direction)) {
                    continue;
                }
                BlockEntity neighbor = world.getBlockEntity(pos.offset(direction));
                if (neighbor instanceof ManaTankBlockEntity tank) {
                    int inserted = tank.receiveMana(pushBudget);
                    if (inserted > 0) {
                        blockEntity.mana -= inserted;
                        pushBudget -= inserted;
                        blockEntity.markTankConnectionFlow(
                                direction, TankConnectionDirection.INPUT);
                    }
                }
            }
        }

        if (blockEntity.mana != manaBeforeTick) {
            blockEntity.markDirty();
        }
        blockEntity.syncManaState();
    }

    private static boolean hasAdjacentManaTank(World world, BlockPos pos, BlockState state) {
        for (Direction direction : Direction.values()) {
            if (!state.get(ManaPipeBlock.getConnectionProperty(direction))) {
                continue;
            }
            if (world.getBlockEntity(pos.offset(direction)) instanceof ManaTankBlockEntity) {
                return true;
            }
        }
        return false;
    }

    private static int getManaDemand(World world, BlockPos startPos) {
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> consumers = new HashSet<>();
        pending.add(startPos);
        visited.add(startPos);
        int demand = 0;

        while (!pending.isEmpty() && visited.size() <= MAX_NETWORK_SEARCH) {
            BlockPos currentPos = pending.removeFirst();
            BlockState currentState = world.getBlockState(currentPos);
            if (!currentState.isOf(ModBlocks.MANA_PIPE)) {
                continue;
            }

            for (Direction direction : Direction.values()) {
                if (!currentState.get(ManaPipeBlock.getConnectionProperty(direction))) {
                    continue;
                }
                BlockPos neighborPos = currentPos.offset(direction);
                BlockEntity neighbor = world.getBlockEntity(neighborPos);
                if (neighbor instanceof FlyBeaconBlockEntity beacon
                        && consumers.add(neighborPos)) {
                    demand = Math.min(TRANSFER_RATE, demand + beacon.getManaSpace());
                }
                if (world.getBlockState(neighborPos).isOf(ModBlocks.MANA_PIPE)
                        && visited.add(neighborPos)) {
                    pending.addLast(neighborPos);
                }
            }
        }
        return demand;
    }

    private void tickTankConnectionFlows(World world, BlockState state) {
        for (Direction direction : Direction.values()) {
            if (!state.get(ManaPipeBlock.getConnectionProperty(direction))
                    || !(world.getBlockEntity(pos.offset(direction))
                    instanceof ManaTankBlockEntity)) {
                tankConnectionFlows.remove(direction);
                continue;
            }

            TankConnectionFlow flow = tankConnectionFlows.get(direction);
            if (flow == null) {
                continue;
            }
            if (flow.remainingTicks() <= 1) {
                tankConnectionFlows.remove(direction);
            } else {
                tankConnectionFlows.put(direction,
                        new TankConnectionFlow(flow.direction(), flow.remainingTicks() - 1));
            }
        }
    }

    private boolean canPullFromTank(Direction direction) {
        TankConnectionFlow flow = tankConnectionFlows.get(direction);
        return flow == null || flow.direction() == TankConnectionDirection.OUTPUT;
    }

    private boolean canPushToTank(Direction direction) {
        TankConnectionFlow flow = tankConnectionFlows.get(direction);
        return flow == null || flow.direction() == TankConnectionDirection.INPUT;
    }

    private void markTankConnectionFlow(Direction direction,
                                        TankConnectionDirection connectionDirection) {
        tankConnectionFlows.put(direction,
                new TankConnectionFlow(connectionDirection,
                        TANK_CONNECTION_DIRECTION_DELAY_TICKS));
    }

    private int receiveMana(int amount) {
        int inserted = Math.min(Math.max(0, amount), MAX_MANA - mana);
        if (inserted > 0) {
            mana += inserted;
            markDirty();
            syncManaState();
        }
        return inserted;
    }

    private void syncManaState() {
        if (world == null || world.isClient) {
            return;
        }
        BlockState state = world.getBlockState(pos);
        boolean hasMana = mana > 0;
        if (state.isOf(ModBlocks.MANA_PIPE) && state.get(ManaPipeBlock.HAS_MANA) != hasMana) {
            world.setBlockState(pos, state.with(ManaPipeBlock.HAS_MANA, hasMana),
                    Block.NOTIFY_LISTENERS);
        }
    }

    private enum TankConnectionDirection {
        INPUT,
        OUTPUT
    }

    private record TankConnectionFlow(TankConnectionDirection direction, int remainingTicks) {
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("Mana", mana);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        mana = Math.max(0, Math.min(MAX_MANA, nbt.getInt("Mana")));
    }
}
