package org.example.sparkle_craft.block.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.example.sparkle_craft.block.ModBlocks;
import org.example.sparkle_craft.block.custom.CrystalManaExtractorBlock;
import org.example.sparkle_craft.block.custom.ManaPipeBlock;

public class ManaPipeBlockEntity extends BlockEntity {

    public static final int MAX_MANA = 100;
    public static final int TRANSFER_RATE = 10;

    private int mana;

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

        if (blockEntity.mana != manaBeforeTick) {
            blockEntity.markDirty();
        }
        blockEntity.syncManaState();
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
