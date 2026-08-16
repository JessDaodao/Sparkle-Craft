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
import java.util.HashSet;
import java.util.Set;

public class ManaPipeBlockEntity extends BlockEntity {

    public static final int MAX_MANA = 100;
    public static final int TRANSFER_RATE = 10;
    private static final int MAX_NETWORK_SEARCH = 256;

    private int mana;

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

        int manaPulledFromTank = 0;
        int tankPullBudget = 0;
        if (connectedToTank && blockEntity.mana == 0 && pullBudget > 0) {
            tankPullBudget = Math.min(pullBudget,
                    getManaNetworkState(world, pos).unmetDemand());
        }
        if (tankPullBudget > 0) {
            for (Direction direction : Direction.values()) {
                if (!currentState.get(ManaPipeBlock.getConnectionProperty(direction))) {
                    continue;
                }
                BlockEntity neighbor = world.getBlockEntity(pos.offset(direction));
                if (neighbor instanceof ManaTankBlockEntity tank) {
                    int extracted = tank.extractMana(tankPullBudget);
                    blockEntity.mana += extracted;
                    manaPulledFromTank += extracted;
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

        if (connectedToTank && manaPulledFromTank == 0 && pushBudget > 0
                && getManaNetworkState(world, pos).demand() == 0) {
            for (Direction direction : Direction.values()) {
                if (pushBudget == 0
                        || !currentState.get(ManaPipeBlock.getConnectionProperty(direction))) {
                    continue;
                }
                BlockEntity neighbor = world.getBlockEntity(pos.offset(direction));
                if (neighbor instanceof ManaTankBlockEntity tank) {
                    int inserted = tank.receiveMana(pushBudget);
                    blockEntity.mana -= inserted;
                    pushBudget -= inserted;
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
            if (state.get(ManaPipeBlock.getConnectionProperty(direction))
                    && world.getBlockEntity(pos.offset(direction)) instanceof ManaTankBlockEntity) {
                return true;
            }
        }
        return false;
    }

    private static ManaNetworkState getManaNetworkState(World world, BlockPos startPos) {
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        Set<BlockPos> consumers = new HashSet<>();
        pending.add(startPos);
        visited.add(startPos);
        int demand = 0;
        int bufferedMana = 0;

        while (!pending.isEmpty() && visited.size() <= MAX_NETWORK_SEARCH) {
            BlockPos currentPos = pending.removeFirst();
            BlockState currentState = world.getBlockState(currentPos);
            if (!currentState.isOf(ModBlocks.MANA_PIPE)) {
                continue;
            }
            if (world.getBlockEntity(currentPos) instanceof ManaPipeBlockEntity pipe) {
                bufferedMana = Math.min(TRANSFER_RATE, bufferedMana + pipe.mana);
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
        return new ManaNetworkState(demand, bufferedMana);
    }

    private record ManaNetworkState(int demand, int bufferedMana) {
        private int unmetDemand() {
            return Math.max(0, demand - bufferedMana);
        }
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
